/*
 * micro3D v3 OpenGL software-rendering-via-GL backend for freej2meOnMinijvm.
 *
 * Implements com.mascotcapsule.micro3d.v3.base.Micro3dBackend. Consumes the
 * platform-independent FrameState draw items produced by freej2me's Engine and
 * renders them with the miniJVM OpenGL bindings (org.mini.gl.GL) into an offscreen
 * FBO, then glReadPixels the result back into the MIDP Graphics backbuffer.
 *
 * Lives in the .base package ON PURPOSE: Light.ambIntensity/dirIntensity/x/y/z and
 * TextureData.width/height are package-private, and this backend needs to read them.
 *
 * Threading: miniJVM renders the whole app (nanovg UI + this 3D pipeline) on a
 * single GL thread. All GL work here is dispatched onto that thread synchronously
 * (runOnGlThreadAndWait), mirroring the m3g backend. If the GL thread isn't ready,
 * isAvailable() returns false and Graphics3D falls back to the software rasterizer.
 *
 * This backend is fully independent of the m3g (JSR-184) GL backend and only
 * reuses the proven patterns (GLFrameBuffer, GlStateSaver, RGBA readback+flip).
 */
package com.mascotcapsule.micro3d.v3.base;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Hashtable;

import org.mini.glwrap.GLFrameBuffer;
import org.mini.gl.GLMath;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.GForm;
import org.mini.gui.callback.GCmd;
import org.recompile.mobile.PlatformGraphics;

import static org.mini.gl.GL.*;
import static org.mini.glwrap.GLUtil.toCstyleBytes;

public final class MiniJvmMicro3dGlBackend implements Micro3dBackend {

    private static final int FLOAT_SIZE = 4;
    // interleaved vertex: pos(3) normal(3) color(4) uv(2) flags(4) = 16 floats
    private static final int COMPONENTS_PER_VERTEX = 16;
    private static final int STRIDE = COMPONENTS_PER_VERTEX * FLOAT_SIZE;
    private static final int READBACK_BYTES_PER_PIXEL = 4;

    private static final String VERTEX_SHADER_RESOURCE = "/glsl/micro3d.vert.glsl";
    private static final String FRAGMENT_SHADER_RESOURCE = "/glsl/micro3d.frag.glsl";

    // blend mode constants (match Model.Polygon / Micro3dRasterizer)
    private static final int BLEND_NORMAL = 0;
    private static final int BLEND_HALF = 2;
    private static final int BLEND_ADD = 4;
    private static final int BLEND_SUB = 6;

    // env attribute bits (Graphics3D.ENV_ATTR_*)
    private static final int ENV_ATTR_LIGHTING = 1;
    private static final int ENV_ATTR_SPHERE_MAP = 2;
    private static final int ENV_ATTR_TOON_SHADING = 4;
    private static final int ENV_ATTR_SEMI_TRANSPARENT = 8;

    // primitive command masks
    private static final int PRIMITIVE_TYPE_MASK = 0x7000000;
    private static final int PRIMITVE_POINTS = 0x1000000;
    private static final int PRIMITVE_LINES = 0x2000000;
    private static final int PRIMITVE_TRIANGLES = 0x3000000;
    private static final int PRIMITVE_QUADS = 0x4000000;
    private static final int PRIMITVE_POINT_SPRITES = 0x5000000;
    private static final int PATTR_LIGHTING = 1;
    private static final int PATTR_SPHERE_MAP = 2;
    private static final int PATTR_COLORKEY = 16;
    private static final int PATTR_BLEND_SUB = 96;
    private static final int PATTR_BLEND_ADD = 64;
    private static final int PATTR_BLEND_HALF = 32;
    private static final int PDATA_COLOR_PER_COMMAND = 1024;
    private static final boolean LINEAR_FILTER = textureFilterEnabled();

    // bound target state
    private javax.microedition.lcdui.Graphics boundGraphics;
    private BufferedImage canvas;
    private int targetWidth;
    private int targetHeight;
    private boolean doClip;
    private int clipX, clipY, clipW, clipH;

    // GL resources (lazily created on the GL thread)
    private GLFrameBuffer frameBuffer;
    private int fbWidth = -1;
    private int fbHeight = -1;
    private int program;
    private final int[] vao = new int[]{0};
    private final int[] vbo = new int[]{0};

    // uniform locations
    private int uMvp, uNormalMat, uHasTexture, uEnableLighting, uToon, uTexSize,
            uTexture, uHasSphere, uSphere, uAmbIntensity, uDirIntensity, uLightDir,
            uToonThreshold, uToonHigh, uToonLow, uBlendMode, uUseTextureAlpha;

    // texture cache: TextureImpl -> GL texture name
    private final Hashtable textureCache = new Hashtable();

    // scratch
    private final float[] mvp = new float[16];
    private final float[] normalMat = new float[9];
    private float[] vertexBuffer = new float[0];
    private byte[] readBackBuffer = new byte[0];
    // reused across frames to avoid per-frame allocation
    private GlStateSaver stateSaver;
    private final int[] boundFboCheck = new int[1];
    // scratch arrays for bulk FloatBuffer/ByteBuffer -> array copies (avoids
    // per-element JNI get() in the hot vertex-assembly loop).
    private float[] posScratch = new float[0];
    private float[] nrmScratch = new float[0];
    private byte[] colorScratch = new byte[0];
    private byte[] tcScratch = new byte[0];

    public MiniJvmMicro3dGlBackend() {
    }

    @Override
    public boolean isAvailable() {
        // GL must be present AND we must be able to schedule onto the GL thread.
        try {
            Class.forName("org.mini.gl.GL");
        } catch (Throwable t) {
            return false;
        }
        return isGlThreadReady();
    }

    private static boolean isGlThreadReady() {
        try {
            return GCallBack.getInstance().getOpenglThread() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public int getTargetWidth() {
        return targetWidth;
    }

    @Override
    public int getTargetHeight() {
        return targetHeight;
    }

    @Override
    public void bind(Object target, boolean doClip) {
        if (!(target instanceof javax.microedition.lcdui.Graphics)) {
            throw new IllegalStateException("micro3d GL backend only supports Graphics targets");
        }
        javax.microedition.lcdui.Graphics g = (javax.microedition.lcdui.Graphics) target;
        boundGraphics = g;
        canvas = canvasOf(g);
        targetWidth = canvas.getWidth();
        targetHeight = canvas.getHeight();
        this.doClip = doClip;
        if (doClip) {
            clipX = g.getClipX();
            clipY = g.getClipY();
            clipW = g.getClipWidth();
            clipH = g.getClipHeight();
        } else {
            clipX = 0;
            clipY = 0;
            clipW = targetWidth;
            clipH = targetHeight;
        }
    }

    private static BufferedImage canvasOf(javax.microedition.lcdui.Graphics g) {
        PlatformGraphics pg;
        if (g instanceof PlatformGraphics) {
            pg = (PlatformGraphics) g;
        } else {
            pg = (PlatformGraphics) g.platformGraphics;
        }
        return pg.getCanvas();
    }

    @Override
    public void flushFrame(FrameState frame) {
        // A full frame: the 2D backbuffer already holds the painted background,
        // so we render the 3D items over it. For micro3d there is no separate
        // background copy step (the MIDP paint() already filled the canvas), so
        // flushFrame and flushItems do the same item rendering.
        flushItems(frame);
    }

    @Override
    public void flushItems(final FrameState frame) {
        if (frame.items.isEmpty() || canvas == null) {
            return;
        }
        try {
            runOnGlThreadAndWait(new Runnable() {
                public void run() {
                    renderFrame(frame);
                }
            });
        } catch (Throwable t) {
            // Log the real GL-side error (with cause) but do NOT propagate: the game's
            // paint()/bind/release sequence isn't guarded by try/finally, so throwing
            // here would leave Graphics3D permanently "Target already bound" and the
            // app would spam the same failure every frame. Fall back gracefully.
            t.printStackTrace();
        }
    }

    @Override
    public void release(Object target) {
        // nothing extra; the frame is presented during flushItems readback.
        boundGraphics = null;
    }

    // ============================ rendering ============================

    private void renderFrame(FrameState frame) {
        ensureInitialized(targetWidth, targetHeight);
        if (stateSaver == null) stateSaver = new GlStateSaver();
        GlStateSaver saved = stateSaver;
        saved.capture();
        frameBuffer.begin();
        boundFboCheck[0] = 0;
        glGetIntegerv(org.mini.gl.GL.GL_FRAMEBUFFER_BINDING, boundFboCheck, 0);
        if (boundFboCheck[0] == 0) {
            frameBuffer.end();
            saved.restore();
            throw new IllegalStateException("micro3d FBO not bound");
        }
        try {
            glViewport(0, 0, targetWidth, targetHeight);
            // clear color to fully transparent so readback can blend over the
            // existing canvas where no geometry was drawn. Clear depth to far (1.0):
            // glClearDepthf/glClearDepth are not implemented in the miniJVM GL bindings,
            // but the default depth clear value is already 1.0, so a plain glClear of
            // the depth buffer clears to far without needing to set the clear value.
            glClearColor(0f, 0f, 0f, 0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(program);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glDisable(GL_SCISSOR_TEST_SAFE);
            // we don't use GL scissor; clip is honored during readback blit.

            // pass 0: opaque, depth write on; pass 1: translucent, depth write off
            for (int pass = 0; pass < 2; pass++) {
                boolean depthWrite = (pass == 0);
                glDepthMask(depthWrite ? GL_TRUE : GL_FALSE);
                for (int i = 0; i < frame.items.size(); i++) {
                    FrameState.DrawItem item = (FrameState.DrawItem) frame.items.elementAt(i);
                    multiplyMM(mvp, item.projMatrix, item.viewMatrix);
                    // normal matrix: upper-left 3x3 of view, packed column-major.
                    normalMat[0] = item.viewMatrix[0];
                    normalMat[1] = item.viewMatrix[1];
                    normalMat[2] = item.viewMatrix[2];
                    normalMat[3] = item.viewMatrix[3];
                    normalMat[4] = item.viewMatrix[4];
                    normalMat[5] = item.viewMatrix[5];
                    normalMat[6] = item.viewMatrix[6];
                    normalMat[7] = item.viewMatrix[7];
                    normalMat[8] = item.viewMatrix[8];
                    glUniformMatrix4fv(uMvp, 1, GL_FALSE, mvp, 0);
                    glUniformMatrix3fv(uNormalMat, 1, GL_FALSE, normalMat, 0);
                    // per-item uniforms that don't change across buckets of the same item.
                    setupItemUniforms(item);

                    if (item instanceof FrameState.FigureItem) {
                        renderFigure((FrameState.FigureItem) item, pass, depthWrite);
                    } else if (item instanceof FrameState.PrimitiveItem) {
                        renderPrimitive((FrameState.PrimitiveItem) item, pass);
                    }
                }
            }
            readBack();
        } finally {
            glUseProgram(0);
            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
            frameBuffer.end();
            saved.restore();
        }
    }
    private static final int GL_SCISSOR_TEST_SAFE = org.mini.gl.GL.GL_SCISSOR_TEST;

    private void renderFigure(FrameState.FigureItem item, int pass, boolean depthWrite) {
        Model model = item.model;
        if (!model.hasPolyT && !model.hasPolyC) {
            return;
        }
        if (model.hasPolyT && item.textures != null && item.textures.length > 0) {
            renderFigureTextured(item, pass, depthWrite);
        }
        if (model.hasPolyC) {
            renderFigureColored(item, pass);
        }
    }

    private void renderFigureTextured(FrameState.FigureItem item, int pass, boolean depthWrite) {
        Model model = item.model;
        boolean semiTrans = (item.attrs & ENV_ATTR_SEMI_TRANSPARENT) != 0;
        int[][][] meshes = model.subMeshesLengthsT;   // [4][numTex][2]
        ByteBuffer texCoords = model.texCoordArray;

        FloatBuffer vertices = item.vertices;
        FloatBuffer normals = item.normals;
        vertices.position(0);

        int length = meshes.length;
        int blendIndex = 0;
        int pos = 0;
        if (semiTrans) {
            if (pass == 0) {
                length = 1;   // only the NORMAL (blendIndex 0) bucket, opaque
            } else {
                // skip past blendIndex 0 entirely
                int[][] mesh0 = meshes[blendIndex++];
                for (int f = 0; f < mesh0.length; f++) {
                    pos += mesh0[f][0] + mesh0[f][1];
                }
            }
        } else if (pass == 1) {
            return;
        }

        while (blendIndex < length) {
            int[][] texMesh = meshes[blendIndex];
            int blendMode = (semiTrans && pass == 1) ? (blendIndex << 1) : BLEND_NORMAL;
            for (int face = 0; face < texMesh.length; face++) {
                TextureImpl tex = face < item.textures.length ? item.textures[face] : null;
                int texName = tex != null ? ensureTexture(tex) : 0;
                int[] lens = texMesh[face];
                int cnt = lens[0];   // cullBack bucket (single-sided)
                if (cnt > 0) {
                    drawFigureBucket(item, vertices, normals, texCoords, pos, cnt, tex, texName,
                            blendMode, true /*cullBack*/, false /*noCull*/);
                    pos += cnt;
                }
                cnt = lens[1];       // noCull bucket (double-sided)
                if (cnt > 0) {
                    drawFigureBucket(item, vertices, normals, texCoords, pos, cnt, tex, texName,
                            blendMode, false /*cullBack*/, true /*noCull*/);
                    pos += cnt;
                }
            }
            blendIndex++;
        }
    }

    private void renderFigureColored(FrameState.FigureItem item, int pass) {
        boolean semiTrans = (item.attrs & ENV_ATTR_SEMI_TRANSPARENT) != 0;
        int[][] meshes = item.model.subMeshesLengthsC;
        ByteBuffer materialData = item.model.texCoordArray;
        int startVertex = item.model.numVerticesPolyT;
        int length = meshes.length;
        int blendIndex = 0;
        int pos = 0;
        if (semiTrans) {
            if (pass == 0) {
                length = 1;
            } else {
                int[] bucket0 = meshes[blendIndex++];
                pos += bucket0[0] + bucket0[1];
            }
        } else if (pass == 1) {
            return;
        }

        FloatBuffer vertices = item.vertices;
        FloatBuffer normals = item.normals;
        for (; blendIndex < length; blendIndex++) {
            int[] bucket = meshes[blendIndex];
            int blendMode = (semiTrans && pass == 1) ? (blendIndex << 1) : BLEND_NORMAL;
            int cnt = bucket[0];
            if (cnt > 0) {
                drawFigureColorBucket(item, vertices, normals, materialData,
                        startVertex + pos, cnt, blendMode, true, false);
                pos += cnt;
            }
            cnt = bucket[1];
            if (cnt > 0) {
                drawFigureColorBucket(item, vertices, normals, materialData,
                        startVertex + pos, cnt, blendMode, false, true);
                pos += cnt;
            }
        }
    }

    private void drawFigureBucket(FrameState.FigureItem item,
                                  FloatBuffer vertices, FloatBuffer normals, ByteBuffer texCoords,
                                  int startVertex, int vertexCount, TextureImpl tex, int texName,
                                  int blendMode, boolean cullBack, boolean noCull) {
        boolean globalLight = (item.attrs & ENV_ATTR_LIGHTING) != 0 && normals != null;
        boolean toon = (item.attrs & ENV_ATTR_TOON_SHADING) != 0;
        boolean sphereAvailable = item.specular != null;
        int sphereName = sphereAvailable ? ensureTexture(item.specular) : 0;

        setupProgramState(tex, texName, sphereName, globalLight, toon,
                sphereAvailable, blendMode, false);
        setCull(cullBack, noCull);
        setBlend(blendMode);

        // Bulk-copy the needed slices out of the direct NIO buffers ONCE (a single
        // JNI region get per buffer) instead of per-element get() in the loop — this
        // is the dominant cost for figure-heavy games (hundreds of vertices/frame).
        int floatOff = startVertex * 3;
        int floatLen = vertexCount * 3;
        if (posScratch.length < floatLen) posScratch = new float[floatLen];
        vertices.position(floatOff);
        vertices.get(posScratch, 0, floatLen);
        float[] pos = posScratch;
        float[] nrm = null;
        if (normals != null) {
            if (nrmScratch.length < floatLen) nrmScratch = new float[floatLen];
            normals.position(floatOff);
            normals.get(nrmScratch, 0, floatLen);
            nrm = nrmScratch;
        }
        int tcLen = vertexCount * 5;
        if (tcScratch.length < tcLen) tcScratch = new byte[tcLen];
        texCoords.position(startVertex * 5);
        texCoords.get(tcScratch, 0, tcLen);
        byte[] tc = tcScratch;

        vertexBuffer = ensureVertexCapacity(vertexBuffer, vertexCount * COMPONENTS_PER_VERTEX);
        float[] v = vertexBuffer;
        int p = 0;
        int fp = 0;   // walks pos/nrm (3 floats/vertex)
        int tp = 0;   // walks tc (5 bytes/vertex)
        for (int n = 0; n < vertexCount; n++) {
            v[p++] = pos[fp];     v[p++] = pos[fp + 1]; v[p++] = pos[fp + 2];
            if (nrm != null) {
                v[p++] = nrm[fp]; v[p++] = nrm[fp + 1]; v[p++] = nrm[fp + 2];
            } else {
                v[p++] = 0f; v[p++] = 0f; v[p++] = 0f;
            }
            fp += 3;
            // color white opaque
            v[p++] = 1f; v[p++] = 1f; v[p++] = 1f; v[p++] = 1f;
            float u = tc[tp] & 0xFF;
            float vv = tc[tp + 1] & 0xFF;
            v[p++] = u; v[p++] = vv;
            int lightFlag = (tc[tp + 2] & 0xFF) != 0 ? 1 : 0;
            int specFlag = (tc[tp + 3] & 0xFF) != 0 ? 1 : 0;
            int transFlag = (tc[tp + 4] & 0xFF) != 0 ? 1 : 0;
            v[p++] = lightFlag; v[p++] = specFlag; v[p++] = transFlag; v[p++] = 0f;
            tp += 5;
        }
        uploadAndDraw(v, vertexCount);
    }

    private void drawFigureColorBucket(FrameState.FigureItem item,
                                       FloatBuffer vertices, FloatBuffer normals, ByteBuffer materialData,
                                       int startVertex, int vertexCount,
                                       int blendMode, boolean cullBack, boolean noCull) {
        boolean globalLight = (item.attrs & ENV_ATTR_LIGHTING) != 0 && normals != null;
        boolean toon = (item.attrs & ENV_ATTR_TOON_SHADING) != 0;
        boolean sphereAvailable = item.specular != null
                && (item.attrs & ENV_ATTR_SPHERE_MAP) != 0;
        int sphereName = sphereAvailable ? ensureTexture(item.specular) : 0;

        setupProgramState(null, 0, sphereName, globalLight, toon,
                sphereAvailable, blendMode, false);
        setCull(cullBack, noCull);
        setBlend(blendMode);

        int floatOff = startVertex * 3;
        int floatLen = vertexCount * 3;
        if (posScratch.length < floatLen) posScratch = new float[floatLen];
        vertices.position(floatOff);
        vertices.get(posScratch, 0, floatLen);
        float[] pos = posScratch;
        float[] nrm = null;
        if (normals != null) {
            if (nrmScratch.length < floatLen) nrmScratch = new float[floatLen];
            normals.position(floatOff);
            normals.get(nrmScratch, 0, floatLen);
            nrm = nrmScratch;
        }
        int mdLen = vertexCount * 5;
        if (tcScratch.length < mdLen) tcScratch = new byte[mdLen];
        materialData.position(startVertex * 5);
        materialData.get(tcScratch, 0, mdLen);
        byte[] md = tcScratch;

        vertexBuffer = ensureVertexCapacity(vertexBuffer, vertexCount * COMPONENTS_PER_VERTEX);
        float[] v = vertexBuffer;
        int p = 0;
        int fp = 0;
        int mp = 0;
        for (int n = 0; n < vertexCount; n++) {
            v[p++] = pos[fp];
            v[p++] = pos[fp + 1];
            v[p++] = pos[fp + 2];
            if (nrm != null) {
                v[p++] = nrm[fp];
                v[p++] = nrm[fp + 1];
                v[p++] = nrm[fp + 2];
            } else {
                v[p++] = 0f; v[p++] = 0f; v[p++] = 0f;
            }
            fp += 3;
            v[p++] = (md[mp] & 0xFF) / 255f;
            v[p++] = (md[mp + 1] & 0xFF) / 255f;
            v[p++] = (md[mp + 2] & 0xFF) / 255f;
            v[p++] = 1f;
            v[p++] = 0f;
            v[p++] = 0f;
            v[p++] = (md[mp + 3] & 0xFF) != 0 ? 1f : 0f;
            v[p++] = (md[mp + 4] & 0xFF) != 0 ? 1f : 0f;
            v[p++] = 0f;
            v[p++] = 0f;
            mp += 5;
        }
        uploadAndDraw(v, vertexCount);
    }

    private void renderPrimitive(FrameState.PrimitiveItem item, int pass) {
        int command = item.command;
        int type = command & PRIMITIVE_TYPE_MASK;
        if (type == PRIMITVE_POINT_SPRITES) {
            renderPointSprites(item, pass);
            return;
        }
        if (type != PRIMITVE_POINTS && type != PRIMITVE_LINES
                && type != PRIMITVE_TRIANGLES && type != PRIMITVE_QUADS) {
            return;
        }
        int blend = item.blendMode();
        boolean drawThisPass = (blend == BLEND_NORMAL) ? pass == 0 : pass == 1;
        if (!drawThisPass) {
            return;
        }
        TextureImpl tex = item.texture;
        int texName = tex != null ? ensureTexture(tex) : 0;
        boolean enableLight = (item.attrs & ENV_ATTR_LIGHTING) != 0
                && (command & PATTR_LIGHTING) != 0 && item.normals != null;
        boolean sphere = enableLight && (item.attrs & ENV_ATTR_SPHERE_MAP) != 0
                && (command & PATTR_SPHERE_MAP) != 0 && item.specular != null;
        int sphereName = sphere ? ensureTexture(item.specular) : 0;
        boolean toon = (item.attrs & ENV_ATTR_TOON_SHADING) != 0;

        setupProgramState(tex, texName, sphereName, enableLight, toon, sphere, blend, false);
        setCull(false, true);   // primitives: no back-face cull
        setBlend(blend);

        FloatBuffer vertices = item.vertices;
        FloatBuffer normals = item.normals;
        ByteBuffer colors = item.colors;
        ByteBuffer tcBuf = item.texCoords;
        int vertexCount = vertices.capacity() / 3;
        boolean expandedVertexColors = colors != null
                && (command & PDATA_COLOR_PER_COMMAND) != PDATA_COLOR_PER_COMMAND;
        boolean perCommandColor = colors != null
                && (command & PDATA_COLOR_PER_COMMAND) == PDATA_COLOR_PER_COMMAND;
        float pcR = 1f, pcG = 1f, pcB = 1f;
        if (perCommandColor && colors.capacity() >= 3) {
            pcR = (colors.get(0) & 0xFF) / 255f;
            pcG = (colors.get(1) & 0xFF) / 255f;
            pcB = (colors.get(2) & 0xFF) / 255f;
        }
        boolean hasTex = tcBuf != null && texName != 0;
        // transparent flag for primitives: PATTR_COLORKEY drives color-key discard
        int colorKey = (command & PATTR_COLORKEY);

        // bulk-copy the whole small primitive buffer once
        int floatLen = vertexCount * 3;
        if (posScratch.length < floatLen) posScratch = new float[floatLen];
        vertices.position(0);
        vertices.get(posScratch, 0, floatLen);
        float[] pos = posScratch;
        float[] nrm = null;
        if (normals != null) {
            if (nrmScratch.length < floatLen) nrmScratch = new float[floatLen];
            normals.position(0);
            normals.get(nrmScratch, 0, floatLen);
            nrm = nrmScratch;
        }
        byte[] colArr = null;
        if (colors != null) {
            if (colorScratch.length < colors.capacity()) colorScratch = new byte[colors.capacity()];
            colors.position(0);
            colors.get(colorScratch, 0, colors.capacity());
            colArr = colorScratch;
        }
        byte[] tc = null;
        if (hasTex) {
            int tcLen = vertexCount * 2;
            if (tcScratch.length < tcLen) tcScratch = new byte[tcLen];
            tcBuf.position(0);
            tcBuf.get(tcScratch, 0, tcLen);
            tc = tcScratch;
        }

        vertexBuffer = ensureVertexCapacity(vertexBuffer, vertexCount * COMPONENTS_PER_VERTEX);
        float[] v = vertexBuffer;
        int p = 0;
        int fp = 0;
        for (int i = 0; i < vertexCount; i++) {
            v[p++] = pos[fp];
            v[p++] = pos[fp + 1];
            v[p++] = pos[fp + 2];
            if (nrm != null) {
                v[p++] = nrm[fp];
                v[p++] = nrm[fp + 1];
                v[p++] = nrm[fp + 2];
            } else {
                v[p++] = 0f; v[p++] = 0f; v[p++] = 0f;
            }
            fp += 3;
            if (expandedVertexColors && colArr != null && colArr.length >= (i + 1) * 3) {
                int ci = i * 3;
                v[p++] = (colArr[ci] & 0xFF) / 255f;
                v[p++] = (colArr[ci + 1] & 0xFF) / 255f;
                v[p++] = (colArr[ci + 2] & 0xFF) / 255f;
                v[p++] = 1f;
            } else {
                v[p++] = pcR; v[p++] = pcG; v[p++] = pcB; v[p++] = 1f;
            }
            if (tc != null) {
                int ti = i * 2;
                v[p++] = tc[ti] & 0xFF;
                v[p++] = tc[ti + 1] & 0xFF;
            } else {
                v[p++] = 0f; v[p++] = 0f;
            }
            // flags: lightFlag = enableLight?1:0, specular = sphere?1:0, transparent = colorKey
            v[p++] = enableLight ? 1f : 0f;
            v[p++] = sphere ? 1f : 0f;
            v[p++] = colorKey;
            v[p++] = 0f;
        }
        int drawMode = GL_TRIANGLES;
        if (type == PRIMITVE_LINES) {
            drawMode = GL_LINES;
        } else if (type == PRIMITVE_POINTS) {
            drawMode = GL_POINTS;
        }
        uploadAndDraw(v, vertexCount, drawMode);
    }

    private void renderPointSprites(FrameState.PrimitiveItem item, int pass) {
        TextureImpl tex = item.texture;
        if (tex == null || item.vertices == null || item.texCoords == null) {
            return;
        }
        int blend = item.blendMode();
        boolean drawThisPass = (blend == BLEND_NORMAL) ? pass == 0 : pass == 1;
        if (!drawThisPass) {
            return;
        }
        int texName = ensureTexture(tex);
        setupProgramState(tex, texName, 0, false, false, false, blend, true);
        setCull(false, true);
        setBlend(blend);

        FloatBuffer vertices = item.vertices;
        ByteBuffer tcBuf = item.texCoords;
        int vertexCount = vertices.capacity() / 4;

        int floatLen = vertexCount * 4;
        if (posScratch.length < floatLen) posScratch = new float[floatLen];
        vertices.position(0);
        vertices.get(posScratch, 0, floatLen);
        float[] pos = posScratch;

        int tcLen = vertexCount * 2;
        if (tcScratch.length < tcLen) tcScratch = new byte[tcLen];
        tcBuf.position(0);
        tcBuf.get(tcScratch, 0, tcLen);
        byte[] tc = tcScratch;

        float colorKey = (item.command & PATTR_COLORKEY) != 0 ? 1f : 0f;
        vertexBuffer = ensureVertexCapacity(vertexBuffer, vertexCount * COMPONENTS_PER_VERTEX);
        float[] v = vertexBuffer;
        int p = 0;
        int fp = 0;
        for (int i = 0; i < vertexCount; i++) {
            v[p++] = pos[fp];
            v[p++] = pos[fp + 1];
            v[p++] = pos[fp + 2];
            v[p++] = 0f; v[p++] = 0f; v[p++] = 0f;
            fp += 4;
            v[p++] = 1f; v[p++] = 1f; v[p++] = 1f; v[p++] = 1f;
            int ti = i * 2;
            v[p++] = tc[ti] & 0xFF;
            v[p++] = tc[ti + 1] & 0xFF;
            v[p++] = 0f;
            v[p++] = 0f;
            v[p++] = colorKey;
            v[p++] = pos[fp - 1];
        }
        uploadAndDraw(v, vertexCount);
    }

    /**
     * Set the uniforms that are constant across all buckets of one DrawItem
     * (light, toon params). Called once per item per pass — avoids re-uploading
     * these for every small figure bucket (which is the dominant per-frame GL
     * call count for figure-heavy games).
     */
    private void setupItemUniforms(FrameState.DrawItem item) {
        Light L = item.light;
        if (L != null) {
            glUniform1f(uAmbIntensity, L.ambIntensity / 4096f);
            glUniform1f(uDirIntensity, L.dirIntensity / 4096f);
            glUniform3f(uLightDir, L.x, L.y, L.z);
        } else {
            glUniform1f(uAmbIntensity, 1f);
            glUniform1f(uDirIntensity, 0f);
            glUniform3f(uLightDir, 0f, 0f, 4096f);
        }
        glUniform1i(uToonThreshold, item.toonThreshold);
        glUniform1i(uToonHigh, item.toonHigh);
        glUniform1i(uToonLow, item.toonLow);
    }

    /** Per-bucket state: texture binding, sphere binding, lighting/toon/blend flags. */
    private void setupProgramState(TextureImpl tex, int texName, int sphereName, boolean globalLight,
                                   boolean toon, boolean sphereAvailable, int blendMode,
                                   boolean useTextureAlpha) {
        boolean useTex = texName != 0;
        glUniform1i(uHasTexture, useTex ? 1 : 0);
        if (useTex) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texName);
            glUniform1i(uTexture, 0);
            int tw = tex != null ? tex.image.width : 1;
            int th = tex != null ? tex.image.height : 1;
            glUniform2f(uTexSize, tw, th);
        } else {
            glUniform2f(uTexSize, 1f, 1f);
        }
        glUniform1i(uHasSphere, (sphereAvailable && sphereName != 0) ? 1 : 0);
        if (sphereAvailable && sphereName != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, sphereName);
            glUniform1i(uSphere, 1);
        }
        glUniform1i(uEnableLighting, globalLight ? 1 : 0);
        glUniform1i(uToon, toon ? 1 : 0);
        glUniform1i(uBlendMode, blendMode);
        glUniform1i(uUseTextureAlpha, useTextureAlpha ? 1 : 0);
    }


    private void setCull(boolean cullBack, boolean noCull) {
        if (noCull || !cullBack) {
            glDisable(GL_CULL_FACE);
        } else {
            glEnable(GL_CULL_FACE);
            glCullFace(org.mini.gl.GL.GL_BACK);
            // GL convention in the FBO: front-facing winding. The micro3d front face
            // sign was empirically determined against the software backend (which now
            // culls back faces correctly). CCW is the default front; if a model renders
            // inside-out, flip this to GL_CW.
            glFrontFace(GL_CCW);
        }
    }

    private void setBlend(int blendMode) {
        if (blendMode == BLEND_NORMAL) {
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        } else if (blendMode == BLEND_HALF) {
            glEnable(GL_BLEND);
            glBlendColor(0f, 0f, 0f, 0.5f);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFuncSeparate(GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA, GL_ONE, GL_ZERO);
        } else if (blendMode == BLEND_ADD) {
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFunc(GL_ONE, GL_ONE);
        } else if (blendMode == BLEND_SUB) {
            // dst - src : REVERSE_SUBTRACT with (ONE, ONE)
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_REVERSE_SUBTRACT);
            glBlendFunc(GL_ONE, GL_ONE);
        }
    }

    private void uploadAndDraw(float[] verts, int vertexCount) {
        uploadAndDraw(verts, vertexCount, GL_TRIANGLES);
    }

    private void uploadAndDraw(float[] verts, int vertexCount, int drawMode) {
        // VAO already holds the attrib pointers (set once in ensureInitialized),
        // so we only need to re-upload the data and draw.
        glBindVertexArray(vao[0]);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, (long) vertexCount * STRIDE, verts, 0, GL_DYNAMIC_DRAW);
        glDrawArrays(drawMode, 0, vertexCount);
    }

    private int ensureTexture(TextureImpl tex) {
        if (tex == null) return 0;
        Integer cached = (Integer) textureCache.get(tex);
        if (cached != null) return cached.intValue();
        final int[] name = new int[1];
        glGenTextures(1, name, 0);
        TextureData td = tex.image;
        ByteBuffer raster = td.getRaster();
        byte[] pixels = new byte[raster.remaining()];
        raster.get(pixels);
        glBindTexture(GL_TEXTURE_2D, name[0]);
        // Match the software backend / KEmu-style texel fetch.
        // Linear filtering on atlas edges makes block faces show dark seams.
        int filter = LINEAR_FILTER ? GL_LINEAR : GL_NEAREST;
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, td.width, td.height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, pixels, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        textureCache.put(tex, Integer.valueOf(name[0]));
        return name[0];
    }

    private Integer integerFor(TextureImpl tex) {
        return (Integer) textureCache.get(tex);
    }

    // ============================ readback ============================

    private void readBack() {
        int w = targetWidth;
        int h = targetHeight;
        int needed = w * h * READBACK_BYTES_PER_PIXEL;
        if (readBackBuffer.length < needed) {
            readBackBuffer = new byte[needed];
        }
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, readBackBuffer, 0);
        blitToCanvas(readBackBuffer, w, h);
    }

    /** Flip rows vertically (GL origin is bottom-left) and write into the BufferedImage. */
    private void blitToCanvas(byte[] rgba, int w, int h) {
        org.mini.gui.ImageMutable mutable = null;
        byte[] data = null;
        int dataWidth = w;
        try {
            mutable = canvas.getImage();
            data = mutable.getData().array();
            dataWidth = canvas.getWidth();
        } catch (Throwable t) {
            // fall back to per-pixel setRGB
        }
        int cw = canvas.getWidth();
        int ch = canvas.getHeight();
        // honor clip: only write inside [clipX,clipY,clipW,clipH]
        int x0 = Math.max(0, clipX);
        int y0 = Math.max(0, clipY);
        int x1 = Math.min(cw, clipX + clipW);
        int y1 = Math.min(ch, clipY + clipH);
        if (data != null && dataWidth == cw) {
            synchronized (mutable) {
                // The native image compositor is equivalent to the original
                // blend when the destination is opaque. For transparent
                // destinations retain the old forced-opaque blending semantics.
                boolean opaque = true;
                for (int y = y0; y < y1 && opaque; y++) {
                    int end = (y * cw + x1) * READBACK_BYTES_PER_PIXEL;
                    for (int alpha = (y * cw + x0) * READBACK_BYTES_PER_PIXEL + 3; alpha < end; alpha += 4) {
                        if ((data[alpha] & 0xff) != 0xff) {
                            opaque = false;
                            break;
                        }
                    }
                }
                if (opaque && x1 > x0 && y1 > y0 && GLMath.img_draw(data, cw, rgba, w,
                        x0, y0, x1 - x0, y1 - y0,
                        1f, 0f, 0f, 0f, 1f, 0f, 1f, false, 0) == 0) {
                    return;
                }
                for (int y = y0; y < y1; y++) {
                    // miniJVM's GL readback already returns rows top-to-bottom for the
                    // FBO (the framebuffer/texture is created FLIPY), so no vertical
                    // flip is needed here. Mapping srcY = y directly.
                    int srcY = y;
                    int srcRow = srcY * w * READBACK_BYTES_PER_PIXEL;
                    for (int x = x0; x < x1; x++) {
                        int s = srcRow + x * READBACK_BYTES_PER_PIXEL;
                        int a = rgba[s + 3] & 0xFF;
                        if (a == 0) continue;
                        int dst = (y * cw + x) * READBACK_BYTES_PER_PIXEL;
                        if (a == 255) {
                            data[dst] = rgba[s];
                            data[dst + 1] = rgba[s + 1];
                            data[dst + 2] = rgba[s + 2];
                            data[dst + 3] = (byte) 0xFF;
                        } else {
                            int inv = 255 - a;
                            int dr = data[dst] & 0xFF;
                            int dg = data[dst + 1] & 0xFF;
                            int db = data[dst + 2] & 0xFF;
                            int sr = rgba[s] & 0xFF;
                            int sg = rgba[s + 1] & 0xFF;
                            int sb = rgba[s + 2] & 0xFF;
                            data[dst] = (byte) ((sr * a + dr * inv + 127) / 255);
                            data[dst + 1] = (byte) ((sg * a + dg * inv + 127) / 255);
                            data[dst + 2] = (byte) ((sb * a + db * inv + 127) / 255);
                            data[dst + 3] = (byte) 0xFF;
                        }
                    }
                }
            }
        } else {
            for (int y = y0; y < y1; y++) {
                int srcY = y;   // no flip (see comment in fast path above)
                int srcRow = srcY * w * READBACK_BYTES_PER_PIXEL;
                for (int x = x0; x < x1; x++) {
                    int s = srcRow + x * READBACK_BYTES_PER_PIXEL;
                    int a = rgba[s + 3] & 0xFF;
                    if (a == 0) continue;
                    int argb = (a << 24) | ((rgba[s] & 0xFF) << 16)
                            | ((rgba[s + 1] & 0xFF) << 8) | (rgba[s + 2] & 0xFF);
                    canvas.setRGB(x, y, argb | 0xFF000000);
                }
            }
        }
    }

    // ============================ GL init ============================

    private void ensureInitialized(int width, int height) {
        if (frameBuffer == null || fbWidth != width || fbHeight != height) {
            if (frameBuffer != null) {
                frameBuffer.delete();
            }
            frameBuffer = new GLFrameBuffer(width, height, 1f, true);
            frameBuffer.gl_init();
            fbWidth = width;
            fbHeight = height;
        }
        if (program == 0) {
            String vert = adaptShaderVersion(readShaderResource(VERTEX_SHADER_RESOURCE));
            String frag = adaptShaderVersion(readShaderResource(FRAGMENT_SHADER_RESOURCE));
            program = loadProgram(vert, frag);
            uMvp = glGetUniformLocation(program, toCstyleBytes("uMvp"));
            uNormalMat = glGetUniformLocation(program, toCstyleBytes("uNormalMat"));
            uHasTexture = glGetUniformLocation(program, toCstyleBytes("uHasTexture"));
            uEnableLighting = glGetUniformLocation(program, toCstyleBytes("uEnableLighting"));
            uToon = glGetUniformLocation(program, toCstyleBytes("uToon"));
            uUseTextureAlpha = glGetUniformLocation(program, toCstyleBytes("uUseTextureAlpha"));
            uTexSize = glGetUniformLocation(program, toCstyleBytes("uTexSize"));
            uTexture = glGetUniformLocation(program, toCstyleBytes("uTexture"));
            uHasSphere = glGetUniformLocation(program, toCstyleBytes("uHasSphere"));
            uSphere = glGetUniformLocation(program, toCstyleBytes("uSphere"));
            uAmbIntensity = glGetUniformLocation(program, toCstyleBytes("uAmbIntensity"));
            uDirIntensity = glGetUniformLocation(program, toCstyleBytes("uDirIntensity"));
            uLightDir = glGetUniformLocation(program, toCstyleBytes("uLightDir"));
            uToonThreshold = glGetUniformLocation(program, toCstyleBytes("uToonThreshold"));
            uToonHigh = glGetUniformLocation(program, toCstyleBytes("uToonHigh"));
            uToonLow = glGetUniformLocation(program, toCstyleBytes("uToonLow"));
            uBlendMode = glGetUniformLocation(program, toCstyleBytes("uBlendMode"));
        }
        if (vao[0] == 0) {
            glGenVertexArrays(1, vao, 0);
            glGenBuffers(1, vbo, 0);
            // Set up the fixed interleaved vertex layout ONCE in the VAO record.
            // The stride/attribute offsets never change, so we bind the VAO/VBO
            // here, configure all attrib pointers, and from then on each draw only
            // needs: bind VAO, bind VBO, buffer data, draw. The VAO remembers the
            // attrib bindings — no need to re-issue glEnableVertexAttribArray /
            // glVertexAttribPointer per bucket (that was dozens of GL calls/frame).
            glBindVertexArray(vao[0]);
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            int off = 0;
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, STRIDE, null, off);
            off += 3 * FLOAT_SIZE;
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, STRIDE, null, off);
            off += 3 * FLOAT_SIZE;
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 4, GL_FLOAT, GL_FALSE, STRIDE, null, off);
            off += 4 * FLOAT_SIZE;
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 2, GL_FLOAT, GL_FALSE, STRIDE, null, off);
            off += 2 * FLOAT_SIZE;
            glEnableVertexAttribArray(4);
            glVertexAttribPointer(4, 4, GL_FLOAT, GL_FALSE, STRIDE, null, off);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    private int loadProgram(String vertSrc, String fragSrc) {
        int vs = compileShader(GL_VERTEX_SHADER, vertSrc);
        int fs = compileShader(GL_FRAGMENT_SHADER, fragSrc);
        int prog = glCreateProgram();
        int[] status = new int[]{0};
        glAttachShader(prog, vs);
        glAttachShader(prog, fs);
        glLinkProgram(prog);
        glGetProgramiv(prog, GL_LINK_STATUS, status, 0);
        if (status[0] == GL_FALSE) {
            glGetProgramiv(prog, GL_INFO_LOG_LENGTH, status, 0);
            byte[] log = new byte[status[0] + 1];
            glGetProgramInfoLog(prog, log.length, status, 0, log);
            throw new RuntimeException("micro3d program link failed: "
                    + new String(log, 0, status[0]));
        }
        glDeleteShader(vs);
        glDeleteShader(fs);
        return prog;
    }

    private int compileShader(int type, String src) {
        int shader = glCreateShader(type);
        int[] status = new int[]{0};
        glShaderSource(shader, 1, new byte[][]{toCstyleBytes(src)}, null, 0);
        glCompileShader(shader);
        glGetShaderiv(shader, org.mini.gl.GL.GL_COMPILE_STATUS, status, 0);
        if (status[0] == GL_FALSE) {
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, status, 0);
            byte[] log = new byte[status[0] + 1];
            glGetShaderInfoLog(shader, log.length, status, 0, log);
            throw new RuntimeException("micro3d shader compile failed: "
                    + new String(log, 0, status[0]));
        }
        return shader;
    }

    private String adaptShaderVersion(String shader) {
        byte[] ver = glGetString(GL_VERSION);
        String version = ver == null ? "" : new String(ver);
        if (version.toLowerCase().indexOf("opengl es") >= 0) {
            return shader.replace("version 330", "version 300 es");
        }
        return shader;
    }

    private String readShaderResource(String path) {
        java.io.InputStream in = null;
        java.io.BufferedReader reader = null;
        try {
            in = MiniJvmMicro3dGlBackend.class.getResourceAsStream(path);
            if (in == null) {
                try {
                    in = GCallBack.getInstance().getResourceAsStream(
                            path.startsWith("/") ? path.substring(1) : path);
                } catch (Throwable ignored) {
                }
            }
            if (in == null) {
                throw new IllegalStateException("Missing micro3d shader resource: " + path);
            }
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (reader != null) reader.close();
                else if (in != null) in.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ============================ helpers ============================

    /** Exact copy of MathUtil.multiplyMM (package-private there): proj(16) * view(12). */
    private static void multiplyMM(float[] m, float[] pm, float[] mvm) {
        float l00 = pm[0], l01 = pm[4], l02 = pm[ 8], l03 = pm[12];
        float l10 = pm[1], l11 = pm[5], l12 = pm[ 9], l13 = pm[13];
        float l20 = pm[2], l21 = pm[6], l22 = pm[10], l23 = pm[14];
        float l30 = pm[3], l31 = pm[7], l32 = pm[11], l33 = pm[15];
        float r00 = mvm[0], r01 = mvm[3], r02 = mvm[6], r03 = mvm[ 9];
        float r10 = mvm[1], r11 = mvm[4], r12 = mvm[7], r13 = mvm[10];
        float r20 = mvm[2], r21 = mvm[5], r22 = mvm[8], r23 = mvm[11];
        m[ 0] = l00 * r00 + l01 * r10 + l02 * r20;
        m[ 1] = l10 * r00 + l11 * r10 + l12 * r20;
        m[ 2] = l20 * r00 + l21 * r10 + l22 * r20;
        m[ 3] = l30 * r00 + l31 * r10 + l32 * r20;
        m[ 4] = l00 * r01 + l01 * r11 + l02 * r21;
        m[ 5] = l10 * r01 + l11 * r11 + l12 * r21;
        m[ 6] = l20 * r01 + l21 * r11 + l22 * r21;
        m[ 7] = l30 * r01 + l31 * r11 + l32 * r21;
        m[ 8] = l00 * r02 + l01 * r12 + l02 * r22;
        m[ 9] = l10 * r02 + l11 * r12 + l12 * r22;
        m[10] = l20 * r02 + l21 * r12 + l22 * r22;
        m[11] = l30 * r02 + l31 * r12 + l32 * r22;
        m[12] = l00 * r03 + l01 * r13 + l02 * r23 + l03;
        m[13] = l10 * r03 + l11 * r13 + l12 * r23 + l13;
        m[14] = l20 * r03 + l21 * r13 + l22 * r23 + l23;
        m[15] = l30 * r03 + l31 * r13 + l32 * r23 + l33;
    }

    private static float[] ensureVertexCapacity(float[] buf, int neededFloats) {
        if (buf.length < neededFloats) {
            return new float[neededFloats];
        }
        return buf;
    }

    private static boolean textureFilterEnabled() {
        String mode = System.getProperty("freej2me.micro3d.textureFilter", "").trim();
        if (mode.length() == 0) {
            mode = System.getProperty("mascotTextureFilter", "").trim();
        }
        return "linear".equalsIgnoreCase(mode)
                || "true".equalsIgnoreCase(mode)
                || "1".equals(mode);
    }

    private static void runOnGlThreadAndWait(Runnable runnable) {
        try {
            if (Thread.currentThread() == GCallBack.getInstance().getOpenglThread()) {
                runnable.run();
                return;
            }
        } catch (Throwable ignored) {
        }
        final Object lock = new Object();
        final boolean[] done = new boolean[]{false};
        final Throwable[] failure = new Throwable[1];
        GForm.addCmd(new GCmd(new Runnable() {
            public void run() {
                synchronized (lock) {
                    try {
                        runnable.run();
                    } catch (Throwable e) {
                        failure[0] = e;
                    }
                    done[0] = true;
                    lock.notifyAll();
                }
            }
        }));
        synchronized (lock) {
            while (!done[0]) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
        if (failure[0] != null) {
            Throwable cause = failure[0];
            // Preserve the real GL-side message + cause instead of swallowing it.
            throw new RuntimeException("micro3d GL render failed: " + cause, cause);
        }
    }

    /**
     * Saves/restores the global GL state this backend mutates, because miniJVM's
     * whole app (nanovg UI + 3D pipelines) shares one GL context.
     */
    private final class GlStateSaver {
        private boolean blend, depthTest, cullFace, scissorTest;
        private final int[] viewport = new int[4];
        private final int[] scissorBox = new int[4];
        private final int[] colorWriteMask = new int[4];
        private final int[] depthWriteMask = new int[1];
        private final int[] depthFunc = new int[1];
        private final int[] activeTexture = new int[1];
        private final int[] currentProgram = new int[1];
        private final int[] vertexArrayBinding = new int[1];
        private final int[] arrayBufferBinding = new int[1];
        private final int[] textureBinding0 = new int[1];
        private final int[] blendSrcRgb = new int[1];
        private final int[] blendDstRgb = new int[1];
        private final int[] blendSrcAlpha = new int[1];
        private final int[] blendDstAlpha = new int[1];
        private final int[] blendEquationRgb = new int[1];
        private final int[] blendEquationAlpha = new int[1];
        private final float[] blendColor = new float[4];
        private final int[] packAlignment = new int[1];

        void capture() {
            blend = org.mini.gl.GL.glIsEnabled(GL_BLEND) != 0;
            depthTest = org.mini.gl.GL.glIsEnabled(GL_DEPTH_TEST) != 0;
            cullFace = org.mini.gl.GL.glIsEnabled(GL_CULL_FACE) != 0;
            scissorTest = org.mini.gl.GL.glIsEnabled(org.mini.gl.GL.GL_SCISSOR_TEST) != 0;
            glGetIntegerv(org.mini.gl.GL.GL_VIEWPORT, viewport, 0);
            glGetIntegerv(org.mini.gl.GL.GL_SCISSOR_BOX, scissorBox, 0);
            glGetIntegerv(org.mini.gl.GL.GL_COLOR_WRITEMASK, colorWriteMask, 0);
            glGetIntegerv(org.mini.gl.GL.GL_DEPTH_WRITEMASK, depthWriteMask, 0);
            glGetIntegerv(org.mini.gl.GL.GL_DEPTH_FUNC, depthFunc, 0);
            glGetIntegerv(org.mini.gl.GL.GL_ACTIVE_TEXTURE, activeTexture, 0);
            glGetIntegerv(org.mini.gl.GL.GL_CURRENT_PROGRAM, currentProgram, 0);
            glGetIntegerv(org.mini.gl.GL.GL_VERTEX_ARRAY_BINDING, vertexArrayBinding, 0);
            glGetIntegerv(org.mini.gl.GL.GL_ARRAY_BUFFER_BINDING, arrayBufferBinding, 0);
            glGetIntegerv(org.mini.gl.GL.GL_TEXTURE_BINDING_2D, textureBinding0, 0);
            glGetIntegerv(org.mini.gl.GL.GL_BLEND_SRC_RGB, blendSrcRgb, 0);
            glGetIntegerv(org.mini.gl.GL.GL_BLEND_DST_RGB, blendDstRgb, 0);
            glGetIntegerv(org.mini.gl.GL.GL_BLEND_SRC_ALPHA, blendSrcAlpha, 0);
            glGetIntegerv(org.mini.gl.GL.GL_BLEND_DST_ALPHA, blendDstAlpha, 0);
            glGetIntegerv(org.mini.gl.GL.GL_BLEND_EQUATION_RGB, blendEquationRgb, 0);
            glGetIntegerv(org.mini.gl.GL.GL_BLEND_EQUATION_ALPHA, blendEquationAlpha, 0);
            glGetFloatv(org.mini.gl.GL.GL_BLEND_COLOR, blendColor, 0);
            glGetIntegerv(org.mini.gl.GL.GL_PACK_ALIGNMENT, packAlignment, 0);
        }

        void restore() {
            setEnabled(GL_BLEND, blend);
            setEnabled(GL_DEPTH_TEST, depthTest);
            setEnabled(GL_CULL_FACE, cullFace);
            setEnabled(org.mini.gl.GL.GL_SCISSOR_TEST, scissorTest);
            org.mini.gl.GL.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            org.mini.gl.GL.glScissor(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3]);
            org.mini.gl.GL.glColorMask(colorWriteMask[0], colorWriteMask[1], colorWriteMask[2], colorWriteMask[3]);
            org.mini.gl.GL.glDepthMask(depthWriteMask[0]);
            org.mini.gl.GL.glDepthFunc(depthFunc[0]);
            org.mini.gl.GL.glActiveTexture(activeTexture[0]);
            org.mini.gl.GL.glUseProgram(currentProgram[0]);
            org.mini.gl.GL.glBindVertexArray(vertexArrayBinding[0]);
            org.mini.gl.GL.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferBinding[0]);
            org.mini.gl.GL.glActiveTexture(org.mini.gl.GL.GL_TEXTURE0);
            org.mini.gl.GL.glBindTexture(GL_TEXTURE_2D, textureBinding0[0]);
            org.mini.gl.GL.glActiveTexture(activeTexture[0]);
            org.mini.gl.GL.glBlendColor(blendColor[0], blendColor[1], blendColor[2], blendColor[3]);
            org.mini.gl.GL.glBlendFuncSeparate(
                    blendSrcRgb[0], blendDstRgb[0], blendSrcAlpha[0], blendDstAlpha[0]);
            org.mini.gl.GL.glBlendEquationSeparate(blendEquationRgb[0], blendEquationAlpha[0]);
            org.mini.gl.GL.glPixelStorei(org.mini.gl.GL.GL_PACK_ALIGNMENT, packAlignment[0]);
        }

        private void setEnabled(int cap, boolean enabled) {
            if (enabled) org.mini.gl.GL.glEnable(cap);
            else org.mini.gl.GL.glDisable(cap);
        }
    }
}

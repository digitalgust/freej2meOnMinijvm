package javax.microedition.m3g;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.m3g.base.M3GMath;

import org.mini.glwrap.GLFrameBuffer;
import org.mini.gui.ImageMutable;
import org.mini.gui.GForm;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.callback.GCmd;
import org.recompile.mobile.PlatformGraphics;

import static javax.microedition.m3g.MiniJvmGraphics3DFactory.GlRenderer.*;
import static org.mini.gl.GL.GL_ARRAY_BUFFER;
import static org.mini.gl.GL.GL_BACK;
import static org.mini.gl.GL.GL_BLEND;
import static org.mini.gl.GL.GL_BGRA;
import static org.mini.gl.GL.GL_CCW;
import static org.mini.gl.GL.GL_CLAMP_TO_EDGE;
import static org.mini.gl.GL.GL_COLOR_BUFFER_BIT;
import static org.mini.gl.GL.GL_CULL_FACE;
import static org.mini.gl.GL.GL_DEPTH_BUFFER_BIT;
import static org.mini.gl.GL.GL_DEPTH_TEST;
import static org.mini.gl.GL.GL_DST_COLOR;
import static org.mini.gl.GL.GL_DYNAMIC_DRAW;
import static org.mini.gl.GL.GL_FALSE;
import static org.mini.gl.GL.GL_FLOAT;
import static org.mini.gl.GL.GL_FRAGMENT_SHADER;
import static org.mini.gl.GL.GL_FRONT;
import static org.mini.gl.GL.GL_GEQUAL;
import static org.mini.gl.GL.GL_INFO_LOG_LENGTH;
import static org.mini.gl.GL.GL_LEQUAL;
import static org.mini.gl.GL.GL_LINK_STATUS;
import static org.mini.gl.GL.GL_LINEAR;
import static org.mini.gl.GL.GL_ONE;
import static org.mini.gl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static org.mini.gl.GL.GL_NEAREST;
import static org.mini.gl.GL.GL_POLYGON_OFFSET_FILL;
import static org.mini.gl.GL.GL_RGBA;
import static org.mini.gl.GL.GL_RGB;
import static org.mini.gl.GL.GL_REPEAT;
import static org.mini.gl.GL.GL_SRC_ALPHA;
import static org.mini.gl.GL.GL_SRC_COLOR;
import static org.mini.gl.GL.GL_STATIC_DRAW;
import static org.mini.gl.GL.GL_TEXTURE0;
import static org.mini.gl.GL.GL_TEXTURE1;
import static org.mini.gl.GL.GL_TEXTURE2;
import static org.mini.gl.GL.GL_TEXTURE_2D;
import static org.mini.gl.GL.GL_TEXTURE_BUFFER;
import static org.mini.gl.GL.GL_RGBA32F;
import static org.mini.gl.GL.GL_TEXTURE_MAG_FILTER;
import static org.mini.gl.GL.GL_TEXTURE_MIN_FILTER;
import static org.mini.gl.GL.GL_TEXTURE_WRAP_S;
import static org.mini.gl.GL.GL_TEXTURE_WRAP_T;
import static org.mini.gl.GL.GL_TRIANGLES;
import static org.mini.gl.GL.GL_TRIANGLE_STRIP;
import static org.mini.gl.GL.GL_TRUE;
import static org.mini.gl.GL.GL_UNSIGNED_BYTE;
import static org.mini.gl.GL.GL_VERSION;
import static org.mini.gl.GL.GL_VERTEX_SHADER;
import static org.mini.gl.GL.GL_ZERO;
import static org.mini.gl.GL.glActiveTexture;
import static org.mini.gl.GL.glAttachShader;
import static org.mini.gl.GL.glBindBuffer;
import static org.mini.gl.GL.glBindVertexArray;
import static org.mini.gl.GL.glBlendFunc;
import static org.mini.gl.GL.glBufferData;
import static org.mini.gl.GL.glClear;
import static org.mini.gl.GL.glClearColor;
import static org.mini.gl.GL.glColorMask;
import static org.mini.gl.GL.glCompileShader;
import static org.mini.gl.GL.glCreateProgram;
import static org.mini.gl.GL.glCreateShader;
import static org.mini.gl.GL.glCullFace;
import static org.mini.gl.GL.glDeleteBuffers;
import static org.mini.gl.GL.glDeleteProgram;
import static org.mini.gl.GL.glDeleteShader;
import static org.mini.gl.GL.glDeleteTextures;
import static org.mini.gl.GL.glDeleteVertexArrays;
import static org.mini.gl.GL.glDepthFunc;
import static org.mini.gl.GL.glDepthMask;
import static org.mini.gl.GL.glDetachShader;
import static org.mini.gl.GL.glDisable;
import static org.mini.gl.GL.glDisableVertexAttribArray;
import static org.mini.gl.GL.glDrawArrays;
import static org.mini.gl.GL.glDrawArraysInstanced;
import static org.mini.gl.GL.glEnable;
import static org.mini.gl.GL.glEnableVertexAttribArray;
import static org.mini.gl.GL.glFrontFace;
import static org.mini.gl.GL.glGenBuffers;
import static org.mini.gl.GL.glGenTextures;
import static org.mini.gl.GL.glGenVertexArrays;
import static org.mini.gl.GL.glGetIntegerv;
import static org.mini.gl.GL.glGetProgramInfoLog;
import static org.mini.gl.GL.glGetProgramiv;
import static org.mini.gl.GL.glGetShaderInfoLog;
import static org.mini.gl.GL.glGetShaderiv;
import static org.mini.gl.GL.glGetString;
import static org.mini.gl.GL.glGetUniformLocation;
import static org.mini.gl.GL.glLinkProgram;
import static org.mini.gl.GL.glPolygonOffset;
import static org.mini.gl.GL.glReadPixels;
import static org.mini.gl.GL.glShaderSource;
import static org.mini.gl.GL.glUniform1f;
import static org.mini.gl.GL.glUniform1i;
import static org.mini.gl.GL.glUniform1iv;
import static org.mini.gl.GL.glUniform3fv;
import static org.mini.gl.GL.glUniform4f;
import static org.mini.gl.GL.glUniform4fv;
import static org.mini.gl.GL.glUniformMatrix4fv;
import static org.mini.gl.GL.glUseProgram;
import static org.mini.gl.GL.glTexBuffer;
import static org.mini.gl.GL.glVertexAttribPointer;
import static org.mini.gl.GL.glVertexAttribDivisor;
import static org.mini.gl.GL.glViewport;
import static org.mini.gl.GL.glBindTexture;
import static org.mini.gl.GL.glTexImage2D;
import static org.mini.gl.GL.glTexParameteri;
import static org.mini.glwrap.GLUtil.toCstyleBytes;

public final class MiniJvmGraphics3DFactory implements Graphics3D.BackendFactory {

    public Graphics3D.Backend create(Graphics3D owner, Graphics3D.Backend softwareFallback) {
        return new MiniJvmGlBackend(owner, softwareFallback);
    }

    private static final class MiniJvmGlBackend implements Graphics3D.SkinningBackend {
        private final Graphics3D owner;
        private final Graphics3D.Backend softwareFallback;
        private final boolean glAvailable;
        private final FrameState frame = new FrameState();
        private final GlRenderer renderer = new GlRenderer();

        MiniJvmGlBackend(Graphics3D owner, Graphics3D.Backend softwareFallback) {
            this.owner = owner;
            this.softwareFallback = softwareFallback;
            this.glAvailable = isGlAvailable();
        }

        public void bindTarget(Object target, boolean depthBuffer, int hints) {
            frame.reset();
            frame.allowImplicitClear = shouldAllowImplicitClear(target, hints);
            frame.boundTarget = target;
            frame.boundHints = hints;
        }

        public void clear(Background background) {
            if (!glAvailable) {
                softwareFallback.clear(background);
                frame.softwarePassthrough = true;
                frame.fallbackReason = "glUnavailableInClear";
                frame.fallbackDetail = "org.mini.gl.GL unavailable";
                return;
            }

            frame.reset();
            frame.clearCalled = true;
            frame.background = background;
            String backgroundRejectReason = getBackgroundRejectReason(background);
            frame.forceSoftware = backgroundRejectReason != null;
            if (frame.forceSoftware) {
                frame.fallbackReason = "unsupportedBackground";
                frame.fallbackDetail = backgroundRejectReason;
            }
        }

        public void render(Mesh mesh, int submeshIndex, VertexBuffer vertices, TriangleStripArray triangles, Appearance appearance, Transform transform) {
            if (frame.softwarePassthrough) {
                softwareFallback.render(mesh, submeshIndex, vertices, triangles, appearance, transform);
                return;
            }
            if (!glAvailable) {
                softwareFallback.render(mesh, submeshIndex, vertices, triangles, appearance, transform);
                return;
            }

            RenderItem item = new RenderItem(mesh, submeshIndex, vertices, triangles, appearance, copyTransform(transform));
            frame.items.addElement(item);
            if (!frame.clearCalled) {
                if (frame.allowImplicitClear) {
                    frame.implicitClearRequested = true;
                } else {
                    frame.forceSoftware = true;
                    if (frame.fallbackReason == null) {
                        frame.fallbackReason = "renderBeforeClear";
                        frame.fallbackDetail = "target requires explicit clear";
                    }
                }
            } else {
                String renderItemRejectReason = getRenderItemRejectReason(item);
                if (renderItemRejectReason != null) {
                    frame.forceSoftware = true;
                    if (frame.fallbackReason == null) {
                        frame.fallbackReason = "unsupportedRenderItem";
                        frame.fallbackDetail = renderItemRejectReason;
                    }
                }
            }
        }

        public boolean renderSkinned(SkinnedMesh mesh, TriangleStripArray triangles, Appearance appearance, Transform transform) {
            if (frame.softwarePassthrough || !glAvailable || frame.forceSoftware || (!frame.clearCalled && !frame.allowImplicitClear)) {
                return false;
            }
            if (!frame.clearCalled) {
                frame.implicitClearRequested = true;
            }
            RenderItem item = RenderItem.createSkinned(mesh, triangles, appearance, copyTransform(transform));
            if (getRenderItemRejectReason(item) != null) {
                return false;
            }
            String skinnedRejectReason = getSkinnedRenderItemRejectReason(item);
            if (skinnedRejectReason != null) {
                return false;
            }
            frame.items.addElement(item);
            return true;
        }

        public void releaseTarget() {
            if (frame.softwarePassthrough) {
                frame.reset();
                return;
            }
            if (!glAvailable) {
                frame.reset();
                return;
            }
            if (!frame.clearCalled && frame.items.isEmpty()) {
                frame.reset();
                return;
            }
            if (!frame.clearCalled && frame.items.size() > 0) {
                if (frame.allowImplicitClear) {
                    frame.implicitClearRequested = true;
                } else {
                    frame.forceSoftware = true;
                    if (frame.fallbackReason == null) {
                        frame.fallbackReason = "renderBeforeClear";
                        frame.fallbackDetail = "target requires explicit clear";
                    }
                }
            }

            boolean renderableTarget = renderer.isRenderableTarget(owner.getTarget());
            boolean glThreadReady = isGlThreadReady();
            if (frame.forceSoftware || !renderableTarget || !glThreadReady) {
                if (!frame.forceSoftware) {
                    frame.fallbackReason = !renderableTarget ? "targetNotRenderable" : "glThreadNotReady";
                    frame.fallbackDetail = !renderableTarget
                            ? (owner.getTarget() == null ? "null" : owner.getTarget().getClass().getName())
                            : "OpenGL thread unavailable";
                }
                replaySoftware();
                frame.reset();
                return;
            }
            final Throwable[] failure = new Throwable[1];
            final long glSubmitStartNs = System.nanoTime();
            runOnGlThreadAndWait(new Runnable() {
                public void run() {
                    try {
                        renderer.renderFrame(owner, frame);
                    } catch (Throwable error) {
                        failure[0] = error;
                    }
                }
            });

            if (failure[0] != null) {
                frame.fallbackReason = "glRenderFailure";
                frame.fallbackDetail = String.valueOf(failure[0]);
                replaySoftware();
            }
            frame.reset();
        }

        private void replaySoftware() {
            if (frame.clearCalled) {
                softwareFallback.clear(frame.background);
            }
            for (int i = 0; i < frame.items.size(); i++) {
                RenderItem item = frame.items.elementAt(i);
                softwareFallback.render(item.mesh, item.submeshIndex, item.vertices, item.triangles, item.appearance, item.transform);
            }
        }

        private boolean supportsBackground(Background background) {
            return getBackgroundRejectReason(background) == null;
        }

        private boolean supportsRenderItem(RenderItem item) {
            return getRenderItemRejectReason(item) == null;
        }

        private String getSkinnedRenderItemRejectReason(RenderItem item) {
            if (item.skinnedMesh == null) {
                return null;
            }
            PolygonMode polygonMode = item.appearance != null ? item.appearance.getPolygonMode() : null;
            if (polygonMode != null && polygonMode.getShading() == PolygonMode.SHADE_FLAT) {
                return "flatShading";
            }
            if (owner.getLightCount() > GlRenderer.MAX_GPU_LIGHTS) {
                return "tooManyLights";
            }
            return item.skinnedMesh.getGpuSkinningUnavailableReason(
                    GlRenderer.MAX_GPU_INFLUENCES, GlRenderer.MAX_GPU_BONES);
        }

        private boolean supportsTexture(Texture2D texture) {
            Image2D image = texture.getImage();
            return image != null && isSupportedImageFormat(image.getFormat());
        }

        private boolean supportsBackgroundImage(Background background) {
            Image2D image = background.getImage();
            if (image == null) {
                return true;
            }
            return isSupportedImageFormat(image.getFormat());
        }

        private String getBackgroundRejectReason(Background background) {
            if (background == null) {
                return null;
            }
            Image2D image = background.getImage();
            if (image == null) {
                return null;
            }
            return supportsBackgroundImage(background)
                    ? null
                    : "backgroundImage.format=" + describeImageFormat(image.getFormat());
        }

        private String getRenderItemRejectReason(RenderItem item) {
            if (item.appearance == null) {
                return null;
            }
            Fog fog = item.appearance.getFog();
            if (fog != null) {
                int fogMode = fog.getMode();
                if (fogMode != Fog.LINEAR && fogMode != Fog.EXPONENTIAL) {
                    return "fog.mode=" + fogMode;
                }
            }

            for (int unit = 0; unit < GlRenderer.TEXTURE_UNIT_COUNT; unit++) {
                Texture2D texture = item.appearance.getTexture(unit);
                if (texture != null && !supportsTexture(texture)) {
                    Image2D image = texture.getImage();
                    return image == null
                            ? "texture" + unit + ".image=null"
                            : "texture" + unit + ".format=" + describeImageFormat(image.getFormat());
                }
            }

            CompositingMode compositingMode = item.appearance.getCompositingMode();
            if (compositingMode != null) {
                int blending = compositingMode.getBlending();
                if (blending != CompositingMode.ALPHA
                        && blending != CompositingMode.ALPHA_ADD
                        && blending != CompositingMode.MODULATE
                        && blending != CompositingMode.MODULATE_X2
                        && blending != CompositingMode.REPLACE) {
                    return "compositing.blending=" + blending;
                }
            }
            return null;
        }

    }

    private static final class FrameState {
        private final Vector<RenderItem> items = new Vector<RenderItem>();
        private Background background;
        private boolean clearCalled;
        private boolean forceSoftware;
        private boolean softwarePassthrough;
        private String fallbackReason;
        private String fallbackDetail;
        private boolean allowImplicitClear;
        private boolean implicitClearRequested;
        private Object boundTarget;
        private int boundHints;

        void reset() {
            items.removeAllElements();
            background = null;
            clearCalled = false;
            forceSoftware = false;
            softwarePassthrough = false;
            fallbackReason = null;
            fallbackDetail = null;
            allowImplicitClear = false;
            implicitClearRequested = false;
            boundTarget = null;
            boundHints = 0;
        }
    }

    private static final class RenderItem {
        private final Mesh mesh;
        private final int submeshIndex;
        private final VertexBuffer vertices;
        private final SkinnedMesh skinnedMesh;
        private final TriangleStripArray triangles;
        private final Appearance appearance;
        private final Transform transform;

        RenderItem(Mesh mesh, int submeshIndex, VertexBuffer vertices, TriangleStripArray triangles, Appearance appearance, Transform transform) {
            this.mesh = mesh;
            this.submeshIndex = submeshIndex;
            this.vertices = vertices;
            this.skinnedMesh = null;
            this.triangles = triangles;
            this.appearance = appearance;
            this.transform = transform;
        }

        private RenderItem(SkinnedMesh skinnedMesh, TriangleStripArray triangles, Appearance appearance, Transform transform) {
            this.mesh = skinnedMesh;
            this.submeshIndex = -1;
            this.vertices = skinnedMesh.getVertexBuffer();
            this.skinnedMesh = skinnedMesh;
            this.triangles = triangles;
            this.appearance = appearance;
            this.transform = transform;
        }

        static RenderItem createSkinned(SkinnedMesh skinnedMesh, TriangleStripArray triangles, Appearance appearance, Transform transform) {
            return new RenderItem(skinnedMesh, triangles, appearance, transform);
        }
    }

    private static final class TriangleData {
        private final float[] vertices;
        private final int vertexCount;
        private final boolean[] texturedUnits;

        TriangleData(float[] vertices, int vertexCount, boolean[] texturedUnits) {
            this.vertices = vertices;
            this.vertexCount = vertexCount;
            this.texturedUnits = texturedUnits;
        }
    }

    private static final class MeshGeometryData {
        private final float[] vertices;
        private final int vertexCount;
        private final boolean[] texCoordUnits;
        private final boolean hasVertexColor;
        private final boolean hasNormals;

        MeshGeometryData(float[] vertices, int vertexCount, boolean[] texCoordUnits, boolean hasVertexColor, boolean hasNormals) {
            this.vertices = vertices;
            this.vertexCount = vertexCount;
            this.texCoordUnits = texCoordUnits;
            this.hasVertexColor = hasVertexColor;
            this.hasNormals = hasNormals;
        }
    }

    private static final class GeometryKey {
        private final boolean useMeshAnchor;
        private final Object anchor;
        private final int submeshIndex;
        private final VertexBuffer vertices;
        private final TriangleStripArray triangles;

        GeometryKey(RenderItem item) {
            boolean mutableMeshGeometry = item.mesh instanceof MorphingMesh;
            this.useMeshAnchor = mutableMeshGeometry;
            this.anchor = mutableMeshGeometry ? item.mesh : item.vertices;
            this.submeshIndex = mutableMeshGeometry ? item.submeshIndex : -1;
            this.vertices = item.vertices;
            this.triangles = item.triangles;
        }

        public int hashCode() {
            int hash = System.identityHashCode(anchor) * 31 + submeshIndex;
            if (!useMeshAnchor) {
                hash = hash * 31 + System.identityHashCode(vertices);
                hash = hash * 31 + System.identityHashCode(triangles);
            }
            return hash;
        }

        public boolean equals(Object object) {
            if (!(object instanceof GeometryKey)) {
                return false;
            }
            GeometryKey other = (GeometryKey) object;
            if (other.anchor != anchor || other.submeshIndex != submeshIndex || other.useMeshAnchor != useMeshAnchor) {
                return false;
            }
            if (useMeshAnchor) {
                return true;
            }
            return other.vertices == vertices && other.triangles == triangles;
        }
    }

    private static final class MeshGeometry {
        private final int[] vbo = new int[]{0};
        private int vertexCount;
        private boolean[] texCoordUnits = new boolean[GlRenderer.TEXTURE_UNIT_COUNT];
        private boolean hasVertexColor;
        private boolean hasNormals;
        private int vertexBufferRevision = -1;
        private VertexArray positions;
        private int positionsRevision = -1;
        private VertexArray normals;
        private int normalsRevision = -1;
        private VertexArray colors;
        private int colorsRevision = -1;
        private final VertexArray[] texCoords = new VertexArray[GlRenderer.TEXTURE_UNIT_COUNT];
        private final int[] texCoordRevisions = new int[]{-1, -1};
        private int morphStateHash = Integer.MIN_VALUE;
    }

    private static final class GeometryCacheEntry {
        private final MeshGeometry geometry = new MeshGeometry();
    }

    private static final class SkinGeometryKey {
        private final Object skinLayout;
        private final VertexBuffer vertices;
        private final TriangleStripArray triangles;
        private final Appearance appearance;

        SkinGeometryKey(RenderItem item) {
            skinLayout = item.skinnedMesh.getGpuGeometrySharingKey();
            vertices = item.vertices;
            triangles = item.triangles;
            appearance = item.appearance;
        }

        public int hashCode() {
            int hash = System.identityHashCode(skinLayout);
            hash = hash * 31 + System.identityHashCode(vertices);
            hash = hash * 31 + System.identityHashCode(triangles);
            return hash * 31 + System.identityHashCode(appearance);
        }

        public boolean equals(Object object) {
            if (!(object instanceof SkinGeometryKey)) {
                return false;
            }
            SkinGeometryKey other = (SkinGeometryKey) object;
            return skinLayout == other.skinLayout
                    && vertices == other.vertices
                    && triangles == other.triangles
                    && appearance == other.appearance;
        }
    }

    private static final class SkinGeometry {
        private final int[] vbo = new int[]{0};
        private int vertexCount;
        private boolean hasVertexColor;
        private boolean hasNormals;
        private int vertexBufferRevision = -1;
        private VertexArray positions;
        private int positionsRevision = -1;
        private VertexArray normals;
        private int normalsRevision = -1;
        private VertexArray colors;
        private int colorsRevision = -1;
        private final VertexArray[] texCoords = new VertexArray[GlRenderer.TEXTURE_UNIT_COUNT];
        private final int[] texCoordRevisions = new int[]{-1, -1};
        private final boolean[] texturedUnits = new boolean[GlRenderer.TEXTURE_UNIT_COUNT];
        private final float[][] textureMatrices = new float[][]{
                new float[16], new float[16]
        };
    }

    private static final class SkinDrawData {
        private final boolean[] texturedUnits = new boolean[GlRenderer.TEXTURE_UNIT_COUNT];
        private int baseColor;
        private boolean hasVertexColor;
        private boolean lightingEnabled;
    }

    private static final class SkinTriangleData {
        private final float[] vertices;
        private final int vertexCount;
        private final boolean[] texturedUnits;
        private final int baseColor;
        private final boolean hasVertexColor;
        private final boolean lightingEnabled;

        SkinTriangleData(float[] vertices, int vertexCount, boolean[] texturedUnits, int baseColor, boolean hasVertexColor,
                         boolean lightingEnabled) {
            this.vertices = vertices;
            this.vertexCount = vertexCount;
            this.texturedUnits = texturedUnits;
            this.baseColor = baseColor;
            this.hasVertexColor = hasVertexColor;
            this.lightingEnabled = lightingEnabled;
        }
    }

    static final class GlRenderer {
        private static final int TEXTURE_UNIT_COUNT = 2;
        private static final int FLOAT_SIZE = 4;
        private static final int COMPONENTS_PER_VERTEX = 14;
        private static final int STRIDE_BYTES = COMPONENTS_PER_VERTEX * FLOAT_SIZE;
        static final int MAX_GPU_INFLUENCES = 4;
        static final int MAX_GPU_BONES = 24;
        static final int MAX_GPU_LIGHTS = 4;
        private static final int SKIN_COMPONENTS_PER_VERTEX = 22;
        private static final int SKIN_STRIDE_BYTES = SKIN_COMPONENTS_PER_VERTEX * FLOAT_SIZE;
        static final int READBACK_BYTES_PER_PIXEL = 4;
        private static final String MESH_VERTEX_SHADER_RESOURCE = "/glsl/m3g_mesh.vert.glsl";
        private static final String SKIN_VERTEX_SHADER_RESOURCE = "/glsl/m3g_skin.vert.glsl";
        private static final String FRAGMENT_SHADER_RESOURCE = "/glsl/m3g_common.frag.glsl";
        private static final int TEXTURE_MODE_REPLACE = 0;
        private static final int TEXTURE_MODE_MODULATE = 1;
        private static final int TEXTURE_MODE_ADD = 2;
        private static final int TEXTURE_MODE_BLEND = 3;
        private static final int TEXTURE_MODE_DECAL = 4;

        private final int[] vao = new int[]{0};
        private final int[] vbo = new int[]{0};
        private final int[] skinInstancedVao = new int[]{0};
        private final int[] skinInstanceVbo = new int[]{0};
        private final int[] skinPaletteBuffer = new int[]{0};
        private final int[] skinPaletteTexture = new int[]{0};
        private final int[] backgroundTexture = new int[]{0};
        private final float[] backgroundVertices = new float[4 * COMPONENTS_PER_VERTEX];
        private final float[] identityMatrix = new float[16];
        private final float[] cameraMatrix = new float[16];
        private final float[] viewMatrix = new float[16];
        private final float[] modelMatrix = new float[16];
        private final float[] modelViewMatrix = new float[16];
        private final float[] modelViewProjection = new float[16];
        private final Transform textureTransformScratch = new Transform();
        private final float[] textureVector = new float[4];
        private final float[] skinBoneRows = new float[MAX_GPU_BONES * 12];
        private float[] skinInstanceData = new float[0];
        private float[] skinPaletteData = new float[0];
        private final SkinDrawData skinDrawDataScratch = new SkinDrawData();
        private final int[] skinLightModes = new int[MAX_GPU_LIGHTS];
        private final float[] skinLightColors = new float[MAX_GPU_LIGHTS * 4];
        private final float[] skinLightPositions = new float[MAX_GPU_LIGHTS * 4];
        private final float[] skinLightDirections = new float[MAX_GPU_LIGHTS * 4];
        private final float[] skinLightAttenuations = new float[MAX_GPU_LIGHTS * 4];
        private final float[] skinLightSpots = new float[MAX_GPU_LIGHTS * 4];
        private final java.util.Hashtable textureCache = new java.util.Hashtable();
        private final java.util.HashMap geometryCache = new java.util.HashMap();
        private final java.util.HashMap skinGeometryCache = new java.util.HashMap();
        private GLFrameBuffer frameBuffer;
        private int program;
        private int skinProgram;
        private int mvpLocation = -1;
        private int modelLocation = -1;
        private int modelViewLocation = -1;
        private int alphaThresholdLocation = -1;
        private int depthRangeScaleLocation = -1;
        private int depthRangeBiasLocation = -1;
        private int fogModeLocation = -1;
        private int fogColorLocation = -1;
        private int fogNearLocation = -1;
        private int fogFarLocation = -1;
        private int fogDensityLocation = -1;
        private final int[] useTextureLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] textureModeLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] textureSamplerLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] textureBlendColorLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] textureMatrixLocations = new int[TEXTURE_UNIT_COUNT];
        private int hasVertexColorLocation = -1;
        private int baseColorLocation = -1;
        private int lightingEnabledLocation = -1;
        private int vertexColorTrackingLocation = -1;
        private int materialAmbientLocation = -1;
        private int materialDiffuseLocation = -1;
        private int materialEmissiveLocation = -1;
        private int materialSpecularLocation = -1;
        private int materialShininessLocation = -1;
        private int cameraWorldPositionLocation = -1;
        private int lightCountLocation = -1;
        private int lightModesLocation = -1;
        private int lightColorsLocation = -1;
        private int lightPositionsLocation = -1;
        private int lightDirectionsLocation = -1;
        private int lightAttenuationsLocation = -1;
        private int lightSpotsLocation = -1;
        private int skinMvpLocation = -1;
        private int skinModelLocation = -1;
        private int skinModelViewLocation = -1;
        private int skinAlphaThresholdLocation = -1;
        private int skinDepthRangeScaleLocation = -1;
        private int skinDepthRangeBiasLocation = -1;
        private int skinFogModeLocation = -1;
        private int skinFogColorLocation = -1;
        private int skinFogNearLocation = -1;
        private int skinFogFarLocation = -1;
        private int skinFogDensityLocation = -1;
        private final int[] skinUseTextureLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] skinTextureModeLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] skinTextureSamplerLocations = new int[TEXTURE_UNIT_COUNT];
        private final int[] skinTextureBlendColorLocations = new int[TEXTURE_UNIT_COUNT];
        private int skinHasVertexColorLocation = -1;
        private int skinBaseColorLocation = -1;
        private int skinLightingEnabledLocation = -1;
        private int skinVertexColorTrackingLocation = -1;
        private int skinMaterialAmbientLocation = -1;
        private int skinMaterialDiffuseLocation = -1;
        private int skinMaterialEmissiveLocation = -1;
        private int skinMaterialSpecularLocation = -1;
        private int skinMaterialShininessLocation = -1;
        private int skinCameraWorldPositionLocation = -1;
        private int frameDrawSequence = 0;
        private int skinLightCountLocation = -1;
        private int skinLightModesLocation = -1;
        private int skinLightColorsLocation = -1;
        private int skinLightPositionsLocation = -1;
        private int skinLightDirectionsLocation = -1;
        private int skinLightAttenuationsLocation = -1;
        private int skinLightSpotsLocation = -1;
        private int skinBoneRowsLocation = -1;
        private int skinInstancedLocation = -1;
        private int skinCameraMatrixLocation = -1;
        private int skinViewMatrixLocation = -1;
        private int skinBonePaletteSamplerLocation = -1;
        private int frameWidth = -1;
        private int frameHeight = -1;
        private byte[] rgbaReadBackBuffer = new byte[0];
        private byte[] nativeReadBackBuffer = new byte[0];
        private Boolean nativeBufferedImageReadbackSupported;
        private Boolean openGlesContext;

        GlRenderer() {
            for (int i = 0; i < 16; i++) {
                identityMatrix[i] = (i % 5) == 0 ? 1f : 0f;
            }
        }

        boolean isRenderableTarget(Object target) {
            return resolveTargetSurface(target, false) != null;
        }

        void renderFrame(Graphics3D owner, FrameState frame) {
            RenderTargetSurface target = resolveTargetSurface(owner.getTarget(), false);
            if (target == null) {
                throw new IllegalStateException();
            }
            ensureInitialized(owner.getViewportWidth(), owner.getViewportHeight());
            // miniJVM 整个应用（EmuForm 的 nanovg 界面 + 本 3D 管线）共享同一个 GL 上下文，
            // 都在 GL 线程上绘制。3D 管线会把大量全局 GL 状态（viewport / scissor / blend /
            // depth / cull / colorMask / polygonOffset / program / vao / vbo / texture binding /
            // active texture / pixelStore）改成自己需要的值。这里在进入 FBO 渲染前把相关
            // 状态整体保存，finally 里整体恢复，避免污染共享上下文里的其它绘制。
            GlStateSaver savedState = new GlStateSaver();
            savedState.capture();
            frameBuffer.begin();
            // 安全校验：确认我们的离屏 FBO 真的绑上了。正常情况下它一定非零；
            // 若发生 FBO 句柄异常失效（历史上由 GLFrameBuffer.finalize 误删复用 id 引发），
            // 绑定会落到默认窗口（0），此时 glViewport(0,0,3D视口) 会让整帧画到窗口左下角。
            // 这种情况下整帧回退软件渲染，宁可降速也不污染屏幕。
            int[] boundFbo = new int[1];
            org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_FRAMEBUFFER_BINDING, boundFbo, 0);
            if (boundFbo[0] == 0) {
                frameBuffer.end();
                savedState.restore();
                throw new IllegalStateException("FBO not bound");
            }
            try {
                glViewport(0, 0, owner.getViewportWidth(), owner.getViewportHeight());
                if (frame.implicitClearRequested && !frame.clearCalled) {
                    boolean copiedExistingTarget = false;
                    if (!frame.clearCalled) {
                        copiedExistingTarget = renderExistingTargetBackground(owner);
                    }
                    if (!copiedExistingTarget) {
                        applyBackground(owner, null);
                    }
                    clearDepthBuffer(owner);
                } else {
                    applyBackground(owner, frame.background);
                }
                prepareCamera(owner);
                frameDrawSequence = 0;
                for (int i = 0; i < frame.items.size(); i++) {
                    RenderItem item = frame.items.elementAt(i);
                    if (isOpaqueSkinnedBatchItem(owner, item)) {
                        // Collapse adjacent compatible items only, so opaque batching
                        // does not reorder them across other world draws.
                        int end = i + 1;
                        while (end < frame.items.size()) {
                            RenderItem next = frame.items.elementAt(end);
                            if (!isOpaqueSkinnedBatchItem(owner, next)
                                    || !sameSkinGeometryGroup(item, next)) {
                                break;
                            }
                            end++;
                        }
                        if (end - i > 1 && renderSkinnedBatch(owner, frame.items, i, end)) {
                            i = end - 1;
                            continue;
                        }
                    }
                    renderItem(owner, item);
                }
                readBack(owner, target);
            } finally {
                // 最小解绑
                org.mini.gl.GL.glUseProgram(0);
                org.mini.gl.GL.glBindVertexArray(0);
                org.mini.gl.GL.glBindBuffer(org.mini.gl.GL.GL_ARRAY_BUFFER, 0);
                org.mini.gl.GL.glActiveTexture(org.mini.gl.GL.GL_TEXTURE0);
                org.mini.gl.GL.glBindTexture(org.mini.gl.GL.GL_TEXTURE_2D, 0);

                frameBuffer.end();
                // 恢复进入前保存的全局 GL 状态，避免污染共享上下文里的其它绘制管线。
                savedState.restore();
            }
        }

        private void ensureInitialized(int width, int height) {
            if (frameBuffer == null || frameWidth != width || frameHeight != height) {
                if (frameBuffer != null) {
                    frameBuffer.delete();
                }
                frameBuffer = new GLFrameBuffer(width, height, 1f, true);
                frameBuffer.gl_init();
                frameWidth = width;
                frameHeight = height;
            }
            if (program == 0) {
                String meshVertexShader = adaptShaderVersion(readShaderResource(MESH_VERTEX_SHADER_RESOURCE));
                String fragmentShader = adaptShaderVersion(readShaderResource(FRAGMENT_SHADER_RESOURCE));
                String skinVertexShader = adaptShaderVersion(readShaderResource(SKIN_VERTEX_SHADER_RESOURCE));
                program = loadProgram(meshVertexShader, fragmentShader);
                mvpLocation = glGetUniformLocation(program, toCstyleBytes("uMvp"));
                modelLocation = glGetUniformLocation(program, toCstyleBytes("uModel"));
                modelViewLocation = glGetUniformLocation(program, toCstyleBytes("uModelView"));
                alphaThresholdLocation = glGetUniformLocation(program, toCstyleBytes("uAlphaThreshold"));
                depthRangeScaleLocation = glGetUniformLocation(program, toCstyleBytes("uDepthRangeScale"));
                depthRangeBiasLocation = glGetUniformLocation(program, toCstyleBytes("uDepthRangeBias"));
                fogModeLocation = glGetUniformLocation(program, toCstyleBytes("uFogMode"));
                fogColorLocation = glGetUniformLocation(program, toCstyleBytes("uFogColor"));
                fogNearLocation = glGetUniformLocation(program, toCstyleBytes("uFogNear"));
                fogFarLocation = glGetUniformLocation(program, toCstyleBytes("uFogFar"));
                fogDensityLocation = glGetUniformLocation(program, toCstyleBytes("uFogDensity"));
                for (int i = 0; i < TEXTURE_UNIT_COUNT; i++) {
                    useTextureLocations[i] = glGetUniformLocation(program, toCstyleBytes("uUseTexture" + i));
                    textureModeLocations[i] = glGetUniformLocation(program, toCstyleBytes("uTextureMode" + i));
                    textureSamplerLocations[i] = glGetUniformLocation(program, toCstyleBytes("uTexture" + i));
                    textureBlendColorLocations[i] = glGetUniformLocation(program, toCstyleBytes("uTextureBlendColor" + i));
                    textureMatrixLocations[i] = glGetUniformLocation(program, toCstyleBytes("uTextureMatrix" + i));
                }
                hasVertexColorLocation = glGetUniformLocation(program, toCstyleBytes("uHasVertexColor"));
                baseColorLocation = glGetUniformLocation(program, toCstyleBytes("uBaseColor"));
                lightingEnabledLocation = glGetUniformLocation(program, toCstyleBytes("uLightingEnabled"));
                vertexColorTrackingLocation = glGetUniformLocation(program, toCstyleBytes("uVertexColorTracking"));
                materialAmbientLocation = glGetUniformLocation(program, toCstyleBytes("uMaterialAmbient"));
                materialDiffuseLocation = glGetUniformLocation(program, toCstyleBytes("uMaterialDiffuse"));
                materialEmissiveLocation = glGetUniformLocation(program, toCstyleBytes("uMaterialEmissive"));
                materialSpecularLocation = glGetUniformLocation(program, toCstyleBytes("uMaterialSpecular"));
                materialShininessLocation = glGetUniformLocation(program, toCstyleBytes("uMaterialShininess"));
                cameraWorldPositionLocation = glGetUniformLocation(program, toCstyleBytes("uCameraWorldPos"));
                lightCountLocation = glGetUniformLocation(program, toCstyleBytes("uLightCount"));
                lightModesLocation = glGetUniformLocation(program, toCstyleBytes("uLightMode[0]"));
                lightColorsLocation = glGetUniformLocation(program, toCstyleBytes("uLightColor[0]"));
                lightPositionsLocation = glGetUniformLocation(program, toCstyleBytes("uLightPosition[0]"));
                lightDirectionsLocation = glGetUniformLocation(program, toCstyleBytes("uLightDirection[0]"));
                lightAttenuationsLocation = glGetUniformLocation(program, toCstyleBytes("uLightAttenuation[0]"));
                lightSpotsLocation = glGetUniformLocation(program, toCstyleBytes("uLightSpot[0]"));
                skinProgram = loadProgram(skinVertexShader, fragmentShader);
                skinMvpLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uMvp"));
                skinModelLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uModel"));
                skinModelViewLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uModelView"));
                skinAlphaThresholdLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uAlphaThreshold"));
                skinDepthRangeScaleLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uDepthRangeScale"));
                skinDepthRangeBiasLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uDepthRangeBias"));
                skinFogModeLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uFogMode"));
                skinFogColorLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uFogColor"));
                skinFogNearLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uFogNear"));
                skinFogFarLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uFogFar"));
                skinFogDensityLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uFogDensity"));
                for (int i = 0; i < TEXTURE_UNIT_COUNT; i++) {
                    skinUseTextureLocations[i] = glGetUniformLocation(skinProgram, toCstyleBytes("uUseTexture" + i));
                    skinTextureModeLocations[i] = glGetUniformLocation(skinProgram, toCstyleBytes("uTextureMode" + i));
                    skinTextureSamplerLocations[i] = glGetUniformLocation(skinProgram, toCstyleBytes("uTexture" + i));
                    skinTextureBlendColorLocations[i] = glGetUniformLocation(skinProgram, toCstyleBytes("uTextureBlendColor" + i));
                }
                skinHasVertexColorLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uHasVertexColor"));
                skinBaseColorLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uBaseColor"));
                skinLightingEnabledLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightingEnabled"));
                skinVertexColorTrackingLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uVertexColorTracking"));
                skinMaterialAmbientLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uMaterialAmbient"));
                skinMaterialDiffuseLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uMaterialDiffuse"));
                skinMaterialEmissiveLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uMaterialEmissive"));
                skinMaterialSpecularLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uMaterialSpecular"));
                skinMaterialShininessLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uMaterialShininess"));
                skinCameraWorldPositionLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uCameraWorldPos"));
                skinLightCountLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightCount"));
                skinLightModesLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightMode[0]"));
                skinLightColorsLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightColor[0]"));
                skinLightPositionsLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightPosition[0]"));
                skinLightDirectionsLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightDirection[0]"));
                skinLightAttenuationsLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightAttenuation[0]"));
                skinLightSpotsLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uLightSpot[0]"));
                skinBoneRowsLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uBoneRows[0]"));
                skinInstancedLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uUseInstances"));
                skinCameraMatrixLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uCameraMatrix"));
                skinViewMatrixLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uViewMatrix"));
                skinBonePaletteSamplerLocation = glGetUniformLocation(skinProgram, toCstyleBytes("uBonePalette"));
            }
            if (vao[0] == 0) {
                glGenVertexArrays(1, vao, 0);
                glGenBuffers(1, vbo, 0);
                glGenVertexArrays(1, skinInstancedVao, 0);
                glGenBuffers(1, skinInstanceVbo, 0);
                glGenBuffers(1, skinPaletteBuffer, 0);
                glGenTextures(1, skinPaletteTexture, 0);

                glBindVertexArray(skinInstancedVao[0]);
                for (int attribute = 7; attribute <= 11; attribute++) {
                    glEnableVertexAttribArray(attribute);
                    glVertexAttribDivisor(attribute, 1);
                }
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_BUFFER, skinPaletteTexture[0]);
                glBindBuffer(GL_TEXTURE_BUFFER, skinPaletteBuffer[0]);
                glBufferData(GL_TEXTURE_BUFFER, 4L * FLOAT_SIZE, new float[4], 0, GL_DYNAMIC_DRAW);
                glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32F, skinPaletteBuffer[0]);
                glBindBuffer(GL_TEXTURE_BUFFER, 0);
                glBindTexture(GL_TEXTURE_BUFFER, 0);
                glBindVertexArray(0);
            }
        }

        /**
         * 保存并恢复 3D 管线会修改的全部全局 GL 状态。
         * miniJVM 整个应用（EmuForm 的 nanovg 界面 + 本 3D 管线）共享同一个 GL 上下文，
         * 因此每帧 3D 渲染结束后必须把状态还原，否则会污染 nanovg 的后续绘制。
         */
        private final class GlStateSaver {
            private boolean blend;
            private boolean depthTest;
            private boolean cullFace;
            private boolean scissorTest;
            private boolean polygonOffsetFill;
            private int[] viewport = new int[4];
            private int[] scissorBox = new int[4];
            private int[] colorWriteMask = new int[4];
            private int[] depthWriteMask = new int[1];
            private int[] depthFunc = new int[1];
            private int[] activeTexture = new int[1];
            private int[] currentProgram = new int[1];
            private int[] vertexArrayBinding = new int[1];
            private int[] arrayBufferBinding = new int[1];
            private int[] elementArrayBufferBinding = new int[1];
            private int[] textureBufferObjectBinding = new int[1];
            private int[] textureBinding0 = new int[1];
            private int[] textureBinding1 = new int[1];
            private int[] textureBufferBinding2 = new int[1];
            private int[] blendSrcRgb = new int[1];
            private int[] blendDstRgb = new int[1];
            private int[] blendSrcAlpha = new int[1];
            private int[] blendDstAlpha = new int[1];
            private int[] blendEquationRgb = new int[1];
            private int[] blendEquationAlpha = new int[1];
            private int[] packAlignment = new int[1];
            private int[] unpackAlignment = new int[1];

            void capture() {
                blend = org.mini.gl.GL.glIsEnabled(GL_BLEND) != 0;
                depthTest = org.mini.gl.GL.glIsEnabled(GL_DEPTH_TEST) != 0;
                cullFace = org.mini.gl.GL.glIsEnabled(GL_CULL_FACE) != 0;
                scissorTest = org.mini.gl.GL.glIsEnabled(org.mini.gl.GL.GL_SCISSOR_TEST) != 0;
                polygonOffsetFill = org.mini.gl.GL.glIsEnabled(GL_POLYGON_OFFSET_FILL) != 0;
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_VIEWPORT, viewport, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_SCISSOR_BOX, scissorBox, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_COLOR_WRITEMASK, colorWriteMask, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_DEPTH_WRITEMASK, depthWriteMask, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_DEPTH_FUNC, depthFunc, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_ACTIVE_TEXTURE, activeTexture, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_CURRENT_PROGRAM, currentProgram, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_VERTEX_ARRAY_BINDING, vertexArrayBinding, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_ARRAY_BUFFER_BINDING, arrayBufferBinding, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_ELEMENT_ARRAY_BUFFER_BINDING, elementArrayBufferBinding, 0);
                org.mini.gl.GL.glGetIntegerv(GL_TEXTURE_BUFFER, textureBufferObjectBinding, 0);
                org.mini.gl.GL.glActiveTexture(GL_TEXTURE0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_TEXTURE_BINDING_2D, textureBinding0, 0);
                org.mini.gl.GL.glActiveTexture(GL_TEXTURE1);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_TEXTURE_BINDING_2D, textureBinding1, 0);
                org.mini.gl.GL.glActiveTexture(GL_TEXTURE2);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_TEXTURE_BINDING_BUFFER, textureBufferBinding2, 0);
                org.mini.gl.GL.glActiveTexture(activeTexture[0]);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_BLEND_SRC_RGB, blendSrcRgb, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_BLEND_DST_RGB, blendDstRgb, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_BLEND_SRC_ALPHA, blendSrcAlpha, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_BLEND_DST_ALPHA, blendDstAlpha, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_BLEND_EQUATION_RGB, blendEquationRgb, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_BLEND_EQUATION_ALPHA, blendEquationAlpha, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_PACK_ALIGNMENT, packAlignment, 0);
                org.mini.gl.GL.glGetIntegerv(org.mini.gl.GL.GL_UNPACK_ALIGNMENT, unpackAlignment, 0);
            }

            void restore() {
                setEnabled(GL_BLEND, blend);
                setEnabled(GL_DEPTH_TEST, depthTest);
                setEnabled(GL_CULL_FACE, cullFace);
                setEnabled(org.mini.gl.GL.GL_SCISSOR_TEST, scissorTest);
                setEnabled(GL_POLYGON_OFFSET_FILL, polygonOffsetFill);
                org.mini.gl.GL.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
                org.mini.gl.GL.glScissor(scissorBox[0], scissorBox[1], scissorBox[2], scissorBox[3]);
                org.mini.gl.GL.glColorMask(
                        colorWriteMask[0], colorWriteMask[1], colorWriteMask[2], colorWriteMask[3]);
                org.mini.gl.GL.glDepthMask(depthWriteMask[0]);
                org.mini.gl.GL.glDepthFunc(depthFunc[0]);
                org.mini.gl.GL.glActiveTexture(activeTexture[0]);
                org.mini.gl.GL.glUseProgram(currentProgram[0]);
                org.mini.gl.GL.glBindVertexArray(vertexArrayBinding[0]);
                org.mini.gl.GL.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferBinding[0]);
                org.mini.gl.GL.glBindBuffer(org.mini.gl.GL.GL_ELEMENT_ARRAY_BUFFER, elementArrayBufferBinding[0]);
                org.mini.gl.GL.glBindBuffer(GL_TEXTURE_BUFFER, textureBufferObjectBinding[0]);
                org.mini.gl.GL.glActiveTexture(org.mini.gl.GL.GL_TEXTURE0);
                org.mini.gl.GL.glBindTexture(GL_TEXTURE_2D, textureBinding0[0]);
                org.mini.gl.GL.glActiveTexture(GL_TEXTURE1);
                org.mini.gl.GL.glBindTexture(GL_TEXTURE_2D, textureBinding1[0]);
                org.mini.gl.GL.glActiveTexture(GL_TEXTURE2);
                org.mini.gl.GL.glBindTexture(GL_TEXTURE_BUFFER, textureBufferBinding2[0]);
                org.mini.gl.GL.glActiveTexture(activeTexture[0]);
                org.mini.gl.GL.glBlendFuncSeparate(blendSrcRgb[0], blendDstRgb[0], blendSrcAlpha[0], blendDstAlpha[0]);
                org.mini.gl.GL.glBlendEquationSeparate(blendEquationRgb[0], blendEquationAlpha[0]);
                org.mini.gl.GL.glPixelStorei(org.mini.gl.GL.GL_PACK_ALIGNMENT, packAlignment[0]);
                org.mini.gl.GL.glPixelStorei(org.mini.gl.GL.GL_UNPACK_ALIGNMENT, unpackAlignment[0]);
            }

            private void setEnabled(int cap, boolean enabled) {
                if (enabled) {
                    org.mini.gl.GL.glEnable(cap);
                } else {
                    org.mini.gl.GL.glDisable(cap);
                }
            }
        }

        private void applyBackground(Graphics3D owner, Background background) {
            int color = background != null ? background.getColor() : 0x00000000;
            boolean clearColor = background == null || background.isColorClearEnabled();
            boolean effectiveDepthEnabled = owner.isDepthBufferEnabled();
            boolean clearDepth = effectiveDepthEnabled && (background == null || background.isDepthClearEnabled());
            if (!clearColor && background != null && background.getImage() == null && owner.getTarget() instanceof javax.microedition.lcdui.Graphics) {
                // 游戏常以 `setColorClearEnable(false)` 的 Background 做“只清深度”的清屏，
                // 以便保留 bindTarget 前先画到 Graphics 目标上的 2D 背景（天空/路面）。
                // 之前这里强制 clearColor=true 会把整张目标颜色擦成 Background.color，
                // 导致 2D 背景丢失、整窗口闪烁。
                // 这里改为把当前目标画布内容拷进 FBO 作为背景，从而保留 2D 背景；
                // 拷贝不可用时再退化为强制清色。
                clearColor = !renderExistingTargetBackground(owner);
            }
            if (effectiveDepthEnabled) {
                glEnable(GL_DEPTH_TEST);
                glDepthMask(GL_TRUE);
                glDepthFunc(GL_LEQUAL);
            } else {
                glDisable(GL_DEPTH_TEST);
                glDepthMask(GL_FALSE);
            }
            glDisable(GL_BLEND);
            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
            glClearColor(((color >>> 16) & 0xFF) / 255f, ((color >>> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >>> 24) & 0xFF) / 255f);
            int clearMask = 0;
            if (clearColor) {
                clearMask |= GL_COLOR_BUFFER_BIT;
            }
            if (clearDepth) {
                clearMask |= GL_DEPTH_BUFFER_BIT;
            }
            if (clearMask != 0) {
                glClear(clearMask);
            }
            if (clearColor && background != null && background.getImage() != null) {
                renderBackgroundImage(owner, background);
            }
        }

        private void clearDepthBuffer(Graphics3D owner) {
            if (!owner.isDepthBufferEnabled()) {
                return;
            }
            glEnable(GL_DEPTH_TEST);
            glDepthMask(GL_TRUE);
            glClear(GL_DEPTH_BUFFER_BIT);
        }

        private void prepareCamera(Graphics3D owner) {
            Transform cameraTransform = new Transform();
            Camera camera = owner.getCamera(cameraTransform);
            Transform projection = camera.getProjectionTransform(owner.getViewportWidth(), owner.getViewportHeight());
            cameraTransform.invert();
            System.arraycopy(cameraTransform.getMatrix(), 0, viewMatrix, 0, 16);
            Transform combined = new Transform(projection);
            combined.postMultiply(cameraTransform);
            System.arraycopy(combined.getMatrix(), 0, cameraMatrix, 0, 16);
        }

        private void renderItem(Graphics3D owner, RenderItem item) {
            if (item.skinnedMesh != null) {
                renderSkinnedItem(owner, item);
                return;
            }
            MeshGeometry geometry = getOrCreateMeshGeometry(item);
            if (geometry.vertexCount == 0) {
                return;
            }

            Transform combined = new Transform();
            combined.set(cameraMatrix);
            if (item.transform != null) {
                combined.postMultiply(item.transform);
                System.arraycopy(item.transform.getMatrix(), 0, modelMatrix, 0, 16);
            } else {
                System.arraycopy(identityMatrix, 0, modelMatrix, 0, 16);
            }
            System.arraycopy(combined.getMatrix(), 0, modelViewProjection, 0, 16);
            Transform modelView = new Transform();
            modelView.set(viewMatrix);
            if (item.transform != null) {
                modelView.postMultiply(item.transform);
            }
            System.arraycopy(modelView.getMatrix(), 0, modelViewMatrix, 0, 16);

            applyAppearance(owner, item.appearance, item);

            glUseProgram(program);
            glUniformMatrix4fv(mvpLocation, 1, GL_TRUE, modelViewProjection, 0);
            glUniformMatrix4fv(modelLocation, 1, GL_TRUE, modelMatrix, 0);
            glUniformMatrix4fv(modelViewLocation, 1, GL_TRUE, modelViewMatrix, 0);
            glUniform1f(alphaThresholdLocation, resolveAlphaThreshold(item.appearance));
            glUniform1f(depthRangeScaleLocation, owner.getDepthRangeFar() - owner.getDepthRangeNear());
            glUniform1f(depthRangeBiasLocation, owner.getDepthRangeFar() + owner.getDepthRangeNear() - 1f);
            applyFog(item.appearance);
            applyTextures(item.appearance, geometry.texCoordUnits);
            int baseColor = resolveBaseColor(item.vertices, item.appearance);
            boolean lightingEnabled = geometry.hasNormals
                    && item.appearance != null
                    && item.appearance.getMaterial() != null
                    && owner.getLightCount() > 0;
            applyMaterial(owner, item, geometry);
            applyLights(owner);

            glBindVertexArray(vao[0]);
            glBindBuffer(GL_ARRAY_BUFFER, geometry.vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 3 * FLOAT_SIZE);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 4, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 6 * FLOAT_SIZE);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(3, 2, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 10 * FLOAT_SIZE);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(4, 2, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 12 * FLOAT_SIZE);
            glEnableVertexAttribArray(4);
            int drawSequence = ++frameDrawSequence;
            glDrawArrays(GL_TRIANGLES, 0, geometry.vertexCount);
            glDisableVertexAttribArray(4);
            glDisableVertexAttribArray(3);
            glDisableVertexAttribArray(2);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);
        }

        private void renderSkinnedItem(Graphics3D owner, RenderItem item) {
            SkinnedMesh.GpuSkinningData skinningData = item.skinnedMesh.getGpuSkinningData(MAX_GPU_INFLUENCES);
            if (skinningData == null || skinningData.boneCount > MAX_GPU_BONES) {
                return;
            }
            SkinGeometry geometry = getOrCreateSkinnedGeometry(owner, item, skinningData);
            if (geometry.vertexCount == 0) {
                return;
            }
            SkinDrawData drawData = prepareSkinDrawData(owner, item, geometry);

            Transform combined = new Transform();
            combined.set(cameraMatrix);
            if (item.transform != null) {
                combined.postMultiply(item.transform);
                System.arraycopy(item.transform.getMatrix(), 0, modelMatrix, 0, 16);
            } else {
                System.arraycopy(identityMatrix, 0, modelMatrix, 0, 16);
            }
            System.arraycopy(combined.getMatrix(), 0, modelViewProjection, 0, 16);
            Transform modelView = new Transform();
            modelView.set(viewMatrix);
            if (item.transform != null) {
                modelView.postMultiply(item.transform);
            }
            System.arraycopy(modelView.getMatrix(), 0, modelViewMatrix, 0, 16);

            applyAppearance(owner, item.appearance, item);
            glUseProgram(skinProgram);
            glUniform1i(skinInstancedLocation, 0);
            glUniformMatrix4fv(skinMvpLocation, 1, GL_TRUE, modelViewProjection, 0);
            glUniformMatrix4fv(skinModelLocation, 1, GL_TRUE, modelMatrix, 0);
            glUniformMatrix4fv(skinModelViewLocation, 1, GL_TRUE, modelViewMatrix, 0);
            applySkinCommonState(owner, item, drawData);
            uploadBoneRows(skinningData.boneMatrices, skinningData.boneCount);
            bindSkinPaletteTexture();

            glBindVertexArray(vao[0]);
            glBindBuffer(GL_ARRAY_BUFFER, geometry.vbo[0]);
            glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 3 * FLOAT_SIZE);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 4, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 6 * FLOAT_SIZE);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(3, 2, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 10 * FLOAT_SIZE);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(4, 2, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 12 * FLOAT_SIZE);
            glEnableVertexAttribArray(4);
            glVertexAttribPointer(5, 4, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 14 * FLOAT_SIZE);
            glEnableVertexAttribArray(5);
            glVertexAttribPointer(6, 4, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 18 * FLOAT_SIZE);
            glEnableVertexAttribArray(6);
            ++frameDrawSequence;
            glDrawArrays(GL_TRIANGLES, 0, geometry.vertexCount);
            glDisableVertexAttribArray(6);
            glDisableVertexAttribArray(5);
            glDisableVertexAttribArray(4);
            glDisableVertexAttribArray(3);
            glDisableVertexAttribArray(2);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);
        }

        private boolean isOpaqueSkinnedBatchItem(Graphics3D owner, RenderItem item) {
            if (item.skinnedMesh == null || !owner.isDepthBufferEnabled()) {
                return false;
            }
            CompositingMode compositingMode = item.appearance != null ? item.appearance.getCompositingMode() : null;
            return compositingMode == null
                    || (compositingMode.getBlending() == CompositingMode.REPLACE
                    && compositingMode.isDepthTestEnabled()
                    && compositingMode.isDepthWriteEnabled());
        }

        private boolean sameSkinGeometryGroup(RenderItem a, RenderItem b) {
            return a.skinnedMesh.getGpuGeometrySharingKey() == b.skinnedMesh.getGpuGeometrySharingKey()
                    && a.vertices == b.vertices
                    && a.triangles == b.triangles
                    && a.appearance == b.appearance;
        }

        private boolean renderSkinnedBatch(Graphics3D owner, Vector<RenderItem> items, int start, int end) {
            RenderItem first = items.elementAt(start);
            SkinnedMesh.GpuSkinningData firstData = first.skinnedMesh.getGpuSkinningData(MAX_GPU_INFLUENCES);
            if (firstData == null || firstData.boneCount > MAX_GPU_BONES || firstData.boneCount == 0) {
                return false;
            }
            int instanceCount = end - start;
            int floatsPerInstance = 17;
            int paletteFloatsPerInstance = firstData.boneCount * 12;
            ensureSkinBatchCapacity(instanceCount * floatsPerInstance, instanceCount * paletteFloatsPerInstance);

            for (int instance = 0; instance < instanceCount; instance++) {
                RenderItem item = items.elementAt(start + instance);
                SkinnedMesh.GpuSkinningData skinningData = instance == 0
                        ? firstData : item.skinnedMesh.getGpuSkinningData(MAX_GPU_INFLUENCES);
                if (skinningData == null || skinningData.boneCount != firstData.boneCount
                        || skinningData.boneIndices.length != firstData.boneIndices.length
                        || skinningData.boneWeights.length != firstData.boneWeights.length) {
                    return false;
                }

                float[] model = item.transform != null ? item.transform.getMatrix() : identityMatrix;
                int instanceOffset = instance * floatsPerInstance;
                for (int column = 0; column < 4; column++) {
                    for (int row = 0; row < 4; row++) {
                        skinInstanceData[instanceOffset + column * 4 + row] = model[row * 4 + column];
                    }
                }
                skinInstanceData[instanceOffset + 16] = instance * firstData.boneCount * 3;

                int paletteOffset = instance * paletteFloatsPerInstance;
                packBoneRows(skinningData.boneMatrices, skinningData.boneCount, skinPaletteData, paletteOffset);
            }

            SkinGeometry geometry = getOrCreateSkinnedGeometry(owner, first, firstData);
            if (geometry.vertexCount == 0) {
                return true;
            }
            SkinDrawData drawData = prepareSkinDrawData(owner, first, geometry);

            applyAppearance(owner, first.appearance, first);
            glUseProgram(skinProgram);
            glUniform1i(skinInstancedLocation, 1);
            glUniformMatrix4fv(skinCameraMatrixLocation, 1, GL_TRUE, cameraMatrix, 0);
            glUniformMatrix4fv(skinViewMatrixLocation, 1, GL_TRUE, viewMatrix, 0);
            glUniform1i(skinBonePaletteSamplerLocation, 2);
            applySkinCommonState(owner, first, drawData);

            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_BUFFER, skinPaletteTexture[0]);
            glBindBuffer(GL_TEXTURE_BUFFER, skinPaletteBuffer[0]);
            glBufferData(GL_TEXTURE_BUFFER, (long) instanceCount * paletteFloatsPerInstance * FLOAT_SIZE,
                    skinPaletteData, 0, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_TEXTURE_BUFFER, 0);

            glBindVertexArray(skinInstancedVao[0]);
            glBindBuffer(GL_ARRAY_BUFFER, geometry.vbo[0]);
            bindSkinVertexAttributes();
            glBindBuffer(GL_ARRAY_BUFFER, skinInstanceVbo[0]);
            glBufferData(GL_ARRAY_BUFFER, (long) instanceCount * floatsPerInstance * FLOAT_SIZE,
                    skinInstanceData, 0, GL_DYNAMIC_DRAW);
            int instanceStride = floatsPerInstance * FLOAT_SIZE;
            for (int column = 0; column < 4; column++) {
                int attribute = 7 + column;
                glVertexAttribPointer(attribute, 4, GL_FLOAT, GL_FALSE, instanceStride, null,
                        column * 4 * FLOAT_SIZE);
                glEnableVertexAttribArray(attribute);
                glVertexAttribDivisor(attribute, 1);
            }
            glVertexAttribPointer(11, 1, GL_FLOAT, GL_FALSE, instanceStride, null, 16 * FLOAT_SIZE);
            glEnableVertexAttribArray(11);
            glVertexAttribDivisor(11, 1);

            frameDrawSequence += instanceCount;
            glDrawArraysInstanced(GL_TRIANGLES, 0, geometry.vertexCount, instanceCount);
            glBindVertexArray(0);
            return true;
        }

        private void applySkinCommonState(Graphics3D owner, RenderItem item, SkinDrawData drawData) {
            glUniform1f(skinAlphaThresholdLocation, resolveAlphaThreshold(item.appearance));
            glUniform1f(skinDepthRangeScaleLocation, owner.getDepthRangeFar() - owner.getDepthRangeNear());
            glUniform1f(skinDepthRangeBiasLocation, owner.getDepthRangeFar() + owner.getDepthRangeNear() - 1f);
            applySkinFog(item.appearance);
            applySkinTextures(item.appearance, drawData.texturedUnits);
            applySkinMaterial(owner, item, drawData);
            applySkinLights(owner);
        }

        private void bindSkinVertexAttributes() {
            glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 3 * FLOAT_SIZE);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 4, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 6 * FLOAT_SIZE);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(3, 2, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 10 * FLOAT_SIZE);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(4, 2, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 12 * FLOAT_SIZE);
            glEnableVertexAttribArray(4);
            glVertexAttribPointer(5, 4, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 14 * FLOAT_SIZE);
            glEnableVertexAttribArray(5);
            glVertexAttribPointer(6, 4, GL_FLOAT, GL_FALSE, SKIN_STRIDE_BYTES, null, 18 * FLOAT_SIZE);
            glEnableVertexAttribArray(6);
        }

        private void ensureSkinBatchCapacity(int instanceFloats, int paletteFloats) {
            if (skinInstanceData.length < instanceFloats) {
                skinInstanceData = new float[Math.max(instanceFloats, Math.max(272, skinInstanceData.length * 2))];
            }
            if (skinPaletteData.length < paletteFloats) {
                skinPaletteData = new float[Math.max(paletteFloats, Math.max(384, skinPaletteData.length * 2))];
            }
        }

        private void packBoneRows(float[] boneMatrices, int boneCount, float[] destination, int destinationOffset) {
            for (int bone = 0; bone < boneCount; bone++) {
                int src = bone * 16;
                int dst = destinationOffset + bone * 12;
                System.arraycopy(boneMatrices, src, destination, dst, 4);
                System.arraycopy(boneMatrices, src + 4, destination, dst + 4, 4);
                System.arraycopy(boneMatrices, src + 8, destination, dst + 8, 4);
            }
        }

        private SkinGeometry getOrCreateSkinnedGeometry(Graphics3D owner, RenderItem item,
                                                         SkinnedMesh.GpuSkinningData skinningData) {
            SkinGeometryKey key = new SkinGeometryKey(item);
            SkinGeometry geometry = (SkinGeometry) skinGeometryCache.get(key);
            boolean newEntry = geometry == null;
            if (newEntry) {
                geometry = new SkinGeometry();
                skinGeometryCache.put(key, geometry);
            }
            if (newEntry || isSkinnedGeometryStale(geometry, item)) {
                SkinTriangleData packed = buildSkinnedTriangleData(owner, item, skinningData);
                if (geometry.vbo[0] == 0) {
                    glGenBuffers(1, geometry.vbo, 0);
                }
                glBindBuffer(GL_ARRAY_BUFFER, geometry.vbo[0]);
                glBufferData(GL_ARRAY_BUFFER, (long) packed.vertices.length * FLOAT_SIZE,
                        packed.vertices, 0, GL_STATIC_DRAW);
                geometry.vertexCount = packed.vertexCount;
                geometry.hasVertexColor = packed.hasVertexColor;
                geometry.hasNormals = item.vertices.getNormals() != null;
                updateSkinGeometrySourceState(geometry, item, packed.texturedUnits);
            }
            return geometry;
        }

        private boolean isSkinnedGeometryStale(SkinGeometry geometry, RenderItem item) {
            VertexBuffer vertices = item.vertices;
            if (geometry.vertexBufferRevision != vertices.getRevision()) {
                return true;
            }
            VertexArray positions = vertices.getPositions(null);
            if (geometry.positions != positions || geometry.positionsRevision != getArrayRevision(positions)) {
                return true;
            }
            VertexArray normals = vertices.getNormals();
            if (geometry.normals != normals || geometry.normalsRevision != getArrayRevision(normals)) {
                return true;
            }
            VertexArray colors = vertices.getColors();
            if (geometry.colors != colors || geometry.colorsRevision != getArrayRevision(colors)) {
                return true;
            }
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                VertexArray texCoords = vertices.getTexCoords(unit, null);
                if (geometry.texCoords[unit] != texCoords
                        || geometry.texCoordRevisions[unit] != getArrayRevision(texCoords)) {
                    return true;
                }
                Texture2D texture = item.appearance != null ? item.appearance.getTexture(unit) : null;
                boolean textured = texture != null && texCoords != null && texture.getImage() != null;
                if (geometry.texturedUnits[unit] != textured) {
                    return true;
                }
                if (textured) {
                    texture.getCompositeTransform(textureTransformScratch);
                    float[] current = textureTransformScratch.getMatrix();
                    float[] cached = geometry.textureMatrices[unit];
                    for (int component = 0; component < 16; component++) {
                        if (current[component] != cached[component]) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void updateSkinGeometrySourceState(SkinGeometry geometry, RenderItem item,
                                                   boolean[] texturedUnits) {
            VertexBuffer vertices = item.vertices;
            geometry.vertexBufferRevision = vertices.getRevision();
            geometry.positions = vertices.getPositions(null);
            geometry.positionsRevision = getArrayRevision(geometry.positions);
            geometry.normals = vertices.getNormals();
            geometry.normalsRevision = getArrayRevision(geometry.normals);
            geometry.colors = vertices.getColors();
            geometry.colorsRevision = getArrayRevision(geometry.colors);
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                VertexArray texCoords = vertices.getTexCoords(unit, null);
                geometry.texCoords[unit] = texCoords;
                geometry.texCoordRevisions[unit] = getArrayRevision(texCoords);
                geometry.texturedUnits[unit] = texturedUnits[unit];
                if (texturedUnits[unit]) {
                    Texture2D texture = item.appearance != null ? item.appearance.getTexture(unit) : null;
                    texture.getCompositeTransform(textureTransformScratch);
                    System.arraycopy(textureTransformScratch.getMatrix(), 0,
                            geometry.textureMatrices[unit], 0, 16);
                } else {
                    for (int component = 0; component < 16; component++) {
                        geometry.textureMatrices[unit][component] = (component % 5) == 0 ? 1f : 0f;
                    }
                }
            }
        }

        private SkinDrawData prepareSkinDrawData(Graphics3D owner, RenderItem item, SkinGeometry geometry) {
            SkinDrawData drawData = skinDrawDataScratch;
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                VertexArray texCoords = item.vertices.getTexCoords(unit, null);
                Texture2D texture = item.appearance != null ? item.appearance.getTexture(unit) : null;
                drawData.texturedUnits[unit] = texture != null && texCoords != null && texture.getImage() != null;
            }
            drawData.baseColor = resolveBaseColor(item.vertices, item.appearance);
            drawData.hasVertexColor = geometry.hasVertexColor;
            Material material = item.appearance != null ? item.appearance.getMaterial() : null;
            drawData.lightingEnabled = material != null && geometry.hasNormals && owner.getLightCount() > 0;
            return drawData;
        }

        private SkinTriangleData buildSkinnedTriangleData(Graphics3D owner, RenderItem item, SkinnedMesh.GpuSkinningData skinningData) {
            VertexBuffer vertices = item.vertices;
            VertexArray positionArray = vertices.getPositions(null);
            if (positionArray == null) {
                throw new IllegalStateException();
            }
            VertexArray normalArray = vertices.getNormals();
            VertexArray colorArray = vertices.getColors();
            VertexArray[] texCoordArrays = new VertexArray[TEXTURE_UNIT_COUNT];
            float[][] texCoordScaleBias = new float[TEXTURE_UNIT_COUNT][];
            Transform[] textureTransforms = new Transform[TEXTURE_UNIT_COUNT];
            float[] positionScaleBias = vertices.getPositionScaleBias();
            boolean[] texturedUnits = new boolean[TEXTURE_UNIT_COUNT];
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                texCoordArrays[unit] = vertices.getTexCoords(unit, null);
                texCoordScaleBias[unit] = texCoordArrays[unit] != null ? vertices.getTexCoordScaleBias(unit) : null;
                Texture2D texture = item.appearance != null ? item.appearance.getTexture(unit) : null;
                texturedUnits[unit] = texture != null && texCoordArrays[unit] != null && texture.getImage() != null;
                textureTransforms[unit] = texturedUnits[unit] ? copyTextureTransform(texture) : null;
            }
            Material material = item.appearance != null ? item.appearance.getMaterial() : null;
            boolean lightingEnabled = material != null && normalArray != null && owner.getLightCount() > 0;
            int triangleCount = countTriangles(item.triangles.getStripLengths());
            float[] data = new float[triangleCount * 3 * SKIN_COMPONENTS_PER_VERTEX];
            int cursor = 0;
            int[] rawIndices = item.triangles.getRawIndices();
            int[] stripLengths = item.triangles.getStripLengths();
            int base = 0;
            for (int strip = 0; strip < stripLengths.length; strip++) {
                int stripLength = stripLengths[strip];
                for (int i = 0; i < stripLength - 2; i++) {
                    int i0 = rawIndices[base + i];
                    int i1 = rawIndices[base + i + 1];
                    int i2 = rawIndices[base + i + 2];
                    if ((i & 1) != 0) {
                        int swap = i1;
                        i1 = i2;
                        i2 = swap;
                    }
                    cursor = appendSkinnedVertex(data, cursor, vertices, positionArray, positionScaleBias, normalArray, colorArray,
                            texCoordArrays, texCoordScaleBias, textureTransforms, skinningData, i0);
                    cursor = appendSkinnedVertex(data, cursor, vertices, positionArray, positionScaleBias, normalArray, colorArray,
                            texCoordArrays, texCoordScaleBias, textureTransforms, skinningData, i1);
                    cursor = appendSkinnedVertex(data, cursor, vertices, positionArray, positionScaleBias, normalArray, colorArray,
                            texCoordArrays, texCoordScaleBias, textureTransforms, skinningData, i2);
                }
                base += stripLength;
            }
            return new SkinTriangleData(data, triangleCount * 3, texturedUnits, resolveBaseColor(vertices, item.appearance),
                    colorArray != null, lightingEnabled);
        }

        private int appendSkinnedVertex(float[] data, int cursor, VertexBuffer vertices, VertexArray positions, float[] positionScaleBias,
                                        VertexArray normals, VertexArray colors, VertexArray[] texCoords, float[][] texCoordScaleBias,
                                        Transform[] textureTransforms, SkinnedMesh.GpuSkinningData skinningData, int index) {
            data[cursor++] = positions.getComponentAsFloat(index, 0) * positionScaleBias[0] + positionScaleBias[1];
            data[cursor++] = positions.getComponentAsFloat(index, 1) * positionScaleBias[0] + positionScaleBias[2];
            data[cursor++] = positions.getComponentAsFloat(index, 2) * positionScaleBias[0] + positionScaleBias[3];
            if (normals != null) {
                data[cursor++] = getNormalizedComponent(normals, index, 0);
                data[cursor++] = getNormalizedComponent(normals, index, 1);
                data[cursor++] = getNormalizedComponent(normals, index, 2);
            } else {
                data[cursor++] = 0f;
                data[cursor++] = 0f;
                data[cursor++] = 1f;
            }
            int trackedColor = colors != null ? getVertexColor(colors, index) : vertices.getDefaultColor();
            data[cursor++] = ((trackedColor >>> 16) & 0xFF) / 255f;
            data[cursor++] = ((trackedColor >>> 8) & 0xFF) / 255f;
            data[cursor++] = (trackedColor & 0xFF) / 255f;
            data[cursor++] = ((trackedColor >>> 24) & 0xFF) / 255f;
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                float u = 0f;
                float v = 0f;
                if (texCoords[unit] != null) {
                    u = texCoords[unit].getComponentAsFloat(index, 0) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][1];
                    v = texCoords[unit].getComponentAsFloat(index, 1) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][2];
                    if (textureTransforms[unit] != null) {
                        textureVector[0] = u;
                        textureVector[1] = v;
                        textureVector[2] = texCoords[unit].getComponentCount() > 2
                                ? texCoords[unit].getComponentAsFloat(index, 2) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][3]
                                : 0f;
                        textureVector[3] = 1f;
                        textureTransforms[unit].transform(textureVector);
                        u = textureVector[0];
                        v = textureVector[1];
                    }
                }
                data[cursor++] = u;
                data[cursor++] = v;
            }
            int influenceBase = index * skinningData.packedInfluenceCount;
            for (int i = 0; i < skinningData.packedInfluenceCount; i++) {
                data[cursor++] = skinningData.boneIndices[influenceBase + i];
            }
            for (int i = 0; i < skinningData.packedInfluenceCount; i++) {
                data[cursor++] = skinningData.boneWeights[influenceBase + i];
            }
            return cursor;
        }

        private void applyAppearance(Graphics3D owner, Appearance appearance, RenderItem item) {
            PolygonMode polygonMode = appearance != null ? appearance.getPolygonMode() : null;
            CompositingMode compositingMode = appearance != null ? appearance.getCompositingMode() : null;
            boolean depthBufferEnabled = owner.isDepthBufferEnabled();

            if (polygonMode == null || polygonMode.getCulling() == PolygonMode.CULL_NONE) {
                glDisable(GL_CULL_FACE);
            } else {
                glEnable(GL_CULL_FACE);
                glCullFace(polygonMode.getCulling() == PolygonMode.CULL_FRONT ? GL_FRONT : GL_BACK);
                glFrontFace(polygonMode.getWinding() == PolygonMode.WINDING_CW ? org.mini.gl.GL.GL_CW : GL_CCW);
            }

            if (compositingMode == null) {
                if (depthBufferEnabled) {
                    glEnable(GL_DEPTH_TEST);
                    glDepthMask(GL_TRUE);
                    glDepthFunc(GL_LEQUAL);
                } else {
                    glDisable(GL_DEPTH_TEST);
                    glDepthMask(GL_FALSE);
                }
                // 默认 CompositingMode(null) 是 REPLACE 语义：参考实现
                // m3gApplyDefaultCompositingMode 里 glDisable(GL_BLEND)。
                // 这里原先误开了 ALPHA 混合(glEnable(GL_BLEND) + SRC_ALPHA)，
                // 导致带纹理且 texel alpha<1.0 的网格(如 truckracer 卡车)被混合成
                // 半透明/透明而看不见——场景 mesh 因显式 setBlending(REPLACE) 关了
                // blend 反而正常。改为关闭 blend，对齐参考实现。
                glDisable(GL_BLEND);
                glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
                glDisable(GL_POLYGON_OFFSET_FILL);
                return;
            }

            if (depthBufferEnabled && compositingMode.isDepthTestEnabled()) {
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LEQUAL);
            } else {
                glDisable(GL_DEPTH_TEST);
            }
            glDepthMask(depthBufferEnabled && compositingMode.isDepthWriteEnabled() ? GL_TRUE : GL_FALSE);
            glColorMask(compositingMode.isColorWriteEnabled() ? GL_TRUE : GL_FALSE,
                    compositingMode.isColorWriteEnabled() ? GL_TRUE : GL_FALSE,
                    compositingMode.isColorWriteEnabled() ? GL_TRUE : GL_FALSE,
                    compositingMode.isAlphaWriteEnabled() ? GL_TRUE : GL_FALSE);
            switch (compositingMode.getBlending()) {
                case CompositingMode.ALPHA:
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    break;
                case CompositingMode.ALPHA_ADD:
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                    break;
                case CompositingMode.MODULATE:
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_DST_COLOR, GL_ZERO);
                    break;
                case CompositingMode.MODULATE_X2:
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_DST_COLOR, GL_SRC_COLOR);
                    break;
                case CompositingMode.REPLACE:
                default:
                    glDisable(GL_BLEND);
                    break;
            }

            if (compositingMode.getDepthOffsetUnits() != 0f || compositingMode.getDepthOffsetFactor() != 0f) {
                glEnable(GL_POLYGON_OFFSET_FILL);
                glPolygonOffset(compositingMode.getDepthOffsetFactor(), compositingMode.getDepthOffsetUnits());
            } else {
                glDisable(GL_POLYGON_OFFSET_FILL);
            }
        }

        private float resolveAlphaThreshold(Appearance appearance) {
            CompositingMode compositingMode = appearance != null ? appearance.getCompositingMode() : null;
            return compositingMode != null ? compositingMode.getAlphaThreshold() : 0f;
        }

        private void applyTextures(Appearance appearance, boolean[] texturedUnits) {
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                Texture2D texture2D = appearance != null ? appearance.getTexture(unit) : null;
                if (!texturedUnits[unit] || texture2D == null || texture2D.getImage() == null) {
                    glUniform1i(useTextureLocations[unit], 0);
                    glUniform1i(textureModeLocations[unit], TEXTURE_MODE_MODULATE);
                    glUniform4f(textureBlendColorLocations[unit], 0f, 0f, 0f, 1f);
                    glUniformMatrix4fv(textureMatrixLocations[unit], 1, GL_TRUE, identityMatrix, 0);
                    glActiveTexture(textureConstant(unit));
                    glBindTexture(GL_TEXTURE_2D, 0);
                    continue;
                }

                Image2D image = texture2D.getImage();
                int texId = configureTexture(unit, texture2D, image,
                        texture2D.getWrappingS() == Texture2D.WRAP_REPEAT ? GL_REPEAT : GL_CLAMP_TO_EDGE,
                        texture2D.getWrappingT() == Texture2D.WRAP_REPEAT ? GL_REPEAT : GL_CLAMP_TO_EDGE,
                        texture2D.getImageFilter() == Texture2D.FILTER_LINEAR ? GL_LINEAR : GL_NEAREST);
                glUniform1i(useTextureLocations[unit], 1);
                glUniform1i(textureModeLocations[unit], mapTextureMode(texture2D.getBlending()));
                glUniform1i(textureSamplerLocations[unit], unit);
                int blendColor = texture2D.getBlendColor();
                glUniform4f(textureBlendColorLocations[unit],
                        ((blendColor >>> 16) & 0xFF) / 255f,
                        ((blendColor >>> 8) & 0xFF) / 255f,
                        (blendColor & 0xFF) / 255f,
                        1f);
                texture2D.getCompositeTransform(textureTransformScratch);
                glUniformMatrix4fv(textureMatrixLocations[unit], 1, GL_TRUE, textureTransformScratch.getMatrix(), 0);

                glActiveTexture(textureConstant(unit));
                glBindTexture(GL_TEXTURE_2D, texId);
            }
        }

        private void applyFog(Appearance appearance) {
            Fog fog = appearance != null ? appearance.getFog() : null;
            if (fog == null) {
                glUniform1i(fogModeLocation, 0);
                glUniform4f(fogColorLocation, 0f, 0f, 0f, 1f);
                glUniform1f(fogNearLocation, 0f);
                glUniform1f(fogFarLocation, 1f);
                glUniform1f(fogDensityLocation, 0f);
                return;
            }

            int color = fog.getColor();
            glUniform1i(fogModeLocation, fog.getMode());
            glUniform4f(fogColorLocation,
                    ((color >>> 16) & 0xFF) / 255f,
                    ((color >>> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    1f);
            glUniform1f(fogNearLocation, fog.getNearDistance());
            glUniform1f(fogFarLocation, fog.getFarDistance());
            glUniform1f(fogDensityLocation, fog.getDensity());
        }

        private void applyMaterial(Graphics3D owner, RenderItem item, MeshGeometry geometry) {
            Material material = item.appearance != null ? item.appearance.getMaterial() : null;
            int baseColor = resolveBaseColor(item.vertices, item.appearance);
            boolean lightingEnabled = geometry.hasNormals && material != null && owner.getLightCount() > 0;
            glUniform1i(hasVertexColorLocation, geometry.hasVertexColor ? 1 : 0);
            setColorUniform(baseColorLocation, baseColor);
            if (!lightingEnabled) {
                glUniform1i(lightingEnabledLocation, 0);
                glUniform1i(vertexColorTrackingLocation, 0);
                setColorUniform(materialAmbientLocation, 0x00000000);
                setColorUniform(materialDiffuseLocation, baseColor);
                setColorUniform(materialEmissiveLocation, 0x00000000);
                setColorUniform(materialSpecularLocation, 0x00000000);
                glUniform1f(materialShininessLocation, 0f);
                return;
            }
            glUniform1i(lightingEnabledLocation, 1);
            glUniform1i(vertexColorTrackingLocation, material.isVertexColorTrackingEnabled() ? 1 : 0);
            setColorUniform(materialAmbientLocation, material.getColor(Material.AMBIENT));
            setColorUniform(materialDiffuseLocation, material.getColor(Material.DIFFUSE));
            setColorUniform(materialEmissiveLocation, material.getColor(Material.EMISSIVE));
            setColorUniform(materialSpecularLocation, material.getColor(Material.SPECULAR));
            glUniform1f(materialShininessLocation, material.getShininess());
        }

        private void applyLights(Graphics3D owner) {
            Transform cameraTransform = new Transform();
            owner.getCamera(cameraTransform);
            float[] cameraWorld = cameraTransform.getMatrix();
            glUniform4f(cameraWorldPositionLocation, cameraWorld[3], cameraWorld[7], cameraWorld[11], 1f);

            for (int i = 0; i < skinLightModes.length; i++) {
                skinLightModes[i] = 0;
                int floatBase = i * 4;
                skinLightColors[floatBase] = 0f;
                skinLightColors[floatBase + 1] = 0f;
                skinLightColors[floatBase + 2] = 0f;
                skinLightColors[floatBase + 3] = 0f;
                skinLightPositions[floatBase] = 0f;
                skinLightPositions[floatBase + 1] = 0f;
                skinLightPositions[floatBase + 2] = 0f;
                skinLightPositions[floatBase + 3] = 1f;
                skinLightDirections[floatBase] = 0f;
                skinLightDirections[floatBase + 1] = 0f;
                skinLightDirections[floatBase + 2] = -1f;
                skinLightDirections[floatBase + 3] = 0f;
                skinLightAttenuations[floatBase] = 1f;
                skinLightAttenuations[floatBase + 1] = 0f;
                skinLightAttenuations[floatBase + 2] = 0f;
                skinLightAttenuations[floatBase + 3] = 0f;
                skinLightSpots[floatBase] = -1f;
                skinLightSpots[floatBase + 1] = 0f;
                skinLightSpots[floatBase + 2] = 0f;
                skinLightSpots[floatBase + 3] = 0f;
            }

            Transform lightTransform = new Transform();
            float[] lightMatrix = new float[16];
            int lightCount = Math.min(owner.getLightCount(), MAX_GPU_LIGHTS);
            for (int i = 0; i < lightCount; i++) {
                Light light = owner.getLight(i, lightTransform);
                lightTransform.get(lightMatrix);
                int base = i * 4;
                float intensity = light.getIntensity();
                skinLightModes[i] = light.getMode();
                skinLightColors[base] = (((light.getColor() >>> 16) & 0xFF) / 255f) * intensity;
                skinLightColors[base + 1] = (((light.getColor() >>> 8) & 0xFF) / 255f) * intensity;
                skinLightColors[base + 2] = ((light.getColor() & 0xFF) / 255f) * intensity;
                skinLightColors[base + 3] = 1f;
                skinLightPositions[base] = lightMatrix[3];
                skinLightPositions[base + 1] = lightMatrix[7];
                skinLightPositions[base + 2] = lightMatrix[11];
                skinLightPositions[base + 3] = 1f;
                skinLightDirections[base] = -lightMatrix[2];
                skinLightDirections[base + 1] = -lightMatrix[6];
                skinLightDirections[base + 2] = -lightMatrix[10];
                skinLightDirections[base + 3] = 0f;
                skinLightAttenuations[base] = light.getConstantAttenuation();
                skinLightAttenuations[base + 1] = light.getLinearAttenuation();
                skinLightAttenuations[base + 2] = light.getQuadraticAttenuation();
                skinLightAttenuations[base + 3] = 0f;
                float spotAngle = light.getSpotAngle();
                skinLightSpots[base] = (spotAngle >= 0f && spotAngle < 180f)
                        ? (float) Math.cos(Math.toRadians(spotAngle * 0.5f))
                        : -1f;
                skinLightSpots[base + 1] = light.getSpotExponent();
                skinLightSpots[base + 2] = 0f;
                skinLightSpots[base + 3] = 0f;
            }
            glUniform1i(lightCountLocation, lightCount);
            glUniform1iv(lightModesLocation, MAX_GPU_LIGHTS, skinLightModes, 0);
            glUniform4fv(lightColorsLocation, MAX_GPU_LIGHTS, skinLightColors, 0);
            glUniform4fv(lightPositionsLocation, MAX_GPU_LIGHTS, skinLightPositions, 0);
            glUniform4fv(lightDirectionsLocation, MAX_GPU_LIGHTS, skinLightDirections, 0);
            glUniform4fv(lightAttenuationsLocation, MAX_GPU_LIGHTS, skinLightAttenuations, 0);
            glUniform4fv(lightSpotsLocation, MAX_GPU_LIGHTS, skinLightSpots, 0);
        }

        private MeshGeometry getOrCreateMeshGeometry(RenderItem item) {
            GeometryKey key = new GeometryKey(item);
            GeometryCacheEntry entry = (GeometryCacheEntry) geometryCache.get(key);
            MeshGeometry geometry = entry != null ? entry.geometry : null;
            boolean newKey = false;
            if (geometry == null) {
                newKey = true;
                entry = new GeometryCacheEntry();
                geometry = entry.geometry;
                geometryCache.put(key, entry);
            }
            String staleReason = newKey ? "new-key" : getGeometryStaleReason(geometry, item);
            if (staleReason != null) {
                MeshGeometryData data = buildMeshGeometryData(item.vertices, item.triangles);
                if (geometry.vbo[0] == 0) {
                    glGenBuffers(1, geometry.vbo, 0);
                }
                glBindBuffer(GL_ARRAY_BUFFER, geometry.vbo[0]);
                glBufferData(GL_ARRAY_BUFFER, data.vertices.length * FLOAT_SIZE, data.vertices, 0, GL_STATIC_DRAW);
                geometry.vertexCount = data.vertexCount;
                geometry.hasVertexColor = data.hasVertexColor;
                geometry.hasNormals = data.hasNormals;
                geometry.texCoordUnits = data.texCoordUnits;
                geometry.vertexBufferRevision = item.vertices.getRevision();
                geometry.positions = item.vertices.getPositions(null);
                geometry.positionsRevision = getArrayRevision(geometry.positions);
                geometry.normals = item.vertices.getNormals();
                geometry.normalsRevision = getArrayRevision(geometry.normals);
                geometry.colors = item.vertices.getColors();
                geometry.colorsRevision = getArrayRevision(geometry.colors);
                for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                    geometry.texCoords[unit] = item.vertices.getTexCoords(unit, null);
                    geometry.texCoordRevisions[unit] = getArrayRevision(geometry.texCoords[unit]);
                }
                geometry.morphStateHash = item.mesh instanceof MorphingMesh ? computeMorphStateHash((MorphingMesh) item.mesh) : Integer.MIN_VALUE;
            }
            return geometry;
        }

        private String getGeometryStaleReason(MeshGeometry geometry, RenderItem item) {
            if (item.mesh instanceof MorphingMesh) {
                return geometry.morphStateHash == computeMorphStateHash((MorphingMesh) item.mesh) ? null : "morph-state";
            }
            VertexBuffer vertices = item.vertices;
            if (geometry.vertexBufferRevision != vertices.getRevision()) {
                return "vertex-buffer-revision";
            }
            VertexArray positions = vertices.getPositions(null);
            if (geometry.positions != positions || geometry.positionsRevision != getArrayRevision(positions)) {
                return "positions";
            }
            VertexArray normals = vertices.getNormals();
            if (geometry.normals != normals || geometry.normalsRevision != getArrayRevision(normals)) {
                return "normals";
            }
            VertexArray colors = vertices.getColors();
            if (geometry.colors != colors || geometry.colorsRevision != getArrayRevision(colors)) {
                return "colors";
            }
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                VertexArray texCoords = vertices.getTexCoords(unit, null);
                if (geometry.texCoords[unit] != texCoords || geometry.texCoordRevisions[unit] != getArrayRevision(texCoords)) {
                    return "texCoords" + unit;
                }
            }
            return null;
        }

        private int getArrayRevision(VertexArray array) {
            return array != null ? array.getRevision() : -1;
        }

        private int computeMorphStateHash(MorphingMesh mesh) {
            int hash = computeVertexBufferStateHash(mesh.getVertexBuffer());
            float[] weights = new float[mesh.getMorphTargetCount()];
            mesh.getWeights(weights);
            for (int i = 0; i < weights.length; i++) {
                hash = hash * 31 + Float.floatToIntBits(weights[i]);
                hash = hash * 31 + computeVertexBufferStateHash(mesh.getMorphTarget(i));
            }
            return hash;
        }

        private int computeVertexBufferStateHash(VertexBuffer buffer) {
            int hash = System.identityHashCode(buffer);
            hash = hash * 31 + buffer.getRevision();
            hash = hash * 31 + buffer.getDefaultColor();
            hash = hash * 31 + computeVertexArrayStateHash(buffer.getPositions(null));
            hash = hash * 31 + computeVertexArrayStateHash(buffer.getNormals());
            hash = hash * 31 + computeVertexArrayStateHash(buffer.getColors());
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                hash = hash * 31 + computeVertexArrayStateHash(buffer.getTexCoords(unit, null));
            }
            return hash;
        }

        private int computeVertexArrayStateHash(VertexArray array) {
            if (array == null) {
                return 0;
            }
            return System.identityHashCode(array) * 31 + array.getRevision();
        }

        private MeshGeometryData buildMeshGeometryData(VertexBuffer vertices, TriangleStripArray triangles) {
            VertexArray positionArray = vertices.getPositions(null);
            if (positionArray == null) {
                throw new IllegalStateException();
            }
            VertexArray normalArray = vertices.getNormals();
            VertexArray colorArray = vertices.getColors();
            VertexArray[] texCoordArrays = new VertexArray[TEXTURE_UNIT_COUNT];
            float[][] texCoordScaleBias = new float[TEXTURE_UNIT_COUNT][];
            boolean[] texCoordUnits = new boolean[TEXTURE_UNIT_COUNT];
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                texCoordArrays[unit] = vertices.getTexCoords(unit, null);
                texCoordScaleBias[unit] = texCoordArrays[unit] != null ? vertices.getTexCoordScaleBias(unit) : null;
                texCoordUnits[unit] = texCoordArrays[unit] != null;
            }
            float[] positionScaleBias = vertices.getPositionScaleBias();
            int triangleCount = countTriangles(triangles.getStripLengths());
            float[] data = new float[triangleCount * 3 * COMPONENTS_PER_VERTEX];
            int cursor = 0;
            int[] rawIndices = triangles.getRawIndices();
            int[] stripLengths = triangles.getStripLengths();
            int base = 0;
            for (int strip = 0; strip < stripLengths.length; strip++) {
                int stripLength = stripLengths[strip];
                for (int i = 0; i < stripLength - 2; i++) {
                    int i0 = rawIndices[base + i];
                    int i1 = rawIndices[base + i + 1];
                    int i2 = rawIndices[base + i + 2];
                    if ((i & 1) != 0) {
                        int swap = i1;
                        i1 = i2;
                        i2 = swap;
                    }
                    cursor = appendMeshVertex(data, cursor, vertices, positionArray, positionScaleBias, normalArray, colorArray,
                            texCoordArrays, texCoordScaleBias, i0);
                    cursor = appendMeshVertex(data, cursor, vertices, positionArray, positionScaleBias, normalArray, colorArray,
                            texCoordArrays, texCoordScaleBias, i1);
                    cursor = appendMeshVertex(data, cursor, vertices, positionArray, positionScaleBias, normalArray, colorArray,
                            texCoordArrays, texCoordScaleBias, i2);
                }
                base += stripLength;
            }
            return new MeshGeometryData(data, triangleCount * 3, texCoordUnits, colorArray != null, normalArray != null);
        }

        private int appendMeshVertex(float[] data, int cursor, VertexBuffer vertices, VertexArray positions, float[] positionScaleBias,
                                     VertexArray normals, VertexArray colors, VertexArray[] texCoords, float[][] texCoordScaleBias, int index) {
            data[cursor++] = positions.getComponentAsFloat(index, 0) * positionScaleBias[0] + positionScaleBias[1];
            data[cursor++] = positions.getComponentAsFloat(index, 1) * positionScaleBias[0] + positionScaleBias[2];
            data[cursor++] = positions.getComponentAsFloat(index, 2) * positionScaleBias[0] + positionScaleBias[3];
            if (normals != null) {
                data[cursor++] = getNormalizedComponent(normals, index, 0);
                data[cursor++] = getNormalizedComponent(normals, index, 1);
                data[cursor++] = getNormalizedComponent(normals, index, 2);
            } else {
                data[cursor++] = 0f;
                data[cursor++] = 0f;
                data[cursor++] = 1f;
            }
            int trackedColor = colors != null ? getVertexColor(colors, index) : vertices.getDefaultColor();
            data[cursor++] = ((trackedColor >>> 16) & 0xFF) / 255f;
            data[cursor++] = ((trackedColor >>> 8) & 0xFF) / 255f;
            data[cursor++] = (trackedColor & 0xFF) / 255f;
            data[cursor++] = ((trackedColor >>> 24) & 0xFF) / 255f;
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                float u = 0f;
                float v = 0f;
                if (texCoords[unit] != null) {
                    u = texCoords[unit].getComponentAsFloat(index, 0) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][1];
                    v = texCoords[unit].getComponentAsFloat(index, 1) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][2];
                }
                data[cursor++] = u;
                data[cursor++] = v;
            }
            return cursor;
        }

        private void applySkinTextures(Appearance appearance, boolean[] texturedUnits) {
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                Texture2D texture2D = appearance != null ? appearance.getTexture(unit) : null;
                if (!texturedUnits[unit] || texture2D == null || texture2D.getImage() == null) {
                    glUniform1i(skinUseTextureLocations[unit], 0);
                    glUniform1i(skinTextureModeLocations[unit], TEXTURE_MODE_MODULATE);
                    glUniform4f(skinTextureBlendColorLocations[unit], 0f, 0f, 0f, 1f);
                    glActiveTexture(textureConstant(unit));
                    glBindTexture(GL_TEXTURE_2D, 0);
                    continue;
                }

                Image2D image = texture2D.getImage();
                int texId = configureTexture(unit, texture2D, image,
                        texture2D.getWrappingS() == Texture2D.WRAP_REPEAT ? GL_REPEAT : GL_CLAMP_TO_EDGE,
                        texture2D.getWrappingT() == Texture2D.WRAP_REPEAT ? GL_REPEAT : GL_CLAMP_TO_EDGE,
                        texture2D.getImageFilter() == Texture2D.FILTER_LINEAR ? GL_LINEAR : GL_NEAREST);
                glUniform1i(skinUseTextureLocations[unit], 1);
                glUniform1i(skinTextureModeLocations[unit], mapTextureMode(texture2D.getBlending()));
                glUniform1i(skinTextureSamplerLocations[unit], unit);
                int blendColor = texture2D.getBlendColor();
                glUniform4f(skinTextureBlendColorLocations[unit],
                        ((blendColor >>> 16) & 0xFF) / 255f,
                        ((blendColor >>> 8) & 0xFF) / 255f,
                        (blendColor & 0xFF) / 255f,
                        1f);
                glActiveTexture(textureConstant(unit));
                glBindTexture(GL_TEXTURE_2D, texId);
            }
        }

        private void applySkinFog(Appearance appearance) {
            Fog fog = appearance != null ? appearance.getFog() : null;
            if (fog == null) {
                glUniform1i(skinFogModeLocation, 0);
                glUniform4f(skinFogColorLocation, 0f, 0f, 0f, 1f);
                glUniform1f(skinFogNearLocation, 0f);
                glUniform1f(skinFogFarLocation, 1f);
                glUniform1f(skinFogDensityLocation, 0f);
                return;
            }

            int color = fog.getColor();
            glUniform1i(skinFogModeLocation, fog.getMode());
            glUniform4f(skinFogColorLocation,
                    ((color >>> 16) & 0xFF) / 255f,
                    ((color >>> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    1f);
            glUniform1f(skinFogNearLocation, fog.getNearDistance());
            glUniform1f(skinFogFarLocation, fog.getFarDistance());
            glUniform1f(skinFogDensityLocation, fog.getDensity());
        }

        private void applySkinMaterial(Graphics3D owner, RenderItem item, SkinDrawData triangleData) {
            Material material = item.appearance != null ? item.appearance.getMaterial() : null;
            glUniform1i(skinHasVertexColorLocation, triangleData.hasVertexColor ? 1 : 0);
            setColorUniform(skinBaseColorLocation, triangleData.baseColor);
            if (!triangleData.lightingEnabled || material == null) {
                glUniform1i(skinLightingEnabledLocation, 0);
                glUniform1i(skinVertexColorTrackingLocation, 0);
                setColorUniform(skinMaterialAmbientLocation, 0x00000000);
                setColorUniform(skinMaterialDiffuseLocation, triangleData.baseColor);
                setColorUniform(skinMaterialEmissiveLocation, 0x00000000);
                setColorUniform(skinMaterialSpecularLocation, 0x00000000);
                glUniform1f(skinMaterialShininessLocation, 0f);
                return;
            }
            glUniform1i(skinLightingEnabledLocation, 1);
            glUniform1i(skinVertexColorTrackingLocation, material.isVertexColorTrackingEnabled() ? 1 : 0);
            setColorUniform(skinMaterialAmbientLocation, material.getColor(Material.AMBIENT));
            setColorUniform(skinMaterialDiffuseLocation, material.getColor(Material.DIFFUSE));
            setColorUniform(skinMaterialEmissiveLocation, material.getColor(Material.EMISSIVE));
            setColorUniform(skinMaterialSpecularLocation, material.getColor(Material.SPECULAR));
            glUniform1f(skinMaterialShininessLocation, material.getShininess());
        }

        private void applySkinLights(Graphics3D owner) {
            Transform cameraTransform = new Transform();
            owner.getCamera(cameraTransform);
            float[] cameraWorld = cameraTransform.getMatrix();
            glUniform4f(skinCameraWorldPositionLocation, cameraWorld[3], cameraWorld[7], cameraWorld[11], 1f);

            for (int i = 0; i < skinLightModes.length; i++) {
                skinLightModes[i] = 0;
                int floatBase = i * 4;
                skinLightColors[floatBase] = 0f;
                skinLightColors[floatBase + 1] = 0f;
                skinLightColors[floatBase + 2] = 0f;
                skinLightColors[floatBase + 3] = 0f;
                skinLightPositions[floatBase] = 0f;
                skinLightPositions[floatBase + 1] = 0f;
                skinLightPositions[floatBase + 2] = 0f;
                skinLightPositions[floatBase + 3] = 1f;
                skinLightDirections[floatBase] = 0f;
                skinLightDirections[floatBase + 1] = 0f;
                skinLightDirections[floatBase + 2] = -1f;
                skinLightDirections[floatBase + 3] = 0f;
                skinLightAttenuations[floatBase] = 1f;
                skinLightAttenuations[floatBase + 1] = 0f;
                skinLightAttenuations[floatBase + 2] = 0f;
                skinLightAttenuations[floatBase + 3] = 0f;
                skinLightSpots[floatBase] = -1f;
                skinLightSpots[floatBase + 1] = 0f;
                skinLightSpots[floatBase + 2] = 0f;
                skinLightSpots[floatBase + 3] = 0f;
            }

            Transform lightTransform = new Transform();
            float[] lightMatrix = new float[16];
            int lightCount = Math.min(owner.getLightCount(), MAX_GPU_LIGHTS);
            for (int i = 0; i < lightCount; i++) {
                Light light = owner.getLight(i, lightTransform);
                lightTransform.get(lightMatrix);
                int base = i * 4;
                float intensity = light.getIntensity();
                skinLightModes[i] = light.getMode();
                skinLightColors[base] = (((light.getColor() >>> 16) & 0xFF) / 255f) * intensity;
                skinLightColors[base + 1] = (((light.getColor() >>> 8) & 0xFF) / 255f) * intensity;
                skinLightColors[base + 2] = ((light.getColor() & 0xFF) / 255f) * intensity;
                skinLightColors[base + 3] = 1f;
                skinLightPositions[base] = lightMatrix[3];
                skinLightPositions[base + 1] = lightMatrix[7];
                skinLightPositions[base + 2] = lightMatrix[11];
                skinLightPositions[base + 3] = 1f;
                skinLightDirections[base] = -lightMatrix[2];
                skinLightDirections[base + 1] = -lightMatrix[6];
                skinLightDirections[base + 2] = -lightMatrix[10];
                skinLightDirections[base + 3] = 0f;
                skinLightAttenuations[base] = light.getConstantAttenuation();
                skinLightAttenuations[base + 1] = light.getLinearAttenuation();
                skinLightAttenuations[base + 2] = light.getQuadraticAttenuation();
                skinLightAttenuations[base + 3] = 0f;
                float spotAngle = light.getSpotAngle();
                skinLightSpots[base] = (spotAngle >= 0f && spotAngle < 180f)
                        ? (float) Math.cos(Math.toRadians(spotAngle * 0.5f))
                        : -1f;
                skinLightSpots[base + 1] = light.getSpotExponent();
                skinLightSpots[base + 2] = 0f;
                skinLightSpots[base + 3] = 0f;
            }
            glUniform1i(skinLightCountLocation, lightCount);
            glUniform1iv(skinLightModesLocation, MAX_GPU_LIGHTS, skinLightModes, 0);
            glUniform4fv(skinLightColorsLocation, MAX_GPU_LIGHTS, skinLightColors, 0);
            glUniform4fv(skinLightPositionsLocation, MAX_GPU_LIGHTS, skinLightPositions, 0);
            glUniform4fv(skinLightDirectionsLocation, MAX_GPU_LIGHTS, skinLightDirections, 0);
            glUniform4fv(skinLightAttenuationsLocation, MAX_GPU_LIGHTS, skinLightAttenuations, 0);
            glUniform4fv(skinLightSpotsLocation, MAX_GPU_LIGHTS, skinLightSpots, 0);
        }

        private void uploadBoneRows(float[] boneMatrices, int boneCount) {
            int rowCount = MAX_GPU_BONES * 3;
            for (int i = 0; i < skinBoneRows.length; i++) {
                skinBoneRows[i] = 0f;
            }
            for (int bone = 0; bone < boneCount; bone++) {
                int src = bone * 16;
                int dst = bone * 12;
                skinBoneRows[dst] = boneMatrices[src];
                skinBoneRows[dst + 1] = boneMatrices[src + 1];
                skinBoneRows[dst + 2] = boneMatrices[src + 2];
                skinBoneRows[dst + 3] = boneMatrices[src + 3];
                skinBoneRows[dst + 4] = boneMatrices[src + 4];
                skinBoneRows[dst + 5] = boneMatrices[src + 5];
                skinBoneRows[dst + 6] = boneMatrices[src + 6];
                skinBoneRows[dst + 7] = boneMatrices[src + 7];
                skinBoneRows[dst + 8] = boneMatrices[src + 8];
                skinBoneRows[dst + 9] = boneMatrices[src + 9];
                skinBoneRows[dst + 10] = boneMatrices[src + 10];
                skinBoneRows[dst + 11] = boneMatrices[src + 11];
            }
            glUniform4fv(skinBoneRowsLocation, rowCount, skinBoneRows, 0);
        }

        private void bindSkinPaletteTexture() {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_BUFFER, skinPaletteTexture[0]);
            glUniform1i(skinBonePaletteSamplerLocation, 2);
        }

        private void setColorUniform(int location, int color) {
            glUniform4f(location,
                    ((color >>> 16) & 0xFF) / 255f,
                    ((color >>> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    ((color >>> 24) & 0xFF) / 255f);
        }

        private int mapTextureMode(int blending) {
            switch (blending) {
                case Texture2D.FUNC_REPLACE:
                    return TEXTURE_MODE_REPLACE;
                case Texture2D.FUNC_ADD:
                    return TEXTURE_MODE_ADD;
                case Texture2D.FUNC_BLEND:
                    return TEXTURE_MODE_BLEND;
                case Texture2D.FUNC_DECAL:
                    return TEXTURE_MODE_DECAL;
                case Texture2D.FUNC_MODULATE:
                default:
                    return TEXTURE_MODE_MODULATE;
            }
        }

        private int configureTexture(int unit, Texture2D texture, Image2D image, int wrapS, int wrapT, int filter) {
            glActiveTexture(textureConstant(unit));
            Integer cached = (Integer) textureCache.get(image);
            int texId;
            if (cached == null || image.isMutable()) {
                GlUploadImage uploadImage = prepareImageForGl(image);
                int[] tex = new int[1];
                if (cached != null) {
                    tex[0] = cached.intValue();
                } else {
                    glGenTextures(1, tex, 0);
                }
                texId = tex[0];
                glBindTexture(GL_TEXTURE_2D, texId);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
                // RGB textures whose row size is not a multiple of 4 (for example 2x2 RGB = 6 bytes/row)
                // upload incorrectly with OpenGL's default UNPACK_ALIGNMENT=4, producing checkerboard-like corruption.
                org.mini.gl.GL.glPixelStorei(org.mini.gl.GL.GL_UNPACK_ALIGNMENT, 1);
                glTexImage2D(GL_TEXTURE_2D, 0, uploadImage.format == Image2D.RGB ? GL_RGB : GL_RGBA,
                        uploadImage.width, uploadImage.height, 0,
                        uploadImage.format == Image2D.RGB ? GL_RGB : GL_RGBA,
                        GL_UNSIGNED_BYTE, uploadImage.data, 0);
                textureCache.put(image, Integer.valueOf(texId));
            } else {
                texId = cached.intValue();
                glBindTexture(GL_TEXTURE_2D, texId);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
            }
            return texId;
        }

        private void renderBackgroundImage(Graphics3D owner, Background background) {
            BackgroundTextureSource texture = buildBackgroundTextureSource(background);
            if (texture == null) {
                return;
            }

            // buildBackgroundTextureSource has already expanded the Background crop
            // rectangle into a standalone texture, including BORDER/REPEAT handling
            // for any imaginary pixels outside the source image. Stretch that baked
            // crop across the viewport once; re-deriving UVs from the original image
            // would apply the crop a second time.
            float u0 = 0f;
            float u1 = 1f;
            float v0 = 0f;
            float v1 = 1f;

            // GL texture v=0 is the first uploaded row, which is M3G image row 0
            // (the top) in the baked background texture. The FBO is rendered with
            // clip-space +y up, then read back flipped so that FBO-top maps to
            // target-top. To make the target's top row show texture row 0, the top
            // quad vertex (y=+1) must sample v0 and the bottom vertex (y=-1) must
            // sample v1.
            fillBackgroundVertex(0, -1f, 1f, u0, v0);   // top-left
            fillBackgroundVertex(1, -1f, -1f, u0, v1);  // bottom-left
            fillBackgroundVertex(2, 1f, 1f, u1, v0);    // top-right
            fillBackgroundVertex(3, 1f, -1f, u1, v1);   // bottom-right

            renderBackgroundTexture(texture,
                    background.getImageModeX() == Background.REPEAT ? GL_REPEAT : GL_CLAMP_TO_EDGE,
                    background.getImageModeY() == Background.REPEAT ? GL_REPEAT : GL_CLAMP_TO_EDGE,
                    GL_LINEAR);
        }

        private boolean renderExistingTargetBackground(Graphics3D owner) {
            BackgroundTextureSource texture = buildTargetBackgroundTextureSource(owner.getTarget(), owner);
            if (texture == null) {
                return false;
            }
            // The target image is stored in top-down row order, while GL sampling expects
            // the first uploaded row at v=0. Use top->0, bottom->1 so the preserved 2D
            // target matches the on-screen orientation when drawn into the FBO.
            fillBackgroundVertex(0, -1f, 1f, 0f, 0f);
            fillBackgroundVertex(1, -1f, -1f, 0f, 1f);
            fillBackgroundVertex(2, 1f, 1f, 1f, 0f);
            fillBackgroundVertex(3, 1f, -1f, 1f, 1f);
            renderBackgroundTexture(texture, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST);
            return true;
        }

        private void renderBackgroundTexture(BackgroundTextureSource texture, int wrapS, int wrapT, int filter) {
            uploadBackgroundTexture(texture, wrapS, wrapT, filter);

            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glDepthMask(GL_FALSE);
            glDisable(GL_POLYGON_OFFSET_FILL);
            if (texture.hasAlpha) {
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            } else {
                glDisable(GL_BLEND);
            }

            glUseProgram(program);
            glUniformMatrix4fv(mvpLocation, 1, GL_TRUE, identityMatrix, 0);
            glUniformMatrix4fv(modelLocation, 1, GL_TRUE, identityMatrix, 0);
            glUniformMatrix4fv(modelViewLocation, 1, GL_TRUE, identityMatrix, 0);
            glUniform1f(alphaThresholdLocation, 0f);
            glUniform1f(depthRangeScaleLocation, 1f);
            glUniform1f(depthRangeBiasLocation, 0f);
            glUniform1i(fogModeLocation, 0);
            glUniform4f(fogColorLocation, 0f, 0f, 0f, 1f);
            glUniform1f(fogNearLocation, 0f);
            glUniform1f(fogFarLocation, 1f);
            glUniform1f(fogDensityLocation, 0f);
            glUniform1i(useTextureLocations[0], 1);
            glUniform1i(textureModeLocations[0], TEXTURE_MODE_REPLACE);
            glUniform1i(textureSamplerLocations[0], 0);
            glUniform4f(textureBlendColorLocations[0], 0f, 0f, 0f, 1f);
            glUniformMatrix4fv(textureMatrixLocations[0], 1, GL_TRUE, identityMatrix, 0);
            glUniform1i(useTextureLocations[1], 0);
            glUniform1i(textureModeLocations[1], TEXTURE_MODE_MODULATE);
            glUniform1i(textureSamplerLocations[1], 1);
            glUniform4f(textureBlendColorLocations[1], 0f, 0f, 0f, 1f);
            glUniformMatrix4fv(textureMatrixLocations[1], 1, GL_TRUE, identityMatrix, 0);
            glUniform1i(hasVertexColorLocation, 1);
            setColorUniform(baseColorLocation, 0xFFFFFFFF);
            glUniform1i(lightingEnabledLocation, 0);
            glUniform1i(vertexColorTrackingLocation, 0);
            setColorUniform(materialAmbientLocation, 0x00000000);
            setColorUniform(materialDiffuseLocation, 0xFFFFFFFF);
            setColorUniform(materialEmissiveLocation, 0x00000000);
            setColorUniform(materialSpecularLocation, 0x00000000);
            glUniform1f(materialShininessLocation, 0f);
            glUniform4f(cameraWorldPositionLocation, 0f, 0f, 1f, 1f);
            glUniform1i(lightCountLocation, 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, backgroundTexture[0]);

            glBindVertexArray(vao[0]);
            glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            glBufferData(GL_ARRAY_BUFFER, backgroundVertices.length * FLOAT_SIZE, backgroundVertices, 0, GL_DYNAMIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 3 * FLOAT_SIZE);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 4, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 6 * FLOAT_SIZE);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(3, 2, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 10 * FLOAT_SIZE);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(4, 2, GL_FLOAT, GL_FALSE, STRIDE_BYTES, null, 12 * FLOAT_SIZE);
            glEnableVertexAttribArray(4);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            glDisableVertexAttribArray(4);
            glDisableVertexAttribArray(3);
            glDisableVertexAttribArray(2);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);
        }

        private void fillBackgroundVertex(int index, float x, float y, float u, float v) {
            int base = index * COMPONENTS_PER_VERTEX;
            backgroundVertices[base] = x;
            backgroundVertices[base + 1] = y;
            backgroundVertices[base + 2] = 0f;
            backgroundVertices[base + 3] = 0f;
            backgroundVertices[base + 4] = 0f;
            backgroundVertices[base + 5] = 1f;
            backgroundVertices[base + 6] = 1f;
            backgroundVertices[base + 7] = 1f;
            backgroundVertices[base + 8] = 1f;
            backgroundVertices[base + 9] = 1f;
            backgroundVertices[base + 10] = u;
            backgroundVertices[base + 11] = v;
            backgroundVertices[base + 12] = 0f;
            backgroundVertices[base + 13] = 0f;
        }

        private boolean hasAnyTexture(Appearance appearance) {
            if (appearance == null) {
                return false;
            }
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                Texture2D texture = appearance.getTexture(unit);
                if (texture != null && texture.getImage() != null) {
                    return true;
                }
            }
            return false;
        }

        private TriangleData buildTriangleData(Graphics3D owner, RenderItem item) {
            VertexBuffer vertices = item.vertices;
            VertexArray positionArray = vertices.getPositions(null);
            if (positionArray == null) {
                throw new IllegalStateException();
            }

            VertexArray colorArray = vertices.getColors();
            VertexArray normalArray = vertices.getNormals();
            VertexArray[] texCoordArrays = new VertexArray[TEXTURE_UNIT_COUNT];
            float[][] texCoordScaleBias = new float[TEXTURE_UNIT_COUNT][];
            Transform[] textureTransforms = new Transform[TEXTURE_UNIT_COUNT];
            float[] positionScaleBias = vertices.getPositionScaleBias();
            int defaultColor = resolveBaseColor(vertices, item.appearance);
            PolygonMode polygonMode = item.appearance != null ? item.appearance.getPolygonMode() : null;
            boolean flatShading = polygonMode != null && polygonMode.getShading() == PolygonMode.SHADE_FLAT;
            boolean[] texturedUnits = new boolean[TEXTURE_UNIT_COUNT];
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                texCoordArrays[unit] = vertices.getTexCoords(unit, null);
                texCoordScaleBias[unit] = texCoordArrays[unit] != null ? vertices.getTexCoordScaleBias(unit) : null;
                Texture2D texture = item.appearance != null ? item.appearance.getTexture(unit) : null;
                texturedUnits[unit] = texture != null && texCoordArrays[unit] != null && texture.getImage() != null;
                textureTransforms[unit] = texturedUnits[unit] ? copyTextureTransform(texture) : null;
            }
            Material material = item.appearance != null ? item.appearance.getMaterial() : null;
            boolean applyLighting = material != null && normalArray != null && owner.getLightCount() > 0;
            float[] modelMatrix = applyLighting
                    ? (item.transform != null ? item.transform.getMatrix() : identityMatrix)
                    : null;
            Transform cameraTransform = applyLighting ? new Transform() : null;
            float[] cameraMatrix = null;
            if (applyLighting) {
                owner.getCamera(cameraTransform);
                cameraMatrix = cameraTransform.getMatrix();
            }
            float[] world = applyLighting ? new float[4] : null;
            float[] normalInput = applyLighting ? new float[4] : null;
            float[] normalWorld = applyLighting ? new float[4] : null;
            float[] lightMatrix = applyLighting ? new float[16] : null;
            Transform lightTransform = applyLighting ? new Transform() : null;

            int triangleCount = countTriangles(item.triangles.getStripLengths());
            float[] data = new float[triangleCount * 3 * COMPONENTS_PER_VERTEX];
            int cursor = 0;
            int[] rawIndices = item.triangles.getRawIndices();
            int[] stripLengths = item.triangles.getStripLengths();
            int base = 0;
            for (int strip = 0; strip < stripLengths.length; strip++) {
                int stripLength = stripLengths[strip];
                for (int i = 0; i < stripLength - 2; i++) {
                    int i0 = rawIndices[base + i];
                    int i1 = rawIndices[base + i + 1];
                    int i2 = rawIndices[base + i + 2];
                    if ((i & 1) != 0) {
                        int swap = i1;
                        i1 = i2;
                        i2 = swap;
                    }

                    int c0 = resolveVertexColor(owner, vertices, positionArray, positionScaleBias, colorArray, normalArray, material,
                            modelMatrix, cameraMatrix, world, normalInput, normalWorld, lightMatrix, lightTransform,
                            i0, defaultColor, applyLighting);
                    int c1 = resolveVertexColor(owner, vertices, positionArray, positionScaleBias, colorArray, normalArray, material,
                            modelMatrix, cameraMatrix, world, normalInput, normalWorld, lightMatrix, lightTransform,
                            i1, defaultColor, applyLighting);
                    int c2 = resolveVertexColor(owner, vertices, positionArray, positionScaleBias, colorArray, normalArray, material,
                            modelMatrix, cameraMatrix, world, normalInput, normalWorld, lightMatrix, lightTransform,
                            i2, defaultColor, applyLighting);
                    if (flatShading) {
                        c1 = c0;
                        c2 = c0;
                    }

                    cursor = appendVertex(data, cursor, positionArray, positionScaleBias, texCoordArrays, texCoordScaleBias, textureTransforms, i0, c0);
                    cursor = appendVertex(data, cursor, positionArray, positionScaleBias, texCoordArrays, texCoordScaleBias, textureTransforms, i1, c1);
                    cursor = appendVertex(data, cursor, positionArray, positionScaleBias, texCoordArrays, texCoordScaleBias, textureTransforms, i2, c2);
                }
                base += stripLength;
            }
            return new TriangleData(data, triangleCount * 3, texturedUnits);
        }

        private int resolveVertexColor(Graphics3D owner, VertexBuffer vertices, VertexArray positionArray, float[] positionScaleBias,
                                       VertexArray colorArray, VertexArray normalArray, Material material, float[] modelMatrix, float[] cameraMatrix,
                                       float[] world, float[] normalInput, float[] normalWorld, float[] lightMatrix, Transform lightTransform,
                                       int index, int defaultColor, boolean applyLighting) {
            int trackedColor = colorArray != null ? getVertexColor(colorArray, index) : vertices.getDefaultColor();
            int vertexColor = colorArray != null ? trackedColor : defaultColor;
            if (!applyLighting) {
                return vertexColor;
            }

            float[] position = new float[]{
                    positionArray.getComponentAsFloat(index, 0) * positionScaleBias[0] + positionScaleBias[1],
                    positionArray.getComponentAsFloat(index, 1) * positionScaleBias[0] + positionScaleBias[2],
                    positionArray.getComponentAsFloat(index, 2) * positionScaleBias[0] + positionScaleBias[3],
                    1f
            };
            M3GMath.transform(modelMatrix, position, 0, world, 0);
            normalInput[0] = normalArray.getComponentAsFloat(index, 0);
            normalInput[1] = normalArray.getComponentAsFloat(index, 1);
            normalInput[2] = normalArray.getComponentAsFloat(index, 2);
            normalInput[3] = 0f;
            M3GMath.transform(modelMatrix, normalInput, 0, normalWorld, 0);
            return applyLighting(owner, trackedColor, material, world, normalWorld, cameraMatrix, lightMatrix, lightTransform);
        }

        private int appendVertex(float[] data, int cursor, VertexArray positions, float[] positionScaleBias,
                                 VertexArray[] texCoords, float[][] texCoordScaleBias, Transform[] textureTransforms, int index, int color) {
            data[cursor++] = positions.getComponentAsFloat(index, 0) * positionScaleBias[0] + positionScaleBias[1];
            data[cursor++] = positions.getComponentAsFloat(index, 1) * positionScaleBias[0] + positionScaleBias[2];
            data[cursor++] = positions.getComponentAsFloat(index, 2) * positionScaleBias[0] + positionScaleBias[3];
            data[cursor++] = ((color >>> 16) & 0xFF) / 255f;
            data[cursor++] = ((color >>> 8) & 0xFF) / 255f;
            data[cursor++] = (color & 0xFF) / 255f;
            data[cursor++] = ((color >>> 24) & 0xFF) / 255f;
            for (int unit = 0; unit < TEXTURE_UNIT_COUNT; unit++) {
                float u = 0f;
                float v = 0f;
                if (texCoords[unit] != null) {
                    u = texCoords[unit].getComponentAsFloat(index, 0) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][1];
                    v = texCoords[unit].getComponentAsFloat(index, 1) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][2];
                    if (textureTransforms[unit] != null) {
                        textureVector[0] = u;
                        textureVector[1] = v;
                        textureVector[2] = texCoords[unit].getComponentCount() > 2 ? texCoords[unit].getComponentAsFloat(index, 2) * texCoordScaleBias[unit][0] + texCoordScaleBias[unit][3] : 0f;
                        textureVector[3] = 1f;
                        textureTransforms[unit].transform(textureVector);
                        u = textureVector[0];
                        v = textureVector[1];
                    }
                }
                data[cursor++] = u;
                data[cursor++] = v;
            }
            return cursor;
        }

        private int textureConstant(int unit) {
            return unit == 0 ? GL_TEXTURE0 : GL_TEXTURE1;
        }

        private Transform copyTextureTransform(Texture2D texture) {
            Transform transform = new Transform();
            texture.getCompositeTransform(transform);
            float[] matrix = transform.getMatrix();
            if (isIdentity(matrix)) {
                return null;
            }
            return transform;
        }

        private boolean isIdentity(float[] matrix) {
            return matrix[0] == 1f && matrix[1] == 0f && matrix[2] == 0f && matrix[3] == 0f
                    && matrix[4] == 0f && matrix[5] == 1f && matrix[6] == 0f && matrix[7] == 0f
                    && matrix[8] == 0f && matrix[9] == 0f && matrix[10] == 1f && matrix[11] == 0f
                    && matrix[12] == 0f && matrix[13] == 0f && matrix[14] == 0f && matrix[15] == 1f;
        }

        private int countTriangles(int[] stripLengths) {
            int total = 0;
            for (int i = 0; i < stripLengths.length; i++) {
                if (stripLengths[i] >= 3) {
                    total += stripLengths[i] - 2;
                }
            }
            return total;
        }

        private int resolveBaseColor(VertexBuffer vertices, Appearance appearance) {
            if (appearance != null && appearance.getMaterial() != null) {
                Material material = appearance.getMaterial();
                if (material.isVertexColorTrackingEnabled()) {
                    return vertices.getDefaultColor();
                }
                int materialColor = material.getColor(Material.DIFFUSE);
                if (materialColor != 0) {
                    return materialColor;
                }
            }
            return vertices.getDefaultColor();
        }

        private int getVertexColor(VertexArray colors, int index) {
            int r = ((int) colors.getComponentAsFloat(index, 0)) & 0xFF;
            int g = ((int) colors.getComponentAsFloat(index, 1)) & 0xFF;
            int b = ((int) colors.getComponentAsFloat(index, 2)) & 0xFF;
            int a = colors.getComponentCount() > 3 ? (((int) colors.getComponentAsFloat(index, 3)) & 0xFF) : 0xFF;
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private float getNormalizedComponent(VertexArray values, int index, int component) {
            float raw = values.getComponentAsFloat(index, component);
            if (values.getComponentType() == 1) {
                return raw / 127.0f;
            }
            return raw / 32767.0f;
        }

        private int applyLighting(Graphics3D owner, int trackedColor, Material material, float[] worldPosition,
                                  float[] worldNormal, float[] cameraMatrix, float[] lightMatrix, Transform lightTransform) {
            float nx = worldNormal[0];
            float ny = worldNormal[1];
            float nz = worldNormal[2];
            float normalLength = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (normalLength <= 1.0e-6f) {
                return trackedColor;
            }
            nx /= normalLength;
            ny /= normalLength;
            nz /= normalLength;

            float ambientR = 0f;
            float ambientG = 0f;
            float ambientB = 0f;
            float diffuseR = 0f;
            float diffuseG = 0f;
            float diffuseB = 0f;
            float specularR = 0f;
            float specularG = 0f;
            float specularB = 0f;
            float vx = cameraMatrix[3] - worldPosition[0];
            float vy = cameraMatrix[7] - worldPosition[1];
            float vz = cameraMatrix[11] - worldPosition[2];
            float viewLength = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (viewLength > 1.0e-6f) {
                vx /= viewLength;
                vy /= viewLength;
                vz /= viewLength;
            } else {
                vx = 0f;
                vy = 0f;
                vz = 1f;
            }

            for (int i = 0; i < owner.getLightCount(); i++) {
                Light light = owner.getLight(i, lightTransform);
                lightTransform.get(lightMatrix);
                float intensity = light.getIntensity();
                float lightR = (((light.getColor() >>> 16) & 0xFF) / 255f) * intensity;
                float lightG = (((light.getColor() >>> 8) & 0xFF) / 255f) * intensity;
                float lightB = ((light.getColor() & 0xFF) / 255f) * intensity;
                if (light.getMode() == Light.AMBIENT) {
                    ambientR += lightR;
                    ambientG += lightG;
                    ambientB += lightB;
                    continue;
                }

                float lx;
                float ly;
                float lz;
                float attenuation = 1f;
                float spotFactor = 1f;
                if (light.getMode() == Light.OMNI || light.getMode() == Light.SPOT) {
                    lx = lightMatrix[3] - worldPosition[0];
                    ly = lightMatrix[7] - worldPosition[1];
                    lz = lightMatrix[11] - worldPosition[2];
                    float distance = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
                    if (distance <= 1.0e-6f) {
                        continue;
                    }
                    lx /= distance;
                    ly /= distance;
                    lz /= distance;
                    float denominator = light.getConstantAttenuation()
                            + light.getLinearAttenuation() * distance
                            + light.getQuadraticAttenuation() * distance * distance;
                    if (denominator > 1.0e-6f) {
                        attenuation = 1f / denominator;
                    }
                    if (light.getMode() == Light.SPOT) {
                        float spotDirX = -lightMatrix[2];
                        float spotDirY = -lightMatrix[6];
                        float spotDirZ = -lightMatrix[10];
                        float spotLength = (float) Math.sqrt(spotDirX * spotDirX + spotDirY * spotDirY + spotDirZ * spotDirZ);
                        if (spotLength <= 1.0e-6f) {
                            continue;
                        }
                        spotDirX /= spotLength;
                        spotDirY /= spotLength;
                        spotDirZ /= spotLength;
                        float cosTheta = -(lx * spotDirX + ly * spotDirY + lz * spotDirZ);
                        float spotAngle = light.getSpotAngle();
                        if (spotAngle >= 0f && spotAngle < 180f) {
                            float cutoff = (float) Math.cos(Math.toRadians(spotAngle * 0.5f));
                            if (cosTheta < cutoff) {
                                continue;
                            }
                        }
                        spotFactor = (float) Math.pow(Math.max(0f, cosTheta), Math.max(0f, light.getSpotExponent()));
                    }
                } else {
                    lx = -lightMatrix[2];
                    ly = -lightMatrix[6];
                    lz = -lightMatrix[10];
                    float length = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
                    if (length <= 1.0e-6f) {
                        continue;
                    }
                    lx /= length;
                    ly /= length;
                    lz /= length;
                }

                float ndotl = nx * lx + ny * ly + nz * lz;
                if (ndotl > 0f) {
                    float lightingScale = attenuation * spotFactor;
                    diffuseR += lightR * ndotl * lightingScale;
                    diffuseG += lightG * ndotl * lightingScale;
                    diffuseB += lightB * ndotl * lightingScale;
                    int specularColor = material.getColor(Material.SPECULAR);
                    float shininess = material.getShininess();
                    if ((specularColor & 0x00FFFFFF) != 0 && shininess > 0f) {
                        float hx = lx + vx;
                        float hy = ly + vy;
                        float hz = lz + vz;
                        float halfLength = (float) Math.sqrt(hx * hx + hy * hy + hz * hz);
                        if (halfLength > 1.0e-6f) {
                            hx /= halfLength;
                            hy /= halfLength;
                            hz /= halfLength;
                            float ndoth = Math.max(0f, nx * hx + ny * hy + nz * hz);
                            if (ndoth > 0f) {
                                float specularFactor = (float) Math.pow(ndoth, Math.max(1f, shininess)) * lightingScale;
                                specularR += lightR * specularFactor;
                                specularG += lightG * specularFactor;
                                specularB += lightB * specularFactor;
                            }
                        }
                    }
                }
            }

            int ambientColor = material.isVertexColorTrackingEnabled()
                    ? (trackedColor & 0x00FFFFFF)
                    : material.getColor(Material.AMBIENT);
            int diffuseColor = material.isVertexColorTrackingEnabled()
                    ? trackedColor
                    : material.getColor(Material.DIFFUSE);
            int emissiveColor = material.getColor(Material.EMISSIVE);
            int specularColor = material.getColor(Material.SPECULAR);
            float diffuseRBase = ((diffuseColor >>> 16) & 0xFF) / 255f;
            float diffuseGBase = ((diffuseColor >>> 8) & 0xFF) / 255f;
            float diffuseBBase = (diffuseColor & 0xFF) / 255f;
            float specularRBase = ((specularColor >>> 16) & 0xFF) / 255f;
            float specularGBase = ((specularColor >>> 8) & 0xFF) / 255f;
            float specularBBase = (specularColor & 0xFF) / 255f;
            float outR = (((emissiveColor >>> 16) & 0xFF) / 255f)
                    + ((((ambientColor >>> 16) & 0xFF) / 255f) * ambientR)
                    + (diffuseRBase * diffuseR)
                    + (specularRBase * specularR);
            float outG = (((emissiveColor >>> 8) & 0xFF) / 255f)
                    + ((((ambientColor >>> 8) & 0xFF) / 255f) * ambientG)
                    + (diffuseGBase * diffuseG)
                    + (specularGBase * specularG);
            float outB = ((emissiveColor & 0xFF) / 255f)
                    + (((ambientColor & 0xFF) / 255f) * ambientB)
                    + (diffuseBBase * diffuseB)
                    + (specularBBase * specularB);
            int alpha = (diffuseColor >>> 24) & 0xFF;
            return (alpha << 24)
                    | (clampColor(Math.round(outR * 255f)) << 16)
                    | (clampColor(Math.round(outG * 255f)) << 8)
                    | clampColor(Math.round(outB * 255f));
        }

        private int clampColor(int value) {
            if (value < 0) {
                return 0;
            }
            return value > 255 ? 255 : value;
        }

        private void readBack(Graphics3D owner, RenderTargetSurface target) {
            int width = owner.getViewportWidth();
            int height = owner.getViewportHeight();
            int viewX = owner.getViewportX();
            int viewY = owner.getViewportY();
            int byteCount = width * height * READBACK_BYTES_PER_PIXEL;
            if (target instanceof BufferedImageTargetSurface) {
                BufferedImageTargetSurface bufferedTarget = (BufferedImageTargetSurface) target;
                if (supportsNativeBufferedImageReadback()) {
                    nativeReadBackBuffer = ensureReadBackCapacity(nativeReadBackBuffer, byteCount);
                    glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, nativeReadBackBuffer, 0);
                    bufferedTarget.writeNativeRowsFlipped(nativeReadBackBuffer, width, height, viewX, viewY);
                } else {
                    rgbaReadBackBuffer = ensureReadBackCapacity(rgbaReadBackBuffer, byteCount);
                    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, rgbaReadBackBuffer, 0);
                    bufferedTarget.writeRgbaRowsFlipped(rgbaReadBackBuffer, width, height, viewX, viewY);
                }
            } else if (target instanceof Image2DTargetSurface) {
                rgbaReadBackBuffer = ensureReadBackCapacity(rgbaReadBackBuffer, byteCount);
                glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, rgbaReadBackBuffer, 0);
                ((Image2DTargetSurface) target).writeRgbaRowsFlipped(rgbaReadBackBuffer, width, height, viewX, viewY);
            } else {
                rgbaReadBackBuffer = ensureReadBackCapacity(rgbaReadBackBuffer, byteCount);
                glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, rgbaReadBackBuffer, 0);
                for (int srcY = 0; srcY < height; srcY++) {
                    int dstY = viewY + (height - 1 - srcY);
                    int srcRow = srcY * width * READBACK_BYTES_PER_PIXEL;
                    for (int x = 0; x < width; x++) {
                        int src = srcRow + x * READBACK_BYTES_PER_PIXEL;
                        int argb = ((rgbaReadBackBuffer[src + 3] & 0xFF) << 24)
                                | ((rgbaReadBackBuffer[src] & 0xFF) << 16)
                                | ((rgbaReadBackBuffer[src + 1] & 0xFF) << 8)
                                | (rgbaReadBackBuffer[src + 2] & 0xFF);
                        target.setPixel(viewX + x, dstY, argb);
                    }
                }
            }
        }

        private byte[] ensureReadBackCapacity(byte[] buffer, int requiredLength) {
            if (buffer.length < requiredLength) {
                return new byte[requiredLength];
            }
            return buffer;
        }

        private boolean supportsNativeBufferedImageReadback() {
            if (nativeBufferedImageReadbackSupported == null) {
                nativeBufferedImageReadbackSupported = Boolean.FALSE;
            }
            return nativeBufferedImageReadbackSupported.booleanValue();
        }

        private int loadProgram(String vertexShaderSource, String fragmentShaderSource) {
            int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
            int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
            int shaderProgram = glCreateProgram();
            int[] status = new int[]{0};
            glAttachShader(shaderProgram, vertexShader);
            glAttachShader(shaderProgram, fragmentShader);
            glLinkProgram(shaderProgram);
            glGetProgramiv(shaderProgram, GL_LINK_STATUS, status, 0);
            if (status[0] == GL_FALSE) {
                glGetProgramiv(shaderProgram, GL_INFO_LOG_LENGTH, status, 0);
                byte[] log = new byte[status[0] + 1];
                glGetProgramInfoLog(shaderProgram, log.length, status, 0, log);
                throw new RuntimeException(new String(log, 0, status[0]));
            }
            glDetachShader(shaderProgram, vertexShader);
            glDetachShader(shaderProgram, fragmentShader);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            return shaderProgram;
        }

        private int compileShader(int shaderType, String shaderSource) {
            int shader = glCreateShader(shaderType);
            int[] status = new int[]{0};
            glShaderSource(shader, 1, new byte[][]{toCstyleBytes(shaderSource)}, null, 0);
            glCompileShader(shader);
            glGetShaderiv(shader, org.mini.gl.GL.GL_COMPILE_STATUS, status, 0);
            if (status[0] == GL_FALSE) {
                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, status, 0);
                byte[] log = new byte[status[0] + 1];
                glGetShaderInfoLog(shader, log.length, status, 0, log);
                throw new RuntimeException(new String(log, 0, status[0]));
            }
            return shader;
        }

        private String adaptShaderVersion(String shader) {
            String version = new String(glGetString(GL_VERSION));
            if (version.toLowerCase().indexOf("opengl es") >= 0) {
                return shader.replace("version 330", "version 300 es");
            }
            return shader;
        }

        private String readShaderResource(String resourcePath) {
            InputStream input = null;
            BufferedReader reader = null;
            try {
                input = MiniJvmGraphics3DFactory.class.getResourceAsStream(resourcePath);
                if (input == null) {
                    input = GCallBack.getInstance().getResourceAsStream(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
                }
                if (input == null) {
                    throw new IllegalStateException("Missing shader resource: " + resourcePath);
                }
                reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }
                return buffer.toString();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    } else if (input != null) {
                        input.close();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        private RenderTargetSurface resolveTargetSurface(Object target, boolean preserveContents) {
            if (target instanceof Image2D) {
                Image2D image = (Image2D) target;
                if (image.getFormat() == Image2D.RGB || image.getFormat() == Image2D.RGBA) {
                    return new Image2DTargetSurface(image);
                }
                return null;
            }
            BufferedImage image = resolveTargetImage(target);
            return image != null ? new BufferedImageTargetSurface(image, true, preserveContents) : null;
        }

        private BackgroundTextureSource buildTargetBackgroundTextureSource(Object target, Graphics3D owner) {
            BufferedImage image = resolveTargetImage(target);
            if (image == null) {
                return null;
            }
            int width = owner.getViewportWidth();
            int height = owner.getViewportHeight();
            int srcX = owner.getViewportX();
            int srcY = owner.getViewportY();
            if (width <= 0 || height <= 0 || srcX < 0 || srcY < 0
                    || srcX + width > image.getWidth() || srcY + height > image.getHeight()) {
                return null;
            }

            byte[] source = image.getImage().getData().array();
            int sourceRowBytes = image.getWidth() * READBACK_BYTES_PER_PIXEL;
            int rowBytes = width * READBACK_BYTES_PER_PIXEL;
            if (!isOpenGlesContext()) {
                byte[] rgba = new byte[height * rowBytes];
                for (int y = 0; y < height; y++) {
                    int srcOffset = ((srcY + y) * image.getWidth() + srcX) * READBACK_BYTES_PER_PIXEL;
                    int dstOffset = y * rowBytes;
                    System.arraycopy(source, srcOffset, rgba, dstOffset, rowBytes);
                }
                return new BackgroundTextureSource(width, height, Image2D.RGBA, rgba, GL_RGBA, GL_RGBA, false);
            }

            byte[] rgb = new byte[width * height * 3];
            int dst = 0;
            for (int y = 0; y < height; y++) {
                int srcOffset = ((srcY + y) * image.getWidth() + srcX) * READBACK_BYTES_PER_PIXEL;
                int srcEnd = srcOffset + rowBytes;
                while (srcOffset < srcEnd) {
                    rgb[dst++] = source[srcOffset];
                    rgb[dst++] = source[srcOffset + 1];
                    rgb[dst++] = source[srcOffset + 2];
                    srcOffset += READBACK_BYTES_PER_PIXEL;
                }
            }
            return new BackgroundTextureSource(width, height, Image2D.RGB, rgb, GL_RGB, GL_RGB, false);
        }

        private BufferedImage resolveTargetImage(Object target) {
            if (target instanceof PlatformGraphics) {
                return ((PlatformGraphics) target).getCanvas();
            }
            if (target instanceof javax.microedition.lcdui.Graphics) {
                javax.microedition.lcdui.Graphics graphics = (javax.microedition.lcdui.Graphics) target;
                if (graphics.platformGraphics != null) {
                    return graphics.platformGraphics.getCanvas();
                }
            }
            return null;
        }

        private BackgroundTextureSource buildBackgroundTextureSource(Background background) {
            Image2D image = background.getImage();
            if (image == null) {
                return null;
            }
            int cropX = 0;
            int cropY = 0;
            int cropWidth = image.getWidth();
            int cropHeight = image.getHeight();
            if (background.getCropWidth() > 0 && background.getCropHeight() > 0) {
                cropX = background.getCropX();
                cropY = background.getCropY();
                cropWidth = background.getCropWidth();
                cropHeight = background.getCropHeight();
            }
            if (cropWidth <= 0 || cropHeight <= 0) {
                return null;
            }

            boolean repeatX = background.getImageModeX() == Background.REPEAT;
            boolean repeatY = background.getImageModeY() == Background.REPEAT;
            boolean needsAlpha = needsAlphaInGlFormat(image.getFormat())
                    || (!repeatX && (cropX < 0 || cropX + cropWidth > image.getWidth()))
                    || (!repeatY && (cropY < 0 || cropY + cropHeight > image.getHeight()));
            int format = needsAlpha ? Image2D.RGBA : Image2D.RGB;
            int bytesPerPixel = format == Image2D.RGBA ? 4 : 3;
            byte[] data = new byte[cropWidth * cropHeight * bytesPerPixel];
            byte[] source = image.getImageData();

            for (int y = 0; y < cropHeight; y++) {
                int sampleY = resolveBackgroundSampleCoordinate(cropY + y, image.getHeight(), repeatY);
                for (int x = 0; x < cropWidth; x++) {
                    int sampleX = resolveBackgroundSampleCoordinate(cropX + x, image.getWidth(), repeatX);
                    int dst = (y * cropWidth + x) * bytesPerPixel;
                    if (sampleX < 0 || sampleY < 0) {
                        if (format == Image2D.RGBA) {
                            data[dst + 3] = 0;
                        }
                        continue;
                    }

                    int argb = readImageArgb(image, source, sampleX, sampleY);
                    data[dst] = (byte) ((argb >>> 16) & 0xFF);
                    data[dst + 1] = (byte) ((argb >>> 8) & 0xFF);
                    data[dst + 2] = (byte) (argb & 0xFF);
                    if (format == Image2D.RGBA) {
                        data[dst + 3] = (byte) ((argb >>> 24) & 0xFF);
                    }
                }
            }
            return new BackgroundTextureSource(cropWidth, cropHeight, format, data,
                    format == Image2D.RGB ? GL_RGB : GL_RGBA,
                    format == Image2D.RGB ? GL_RGB : GL_RGBA,
                    format == Image2D.RGBA);
        }

        private int resolveBackgroundSampleCoordinate(int coordinate, int size, boolean repeat) {
            if (repeat) {
                int wrapped = coordinate % size;
                return wrapped < 0 ? wrapped + size : wrapped;
            }
            return coordinate < 0 || coordinate >= size ? -1 : coordinate;
        }

        private void uploadBackgroundTexture(BackgroundTextureSource texture, int wrapS, int wrapT, int filter) {
            glActiveTexture(GL_TEXTURE0);
            if (backgroundTexture[0] == 0) {
                glGenTextures(1, backgroundTexture, 0);
            }
            glBindTexture(GL_TEXTURE_2D, backgroundTexture[0]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
            org.mini.gl.GL.glPixelStorei(org.mini.gl.GL.GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, texture.internalGlFormat,
                    texture.width, texture.height, 0,
                    texture.pixelGlFormat,
                    GL_UNSIGNED_BYTE, texture.data, 0);
        }

        private boolean isOpenGlesContext() {
            if (openGlesContext == null) {
                String version = new String(glGetString(GL_VERSION));
                openGlesContext = Boolean.valueOf(version.toLowerCase().indexOf("opengl es") >= 0);
            }
            return openGlesContext.booleanValue();
        }

        @SuppressWarnings("unused")
        private void deleteResources() {
            java.util.Enumeration e = textureCache.elements();
            while (e.hasMoreElements()) {
                int texId = ((Integer) e.nextElement()).intValue();
                glDeleteTextures(1, new int[]{texId}, 0);
            }
            textureCache.clear();
            if (backgroundTexture[0] != 0) {
                glDeleteTextures(1, backgroundTexture, 0);
                backgroundTexture[0] = 0;
            }
        }
    }

    private interface RenderTargetSurface {
        void setPixel(int x, int y, int argb);
    }

    private static final class BufferedImageTargetSurface implements RenderTargetSurface {
        private final BufferedImage image;
        private final ImageMutable mutableImage;
        private final byte[] data;
        private final int width;
        private final int height;
        private final boolean opaque;
        private final boolean preserveContents;

        BufferedImageTargetSurface(BufferedImage image, boolean opaque, boolean preserveContents) {
            this.image = image;
            this.mutableImage = image.getImage();
            this.data = mutableImage.getData().array();
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.opaque = opaque;
            this.preserveContents = preserveContents;
        }

        private boolean isInBounds(int x, int y) {
            return x >= 0 && y >= 0 && x < width && y < height;
        }

        public void setPixel(int x, int y, int argb) {
            if (!isInBounds(x, y)) {
                return;
            }
            image.setRGB(x, y, opaque ? (argb | 0xFF000000) : argb);
        }

        void writeRgbaRowsFlipped(byte[] rgba, int srcWidth, int srcHeight, int dstX, int dstY) {
            int rowBytes = srcWidth * READBACK_BYTES_PER_PIXEL;
            int clippedDstX = Math.max(0, dstX);
            int clippedSrcX = clippedDstX - dstX;
            int clippedWidth = Math.min(srcWidth - clippedSrcX, width - clippedDstX);
            if (clippedWidth <= 0) {
                return;
            }
            int clippedRowBytes = clippedWidth * READBACK_BYTES_PER_PIXEL;
            synchronized (mutableImage) {
                for (int srcY = 0; srcY < srcHeight; srcY++) {
                    int dstRow = dstY + (srcHeight - 1 - srcY);
                    if (dstRow < 0 || dstRow >= height) {
                        continue;
                    }
                    int srcOffset = srcY * rowBytes + clippedSrcX * READBACK_BYTES_PER_PIXEL;
                    int dstOffset = ((dstRow * width) + clippedDstX) * READBACK_BYTES_PER_PIXEL;
                    if (preserveContents) {
                        blendRgbaRow(rgba, srcOffset, dstOffset, clippedWidth);
                    } else {
                        System.arraycopy(rgba, srcOffset, data, dstOffset, clippedRowBytes);
                        if (opaque) {
                            for (int alphaOffset = dstOffset + 3, alphaEnd = dstOffset + clippedRowBytes; alphaOffset < alphaEnd; alphaOffset += READBACK_BYTES_PER_PIXEL) {
                                data[alphaOffset] = (byte) 0xFF;
                            }
                        }
                    }
                }
            }
        }

        void writeNativeRowsFlipped(byte[] nativePixels, int srcWidth, int srcHeight, int dstX, int dstY) {
            int rowBytes = srcWidth * READBACK_BYTES_PER_PIXEL;
            int clippedDstX = Math.max(0, dstX);
            int clippedSrcX = clippedDstX - dstX;
            int clippedWidth = Math.min(srcWidth - clippedSrcX, width - clippedDstX);
            if (clippedWidth <= 0) {
                return;
            }
            synchronized (mutableImage) {
                for (int srcY = 0; srcY < srcHeight; srcY++) {
                    int dstRow = dstY + (srcHeight - 1 - srcY);
                    if (dstRow < 0 || dstRow >= height) {
                        continue;
                    }
                    int srcOffset = srcY * rowBytes + clippedSrcX * READBACK_BYTES_PER_PIXEL;
                    int dstOffset = ((dstRow * width) + clippedDstX) * READBACK_BYTES_PER_PIXEL;
                    if (preserveContents) {
                        blendNativeRow(nativePixels, srcOffset, dstOffset, clippedWidth);
                    } else {
                        for (int x = 0; x < clippedWidth; x++) {
                            data[dstOffset] = nativePixels[srcOffset + 2];
                            data[dstOffset + 1] = nativePixels[srcOffset + 1];
                            data[dstOffset + 2] = nativePixels[srcOffset];
                            data[dstOffset + 3] = opaque ? (byte) 0xFF : nativePixels[srcOffset + 3];
                            srcOffset += READBACK_BYTES_PER_PIXEL;
                            dstOffset += READBACK_BYTES_PER_PIXEL;
                        }
                    }
                }
            }
        }

        private void blendRgbaRow(byte[] rgba, int srcOffset, int dstOffset, int pixelCount) {
            for (int x = 0; x < pixelCount; x++) {
                blendPixel(rgba[srcOffset] & 0xFF,
                        rgba[srcOffset + 1] & 0xFF,
                        rgba[srcOffset + 2] & 0xFF,
                        rgba[srcOffset + 3] & 0xFF,
                        dstOffset);
                srcOffset += READBACK_BYTES_PER_PIXEL;
                dstOffset += READBACK_BYTES_PER_PIXEL;
            }
        }

        private void blendNativeRow(byte[] nativePixels, int srcOffset, int dstOffset, int pixelCount) {
            for (int x = 0; x < pixelCount; x++) {
                blendPixel(nativePixels[srcOffset + 2] & 0xFF,
                        nativePixels[srcOffset + 1] & 0xFF,
                        nativePixels[srcOffset] & 0xFF,
                        nativePixels[srcOffset + 3] & 0xFF,
                        dstOffset);
                srcOffset += READBACK_BYTES_PER_PIXEL;
                dstOffset += READBACK_BYTES_PER_PIXEL;
            }
        }

        private void blendPixel(int srcR, int srcG, int srcB, int srcA, int dstOffset) {
            if (srcA <= 0) {
                if (opaque) {
                    data[dstOffset + 3] = (byte) 0xFF;
                }
                return;
            }
            if (srcA >= 255) {
                data[dstOffset] = (byte) srcR;
                data[dstOffset + 1] = (byte) srcG;
                data[dstOffset + 2] = (byte) srcB;
                data[dstOffset + 3] = (byte) (opaque ? 0xFF : srcA);
                return;
            }
            int dstR = data[dstOffset] & 0xFF;
            int dstG = data[dstOffset + 1] & 0xFF;
            int dstB = data[dstOffset + 2] & 0xFF;
            int invA = 255 - srcA;
            data[dstOffset] = (byte) ((srcR * srcA + dstR * invA + 127) / 255);
            data[dstOffset + 1] = (byte) ((srcG * srcA + dstG * invA + 127) / 255);
            data[dstOffset + 2] = (byte) ((srcB * srcA + dstB * invA + 127) / 255);
            data[dstOffset + 3] = (byte) 0xFF;
        }
    }

    private static final class Image2DTargetSurface implements RenderTargetSurface {
        private final Image2D image;
        private final byte[] data;
        private final int width;
        private final int height;
        private final int pixelBytes;

        Image2DTargetSurface(Image2D image) {
            this.image = image;
            this.data = image.getImageData();
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.pixelBytes = image.getFormat() == Image2D.RGB ? 3 : READBACK_BYTES_PER_PIXEL;
        }

        private boolean isInBounds(int x, int y) {
            return x >= 0 && y >= 0 && x < width && y < height;
        }

        public void setPixel(int x, int y, int argb) {
            if (!isInBounds(x, y)) {
                return;
            }
            int offset = (y * width + x) * (image.getFormat() == Image2D.RGB ? 3 : 4);
            data[offset] = (byte) ((argb >>> 16) & 0xFF);
            data[offset + 1] = (byte) ((argb >>> 8) & 0xFF);
            data[offset + 2] = (byte) (argb & 0xFF);
            if (image.getFormat() == Image2D.RGBA) {
                data[offset + 3] = (byte) ((argb >>> 24) & 0xFF);
            }
        }

        void writeRgbaRowsFlipped(byte[] rgba, int srcWidth, int srcHeight, int dstX, int dstY) {
            int clippedDstX = Math.max(0, dstX);
            int clippedSrcX = clippedDstX - dstX;
            int clippedWidth = Math.min(srcWidth - clippedSrcX, width - clippedDstX);
            if (clippedWidth <= 0) {
                return;
            }
            if (image.getFormat() == Image2D.RGBA) {
                int rowBytes = srcWidth * READBACK_BYTES_PER_PIXEL;
                int clippedRowBytes = clippedWidth * READBACK_BYTES_PER_PIXEL;
                for (int srcY = 0; srcY < srcHeight; srcY++) {
                    int dstRow = dstY + (srcHeight - 1 - srcY);
                    if (dstRow < 0 || dstRow >= height) {
                        continue;
                    }
                    int srcOffset = srcY * rowBytes + clippedSrcX * READBACK_BYTES_PER_PIXEL;
                    int dstOffset = ((dstRow * width) + clippedDstX) * pixelBytes;
                    System.arraycopy(rgba, srcOffset, data, dstOffset, clippedRowBytes);
                }
                return;
            }
            for (int srcY = 0; srcY < srcHeight; srcY++) {
                int dstRow = dstY + (srcHeight - 1 - srcY);
                if (dstRow < 0 || dstRow >= height) {
                    continue;
                }
                int srcOffset = srcY * srcWidth * READBACK_BYTES_PER_PIXEL + clippedSrcX * READBACK_BYTES_PER_PIXEL;
                int dstOffset = ((dstRow * width) + clippedDstX) * pixelBytes;
                for (int x = 0; x < clippedWidth; x++) {
                    data[dstOffset] = rgba[srcOffset];
                    data[dstOffset + 1] = rgba[srcOffset + 1];
                    data[dstOffset + 2] = rgba[srcOffset + 2];
                    srcOffset += READBACK_BYTES_PER_PIXEL;
                    dstOffset += pixelBytes;
                }
            }
        }
    }

    private static final class BackgroundTextureSource {
        private final int width;
        private final int height;
        private final int format;
        private final byte[] data;
        private final int internalGlFormat;
        private final int pixelGlFormat;
        private final boolean hasAlpha;

        BackgroundTextureSource(int width, int height, int format, byte[] data, int internalGlFormat, int pixelGlFormat, boolean hasAlpha) {
            this.width = width;
            this.height = height;
            this.format = format;
            this.data = data;
            this.internalGlFormat = internalGlFormat;
            this.pixelGlFormat = pixelGlFormat;
            this.hasAlpha = hasAlpha;
        }
    }

    private static final class GlUploadImage {
        private final int width;
        private final int height;
        private final int format;
        private final byte[] data;

        GlUploadImage(int width, int height, int format, byte[] data) {
            this.width = width;
            this.height = height;
            this.format = format;
            this.data = data;
        }
    }

    private static Transform copyTransform(Transform transform) {
        return transform != null ? new Transform(transform) : null;
    }

    private static boolean shouldAllowImplicitClear(Object target, int hints) {
        if ((hints & Graphics3D.OVERWRITE) != 0) {
            return true;
        }
        if (target instanceof PlatformGraphics) {
            return true;
        }
        return target instanceof javax.microedition.lcdui.Graphics;
    }

    private static GlUploadImage prepareImageForGl(Image2D image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        if (isDirectGlImageFormat(image.getFormat())) {
            return new GlUploadImage(image.getWidth(), image.getHeight(), image.getFormat(), image.getImageData());
        }
        // Non-RGB(A) images still need CPU expansion before GL upload.
        int targetFormat = Image2D.RGBA;
        byte[] source = image.getImageData();
        byte[] converted = new byte[image.getWidth() * image.getHeight() * 4];
        int out = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = readImageArgb(image, source, x, y);
                converted[out++] = (byte) ((argb >>> 16) & 0xFF);
                converted[out++] = (byte) ((argb >>> 8) & 0xFF);
                converted[out++] = (byte) (argb & 0xFF);
                converted[out++] = (byte) ((argb >>> 24) & 0xFF);
            }
        }
        return new GlUploadImage(image.getWidth(), image.getHeight(), targetFormat, converted);
    }

    private static boolean isSupportedImageFormat(int format) {
        return format >= Image2D.ALPHA && format <= Image2D.RGBA;
    }

    private static boolean isDirectGlImageFormat(int format) {
        return format == Image2D.RGB || format == Image2D.RGBA;
    }

    private static boolean needsAlphaInGlFormat(int format) {
        return format == Image2D.ALPHA || format == Image2D.LUMINANCE_ALPHA || format == Image2D.RGBA;
    }

    private static int readImageArgb(Image2D image, byte[] data, int x, int y) {
        int offset;
        switch (image.getFormat()) {
            case Image2D.ALPHA:
                offset = y * image.getWidth() + x;
                return ((data[offset] & 0xFF) << 24) | 0x00FFFFFF;
            case Image2D.LUMINANCE: {
                offset = y * image.getWidth() + x;
                int luminance = data[offset] & 0xFF;
                return 0xFF000000 | (luminance << 16) | (luminance << 8) | luminance;
            }
            case Image2D.LUMINANCE_ALPHA: {
                offset = (y * image.getWidth() + x) * 2;
                int luminance = data[offset] & 0xFF;
                int alpha = data[offset + 1] & 0xFF;
                return (alpha << 24) | (luminance << 16) | (luminance << 8) | luminance;
            }
            case Image2D.RGB:
                offset = (y * image.getWidth() + x) * 3;
                return 0xFF000000 | ((data[offset] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | (data[offset + 2] & 0xFF);
            case Image2D.RGBA:
                offset = (y * image.getWidth() + x) * 4;
                return ((data[offset + 3] & 0xFF) << 24) | ((data[offset] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | (data[offset + 2] & 0xFF);
            default:
                return 0xFFFFFFFF;
        }
    }

    private static String describeImageFormat(int format) {
        switch (format) {
            case Image2D.ALPHA:
                return "ALPHA";
            case Image2D.LUMINANCE:
                return "LUMINANCE";
            case Image2D.LUMINANCE_ALPHA:
                return "LUMINANCE_ALPHA";
            case Image2D.RGB:
                return "RGB";
            case Image2D.RGBA:
                return "RGBA";
            default:
                return String.valueOf(format);
        }
    }

    private static boolean isGlAvailable() {
        try {
            Class.forName("org.mini.gl.GL");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isGlThreadReady() {
        try {
            return GCallBack.getInstance().getOpenglThread() != null;
        } catch (Throwable ignored) {
            return false;
        }
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
                    } catch (Throwable error) {
                        failure[0] = error;
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
            throw new RuntimeException(failure[0]);
        }
    }
}

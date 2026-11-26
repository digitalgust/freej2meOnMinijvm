/*
 * micro3D v3 OpenGL backend factory for freej2meOnMinijvm.
 *
 * This is a NEW, independent GL backend for MascotCapsule micro3d v3. It does NOT
 * touch or share the m3g (JSR-184) GL backend. freej2me's Graphics3D reflects in
 * this class by the fixed name "com.mascotcapsule.micro3d.v3.MiniJvmMicro3dFactory"
 * (see Graphics3D.MINIJVM_BACKEND_FACTORY). If the class is absent or the returned
 * backend reports !isAvailable(), Graphics3D silently falls back to the software
 * rasterizer — so there is no risk to non-miniJVM builds.
 *
 * The factory is thin: the real backend lives in the .base package
 * (MiniJvmMicro3dGlBackend), which is the same package as Light/TextureData/Model
 * and therefore can read their package-private fields (ambIntensity/dirIntensity,
 * TextureData.width/height). This factory just constructs it.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.base.Micro3dBackend;
import com.mascotcapsule.micro3d.v3.base.MiniJvmMicro3dGlBackend;

public final class MiniJvmMicro3dFactory implements Graphics3D.BackendFactory {

    // Temporary software-rendering comparison. Set to true to restore GL.
    private static final boolean ENABLE_OPENGL = true;

    public MiniJvmMicro3dFactory() {
    }

    public Micro3dBackend create() {
        return ENABLE_OPENGL ? new MiniJvmMicro3dGlBackend() : null;
    }
}

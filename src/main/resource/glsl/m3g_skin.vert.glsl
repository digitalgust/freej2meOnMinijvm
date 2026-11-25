#version 330
#ifdef GL_ES
precision highp float;
precision highp int;
#endif

const int MAX_GPU_BONES = 24;
const int MAX_GPU_LIGHTS = 4;

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexCoord0;
layout(location = 4) in vec2 aTexCoord1;
layout(location = 5) in vec4 aBoneIndices;
layout(location = 6) in vec4 aBoneWeights;
layout(location = 7) in mat4 aInstanceModel;
layout(location = 11) in float aBonePaletteOffset;

uniform mat4 uMvp;
uniform mat4 uModel;
uniform mat4 uModelView;
uniform mat4 uCameraMatrix;
uniform mat4 uViewMatrix;
uniform samplerBuffer uBonePalette;
uniform int uUseInstances;
uniform float uDepthRangeScale;
uniform float uDepthRangeBias;
uniform int uLightingEnabled;
uniform int uHasVertexColor;
uniform int uVertexColorTracking;
uniform vec4 uBaseColor;
uniform vec4 uMaterialAmbient;
uniform vec4 uMaterialDiffuse;
uniform vec4 uMaterialEmissive;
uniform vec4 uMaterialSpecular;
uniform float uMaterialShininess;
uniform vec4 uCameraWorldPos;
uniform int uLightCount;
uniform int uLightMode[MAX_GPU_LIGHTS];
uniform vec4 uLightColor[MAX_GPU_LIGHTS];
uniform vec4 uLightPosition[MAX_GPU_LIGHTS];
uniform vec4 uLightDirection[MAX_GPU_LIGHTS];
uniform vec4 uLightAttenuation[MAX_GPU_LIGHTS];
uniform vec4 uLightSpot[MAX_GPU_LIGHTS];
uniform vec4 uBoneRows[MAX_GPU_BONES * 3];

out vec4 vColor;
out vec2 vTexCoord0;
out vec2 vTexCoord1;
out float vFogDistance;

vec4 getBoneRow(int rowIndex) {
    if (uUseInstances != 0) {
        return texelFetch(uBonePalette, int(aBonePaletteOffset + 0.5) + rowIndex);
    }
    return uBoneRows[rowIndex];
}

vec4 applyBonePosition(int boneIndex, vec4 position) {
    int base = boneIndex * 3;
    return vec4(
        dot(getBoneRow(base), position),
        dot(getBoneRow(base + 1), position),
        dot(getBoneRow(base + 2), position),
        1.0
    );
}

vec3 applyBoneNormal(int boneIndex, vec3 normal) {
    int base = boneIndex * 3;
    vec4 direction = vec4(normal, 0.0);
    return vec3(
        dot(getBoneRow(base), direction),
        dot(getBoneRow(base + 1), direction),
        dot(getBoneRow(base + 2), direction)
    );
}

void computeSkinning(out vec4 localPosition, out vec3 localNormal) {
    localPosition = vec4(aPosition, 1.0);
    localNormal = aNormal;
    float weightSum = aBoneWeights.x + aBoneWeights.y + aBoneWeights.z + aBoneWeights.w;
    if (weightSum > 0.0) {
        localPosition = vec4(0.0);
        localNormal = vec3(0.0);
        for (int i = 0; i < 4; i++) {
            float weight = aBoneWeights[i];
            if (weight <= 0.0) {
                continue;
            }
            int boneIndex = int(aBoneIndices[i] + 0.5);
            localPosition += applyBonePosition(boneIndex, vec4(aPosition, 1.0)) * weight;
            localNormal += applyBoneNormal(boneIndex, aNormal) * weight;
        }
        if (length(localNormal) > 0.0) {
            localNormal = normalize(localNormal);
        }
    }
}

vec4 computeLitColor(vec3 worldPosition, vec3 worldNormal, vec4 trackedColor) {
    vec3 normal = normalize(worldNormal);
    if (length(normal) <= 0.0) {
        return trackedColor;
    }

    vec3 ambientAccum = vec3(0.0);
    vec3 diffuseAccum = vec3(0.0);
    vec3 specularAccum = vec3(0.0);
    vec3 viewDir = normalize(uCameraWorldPos.xyz - worldPosition);
    if (length(viewDir) <= 0.0) {
        viewDir = vec3(0.0, 0.0, 1.0);
    }

    for (int i = 0; i < MAX_GPU_LIGHTS; i++) {
        if (i >= uLightCount) {
            break;
        }
        vec3 lightColor = uLightColor[i].rgb;
        if (uLightMode[i] == 128) {
            ambientAccum += lightColor;
            continue;
        }

        vec3 lightDir;
        float attenuation = 1.0;
        float spotFactor = 1.0;
        if (uLightMode[i] == 130 || uLightMode[i] == 131) {
            vec3 delta = uLightPosition[i].xyz - worldPosition;
            float distanceValue = length(delta);
            if (distanceValue <= 0.000001) {
                continue;
            }
            lightDir = delta / distanceValue;
            float denominator = uLightAttenuation[i].x
                + uLightAttenuation[i].y * distanceValue
                + uLightAttenuation[i].z * distanceValue * distanceValue;
            if (denominator > 0.000001) {
                attenuation = 1.0 / denominator;
            }
            if (uLightMode[i] == 131) {
                vec3 spotDir = normalize(uLightDirection[i].xyz);
                float cosTheta = -dot(lightDir, spotDir);
                float cutoff = uLightSpot[i].x;
                if (cutoff >= 0.0 && cosTheta < cutoff) {
                    continue;
                }
                spotFactor = pow(max(0.0, cosTheta), max(0.0, uLightSpot[i].y));
            }
        } else {
            lightDir = normalize(uLightDirection[i].xyz);
        }

        float ndotl = dot(normal, lightDir);
        if (ndotl > 0.0) {
            float lightingScale = attenuation * spotFactor;
            diffuseAccum += lightColor * ndotl * lightingScale;
            if (uMaterialShininess > 0.0 && length(uMaterialSpecular.rgb) > 0.0) {
                vec3 halfVector = normalize(lightDir + viewDir);
                float ndoth = max(0.0, dot(normal, halfVector));
                if (ndoth > 0.0) {
                    specularAccum += lightColor * pow(ndoth, max(1.0, uMaterialShininess)) * lightingScale;
                }
            }
        }
    }

    vec4 diffuseColor = uVertexColorTracking != 0 ? trackedColor : uMaterialDiffuse;
    vec3 ambientColor = uVertexColorTracking != 0 ? trackedColor.rgb : uMaterialAmbient.rgb;
    vec3 color = uMaterialEmissive.rgb
        + ambientColor * ambientAccum
        + diffuseColor.rgb * diffuseAccum
        + uMaterialSpecular.rgb * specularAccum;
    return vec4(clamp(color, 0.0, 1.0), diffuseColor.a);
}

void main() {
    vec4 localPosition;
    vec3 localNormal;
    computeSkinning(localPosition, localNormal);

    mat4 modelMatrix = uUseInstances != 0 ? aInstanceModel : uModel;
    vec4 worldPosition = modelMatrix * localPosition;
    vec3 worldNormal = normalize((modelMatrix * vec4(localNormal, 0.0)).xyz);

    vec4 trackedColor = aColor;
    vec4 baseColor = uHasVertexColor != 0 ? trackedColor : uBaseColor;
    vec4 finalColor = baseColor;
    if (uLightingEnabled != 0) {
        finalColor = computeLitColor(worldPosition.xyz, worldNormal, trackedColor);
    }

    vec4 eyePosition = uUseInstances != 0
            ? uViewMatrix * worldPosition
            : uModelView * localPosition;
    gl_Position = uUseInstances != 0
            ? uCameraMatrix * worldPosition
            : uMvp * localPosition;
    gl_Position.z = gl_Position.z * uDepthRangeScale + gl_Position.w * uDepthRangeBias;
    vColor = finalColor;
    vTexCoord0 = aTexCoord0;
    vTexCoord1 = aTexCoord1;
    vFogDistance = length(eyePosition.xyz);
}

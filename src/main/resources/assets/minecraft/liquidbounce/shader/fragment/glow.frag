#version 120

uniform sampler2D texture;
uniform vec2 texelSize;

uniform vec3 color;
uniform int radius;
uniform float intensity;
uniform float fade;
uniform float targetAlpha;
uniform float bloomThreshold; // 新增颜色溢出控制

void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    // 保留原始颜色细节
    if (centerCol.a > 0.95) {
        gl_FragColor = vec4(mix(centerCol.rgb, color, 0.2), targetAlpha);
        return;
    }

    float alpha = 0.0;
    vec3 glowColor = vec3(0.0);
    float totalWeight = 0.0;

    // 高斯模糊参数
    float sigma = float(radius) * 0.5;
    float twoSigmaSquare = 2.0 * sigma * sigma;
    float sigmaRoot = sqrt(twoSigmaSquare * 3.141592);

    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            vec2 offset = vec2(x, y) * texelSize;
            vec4 sampleCol = texture2D(texture, gl_TexCoord[0].xy + offset);

            // 高斯权重计算
            float distanceSquare = float(x*x + y*y);
            float weight = exp(-distanceSquare / twoSigmaSquare) / sigmaRoot;

            // 颜色阈值处理
            if (sampleCol.a > bloomThreshold) {
                float colorIntensity = length(sampleCol.rgb);
                vec3 bloom = sampleCol.rgb * colorIntensity * 2.0;

                // 颜色混合
                glowColor += bloom * weight;
                alpha += weight * smoothstep(0.0, 1.0, sampleCol.a);
                totalWeight += weight;
            }
        }
    }

    // 颜色归一化
    if (totalWeight > 0.0) {
        glowColor /= totalWeight;
    }

    // 叠加原始颜色
    vec3 finalColor = glowColor * intensity * color;
    alpha = clamp(alpha * fade, 0.0, 1.0);

    // 边缘光晕衰减
    float distanceFromCenter = length(gl_TexCoord[0].xy - vec2(0.5));
    float edgeAttenuation = 1.0 - smoothstep(0.3, 0.7, distanceFromCenter);

    gl_FragColor = vec4(
        mix(centerCol.rgb, finalColor, alpha * edgeAttenuation),
        max(centerCol.a, alpha * 0.8)
    );
}
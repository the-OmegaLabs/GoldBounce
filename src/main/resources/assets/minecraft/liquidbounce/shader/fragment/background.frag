/*
 * Original shader from: https://www.shadertoy.com/view/433cW4
 * Slightly modified to match the client's atmosphere.
 */
#version 120
#define NUM_LAYERS 6
#define PI 3.14159265359

#ifdef GL_ES
precision lowp float;
#endif

// glslsandbox uniforms
uniform float iTime;
uniform vec2 iResolution;

float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yzx + 33.33);

    return fract((p3.x + p3.y) * p3.z);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;
    float total_amplitude = 0.0;

    for(int i = 0; i < 5; i++) {
        value += amplitude * noise(p * frequency);
        total_amplitude += amplitude;
        amplitude *= 0.5;
        frequency *= 2.0;
        p += vec2(3.14, 2.72);
    }

    return value / total_amplitude * 0.8 + 0.2;
}

vec3 auroraColor(float t) {
    vec3 a = vec3(0.2, 0.4, 0.3);
    vec3 b = vec3(0.5, 0.6, 0.3);
    vec3 c = vec3(0.8, 1.0, 1.0);
    vec3 d = vec3(0.0, 0.2, 0.3);
    vec3 color = a + b * cos(2.0 * PI * (c * t + d));

    return max(color, vec3(0.15));
}

vec3 addStars(vec2 uv, vec3 col, float iTime) {
    float clusterScale = 100.0;
    float stars = 0.0;

    vec2 scaledUV1 = uv * 500.0 + vec2(1.23, 3.45);
    float rand1 = hash21(scaledUV1);
    vec2 clusterCoord1 = floor(scaledUV1 / clusterScale);
    float clusterSeed1 = hash21(clusterCoord1 + vec2(10.0, 20.0));
    float twinkle1 = sin((iTime + clusterSeed1 * 5.0) * (1.0 + rand1 * 4.0)) * (0.15 + rand1 * 0.05)
                     + (1.0 - (0.15 + rand1 * 0.05));
    stars += step(0.99, rand1) * 0.3 * twinkle1;

    vec2 scaledUV2 = uv * 300.0 + vec2(6.78, 9.01);
    float rand2 = hash21(scaledUV2);
    vec2 clusterCoord2 = floor(scaledUV2 / clusterScale);
    float clusterSeed2 = hash21(clusterCoord2 + vec2(30.0, 40.0));
    float twinkle2 = sin((iTime + clusterSeed2 * 6.0) * (1.0 + rand2 * 3.0)) * (0.15 + rand2 * 0.1)
                     + (1.0 - (0.15 + rand2 * 0.1));
    stars += step(0.998, rand2) * 0.6 * twinkle2;

    vec2 scaledUV3 = uv * 200.0 + vec2(2.34, 5.67);
    float rand3 = hash21(scaledUV3);
    vec2 clusterCoord3 = floor(scaledUV3 / clusterScale);
    float clusterSeed3 = hash21(clusterCoord3 + vec2(50.0, 60.0));
    float twinkle3 = sin((iTime + clusterSeed3 * 7.0) * (1.0 + rand3 * 2.5)) * (0.15 + rand3 * 0.15)
                     + (1.0 - (0.15 + rand3 * 0.15));
    stars += step(0.999, rand3) * 1.5 * twinkle3;

    vec2 sparkleUV = uv * 150.0 + vec2(4.56, 7.89);
    float sparkleRand = hash21(sparkleUV);
    vec2 clusterCoordS = floor(sparkleUV / clusterScale);
    float clusterSeedS = hash21(clusterCoordS + vec2(70.0, 80.0));
    float sparkle = pow(sin((iTime + clusterSeedS * 8.0) * (1.0 + sparkleRand * 3.0)) * 0.5 + 0.5, 2.0)
                    * (0.2 + sparkleRand * 0.2);
    stars += step(0.9995, sparkleRand) * sparkle * 0.5;

    return col + vec3(stars) * (0.2 - length(col) * 0.3);
}

vec3 atmosphere(vec2 uv, vec3 color) {
    float a = pow(1.0 - min(abs(uv.y + 0.2), 1.0), 2.0);
    vec3 c = vec3(0.0, 0.1, 0.2);
    float s = a * 0.4 * (1.0 - length(color));

    return mix(color, c, s);
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    vec2 uv = (fragCoord - 0.5 * iResolution.xy) / iResolution.y;
    vec3 col = vec3(0.02, 0.03, 0.06);
    col = addStars(uv, col, iTime);

    vec3 aurora = vec3(0.0);
    for(int i = 0; i < NUM_LAYERS; i++) {
        float fi = float(i);
        float speed = 0.08 + fi * 0.02;
        float scale = 1.5 + fi * 0.3;
        float intensity = 0.35 / (fi + 1.0) + 0.2;
        vec2 auroraUV = uv;

        auroraUV.x += iTime * speed * 0.2;

        float curtain = fbm(vec2(auroraUV.x * scale, iTime * 0.15));
        curtain = pow(curtain, 1.1);

        float y = sin(auroraUV.x * 2.0 + iTime * 0.5) * 0.2;
        auroraUV.y += y;

        float aurora_shape = pow(max(0.0, 1.0 - abs(auroraUV.y + 0.4 + curtain * 0.3)), 1.3 + fi * 0.4);
        aurora_shape *= smoothstep(0.0, 0.15, curtain);
        aurora_shape *= smoothstep(-0.2, 0.0, auroraUV.y + 0.5);

        vec3 auroraCol = auroraColor(fi / float(NUM_LAYERS) + curtain * 0.2 + iTime * 0.05);
        aurora += aurora_shape * auroraCol * intensity * 1.4;
    }

    col += aurora;

    vec3 glow = vec3(0.0);
    for(float i = 1.0; i < 4.0; i++) {
        vec2 gUV = uv * (1.0 - i * 0.03);
        float gr = pow(max(0.0, 0.5 - abs(gUV.y + 0.6)), 2.2);

        glow += gr * auroraColor(iTime * 0.1) * 0.2;
    }

    col += glow;
    col = atmosphere(uv, col);
    col = pow(col, vec3(0.9));

    float v = 1.0 - smoothstep(0.8, 1.8, length(uv));

    col *= mix(0.8, 1.0, v);
    col = mix(col, vec3(dot(col, vec3(0.299, 0.587, 0.114))), -0.2);
    col = max(col, vec3(0.02, 0.03, 0.06));

    fragColor = vec4(col, 1.0);
}

void main() {
    mainImage(gl_FragColor, gl_FragCoord.xy);
}
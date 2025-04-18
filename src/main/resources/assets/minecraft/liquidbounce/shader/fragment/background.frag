/*
 * Original shader from: https://www.shadertoy.com/view/4dsGzH
 * Slightly modified to match the client's atmosphere.
 */
#version 120
#define NUM_LAYERS 6
#define PI 3.14159265359

uniform vec2 iResolution;
uniform float iTime;

vec3 COLOR1 = vec3(0.0, 0.0, 0.3);
vec3 COLOR2 = vec3(0.5, 0.0, 0.0);
float BLOCK_WIDTH = 0.01;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord.xy / max(iResolution.xy, vec2(0.001));

	// To create the BG pattern
	vec3 final_color = vec3(1.0);
	vec3 bg_color = vec3(0.0);
	vec3 wave_color = vec3(0.0);

	float c1 = mod(uv.x, 2.0 * BLOCK_WIDTH);
	c1 = step(BLOCK_WIDTH, c1);

	float c2 = mod(uv.y, 2.0 * BLOCK_WIDTH);
	c2 = step(BLOCK_WIDTH, c2);

	bg_color = mix(uv.x * COLOR1, uv.y * COLOR2, c1 * c2);


	// To create the waves
	float wave_width = 0.01;
	uv  = -1.0 + 2.0 * uv;
	uv.y += 0.1;
	for(float i = 0.0; i < 10.0; i++) {

		uv.y += (0.07 * sin(uv.x + i/7.0 + iTime ));
		wave_width = abs(1.0 / (150.0 * uv.y));
		wave_color += vec3(wave_width * 1.9, wave_width, wave_width * 1.5);
	}

	final_color = bg_color + wave_color;


	fragColor = vec4(final_color, 1.0);
}

void main() {
    vec4 color;
    mainImage(color, gl_FragCoord.xy);
    gl_FragColor = color;
}
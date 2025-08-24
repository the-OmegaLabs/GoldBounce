#extension GL_OES_standard_derivatives : enable

precision highp float;

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

// ----------------------------------------------------------------------------------------
//	"Toon Cloud" by Antoine Clappier - March 2015
//
//	Licensed under:
//  A Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
//	http://creativecommons.org/licenses/by-nc-sa/4.0/
// ----------------------------------------------------------------------------------------

#define TAU 6.28318530718


const vec3 BackColor	= vec3(0.0, 0.4, 0.58);
const vec3 CloudColor	= vec3(0.18,0.70,0.87);

const vec3 blue = vec3(0.357, 0.812, 0.98);
const vec3 pink = vec3(0.961, 0.671, 0.725);
const vec3 white = vec3(1.0, 1.0, 1.0);


float Func(float pX)
{
    return 0.6*(0.5*sin(0.1*pX) + 0.5*sin(0.553*pX) + 0.7*sin(1.2*pX));
}


float FuncR(float pX)
{
    return 0.5 + 0.25*(1.0 + sin(mod(40.0*pX, TAU)));
}


float Layer(vec2 pQ, float pT)
{
    vec2 Qt = 3.5*pQ;
    pT *= 0.5;
    Qt.x += pT;

    float Xi = floor(Qt.x);
    float Xf = Qt.x - Xi -0.5;

    vec2 C;
    float Yi;
    float D = 1.0 - step(Qt.y,  Func(Qt.x));

    // Disk:
    Yi = Func(Xi + 0.5);
    C = vec2(Xf, Qt.y - Yi );
    D =  min(D, length(C) - FuncR(Xi+ pT/80.0));

    // Previous disk:
    Yi = Func(Xi+1.0 + 0.5);
    C = vec2(Xf-1.0, Qt.y - Yi );
    D =  min(D, length(C) - FuncR(Xi+1.0+ pT/80.0));

    // Next Disk:
    Yi = Func(Xi-1.0 + 0.5);
    C = vec2(Xf+1.0, Qt.y - Yi );
    D =  min(D, length(C) - FuncR(Xi-1.0+ pT/80.0));

    return min(1.0, D);
}



void main(void)
{
    // Setup:
    vec2 UV = 2.0*(gl_FragCoord.xy - resolution.xy/2.0) / min(resolution.x, resolution.y);

    // Render:
    vec3 Color= blue;

    for(float J=0.0; J<=0.8; J+=0.2)
    {
        // Cloud Layer:
        float Lt =  time*(0.4  + 2.0*J)*(1.0 + 0.1*sin(226.0*J)) + 17.0*J;
        vec2 Lp = vec2(0.0, 0.3+1.5*( J - 0.5));
        float L = Layer(UV + Lp, Lt);

        // Blur and color:
        float Blur = 1.0*(0.5*abs(2.0 - 5.0*J))/(7.0 - 5.0*J);

        float V = mix( 0.0, 1.0, 1.0 - smoothstep( 0.0, 0.01 +0.2*Blur, L ) );
        vec3 Lc=  mix( pink, vec3(1.09), J);

        Color =mix(Color, Lc,  V);
    }

    gl_FragColor = vec4(Color, 1.0);
}
/*
    "Starship" by @XorDev

    Inspired by the debris from SpaceX's 7th Starship test:
    https://x.com/elonmusk/status/1880040599761596689

    My original twigl version:
    https://x.com/XorDev/status/1880344887033569682

    <512 Chars playlist: shadertoy.com/playlist/N3SyzR
*/
void mainImage( out vec4 O, vec2 I)
{
    //Resolution for scaling
    vec2 r = iResolution.xy,
    //Center, rotate and scale
    p = (I+I-r) / r.y * mat2(4,-3,3,4);
    //Time, trailing time and iterator variables
    float t=iTime, T=t+.1*p.x, i;

    //Iterate through 50 particles
    for(
        //Clear fragColor
        O *= i; i++<50.;

        ///Set color:
        //The sine gives us color index between -1 and +1.
        //Then we give each channel a separate frequency.
        //Red is the broadest, while blue dissipates quickly.
        //Add one to avoid negative color values (0 to 2).
        O += (cos(sin(i)*vec4(1,2,3,0))+1.)

        ///Flashing brightness:
        //The brightness fluxuates exponentially between 1/e and e.
        //Each particle has a flash frequency according to its index.
        * exp(sin(i+.1*i*T))

        ///Trail particles with attenuating light:
        //The basic idea is to start with a point light falloff.
        //I used max on the coordinates so that I can scale the
        //positive and negative directions independently.
        //The x axis is scaled down a lot for a long trail.
        //Noise is added to the scaling factor for cloudy depth.
        //The y-axis is also stretched a little for a glare effect.
        //Try a higher value like 4 for more clarity
        / length(max(p,
            p / vec2(texture(iChannel0, p/exp(sin(i)+5.)+vec2(t,i)/8.).r*40.,2))
        ))

        ///Shift position for each particle:
        //Frequencies to distribute particles x and y independently
        //i*i is a quick way to hide the sine wave periods
        //t to shift with time and p.x for leaving trails as it moves
        p+=2.*cos(i*vec2(11,9)+i*i+T*.2);

    //Add sky background and "tanh" tonemap
    O = tanh(.01*p.y*vec4(0,1,2,3)+O*O/1e4);
}
void main() {
    mainImage(gl_FragColor, gl_FragCoord.xy);
}
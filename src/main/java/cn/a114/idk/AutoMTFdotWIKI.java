package cn.a114.idk;

import net.minecraft.util.Util;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Locale;

public class AutoMTFdotWIKI {
    public static void init(){
        // cool b
        if(Util.getOSType() == Util.EnumOS.LINUX){
            // STDOUT reader moment
            try {
                Process nimadepi = Runtime.getRuntime().exec("uname -r");
                BufferedReader caoNiMa = new BufferedReader(
                        new InputStreamReader(
                                nimadepi.getInputStream()
                        )
                );
                String kernelRelease = caoNiMa.readLine();
                caoNiMa.close();
                if(kernelRelease.toLowerCase(Locale.ROOT).contains("arch")) {
                    Desktop.getDesktop().browse(new URI("https://mtf.wiki/en"));
                }

            } catch (Exception e) {
                // We don't give a fuck for Headless exception
            }
        }
    }
}

package net.ccbluex.liquidbounce.utils.render.bruh;

import java.awt.*;

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;

public class Yes {
    public static void drawClientColourWithGradient(String text, float x, float y, int alpha) {
        Color color1 = new Color(255, 215, 0);
        Color color2 = new Color(255, 255, 255);

        double timeFactor = System.currentTimeMillis() / 800.0;
        double gradientSpread = Math.PI;

        float currentX = x;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            double phase = text.length() > 1 ? (double) i / (text.length() - 1) : 0;
            double sinArgument = timeFactor + phase * gradientSpread;
            float interpolationFactor = (float) (Math.sin(sinArgument) + 1.0) / 2.0f;
            float r = (color1.getRed() * (1 - interpolationFactor) + color2.getRed() * interpolationFactor) / 255.0F;
            float g = (color1.getGreen() * (1 - interpolationFactor) + color2.getGreen() * interpolationFactor) / 255.0F;
            float b = (color1.getBlue() * (1 - interpolationFactor) + color2.getBlue() * interpolationFactor) / 255.0F;

            int charColor = alpha << 24 | (int) (r * 255.0F) << 16 | (int) (g * 255.0F) << 8 | (int) (b * 255.0F);

            String charStr = String.valueOf(character);
            mc.fontRendererObj.drawStringWithShadow(charStr, currentX, y, charColor);
            currentX += mc.fontRendererObj.getStringWidth(charStr);
        }
    }
}

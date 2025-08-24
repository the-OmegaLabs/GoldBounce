package net.ccbluex.liquidbounce.utils.render;

// From Xylitol

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RoundedUtils {
    public static ShaderUtil roundedShader = new ShaderUtil("liquidbounce/shader/roundedRect.frag");
    public static ShaderUtil roundedOutlineShader = new ShaderUtil("liquidbounce/shader/roundRectOutline.frag");
    private static final ShaderUtil roundedTexturedShader = new ShaderUtil("liquidbounce/shader/roundRectTextured.frag");
    private static final ShaderUtil roundedGradientShader = new ShaderUtil("liquidbounce/shader/roundedRectGradient.frag");
    private static final ShaderUtil circleShader = new ShaderUtil("arc");

    public static void drawGradientRoundLR(float x2, float y2, float width, float height, float radius, Color color1, Color color2) {
        RoundedUtils.drawGradientRound(x2, y2, width, height, radius, color1, color2, color2, color1);
    }
    public static void drawGradientHorizontal(float x, float y, float width, float height, float radius, Color left, Color right) {
        drawGradientRound(x, y, width, height, radius, left, left, right, right);
    }
    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
    }
    public static void drawCircle(float x2, float y2, float radius, float progress, int change, Color color, float smoothness) {
        GLUtil.startBlend();
        float borderThickness = 1.0f;
        circleShader.init();
        circleShader.setUniformf("radialSmoothness", smoothness);
        circleShader.setUniformf("radius", radius);
        circleShader.setUniformf("borderThickness", borderThickness);
        circleShader.setUniformf("progress", progress);
        circleShader.setUniformi("change", change);
        circleShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        float wh = radius + 10.0f;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        circleShader.setUniformf("pos", (x2 + (wh / 2.0f - (radius + borderThickness) / 2.0f)) * (float)sr.getScaleFactor(), (float)Minecraft.getMinecraft().displayHeight - (radius + borderThickness) * (float)sr.getScaleFactor() - (y2 + (wh / 2.0f - (radius + borderThickness) / 2.0f)) * (float)sr.getScaleFactor());
        ShaderUtil.drawQuads(x2, y2, wh, wh);
        circleShader.unload();
        GLUtil.endBlend();
    }

    public static void drawRound(float x2, float y2, float width, float height, float radius, Color color) {
        resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        roundedShader.init();
        RoundedUtils.setupRoundedRectUniforms(x2, y2, width, height, radius, roundedShader);
        roundedShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        ShaderUtil.drawQuads(x2 - 1.0f, y2 - 1.0f, width + 2.0f, height + 2.0f);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawRound(float x2, float y2, float width, float height, float radius, boolean blur, Color color) {
        resetColor();
        GlStateManager.enableBlend();
        GL11.glBlendFunc((int)770, (int)771);
        setAlphaLimit(0.0f);
        roundedShader.init();
        RoundedUtils.setupRoundedRectUniforms(x2, y2, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        ShaderUtil.drawQuads(x2 - 1.0f, y2 - 1.0f, width + 2.0f, height + 2.0f);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawGradientRound(float x2, float y2, float width, float height, float radius, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        roundedGradientShader.init();
        RoundedUtils.setupRoundedRectUniforms(x2, y2, width, height, radius, roundedGradientShader);
        roundedGradientShader.setUniformf("color1", (float)topLeft.getRed() / 255.0f, (float)topLeft.getGreen() / 255.0f, (float)topLeft.getBlue() / 255.0f, (float)topLeft.getAlpha() / 255.0f);
        roundedGradientShader.setUniformf("color2", (float)bottomRight.getRed() / 255.0f, (float)bottomRight.getGreen() / 255.0f, (float)bottomRight.getBlue() / 255.0f, (float)bottomRight.getAlpha() / 255.0f);
        roundedGradientShader.setUniformf("color3", (float)bottomLeft.getRed() / 255.0f, (float)bottomLeft.getGreen() / 255.0f, (float)bottomLeft.getBlue() / 255.0f, (float)bottomLeft.getAlpha() / 255.0f);
        roundedGradientShader.setUniformf("color4", (float)topRight.getRed() / 255.0f, (float)topRight.getGreen() / 255.0f, (float)topRight.getBlue() / 255.0f, (float)topRight.getAlpha() / 255.0f);
        ShaderUtil.drawQuads(x2 - 1.0f, y2 - 1.0f, width + 2.0f, height + 2.0f);
        roundedGradientShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawRoundOutline(float x2, float y2, float width, float height, float radius, float outlineThickness, Color color, Color outlineColor) {
        resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        roundedOutlineShader.init();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        RoundedUtils.setupRoundedRectUniforms(x2, y2, width, height, radius, roundedOutlineShader);
        roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * (float)sr.getScaleFactor());
        roundedOutlineShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        roundedOutlineShader.setUniformf("outlineColor", (float)outlineColor.getRed() / 255.0f, (float)outlineColor.getGreen() / 255.0f, (float)outlineColor.getBlue() / 255.0f, (float)outlineColor.getAlpha() / 255.0f);
        ShaderUtil.drawQuads(x2 - (2.0f + outlineThickness), y2 - (2.0f + outlineThickness), width + (4.0f + outlineThickness * 2.0f), height + (4.0f + outlineThickness * 2.0f));
        roundedOutlineShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawGradientCornerLR(float x2, float y2, float width, float height, float radius, Color topLeft, Color bottomRight) {
        Color mixedColor = ColorUtils.INSTANCE.interpolateColor(topLeft, bottomRight, 0.5f);
        RoundedUtils.drawGradientRound(x2, y2, width, height, radius, mixedColor, topLeft, bottomRight, mixedColor);
    }

    public static void drawGradientCornerRL(float x2, float y2, float width, float height, float radius, Color bottomLeft, Color topRight) {
        Color mixedColor = ColorUtils.INSTANCE.interpolateColor(topRight, bottomLeft, 0.5f);
        RoundedUtils.drawGradientRound(x2, y2, width, height, radius, bottomLeft, mixedColor, mixedColor, topRight);
    }

    public static void drawRoundTextured(float x2, float y2, float width, float height, float radius, float alpha) {
        resetColor();
        roundedTexturedShader.init();
        roundedTexturedShader.setUniformi("textureIn", 0);
        RoundedUtils.setupRoundedRectUniforms(x2, y2, width, height, radius, roundedTexturedShader);
        roundedTexturedShader.setUniformf("alpha", alpha);
        ShaderUtil.drawQuads(x2 - 1.0f, y2 - 1.0f, width + 2.0f, height + 2.0f);
        roundedTexturedShader.unload();
        GlStateManager.disableBlend();
    }

    private static void setupRoundedRectUniforms(float x2, float y2, float width, float height, float radius, ShaderUtil roundedTexturedShader) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedTexturedShader.setUniformf("location", x2 * (float)sr.getScaleFactor(), (float)Minecraft.getMinecraft().displayHeight - height * (float)sr.getScaleFactor() - y2 * (float)sr.getScaleFactor());
        roundedTexturedShader.setUniformf("rectSize", width * (float)sr.getScaleFactor(), height * (float)sr.getScaleFactor());
        roundedTexturedShader.setUniformf("radius", radius * (float)sr.getScaleFactor());
    }

    public static void round(float x2, float y2, float width, float height, float radius, Color color) {
        resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        roundedShader.init();
        RoundedUtils.setupRoundedRectUniforms(x2, y2, width, height, radius, roundedShader);
        roundedShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        ShaderUtil.drawQuads(x2 - 1.0f, y2 - 1.0f, width + 2.0f, height + 2.0f);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void rect(float x2, float y2, float width, float height) {
        GL11.glBegin((int)7);
        GL11.glTexCoord2f((float)0.0f, (float)0.0f);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glTexCoord2f((float)0.0f, (float)1.0f);
        GL11.glVertex2f((float)x2, (float)(y2 + height));
        GL11.glTexCoord2f((float)1.0f, (float)1.0f);
        GL11.glVertex2f((float)(x2 + width), (float)(y2 + height));
        GL11.glTexCoord2f((float)1.0f, (float)0.0f);
        GL11.glVertex2f((float)(x2 + width), (float)y2);
        GL11.glEnd();
    }

    public static void round(int x2, int y2, int width, int height, float radius, int rgb) {
        RoundedUtils.round((float)x2, (float)y2, (float)width, (float)height, radius, new Color(rgb));
    }
}


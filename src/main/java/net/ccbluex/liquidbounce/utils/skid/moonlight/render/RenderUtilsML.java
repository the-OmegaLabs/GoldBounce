package net.ccbluex.liquidbounce.utils.skid.moonlight.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;
import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static org.lwjgl.opengl.GL11.*;

public class RenderUtilsML {
    public static void drawImage(ResourceLocation image, float x, float y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture((int) x, (int) y, 0, 0, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawImage(ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        mc.getTextureManager().bindTexture(image);
        drawImage(x, y, z, width, height, color1, color2, color3, color4);
    }


    public static void drawImage(double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        boolean blend = glIsEnabled(GL_BLEND);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE);
        glShadeModel(GL_SMOOTH);
        glAlphaFunc(GL_GREATER, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRenderer.pos((float) x, (float) (y + height), (float) (z)).tex(0, 1 - 0.01f)
                .color((color1 >> 16 & 0xFF) / 255f, (color1 >> 8 & 0xFF) / 255f, (color1 & 0xFF) / 255f, (color1 >> 24 & 0xFF) / 255f).endVertex();
        worldRenderer.pos((float) (x + width), (float) (y + height), (float) (z)).tex(1, 1 - 0.01f)
                .color((color2 >> 16 & 0xFF) / 255f, (color2 >> 8 & 0xFF) / 255f, (color2 & 0xFF) / 255f, (color2 >> 24 & 0xFF) / 255f).endVertex();
        worldRenderer.pos((float) (x + width), (float) y, (float) z).tex(1, 0)
                .color((color3 >> 16 & 0xFF) / 255f, (color3 >> 8 & 0xFF) / 255f, (color3 & 0xFF) / 255f, (color3 >> 24 & 0xFF) / 255f).endVertex();
        worldRenderer.pos((float) x, (float) y, (float) z).tex(0, 0)
                .color((color4 >> 16 & 0xFF) / 255f, (color4 >> 8 & 0xFF) / 255f, (color4 & 0xFF) / 255f, (color4 >> 24 & 0xFF) / 255f).endVertex();
        tessellator.draw();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        glShadeModel(GL_FLAT);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ZERO);
        if (!blend)
            GlStateManager.disableBlend();
    }

    public static void drawImage(ResourceLocation resource, float x, float y, float x2, float y2, int c) {
        mc.getTextureManager().bindTexture(resource);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        float alpha = (c >> 24 & 0xFF) / 255.0F;
        float red = (c >> 16 & 0xFF) / 255.0F;
        float green = (c >> 8 & 0xFF) / 255.0F;
        float blue = (c & 0xFF) / 255.0F;

        worldRenderer.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRenderer.pos(x, y2, 0.0F).tex(0.0F, 1.0F).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x2, y2, 0.0F).tex(1.0F, 1.0F).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x2, y, 0.0F).tex(1.0F, 0.0F).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(x, y, 0.0F).tex(0.0F, 0.0F).color(red, green, blue, alpha).endVertex();

        GL11.glShadeModel(7425);
        GL11.glDepthMask(false);
        tessellator.draw();
        GL11.glDepthMask(true);
        GL11.glShadeModel(7424);
    }
}

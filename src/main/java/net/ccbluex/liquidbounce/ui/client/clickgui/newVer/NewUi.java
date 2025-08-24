/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.newVer;

import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.modules.render.NewGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.CategoryElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.SearchElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.ModuleElement;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.GlowUtils;
import net.ccbluex.liquidbounce.utils.MouseUtils;
// import net.ccbluex.liquidbounce.utils.render.AnimationUtils水影加加; // Replaced with manual implementation
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.Stencil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class NewUi extends GuiScreen {

    private static NewUi instance;
    public static NewUi getInstance() {
        return instance == null ? instance = new NewUi() : instance;
    }

    public static void resetInstance() {
        instance = new NewUi();
    }

    public NewUi() {
        for (Category c : Category.values())
            categoryElements.add(new CategoryElement(c));
        categoryElements.get(0).setFocused(true);
    }

    public final List<CategoryElement> categoryElements = new ArrayList<>();

    // List to hold all active ripple effects
    private final List<Ripple> ripples = new ArrayList<>();

    private float startYAnim = height / 2F;
    private float endYAnim = height / 2F;

    private SearchElement searchElement;

    private float fading = 0F;

    // --- Material Design Animation Helper ---
    // A simple utility to create smooth ease-out animations.
    private static class MaterialAnimate {
        public static float animate(float target, float current, float speed) {
            if (current == target) return target;
            float newSpeed = speed * RenderUtils.INSTANCE.getDeltaTime() * 0.025f;
            if (Math.abs(target - current) < 0.01f) {
                return target;
            }
            return current + (target - current) * MathHelper.clamp_float(newSpeed, 0F, 1F);
        }
    }

    // --- Ripple Effect Class ---
    // Manages the state and rendering of a single ripple.
    private static class Ripple {
        private final float x, y;
        private final long creationTime;
        private final Color color;
        private final float maxRadius;
        private static final long DURATION = 600; // Animation duration in milliseconds

        public Ripple(float x, float y, float maxRadius, Color color) {
            this.x = x;
            this.y = y;
            this.maxRadius = maxRadius;
            this.color = color;
            this.creationTime = System.currentTimeMillis();
        }

        public void draw() {
            long elapsedTime = System.currentTimeMillis() - this.creationTime;
            if (elapsedTime > DURATION) {
                return;
            }

            // Ease-out-cubic function for smooth expansion
            double progress = (double) elapsedTime / DURATION;
            double easedProgress = 1.0 - Math.pow(1.0 - progress, 3);

            float currentRadius = (float) (easedProgress * this.maxRadius);

            // Fade out the ripple as it expands
            int alpha = (int) (this.color.getAlpha() * (1.0 - progress));
            if (alpha <= 0) return;

            Color drawColor = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha);

            RenderUtils.INSTANCE.drawFilledCircle(this.x, this.y, currentRadius, drawColor);
        }

        public boolean isDone() {
            return System.currentTimeMillis() - this.creationTime > DURATION;
        }
    }


    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        for (CategoryElement ce : categoryElements) {
            for (ModuleElement me : ce.getModuleElements()) {
                if (me.listeningKeybind())
                    me.resetState();
            }
        }
        searchElement = new SearchElement(40F, 115F, 180F, 20F);
        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused()) {
                ce.handleMouseRelease(-1, -1, 0, 0, 0, 0, 0);
            }
        }
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
//        RenderUtils.INSTANCE.drawBlurRect(30F, 30F, this.width - 30F, this.height - 30F, 5);
        // will draw reduced ver once it gets under 1140x780.
        GlowUtils.INSTANCE.drawGlow(30, 30, sr.getScaledWidth()-60, sr.getScaledHeight()-60, 4, new Color(0, 0, 0, 150));
//        RenderUtils.INSTANCE.drawRoundedRect(30, sr.getScaledHeight()-30, sr.getScaledWidth()-30, 30, new Color(33, 33, 33, 150).getRGB(), 4F);
        drawFullSized(mouseX, mouseY, partialTicks, new Color(0,140,255));
    }

    private void drawFullSized(int mouseX, int mouseY, float partialTicks, Color accentColor) {
        // Draw the main background
//        RenderUtils.INSTANCE.drawRoundedRect(30F, 30F, this.width - 30F, this.height - 30F, 8, 0xFF101010);

        // something to make it look more like windoze
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, this.width - 54F, 30F, this.width - 30F, 50F)) {
            fading += 0.2F * RenderUtils.INSTANCE.getDeltaTime() * 0.045F;
        } else {
            fading -= 0.2F * RenderUtils.INSTANCE.getDeltaTime() * 0.045F;
        }
        fading = MathHelper.clamp_float(fading, 0F, 1F);
        RenderUtils.INSTANCE.customRounded(this.width - 54F, 30F, this.width - 30F, 50F, 0F, 8F, 0F, 8F, new Color(1F, 0F, 0F, fading).getRGB());
        GlStateManager.disableAlpha();
        RenderUtils.INSTANCE.drawImage(IconManager.removeIcon, this.width - 47, 35, 10, 10);
        GlStateManager.enableAlpha();

        // --- Player Head ---
        Stencil.write(true);
        RenderUtils.INSTANCE.drawFilledCircle(65, 80, 25F, new Color(45, 45, 45));
        Stencil.erase(true);
        if (mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()) != null) {
            final ResourceLocation skin = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getLocationSkin();
            glPushMatrix();
            glTranslatef(40F, 55F, 0F);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glDepthMask(false);
            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            glColor4f(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(skin);
            Gui.drawScaledCustomSizeModalRect(0, 0, 8F, 8F, 8, 8, 50, 50, 64F, 64F);
            glDepthMask(true);
            glDisable(GL_BLEND);
            glEnable(GL_DEPTH_TEST);
            glPopMatrix();
        }
        Stencil.dispose();

        if (Fonts.font52.getStringWidth(mc.thePlayer.getGameProfile().getName()) > 70)
            Fonts.font52.drawString(Fonts.font52.trimStringToWidth(mc.thePlayer.getGameProfile().getName(), 50) + "...", 100, 78 - Fonts.font52.FONT_HEIGHT + 15, -1);
        else
            Fonts.font52.drawString(mc.thePlayer.getGameProfile().getName(), 100, 78 - Fonts.font52.FONT_HEIGHT + 15, -1);

        if (searchElement.drawBox(mouseX, mouseY, accentColor)) {
            searchElement.drawPanel(mouseX, mouseY, 230, 50, width - 260, height - 80, Mouse.getDWheel(), categoryElements, accentColor);
            return;
        }

        final float elementHeight = 24;
        float startY = 140F;
        for (CategoryElement ce : categoryElements) {
            ce.drawLabel(mouseX, mouseY, 30F, startY, 200F, elementHeight);
            if (ce.getFocused()) {
                // Use the new MaterialAnimate helper for smoother sidebar animation
                float animationSpeed = 0.5F; // Adjust speed as needed
                startYAnim = NewGUI.INSTANCE.getFastRenderValue().get() ? startY + 6F : MaterialAnimate.animate(startY + 6F, startYAnim, animationSpeed);
                endYAnim = NewGUI.INSTANCE.getFastRenderValue().get() ? startY + elementHeight - 6F : MaterialAnimate.animate(startY + elementHeight - 6F, endYAnim, animationSpeed);

                ce.drawPanel(mouseX, mouseY, 230, 50, width - 260, height - 80, Mouse.getDWheel(), accentColor);
            }
            startY += elementHeight;
        }
        RenderUtils.INSTANCE.originalRoundedRect(32F, startYAnim, 34F, endYAnim, 1F, accentColor.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Add a new ripple effect at the mouse position on every click
        if (mouseButton == 0 && MouseUtils.mouseWithinBounds(mouseX, mouseY, 30F, 30F, this.width - 30F, this.height - 30F)) {
            ripples.add(new Ripple(mouseX, mouseY, 120F, new Color(255, 255, 255, 60)));
        }

        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, this.width - 54F, 30F, this.width - 30F, 50F)) {
            mc.displayGuiScreen(null);
            return;
        }
        final float elementHeight = 24;
        float startY = 140F;
        searchElement.handleMouseClick(mouseX, mouseY, mouseButton, 230, 50, width - 260, height - 80, categoryElements);
        if (!searchElement.isTyping()) for (CategoryElement ce : categoryElements) {
            if (ce.getFocused())
                ce.handleMouseClick(mouseX, mouseY, mouseButton, 230, 50, width - 260, height - 80);
            if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 30F, startY, 230F, startY + elementHeight) && !searchElement.isTyping()) {
                categoryElements.forEach(e -> e.setFocused(false));
                ce.setFocused(true);
                return;
            }
            startY += elementHeight;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused()) {
                if (ce.handleKeyTyped(typedChar, keyCode))
                    return;
            }
        }
        if (searchElement.handleTyping(typedChar, keyCode, 230, 50, width - 260, height - 80, categoryElements))
            return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        searchElement.handleMouseRelease(mouseX, mouseY, state, 230, 50, width - 260, height - 80, categoryElements);
        if (!searchElement.isTyping())
            for (CategoryElement ce : categoryElements) {
                if (ce.getFocused())
                    ce.handleMouseRelease(mouseX, mouseY, state, 230, 50, width - 260, height - 80);
            }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
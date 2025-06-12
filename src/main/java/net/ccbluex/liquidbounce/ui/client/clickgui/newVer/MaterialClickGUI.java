/*
 * GoldBounce Hacked Client - Material ClickGUI (Self-Contained)
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * This file is a complete redesign based on Material Design principles.
 * It has been refactored by an AI assistant to be fully self-contained with no external render or animation utilities.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.newVer;

import lombok.Getter;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.value.*;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.Stencil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class MaterialClickGUI extends GuiScreen {

    // Singleton instance
    private static MaterialClickGUI instance;

    public static MaterialClickGUI getInstance() {
        if (instance == null) {
            instance = new MaterialClickGUI();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = new MaterialClickGUI();
    }

    // Design System
    private static final int BORDER_RADIUS = 4;
    private static final Color ACCENT_COLOR = new Color(50, 150, 255);
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color SURFACE_COLOR = new Color(45, 45, 45);
    private static final Color ON_SURFACE_COLOR = new Color(230, 230, 230);
    private static final Color ON_SURFACE_VARIANT_COLOR = new Color(180, 180, 180);

    // Layout
    private float x;
    private static float y;
    private float width;
    private float height;
    private float navRailWidth = 72F;
    private float contentX, contentY, contentWidth, contentHeight;

    // State
    private Category selectedCategory = Category.COMBAT;
    private final Map<Category, CategoryPage> categoryPages = new HashMap<>();
    private final List<Ripple> ripples = new ArrayList<>();
    private final LocalAnimation openAnimation = new LocalAnimation();

    // Icons for categories
    private final Map<Category, ResourceLocation> categoryIcons = new HashMap<>();

    private MaterialClickGUI() {
        // Populate category pages
        for (Category category : Category.values()) {
            categoryPages.put(category, new CategoryPage(category));
        }
        // Add a special "Targets" page
        categoryPages.put(null, new TargetsPage());

        // Load icons (replace with your actual icon paths)
        categoryIcons.put(Category.COMBAT, new ResourceLocation("liquidbounce/icon/combat.png"));
        categoryIcons.put(Category.PLAYER, new ResourceLocation("liquidbounce/icon/player.png"));
        categoryIcons.put(Category.MOVEMENT, new ResourceLocation("liquidbounce/icon/movement.png"));
        categoryIcons.put(Category.RENDER, new ResourceLocation("liquidbounce/icon/render.png"));
        categoryIcons.put(Category.WORLD, new ResourceLocation("liquidbounce/icon/world.png"));
        categoryIcons.put(Category.MISC, new ResourceLocation("liquidbounce/icon/misc.png"));
        categoryIcons.put(Category.EXPLOIT, new ResourceLocation("liquidbounce/icon/exploit.png"));
        categoryIcons.put(null, new ResourceLocation("liquidbounce/icon/targets.png")); // Icon for Targets
    }

    @Override
    public void initGui() {
        super.initGui();
        // Center the GUI
        this.width = 560;
        this.height = 340;
        float scaleFactor = new ScaledResolution(mc).getScaleFactor();
        this.x = (this.mc.displayWidth / 2f / scaleFactor) - (this.width / 2f);
        this.y = (this.mc.displayHeight / 2f / scaleFactor) - (this.height / 2f);

        // Define content area
        this.contentX = this.x + navRailWidth;
        this.contentY = this.y;
        this.contentWidth = this.width - navRailWidth;
        this.contentHeight = this.height;

        // Start open animation
        openAnimation.setValue(0);
        openAnimation.animate(1, 0.5, LocalEaseUtils.EaseType.EaseOutBack);

        // Reset any lingering animations in pages
        for (CategoryPage page : categoryPages.values()) {
            page.init();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // --- Global Transformations and Background ---
        drawDefaultBackground();

        float animProgress = (float) openAnimation.get();
        float scale = 0.95f + (0.05f * animProgress);

        GlStateManager.pushMatrix();
        // Translate to center for scaling, then translate to final position
        GlStateManager.translate(x + width / 2, y + height / 2, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-(x + width / 2), -(y + height / 2), 0);
        GlStateManager.color(1, 1, 1, animProgress);

        // --- Main GUI Body ---
        // Shadow
        LocalRenderUtils.drawShadow(x, y, width, height, 15);
        // Background
        LocalRenderUtils.drawRoundedRect(x, y, x + width, y + height, BORDER_RADIUS, BACKGROUND_COLOR.getRGB());

        // --- Navigation Rail ---
        drawNavRail(mouseX, mouseY);

        // --- Content Pane ---
        drawContentPane(mouseX, mouseY);

        // --- Ripples ---
        drawRipples();

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawNavRail(int mouseX, int mouseY) {
        float currentY = y + 12;
        // Draw category icons
        for (Map.Entry<Category, CategoryPage> entry : categoryPages.entrySet()) {
            Category cat = entry.getKey();
            float iconX = x + (navRailWidth / 2) - 12; // 24x24 icon
            float iconY = currentY;

            // Selection indicator
            if (selectedCategory == cat) {
                LocalRenderUtils.drawRoundedRect(x + 8, iconY - 8, x + 8 + (navRailWidth - 16), iconY - 8 + 40, BORDER_RADIUS, SURFACE_COLOR.getRGB());
            }

            // Draw icon
            GlStateManager.color(1, 1, 1, (float) openAnimation.get());
            if (categoryIcons.containsKey(cat)) {
                mc.getTextureManager().bindTexture(categoryIcons.get(cat));
                drawModalRectWithCustomSizedTexture((int) iconX, (int) iconY, 0, 0, 24, 24, 24, 24);
            } else { // Fallback to text if no icon
                String name = cat == null ? "Targets" : cat.getDisplayName();
                Fonts.font40.drawCenteredString(name.substring(0, 1), x + navRailWidth / 2, iconY + 8, -1);
            }

            currentY += 48; // Spacing
        }
    }

    private void drawContentPane(int mouseX, int mouseY) {
        // Header
        Gui.drawRect((int)contentX, (int)contentY, (int)(contentX + contentWidth), (int)(contentY + 40), SURFACE_COLOR.getRGB());
        String title = selectedCategory == null ? "Targets" : selectedCategory.getDisplayName();
        Fonts.font40.drawString(title, contentX + 16, contentY + 14, ON_SURFACE_COLOR.getRGB());

        // Stencil for scrolling content
        Stencil.write(true);
        Gui.drawRect((int)contentX, (int)(contentY + 40), (int)(contentX + contentWidth), (int)(contentY + 40 + (contentHeight - 40)), Color.WHITE.getRGB());
        Stencil.erase(true);

        GlStateManager.pushMatrix();
        categoryPages.get(selectedCategory).draw(mouseX, mouseY, contentX, contentY + 40, contentWidth, contentHeight - 40);
        GlStateManager.popMatrix();

        Stencil.dispose();
    }

    private void drawRipples() {
        ripples.removeIf(Ripple::isEnded);
        for (Ripple ripple : ripples) {
            ripple.draw();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;
        if (openAnimation.get() < 0.9) return; // Prevent clicks during animation

        // --- Nav Rail Clicks ---
        float currentY = y + 12;
        for (Map.Entry<Category, CategoryPage> entry : categoryPages.entrySet()) {
            if (LocalRenderUtils.isHovered(x, currentY - 8, navRailWidth, 40, mouseX, mouseY)) {
                selectedCategory = entry.getKey();
                ripples.add(new Ripple(mouseX, mouseY, Math.max(width, height)));
                return;
            }
            currentY += 48;
        }

        // --- Content Pane Clicks ---
        if (LocalRenderUtils.isHovered(contentX, contentY, contentWidth, contentHeight, mouseX, mouseY)) {
            ripples.add(new Ripple(mouseX, mouseY, Math.max(width, height)));
            categoryPages.get(selectedCategory).mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (openAnimation.get() < 0.9) return;
        categoryPages.get(selectedCategory).mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (openAnimation.get() < 0.9) return;
        categoryPages.get(selectedCategory).mouseClickMove(mouseX, mouseY, clickedMouseButton);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel != 0 && openAnimation.get() > 0.9) {
            categoryPages.get(selectedCategory).handleMouseWheel(wheel);
        }
    }

    @Override
    public void onGuiClosed() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // --- SELF-CONTAINED HELPER CLASSES ---

    /**
     * Re-implementation of RenderUtils for self-containment.
     */
    private static final class LocalRenderUtils {
        public static boolean isHovered(float x, float y, float width, float height, int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public static void drawShadow(float x, float y, float width, float height, int blurRadius) {
            GlStateManager.color(0, 0, 0, 1);
            float shadowAlpha = 15f / blurRadius;
            for (int i = 0; i < blurRadius; i++) {
                drawRoundedRect(x - i, y - i, x + width + i, y + height + i, BORDER_RADIUS, new Color(0, 0, 0, (int)shadowAlpha).getRGB());
            }
        }

        public static Color interpolateColor(Color color1, Color color2, float fraction) {
            fraction = Math.min(1, Math.max(0, fraction));
            int red = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
            int green = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
            int blue = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
            int alpha = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * fraction);
            return new Color(red, green, blue, alpha);
        }

        public static void drawRoundedRect(float x, float y, float x2, float y2, float radius, int color) {
            float width = x2 - x;
            float height = y2 - y;

            glPushAttrib(0);
            glScaled(0.5, 0.5, 0.5);
            x *= 2.0f; y *= 2.0f; width *= 2.0f; height *= 2.0f; radius *= 2.0f;

            glEnable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            setColor(color);

            glBegin(GL_POLYGON);
            // corners
            for (int i = 0; i <= 90; i += 3)
                glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
            for (int i = 90; i <= 180; i += 3)
                glVertex2d(x + radius + Math.sin(i * Math.PI / 180.0D) * radius * -1.0D, y + height - radius + Math.cos(i * Math.PI / 180.0D) * radius * -1.0D);
            for (int i = 0; i <= 90; i += 3)
                glVertex2d(x + width - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + height - radius + Math.cos(i * Math.PI / 180.0D) * radius);
            for (int i = 90; i <= 180; i += 3)
                glVertex2d(x + width - radius + Math.sin(i * Math.PI / 180.0D) * radius, y + radius + Math.cos(i * Math.PI / 180.0D) * radius);
            glEnd();

            glEnable(GL_TEXTURE_2D);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glScaled(2, 2, 2);
            glPopAttrib();
            GlStateManager.color(1, 1, 1, 1);
        }

        public static void drawFilledCircle(int x, int y, float radius, int color) {
            glPushAttrib(0);
            glScaled(0.5, 0.5, 0.5);
            x *= 2; y *= 2; radius *= 2;

            glEnable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            setColor(color);

            glBegin(GL_TRIANGLE_FAN);
            glVertex2d(x, y);
            for (int i = 0; i <= 360; i++) {
                glVertex2d(x + Math.sin(i * Math.PI / 180.0D) * radius, y + Math.cos(i * Math.PI / 180.0D) * radius);
            }
            glEnd();

            glEnable(GL_TEXTURE_2D);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glScaled(2, 2, 2);
            glPopAttrib();
        }

        private static void setColor(int color) {
            float a = (color >> 24 & 255) / 255.0F;
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            GlStateManager.color(r, g, b, a);
        }
    }

    /**
     * Re-implementation of Animation and EaseUtils for self-containment.
     */
    private static final class LocalAnimation {
        private double value;
        @Getter
        private double target;
        private double duration;
        private LocalEaseUtils.EaseType easeType;
        private boolean running = false;
        private double startValue;
        private long startTime;

        public double get() {
            if (running) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= duration) {
                    running = false;
                    value = target;
                } else {
                    double progress = (double) elapsed / duration;
                    value = startValue + (target - startValue) * LocalEaseUtils.ease(progress, easeType);
                }
            }
            return value;
        }

        public void setValue(double value) {
            this.value = value;
            this.target = value;
            this.running = false;
        }

        public void animate(double newTarget, double newDurationSeconds, LocalEaseUtils.EaseType newEaseType) {
            this.target = newTarget;
            this.duration = newDurationSeconds * 1000;
            this.easeType = newEaseType;
            this.running = true;
            this.startValue = this.value;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isDone() {
            return !running;
        }
    }

    private static final class LocalEaseUtils {
        public enum EaseType { EaseOutCubic, EaseOutBack }

        public static double ease(double progress, EaseType type) {
            switch(type) {
                case EaseOutBack: return easeOutBack(progress);
                case EaseOutCubic: default: return easeOutCubic(progress);
            }
        }

        private static double easeOutCubic(double x) { return 1 - Math.pow(1 - x, 3); }
        private static double easeOutBack(double x) {
            double c1 = 1.70158;
            double c3 = c1 + 1;
            return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
        }
    }

    // --- GUI COMPONENT CLASSES ---

    private static class CategoryPage {
        protected final Category category;
        protected final List<ModuleElement> moduleElements = new ArrayList<>();
        protected float scrollY = 0;
        protected float maxScroll = 0;
        private final LocalAnimation scrollAnimation = new LocalAnimation();

        public CategoryPage(Category category) {
            this.category = category;
            if (category != null) {
                List<Module> modules = LiquidBounce.INSTANCE.getModuleManager().getModuleByCategory(category);
                for (Module module : modules) {
                    moduleElements.add(new ModuleElement(module));
                }
            }
        }

        public void init() {
            scrollAnimation.setValue(0);
            for (ModuleElement element : moduleElements) {
                element.init();
            }
        }

        public void draw(int mouseX, int mouseY, float x, float y, float width, float height) {
            scrollY = (float) scrollAnimation.get();
            float elementY = y + scrollY + 16;
            maxScroll = 0;

            for (ModuleElement element : moduleElements) {
                element.update();
                element.draw(mouseX, mouseY, x + 16, elementY, width - 32);
                float elementHeight = element.getHeight();
                elementY += elementHeight + 8; // 8dp margin
                maxScroll += elementHeight + 8;
            }

            maxScroll = height - maxScroll - 16;
            if (maxScroll > 0) maxScroll = 0;
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            float elementY = y + (float)scrollAnimation.get() + 16;
            for (ModuleElement element : moduleElements) {
                element.mouseClicked(mouseX, mouseY, mouseButton, elementY);
                elementY += element.getHeight() + 8;
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            moduleElements.forEach(m -> m.mouseReleased(mouseX, mouseY, state));
        }

        public void mouseClickMove(int mouseX, int mouseY, int button) {
            moduleElements.forEach(m -> m.mouseClickMove(mouseX, mouseY, button));
        }

        public void handleMouseWheel(int wheel) {
            double targetScroll = scrollAnimation.target + wheel * 0.2;
            targetScroll = Math.max(maxScroll, Math.min(0, targetScroll));
            scrollAnimation.animate(targetScroll, 0.3, LocalEaseUtils.EaseType.EaseOutCubic);
        }
    }

    private static class TargetsPage extends CategoryPage {
        public TargetsPage() {
            super(null); // No real category
            moduleElements.clear(); // Clear modules, we use custom elements
            moduleElements.add(new ToggleElement("Players", EntityUtils.INSTANCE :: getTargetPlayer, EntityUtils.INSTANCE :: setTargetPlayer));
            moduleElements.add(new ToggleElement("Mobs", EntityUtils.INSTANCE :: getTargetMobs, EntityUtils.INSTANCE :: setTargetMobs));
            moduleElements.add(new ToggleElement("Animals", EntityUtils.INSTANCE :: getTargetAnimals, EntityUtils.INSTANCE :: setTargetAnimals));
            moduleElements.add(new ToggleElement("Invisible", EntityUtils.INSTANCE :: getTargetInvisible, EntityUtils.INSTANCE :: setTargetInvisible));
            moduleElements.add(new ToggleElement("Dead", EntityUtils.INSTANCE :: getTargetDead, EntityUtils.INSTANCE :: setTargetDead));
        }
    }

    private static class ToggleElement extends ModuleElement {
        private final String name;
        private final java.util.function.Supplier<Boolean> getter;
        private final java.util.function.Consumer<Boolean> setter;

        public ToggleElement(String name, java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
            super(null); // No module
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.stateAnimation.setValue(getter.get() ? 1 : 0);
        }

        @Override
        public void draw(int mouseX, int mouseY, float x, float y, float width) {
            this.y = y; this.x = x; this.width = width;
            LocalRenderUtils.drawRoundedRect(x, y, x + width, y + 48, BORDER_RADIUS, SURFACE_COLOR.getRGB());
            Fonts.font40.drawString(name, x + 12, y + 18, ON_SURFACE_COLOR.getRGB());
            drawSwitch(x + width - 40, y + 16, 32, 16);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton, float currentY) {
            this.y = currentY;
            if (LocalRenderUtils.isHovered(x, y, width, 48, mouseX, mouseY)) {
                boolean newState = !getter.get();
                setter.accept(newState);
                stateAnimation.animate(newState ? 1 : 0, 0.3, LocalEaseUtils.EaseType.EaseOutCubic);
            }
        }

        @Override public float getHeight() { return 48; }

        @Override
        public void update() {
            if ((stateAnimation.get() > 0.5) != getter.get()) {
                stateAnimation.animate(getter.get() ? 1 : 0, 0.3, LocalEaseUtils.EaseType.EaseOutCubic);
            }
        }
    }

    private static class ModuleElement {
        protected final Module module;
        protected List<ValueElement<?>> valueElements = new ArrayList<>();
        protected final LocalAnimation stateAnimation = new LocalAnimation();
        protected final LocalAnimation expandAnimation = new LocalAnimation();
        protected float x, y, width;
        private boolean expanded = false;

        public ModuleElement(Module module) {
            this.module = module;
            if (module != null) {
                this.stateAnimation.setValue(module.getState() ? 1 : 0);
                for (Value<?> value : module.getValues()) {
                    if (value instanceof BoolValue) {
                        valueElements.add(new BoolValueElement((BoolValue) value));
                    }
                    if (value instanceof FloatValue) {
                        valueElements.add(new FloatValueElement((FloatValue) value));
                    }
                    if (value instanceof IntegerValue) {
                        valueElements.add(new IntValueElement((IntegerValue) value));
                    }
                    if (value instanceof ListValue) {
                        valueElements.add(new ListValueElement((ListValue) value));
                    }
                }
            }
        }

        public void init() {
            expandAnimation.setValue(0);
            expanded = false;
        }

        public void update() {
            if (module == null) return;
            if ((stateAnimation.get() > 0.5) != module.getState()) {
                stateAnimation.animate(module.getState() ? 1 : 0, 0.3, LocalEaseUtils.EaseType.EaseOutCubic);
            }
        }

        public float getHeight() {
            float valueHeight = 0;
            for (ValueElement<?> v : valueElements) {
                valueHeight += v.getHeight();
            }
            return 48 + ((valueHeight + 8) * (float)expandAnimation.get());
        }

        public void draw(int mouseX, int mouseY, float x, float y, float width) {
            this.x = x; this.y = y; this.width = width;
            float height = getHeight();

            LocalRenderUtils.drawRoundedRect(x, y, x + width, y + height, BORDER_RADIUS, SURFACE_COLOR.getRGB());
            Fonts.font40.drawString(module.getName(), x + 12, y + 18, ON_SURFACE_COLOR.getRGB());
            drawSwitch(x + width - 40, y + 16, 32, 16);

            if (!valueElements.isEmpty()) {
                drawSettings(mouseX, mouseY, x, y, width);
            }
        }

        private void drawSettings(int mouseX, int mouseY, float x, float y, float width) {
            float settingsIconX = x + width - 64;
            float settingsIconY = y + 16;

            GlStateManager.pushMatrix();
            GlStateManager.translate(settingsIconX + 6, settingsIconY + 6, 0);
            GlStateManager.rotate((float) expandAnimation.get() * 180f, 0, 0, 1);
            GlStateManager.translate(-(settingsIconX + 6), -(settingsIconY + 6), 0);
            Fonts.font40.drawString("v", settingsIconX + 2, settingsIconY, ON_SURFACE_VARIANT_COLOR.getRGB());
            GlStateManager.popMatrix();

            if (expandAnimation.get() > 0.01) {
                float contentAlpha = (float) LocalEaseUtils.ease(expandAnimation.get(), LocalEaseUtils.EaseType.EaseOutCubic);
                GlStateManager.color(1, 1, 1, contentAlpha);

                Stencil.write(true);
                Gui.drawRect((int)x, (int)(y + 48), (int)(x + width), (int)(y + getHeight()), Color.WHITE.getRGB());
                Stencil.erase(true);

                float valueY = y + 48 + 8;
                for (ValueElement<?> element : valueElements) {
                    element.draw(mouseX, mouseY, x + 12, valueY, width - 24);
                    valueY += element.getHeight();
                }
                Stencil.dispose();
                GlStateManager.color(1, 1, 1, 1);
            }
        }

        protected void drawSwitch(float x, float y, float width, float height) {
            float switchProgress = (float) stateAnimation.get();
            Color trackColor = LocalRenderUtils.interpolateColor(ON_SURFACE_VARIANT_COLOR, ACCENT_COLOR, switchProgress);
            LocalRenderUtils.drawRoundedRect(x, y, x + width, y + height, height / 2, trackColor.getRGB());
            float thumbX = x + 2 + (width - height) * switchProgress;
            LocalRenderUtils.drawFilledCircle((int)(thumbX + (height-4)/2), (int)(y + height/2), (height-4)/2f, Color.WHITE.getRGB());
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton, float currentY) {
            this.y = currentY;
            if (!LocalRenderUtils.isHovered(x, y, width, getHeight(), mouseX, mouseY)) return;

            if (LocalRenderUtils.isHovered(x + width - 48, y, 48, 48, mouseX, mouseY)) {
                module.toggle();
                return;
            }
            if (!valueElements.isEmpty() && LocalRenderUtils.isHovered(x + width - 72, y, 24, 48, mouseX, mouseY)) {
                expanded = !expanded;
                expandAnimation.animate(expanded ? 1 : 0, 0.4, LocalEaseUtils.EaseType.EaseOutCubic);
                return;
            }
            if (expanded) {
                float valueY = y + 48 + 8;
                for(ValueElement<?> element : valueElements) {
                    if (mouseY > valueY && mouseY < valueY + element.getHeight()) {
                        element.mouseClicked(mouseX, mouseY, mouseButton);
                    }
                    valueY += element.getHeight();
                }
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            if (expanded) valueElements.forEach(v -> v.mouseReleased(mouseX, mouseY, state));
        }

        public void mouseClickMove(int mouseX, int mouseY, int button) {
            if (expanded) valueElements.forEach(v -> v.mouseClickMove(mouseX, mouseY, button));
        }
    }

    private abstract static class ValueElement<T extends Value<?>> {
        protected T value;
        protected float x, y, width;
        public ValueElement(T value) { this.value = value; }
        public abstract float getHeight();
        public abstract void draw(int mouseX, int mouseY, float x, float y, float width);
        public void mouseClicked(int mouseX, int mouseY, int button) {}
        public void mouseReleased(int mouseX, int mouseY, int state) {}
        public void mouseClickMove(int mouseX, int mouseY, int button) {}
    }

    private static class BoolValueElement extends ValueElement<BoolValue> {
        private final LocalAnimation stateAnimation = new LocalAnimation();
        public BoolValueElement(BoolValue value) {
            super(value);
            stateAnimation.setValue(value.get() ? 1 : 0);
        }

        @Override public float getHeight() { return 24; }

        @Override
        public void draw(int mouseX, int mouseY, float x, float y, float width) {
            this.x = x; this.y = y; this.width = width;
            if ((stateAnimation.get() > 0.5) != value.get())
                stateAnimation.animate(value.get() ? 1 : 0, 0.2, LocalEaseUtils.EaseType.EaseOutCubic);

            Fonts.font40.drawString(value.getName(), x, y + 6, ON_SURFACE_VARIANT_COLOR.getRGB());

            float switchX = x + width - 32;
            float switchY = y + 4;
            float switchProgress = (float) stateAnimation.get();
            Color trackColor = LocalRenderUtils.interpolateColor(ON_SURFACE_VARIANT_COLOR, ACCENT_COLOR, switchProgress);
            LocalRenderUtils.drawRoundedRect(switchX, switchY, switchX+28, switchY+14, 7, trackColor.getRGB());
            float thumbX = switchX + 1.5f + (13 * switchProgress);
            LocalRenderUtils.drawFilledCircle((int)(thumbX + 5.5f), (int)(switchY + 7), 5.5f, Color.WHITE.getRGB());
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int button) {
            if (LocalRenderUtils.isHovered(x, y, width, getHeight(), mouseX, mouseY)) {
                value.set(!value.get());
            }
        }
    }

    private abstract static class NumberValueElement<T extends Number, V extends Value<T>> extends ValueElement<V> {
        protected boolean dragging = false;

        public NumberValueElement(V value) { super(value); }
        @Override public float getHeight() { return 28; }

        @Override
        public void draw(int mouseX, int mouseY, float x, float y, float width) {
            this.x = x; this.y = y; this.width = width;
            float textY = y + 4;

            Fonts.font40.drawString(value.getName(), x, textY, ON_SURFACE_VARIANT_COLOR.getRGB());
            String valueStr = String.valueOf(value.get());
            Fonts.font40.drawString(valueStr, x + width - Fonts.font40.getStringWidth(valueStr), textY, ON_SURFACE_COLOR.getRGB());

            float sliderY = y + 18;
            float sliderWidth = width;
            float sliderHeight = 4;

            float min = getMin().floatValue();
            float max = getMax().floatValue();
            float current = value.get().floatValue();
            float progress = (current - min) / (max - min);

            LocalRenderUtils.drawRoundedRect(x, sliderY, x + sliderWidth, sliderY + sliderHeight, 2, BACKGROUND_COLOR.getRGB());
            LocalRenderUtils.drawRoundedRect(x, sliderY, x + (sliderWidth * progress), sliderY + sliderHeight, 2, ACCENT_COLOR.getRGB());
            LocalRenderUtils.drawFilledCircle((int)(x + sliderWidth * progress), (int)(sliderY + sliderHeight/2), 4, ACCENT_COLOR.getRGB());
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int button) {
            if (LocalRenderUtils.isHovered(x, y + 16, width, 8, mouseX, mouseY)) {
                dragging = true;
                updateValue(mouseX);
            }
        }

        @Override public void mouseReleased(int mouseX, int mouseY, int state) { dragging = false; }
        @Override public void mouseClickMove(int mouseX, int mouseY, int button) { if (dragging) updateValue(mouseX); }
        protected abstract T getMin();
        protected abstract T getMax();
        protected abstract void updateValue(float mouseX);
    }

    private static class FloatValueElement extends NumberValueElement<Float, FloatValue> {
        public FloatValueElement(FloatValue val) { super(val); }
        @Override protected Float getMin() { return value.getMinimum(); }
        @Override protected Float getMax() { return value.getMaximum(); }
        @Override protected void updateValue(float mouseX) {
            float progress = (mouseX - x) / width;
            float newValue = value.getMinimum() + (value.getMaximum() - value.getMinimum()) * progress;
            value.set(Math.max(value.getMinimum(), Math.min(value.getMaximum(), newValue)));
        }
    }

    private static class IntValueElement extends NumberValueElement<Integer, IntegerValue> {
        public IntValueElement(IntegerValue val) { super(val); }
        @Override protected Integer getMin() { return value.getMinimum(); }
        @Override protected Integer getMax() { return value.getMaximum(); }
        @Override protected void updateValue(float mouseX) {
            float progress = (mouseX - x) / width;
            int newValue = (int) (value.getMinimum() + (value.getMaximum() - value.getMinimum()) * progress);
            value.set(Math.max(value.getMinimum(), Math.min(value.getMaximum(), newValue)));
        }
    }

    private static class ListValueElement extends ValueElement<ListValue> {
        public ListValueElement(ListValue value) { super(value); }
        @Override public float getHeight() { return 24; }

        @Override
        public void draw(int mouseX, int mouseY, float x, float y, float width) {
            this.x = x; this.y = y; this.width = width;
            Fonts.font40.drawString(value.getName(), x, y + 6, ON_SURFACE_VARIANT_COLOR.getRGB());
            String valueStr = value.get();
            Fonts.font40.drawString("< " + valueStr + " >", x + width - Fonts.font40.getStringWidth("< " + valueStr + " >"), y + 6, ON_SURFACE_COLOR.getRGB());
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int button) {
            if (LocalRenderUtils.isHovered(x, y, width, getHeight(), mouseX, mouseY)) {
                value.cycle();
            }
        }
    }

    public static class Ripple {
        private final float x, y, maxRadius;
        private final LocalAnimation animation = new LocalAnimation();

        public Ripple(float x, float y, float maxRadius) {
            this.x = x; this.y = y; this.maxRadius = maxRadius;
            animation.animate(1, 0.6, LocalEaseUtils.EaseType.EaseOutCubic);
        }

        public void draw() {
            float progress = (float) animation.get();
            float radius = progress * maxRadius * 0.3f;
            float alpha = 1.0f - progress;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            LocalRenderUtils.drawFilledCircle((int)x, (int)y, radius, new Color(1f, 1f, 1f, alpha * 0.2f).getRGB());
            GlStateManager.disableBlend();
        }

        public boolean isEnded() { return animation.isDone(); }
    }
}
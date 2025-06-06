// Decompiled with: CFR 0.152
// Class Version: 8
package net.ccbluex.liquidbounce.utils.skid.betterchat;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.jhlabs.image.ImageMath.clamp;

@SideOnly(Side.CLIENT)
public class GuiBetterChat extends GuiNewChat {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.newArrayList();
    private final List<ChatLine> chatLines = Lists.newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.newArrayList();
    private int scrollPos;
    private boolean isScrolled;
    public static float percentComplete = 0.0f;
    public static int newLines;
    public static long prevMillis;
    public boolean configuring;

    public GuiBetterChat(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    public static void updatePercentage(long diff) {
        if (percentComplete < 1.0f) {
            percentComplete += 0.004f * (float) diff;
        }
        percentComplete = clamp(percentComplete, 0.0f, 1.0f);
    }

    @Override
    public void drawChat(int updateCounter) {
        if (this.configuring) {
            return;
        }
        if (prevMillis == -1L) {
            prevMillis = System.currentTimeMillis();
            return;
        }
        long current = System.currentTimeMillis();
        long diff = current - prevMillis;
        prevMillis = current;
        GuiBetterChat.updatePercentage(diff);
        float t = percentComplete;
        float percent = 1.0f - (t -= 1.0f) * t * t * t;
        percent = clamp(percent, 0.0f, 1.0f);
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int visibleLines = this.getLineCount();
            int totalLines = this.drawnChatLines.size();
            float opacity = this.mc.gameSettings.chatOpacity * 0.9f + 0.1f;
            if (totalLines > 0) {
                boolean chatOpen = false;
                if (this.getChatOpen()) {
                    chatOpen = true;
                }
                float chatScale = this.getChatScale();
                int chatWidth = (int) Math.floor(this.getChatWidth() / chatScale);
                GlStateManager.pushMatrix();
                if (!this.isScrolled) {
                    GlStateManager.translate(
                        2.0f,
                        8.0f + (9.0f - 9.0f * percent) * chatScale,
                        0.0f
                    );
                } else {
                    GlStateManager.translate(
                        2.0f,
                        8.0f,
                        0.0f
                    );
                }
                GlStateManager.scale(chatScale, chatScale, 1.0f);
                int renderedLines = 0;
                for (int i = 0; i + this.scrollPos < this.drawnChatLines.size() && i < visibleLines; ++i) {
                    int lineAge;
                    ChatLine chatline = this.drawnChatLines.get(i + this.scrollPos);
                    if (chatline == null || (lineAge = updateCounter - chatline.getUpdatedCounter()) >= 200 && !chatOpen) 
                        continue;
                    
                    double ageRatio = (double) lineAge / 200.0;
                    ageRatio = 1.0 - ageRatio;
                    ageRatio *= 10.0;
                    ageRatio = clamp((float) ageRatio, 0.0F, 1.0F);
                    ageRatio *= ageRatio;
                    int alpha = (int) (255.0 * ageRatio);
                    if (chatOpen) {
                        alpha = 255;
                    }
                    alpha = (int) ((float) alpha * opacity);
                    ++renderedLines;
                    if (alpha <= 3) continue;
                    int xOffset = 0;
                    int yPos = -i * 9;
//                    if (!BetterChat.getSettings().clear) {
//                        drawRect(-2, yPos - 9, xOffset + chatWidth + 4, yPos, (alpha / 2) << 24);
//                    }
                    String text = chatline.getChatComponent().getFormattedText();
                    GlStateManager.enableBlend();
                    if (i <= newLines) {
                        this.mc.fontRendererObj.drawStringWithShadow(
                            text, 0.0f, yPos - 8, 0xFFFFFF + ((int) ((float) alpha * percent) << 24)
                        );
                    } else {
                        this.mc.fontRendererObj.drawStringWithShadow(
                            text, xOffset, yPos - 8, 0xFFFFFF + (alpha << 24)
                        );
                    }
                    GlStateManager.disableBlend();
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                }
                if (chatOpen) {
                    int fontHeight = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0f, 0.0f, 0.0f);
                    int totalHeight = totalLines * fontHeight + totalLines;
                    int visibleHeight = renderedLines * fontHeight + renderedLines;
                    int scrollOffset = this.scrollPos * visibleHeight / totalLines;
                    int scrollbarHeight = visibleHeight * visibleHeight / totalHeight;
                    if (totalHeight != visibleHeight) {
                        int barAlpha = scrollOffset > 0 ? 170 : 96;
                        int barColor = this.isScrolled ? 0xCC3333 : 0x3333AA;
                        drawRect(0, -scrollOffset, 2, -scrollOffset - scrollbarHeight, barColor + (barAlpha << 24));
                        drawRect(2, -scrollOffset, 1, -scrollOffset - scrollbarHeight, 0xCCCCCC + (barAlpha << 24));
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void clearChatMessages() {
        this.drawnChatLines.clear();
        this.chatLines.clear();
        this.sentMessages.clear();
    }

    @Override
    public void printChatMessage(IChatComponent chatComponent) {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    @Override
    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
        percentComplete = 0.0f;
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
        LOGGER.info("[CHAT] {}", chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }
        int chatWidth = (int) Math.ceil(this.getChatWidth() / this.getChatScale());
        List<IChatComponent> components = GuiUtilRenderComponents.splitText(
            chatComponent, chatWidth, this.mc.fontRendererObj, false, false
        );
        boolean chatOpen = this.getChatOpen();
        newLines = components.size() - 1;
        for (IChatComponent component : components) {
            if (chatOpen && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }
            this.drawnChatLines.add(0, new ChatLine(updateCounter, component, chatLineId));
        }
        while (this.drawnChatLines.size() > 100) {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }
        if (!displayOnly) {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));
            while (this.chatLines.size() > 100) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    @Override
    public void refreshChat() {
        this.drawnChatLines.clear();
        this.resetScroll();
        for (int i = this.chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    @Override
    public List<String> getSentMessages() {
        return this.sentMessages;
    }

    @Override
    public void addToSentMessages(String message) {
        if (this.sentMessages.isEmpty() || !this.sentMessages.get(this.sentMessages.size() - 1).equals(message)) {
            this.sentMessages.add(message);
        }
    }

    @Override
    public void resetScroll() {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    @Override
    public void scroll(int amount) {
        this.scrollPos += amount;
        int maxLines = this.drawnChatLines.size();
        if (this.scrollPos > maxLines - this.getLineCount()) {
            this.scrollPos = maxLines - this.getLineCount();
        }
        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    @Override
    @Nullable
    public IChatComponent getChatComponent(int mouseX, int mouseY) {
        if (!this.getChatOpen()) {
            return null;
        }
        ScaledResolution scaledRes = new ScaledResolution(this.mc);
        int scaleFactor = scaledRes.getScaleFactor();
        float chatScale = this.getChatScale();
        int adjustedX = mouseX / scaleFactor - 2;
        int adjustedY = mouseY / scaleFactor - 40;
        adjustedX = (int) Math.floor(adjustedX / chatScale);
        adjustedY = (int) Math.floor(adjustedY / chatScale);
        if (adjustedX >= 0 && adjustedY >= 0) {
            int visibleLines = Math.min(this.getLineCount(), this.drawnChatLines.size());
            if (adjustedX <= Math.floor(this.getChatWidth() / chatScale) &&
                adjustedY < this.mc.fontRendererObj.FONT_HEIGHT * visibleLines + visibleLines) {
                int lineIndex = adjustedY / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;
                if (lineIndex >= 0 && lineIndex < this.drawnChatLines.size()) {
                    ChatLine chatline = this.drawnChatLines.get(lineIndex);
                    int textWidth = 0;
                    for (IChatComponent component : chatline.getChatComponent()) {
                        if (!(component instanceof ChatComponentText) ||
                            (textWidth += this.mc.fontRendererObj.getStringWidth(
                                GuiUtilRenderComponents.func_178909_a(
                                    ((ChatComponentText) component).getFormattedText(), false
                                )
                            )) <= adjustedX) {
                            continue;
                        }
                        return component;
                    }
                }
                return null;
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean getChatOpen() {
        return this.mc.currentScreen instanceof GuiChat;
    }

    @Override
    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = this.drawnChatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();
            if (chatline.getChatLineID() != id) continue;
            iterator.remove();
        }
        iterator = this.chatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();
            if (chatline.getChatLineID() != id) continue;
            iterator.remove();
            break;
        }
    }

    @Override
    public int getChatWidth() {
        return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    @Override
    public int getChatHeight() {
        return calculateChatboxHeight(
            this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused
        );
    }

    @Override
    public float getChatScale() {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float scale) {
        return (int) Math.floor(scale * 280.0f + 40.0f);
    }

    public static int calculateChatboxHeight(float scale) {
        return (int) Math.floor(scale * 160.0f + 20.0f);
    }

    @Override
    public int getLineCount() {
        return this.getChatHeight() / 9;
    }

    static {
        prevMillis = -1L;
    }
}
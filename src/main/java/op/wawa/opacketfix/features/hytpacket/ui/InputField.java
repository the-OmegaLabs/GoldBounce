package op.wawa.opacketfix.features.hytpacket.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import net.minecraft.client.renderer.IImageBuffer;

import java.awt.*;

import static net.minecraft.realms.RealmsMth.clamp;

public class InputField {
    public int x;
    public int y;

    /**
     * The width of this text field.
     */
    private final int width;
    private final int height;

    /**
     * Has the current text being edited on the text box.
     */
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;

    /**
     * If this value is true along with isEnabled, keyTyped will process the keys.
     */
    private boolean isFocused;

    /**
     * If this value is true along with isFocused, keyTyped will process the keys.
     */
    private boolean isEnabled = true;

    /**
     * The current character index that should be used as start of the rendered text.
     */
    private int lineScrollOffset;
    private int cursorPosition;

    /**
     * other selection position, maybe the same as the cursor
     */
    private int selectionEnd;

    /**
     * True if this textbox is visible
     */
    private boolean visible = true;

    public InputField(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Increments the cursor counter
     */
    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    /**
     * Sets the text of the textbox, and moves the cursor to the end.
     */
    public void setText(String textIn) {
        if (textIn.length() > this.maxStringLength) {
            this.text = textIn.substring(0, this.maxStringLength);
        } else {
            this.text = textIn;
        }
        this.setCursorPositionEnd();
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText() {
        return this.text;
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText() {
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        return this.text.substring(i, j);
    }

    /**
     * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
     */
    public void writeText(String textToWrite) {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        int k = this.maxStringLength - this.text.length() - (i - j);

        if (!this.text.isEmpty()) {
            s = s + this.text.substring(0, i);
        }

        int l;

        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        } else {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text.isEmpty() && j < this.text.length()) {
            s = s + this.text.substring(j);
        }

        this.text = s;
        this.moveCursorBy(i - this.selectionEnd + l);
    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
     * which case the selection is deleted instead.
     */
    public void deleteWords(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
     * in which case the selection is deleted instead.
     */
    public void deleteFromCursor(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";
                if (i >= 0) {
                    s = this.text.substring(0, i);
                }
                if (j < this.text.length()) {
                    s = s + this.text.substring(j);
                }
                this.text = s;
                if (flag) {
                    this.moveCursorBy(num);
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    public int getNthWordFromPos(int n, int pos) {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1) {
                    i = l;
                } else {
                    while (skipWs && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int num) {
        this.setCursorPosition(this.selectionEnd + num);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void setCursorPosition(int pos) {
        this.cursorPosition = pos;
        int i = this.text.length();
        this.cursorPosition = clamp(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    /**
     * Call this method from your GuiScreen to process the keys into the textbox
     */
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case 14:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;

                case 199:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;

                case 203:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;

                case 205:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;

                case 207:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;

                case 211:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    /**
     * Called when mouse is clicked, regardless as to whether it is over this button or not.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean flag = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
        this.setFocused(flag);
        if (this.isFocused && flag && mouseButton == 0) {
            int i = mouseX - this.x;
            i -= 4;
            String s = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(s, i).length() + this.lineScrollOffset);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Draws the text box
     */
    public void drawTextBox() {
        if (this.getVisible()) {
            Gui.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 102).getRGB());
            Gui.drawRect(x, y, (int) (x + 0.5D), y + height, new Color(44, 44, 44, 102).getRGB());
            Gui.drawRect((int) (x + 0.5D), (int) (y + height - 0.5D), (int) (x + width - 0.5D), y + height, new Color(44, 44, 44, 102).getRGB());
            Gui.drawRect((int) (x + width - 0.5D), y, x + width, y + height, new Color(44, 44, 44, 102).getRGB());
            Gui.drawRect((int) (x + 0.5D), y, (int) (x + width - 0.5D), (int) (y + 0.5), new Color(44, 44, 44, 102).getRGB());
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String s = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean showCursor = this.isFocused && this.cursorCounter / 10 % 2 == 0 && flag;
            int l = this.x + 4;
            int i1 = this.y + (this.height - 8) / 2;
            int j1 = l;

            if (k > s.length()) {
                k = s.length();
            }

            if (s.length() > 0) {
                String s1 = flag ? s.substring(0, j) : s;
                Minecraft.getMinecraft().fontRendererObj.drawString(s1, l, i1, new Color(216, 216, 216).getRGB());
                j1 += Minecraft.getMinecraft().fontRendererObj.getStringWidth(s1);
            }

            boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int cursorX = j1;

            if (!flag) {
                cursorX = j > 0 ? l + this.width : l;
            } else if (flag2) {
                cursorX = j1 - 1;
                j1 += 1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                Minecraft.getMinecraft().fontRendererObj.drawString(s.substring(j), j1, i1, new Color(216, 216, 216).getRGB());
            }

            if (showCursor) {
                if (flag2) {
                    Gui.drawRect(cursorX + 1, i1 - 1, cursorX + 2, i1 + 1 + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, -3092272);
                } else {
                    Minecraft.getMinecraft().fontRendererObj.drawString(this.text.isEmpty() ? "|" : "_", cursorX + 2, this.y + 5, new Color(216, 216, 216).getRGB());
                }
            }

            if (k != j) {
                int l1 = l + Minecraft.getMinecraft().fontRendererObj.getStringWidth(s.substring(0, k));
                //GuiSlot.drawSelectionBox(cursorX, i1 - 1, l1 - 1, i1 + 1 + Fonts.font35.FONT_HEIGHT);
            }
        }
    }

    /**
     * Draws the blue selection box.
     */
/*    private void drawSelectionBox(int x, int startY, int endX, int endY) {
        if (x < endX) {
            int i = x;
            x = endX;
            endX = i;
        }

        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }

        if (x > this.x + this.width) {
            x = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        IImageBuffer bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(x, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }*/

    /**
     * Sets the maximum length for the text in this text box. If the current text is longer than this length, the
     * current text will be trimmed.
     */
    public void setMaxStringLength(int length) {
        this.maxStringLength = length;

        if (this.text.length() > length) {
            this.text = this.text.substring(0, length);
        }
    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition() {
        return this.cursorPosition;
    }
    /**
     * Sets focus to this gui element
     */
    public void setFocused(boolean isFocusedIn) {
        if (isFocusedIn && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = isFocusedIn;

        if (Minecraft.getMinecraft().currentScreen != null) {
            //Minecraft.getMinecraft().currentScreen.setFocused(isFocusedIn);
        }
    }

    /**
     * Getter for the focused field
     */
    public boolean isFocused() {
        return this.isFocused;
    }

    /**
     * Sets whether this text box is enabled. Disabled text boxes cannot be typed in.
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * the side of the selection that is not the cursor, may be the same as the cursor
     */
    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    public int getWidth() {
        return this.width - 8;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
     * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setSelectionPos(int position) {
        int i = this.text.length();

        if (position > i) {
            position = i;
        }

        if (position < 0) {
            position = 0;
        }

        this.selectionEnd = position;

        if (this.lineScrollOffset > i) {
            this.lineScrollOffset = i;
        }

        int j = this.getWidth();
        String s = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
        int k = s.length() + this.lineScrollOffset;

        if (position == this.lineScrollOffset) {
            this.lineScrollOffset -= Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(this.text, j, true).length();
        }

        if (position > k) {
            this.lineScrollOffset += position - k;
        } else if (position <= this.lineScrollOffset) {
            this.lineScrollOffset -= this.lineScrollOffset - position;
        }

        this.lineScrollOffset = clamp(this.lineScrollOffset, 0, i);
    }

    /**
     * returns true if this textbox is visible
     */
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * Sets whether this textbox is visible
     */
    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }
}

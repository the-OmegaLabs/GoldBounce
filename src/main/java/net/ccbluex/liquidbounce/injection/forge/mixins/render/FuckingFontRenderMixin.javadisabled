package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import me.gb2022.quantum3d.vertex.*;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.*;


@Mixin(FontRenderer.class)
public class FuckingFontRenderMixin {

    @Shadow
    private boolean randomStyle;
    @Shadow
    private boolean boldStyle;
    @Shadow
    private boolean italicStyle;
    @Shadow
    private boolean underlineStyle;
    @Shadow
    private boolean strikethroughStyle;

    /**
     * @author GB2022
     * @reason load custom TTF
     */
    @Overwrite
    public void onResourceManagerReload(IResourceManager p_onResourceManagerReload_1_) {

    }

    /**
     * @author GB2022
     * @reason load custom TTF
     */
    @Overwrite
    public float renderUnicodeChar(char p_renderUnicodeChar_1_, boolean p_renderUnicodeChar_2_) {
        if (this.glyphWidth[p_renderUnicodeChar_1_] == 0) {
            return 0.0F;
        } else {
            int i = p_renderUnicodeChar_1_ / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[p_renderUnicodeChar_1_] >>> 4;
            int k = this.glyphWidth[p_renderUnicodeChar_1_] & 15;
            float f = (float) j;
            float f1 = (float) (k + 1);
            float f2 = (float) (p_renderUnicodeChar_1_ % 16 * 16) + f;
            float f3 = (float) ((p_renderUnicodeChar_1_ & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = p_renderUnicodeChar_2_ ? 1.0F : 0.0F;
            GL11.glBegin(5);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, 0.0F);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    /**
     * @author GB2022
     * @reason load custom TTF
     */
    @Overwrite
    public int drawString(String p_drawString_1_, float p_drawString_2_, float p_drawString_3_, int p_drawString_4_, boolean p_drawString_5_) {
        GL11.glEnable(GL11.GL_ALPHA_TEST_REF);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
        this.resetStyles();
        int i;
        if (p_drawString_5_) {
            i = this.renderString(p_drawString_1_, p_drawString_2_ + 1.0F, p_drawString_3_ + 1.0F, p_drawString_4_, true);
            i = Math.max(i, this.renderString(p_drawString_1_, p_drawString_2_, p_drawString_3_, p_drawString_4_, false));
        } else {
            i = this.renderString(p_drawString_1_, p_drawString_2_, p_drawString_3_, p_drawString_4_, false);
        }

        return i;
    }

    @Shadow
    private int renderString(String p_renderString_1_, float p_renderString_2_, float p_renderString_3_, int p_renderString_4_, boolean p_renderString_5_) {
        return 0;
    }

    @Overwrite
    private void renderStringAtPos(String p_renderStringAtPos_1_, boolean p_renderStringAtPos_2_) {



        for (int i = 0; i < p_renderStringAtPos_1_.length(); ++i) {
            char c0 = p_renderStringAtPos_1_.charAt(i);
            if (c0 == 167 && i + 1 < p_renderStringAtPos_1_.length()) {
                int i1 = "0123456789abcdefklmnor".indexOf(p_renderStringAtPos_1_.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                if (i1 < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (i1 < 0 || i1 > 15) {
                        i1 = 15;
                    }

                    if (p_renderStringAtPos_2_) {
                        i1 += 16;
                    }

                    int j1 = this.colorCode[i1];
                    this.textColor = j1;
                    this.setColor((float) (j1 >> 16) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, this.alpha);
                } else if (i1 == 16) {
                    this.randomStyle = true;
                } else if (i1 == 17) {
                    this.boldStyle = true;
                } else if (i1 == 18) {
                    this.strikethroughStyle = true;
                } else if (i1 == 19) {
                    this.underlineStyle = true;
                } else if (i1 == 20) {
                    this.italicStyle = true;
                } else if (i1 == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    this.setColor(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            } else {
                int j = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(c0);
                if (this.randomStyle && j != -1) {
                    int k = this.getCharWidth(c0);

                    char c1;
                    do {
                        j = this.fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
                        c1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(j);
                    } while (k != this.getCharWidth(c1));

                    c0 = c1;
                }

                float f1 = j != -1 && !this.unicodeFlag ? 1.0F : 0.5F;
                boolean flag = (c0 == 0 || j == -1 || this.unicodeFlag) && p_renderStringAtPos_2_;
                if (flag) {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderChar(c0, this.italicStyle);
                if (flag) {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle) {
                    this.posX += f1;
                    if (flag) {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderChar(c0, this.italicStyle);
                    this.posX -= f1;
                    if (flag) {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    ++f;
                }

                this.doDraw(f);
            }
        }

    }

    @Shadow
    private void resetStyles() {
    }


    public int getStringWidth(String p_getStringWidth_1_) {

    }


    public static final int RESOLUTION_SCALE = 2;
    private static final VertexBuilder BUILDER = createSharedBuilder();

    private final HashMap<TextComponent, CompiledComponent> compiled = new HashMap<>();
    private Font fontFamily = new Font("System", Font.PLAIN, 12);

    @Overwrite
    public int getStringWidth(String text) {
        CompiledComponent compiled = this.getCompiled(text);
        return (int) ((double) compiled.width();
    }

    public void gc() {
        if (this.compiled.size() > 114514) {
            Set<CompiledComponent> components = new HashSet<>(this.compiled.values());
            this.compiled.clear();
            components.forEach(CompiledComponent::destroy);
            components.clear();
        }
    }

    public Font getFontFamily() {
        return this.fontFamily;
    }

    public void setFontFamily(Font fontFamily) {
        this.fontFamily = fontFamily;
    }


    @Override
    public void render(TextComponent text, int x, int y, double z, FontAlignment alignment) {
        if (text == null || text.isEmpty()) {
            return;
        }
        TextComponent start = text.getFirst();

        int width = this._width(start, 0);

        var scaleMod = ClientSettings.UISetting.getGUIScaleMod();

        int startX = -1;
        switch (alignment) {
            case LEFT -> startX = x;
            case MIDDLE -> startX = (int) (x - (float) width / 2 * scaleMod);
            case RIGHT -> startX = (int) (x - width * scaleMod);
        }

        _renderComponent(start, startX, y);
    }

    private int _width(TextComponent text, int current) {
        CompiledComponent compiled = this.getCompiled(text);

        current += (int) ((double) compiled.width() / RESOLUTION_SCALE / ClientSettings.UISetting.getGUIScaleMod());

        if (text.getNext() == null) {
            return current;
        }

        return _width(text.getNext(), current);
    }

    private CompiledComponent getCompiled(TextComponent text) {
        if (!this.compiled.containsKey(text)) {
            this.compiled.put(text, CompiledComponent.generate(this.fontFamily, text));
        }

        return this.compiled.get(text);
    }

    private void _renderComponent(TextComponent text, int x, int y) {
        CompiledComponent compiled = this.getCompiled(text);
        compiled.render(x, y);

        if (text.getNext() == null) {
            return;
        }

        _renderComponent(
                text.getNext(),
                (int) (x + (double) compiled.width() / RESOLUTION_SCALE * ClientSettings.UISetting.getGUIScaleMod()),
                y
        );
    }
}

public final class CompiledComponent {
    public static final ByteBuffer SHARED_MEMORY = BufferUtils.createByteBuffer(131072);

    private static final BufferedImage V_HOLDER = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private int handle;
    private final int width;
    private final int height;

    private CompiledComponent(int handle, int width, int height) {
        this.handle = handle;
        this.width = width;
        this.height = height;
    }

    public CompiledComponent() {
        this(0, 1, 1);
    }

    static CompiledComponent generate(Font fontFamily, int color, String text, float size, boolean bold, boolean italic, boolean underline, boolean delete) {
        Graphics2D v_graphics = V_HOLDER.createGraphics();
        Font font = createStyledFont(fontFamily.deriveFont(size), bold, italic, underline, delete);

        v_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        v_graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        v_graphics.setFont(font);

        FontMetrics fm = v_graphics.getFontMetrics(font);
        int width = fm.stringWidth(text);
        int height = fm.getHeight();

        if (width == 0 || height == 0) {
            return new CompiledComponent();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g.setFont(font);
        g.setColor(new Color(color));
        g.drawString(text, 0, size);

        int tex = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);


        int tw = image.getWidth();
        int th = image.getHeight();

        ByteBuffer buffer = ImageUtil.getByteFromBufferedImage_RGBA(image, SHARED_MEMORY);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        return new CompiledComponent(tex, tw, th);
    }

    public static Font createStyledFont(Font baseFont, boolean bold, boolean italic, boolean underline, boolean strikethrough) {
        Map<TextAttribute, Object> attributes = new HashMap<>(baseFont.getAttributes());

        // 设置粗体和斜体
        int style = Font.PLAIN;
        if (bold) {
            style |= Font.BOLD;
        }
        if (italic) {
            style |= Font.ITALIC;
        }
        baseFont = baseFont.deriveFont(style);

        // 设置下划线
        if (underline) {
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }

        // 设置删除线
        if (strikethrough) {
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }

        // 创建并返回新的字体
        return baseFont.deriveFont(attributes);
    }

    public void destroy() {
        if (this.handle == 0) {
            return;
        }

        GL11.glDeleteTextures(this.handle);
        this.handle = 0;
    }

    public int width() {
        return this.texture == null ? 0 : this.texture.getWidth();
    }

    public int height() {
        return this.texture == null ? 0 : this.texture.getHeight();
    }

    private void render(float baseX, float baseY) {
        if (this.texture == null) {
            return;
        }

        BUILDER.reset();
        BUILDER.setColor(1, 1, 1, 1f);

        float x1 = baseX + (float) this.texture.getWidth() / RESOLUTION_SCALE;
        float y1 = baseY + (float) this.texture.getHeight() / RESOLUTION_SCALE;

        ShapeRenderer.drawRectUV(BUILDER, baseX, x1, baseY, y1, 0, 0, 1, 0, 1);


        this.texture.bind();
        GLUtil.enableBlend();

        VertexBuilderUploader.uploadPointer(BUILDER);
        this.texture.unbind();
    }
}

interface ImageUtil {
    static ByteBuffer getByteFromBufferedImage_RGBA(BufferedImage img, ByteBuffer pixels) {
        pixels.put(unpackIntAsRGBAByte(readImageAsRawInt(img)));
        pixels.position(0);
        return pixels;
    }

    static int[] readImageAsRawInt(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] rawPixels = new int[w * h];
        img.getRGB(0, 0, w, h, rawPixels, 0, w);
        return rawPixels;
    }

    static byte[] unpackIntAsRGBAByte(int[] rawPixels) {
        byte[] newPixels1 = new byte[rawPixels.length * 4];
        for (int i = 0; i < rawPixels.length; ++i) {
            int a = rawPixels[i] >> 24 & 0xFF;
            int r = rawPixels[i] >> 16 & 0xFF;
            int g = rawPixels[i] >> 8 & 0xFF;
            int b = rawPixels[i] & 0xFF;
            newPixels1[i * 4] = (byte) r;
            newPixels1[i * 4 + 1] = (byte) g;
            newPixels1[i * 4 + 2] = (byte) b;
            newPixels1[i * 4 + 3] = (byte) a;
        }
        return newPixels1;
    }
}
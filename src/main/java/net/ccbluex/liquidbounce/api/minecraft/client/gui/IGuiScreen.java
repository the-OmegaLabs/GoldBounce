package net.ccbluex.liquidbounce.api.minecraft.client.gui;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.client.gui.inventory.IGuiChest;
import net.ccbluex.liquidbounce.api.minecraft.client.gui.inventory.IGuiContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* compiled from: IGuiScreen.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\f\n\u0002\b\u0006\bf\u0018��2\u00020\u0001J\b\u0010\u0011\u001a\u00020\u0012H&J\b\u0010\u0013\u001a\u00020\u0014H&J\b\u0010\u0015\u001a\u00020\u0016H&J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\fH&J\b\u0010\u001a\u001a\u00020\u0018H&J \u0010\u001b\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u001fH&J\b\u0010 \u001a\u00020\u0018H&J\u0018\u0010!\u001a\u00020\u00182\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\fH&J \u0010%\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\f2\u0006\u0010&\u001a\u00020\fH&J \u0010'\u001a\u00020\u00182\u0006\u0010\u001c\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\f2\u0006\u0010(\u001a\u00020\fH&R\u0018\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0012\u0010\u0007\u001a\u00020\bX¦\u0004¢\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0012\u0010\u000b\u001a\u00020\fX¦\u0004¢\u0006\u0006\u001a\u0004\b\r\u0010\u000eR\u0012\u0010\u000f\u001a\u00020\fX¦\u0004¢\u0006\u0006\u001a\u0004\b\u0010\u0010\u000e¨\u0006)"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiScreen;", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGui;", "buttonList", "", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiButton;", "getButtonList", "()Ljava/util/List;", "fontRendererObj", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IFontRenderer;", "getFontRendererObj", "()Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IFontRenderer;", "height", "", "getHeight", "()I", "width", "getWidth", "asGuiChest", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiChest;", "asGuiContainer", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiContainer;", "asGuiGameOver", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiGameOver;", "drawBackground", "", "i", "drawDefaultBackground", "superDrawScreen", "mouseX", "mouseY", "partialTicks", "", "superHandleMouseInput", "superKeyTyped", "typedChar", "", "keyCode", "superMouseClicked", "mouseButton", "superMouseReleased", "state", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiScreen.class */
public interface IGuiScreen extends IGui {
    int getWidth();

    int getHeight();

    @NotNull
    IFontRenderer getFontRendererObj();

    @NotNull
    List<IGuiButton> getButtonList();

    @NotNull
    IGuiContainer asGuiContainer();

    @NotNull
    IGuiGameOver asGuiGameOver();

    @NotNull
    IGuiChest asGuiChest();

    void superMouseReleased(int i, int i2, int i3);

    void drawBackground(int i);

    void drawDefaultBackground();

    void superKeyTyped(char c, int i);

    void superHandleMouseInput();

    void superMouseClicked(int i, int i2, int i3);

    void superDrawScreen(int i, int i2, float f);
}

package net.ccbluex.liquidbounce.api.minecraft.client.gui;

import jdk.nashorn.internal.runtime.PropertyDescriptor;
import kotlin.Metadata;

/* compiled from: IGuiSlot.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\f\bf\u0018��2\u00020\u0001J \u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\rH&J(\u0010\u000e\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u0003H&J\b\u0010\u0014\u001a\u00020\tH&J\u0018\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\u00032\u0006\u0010\u0017\u001a\u00020\u0003H&J\u0010\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0019\u001a\u00020\u0003H&J\u0010\u0010\u001a\u001a\u00020\t2\u0006\u0010\u001b\u001a\u00020\u0011H&J\u0010\u0010\u001c\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0003H&R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\u0005¨\u0006\u001d"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiSlot;", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGui;", "slotHeight", "", "getSlotHeight", "()I", "width", "getWidth", "drawScreen", "", "mouseX", "mouseY", "partialTicks", "", "elementClicked", "index", "doubleClick", "", "var3", "var4", "handleMouseInput", "registerScrollButtons", "down", "up", "scrollBy", PropertyDescriptor.VALUE, "setEnableScissor", "flag", "setListWidth", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiSlot.class */
public interface IGuiSlot extends IGui {
    int getWidth();

    int getSlotHeight();

    void scrollBy(int i);

    void registerScrollButtons(int i, int i2);

    void drawScreen(int i, int i2, float f);

    void elementClicked(int i, boolean z, int i2, int i3);

    void handleMouseInput();

    void setListWidth(int i);

    void setEnableScissor(boolean z);
}

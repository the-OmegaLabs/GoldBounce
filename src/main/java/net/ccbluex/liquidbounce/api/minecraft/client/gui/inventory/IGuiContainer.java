package net.ccbluex.liquidbounce.api.minecraft.client.gui.inventory;

import kotlin.Metadata;
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiScreen;
import net.ccbluex.liquidbounce.api.minecraft.inventory.IContainer;
import net.ccbluex.liquidbounce.api.minecraft.inventory.ISlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: IGuiContainer.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n��\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n\u0002\b\u0003\bf\u0018��2\u00020\u0001J(\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000b2\u0006\u0010\r\u001a\u00020\u000bH&R\u0014\u0010\u0002\u001a\u0004\u0018\u00010\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005¨\u0006\u000e"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiContainer;", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/IGuiScreen;", "inventorySlots", "Lnet/ccbluex/liquidbounce/api/minecraft/inventory/IContainer;", "getInventorySlots", "()Lnet/ccbluex/liquidbounce/api/minecraft/inventory/IContainer;", "handleMouseClick", "", "slot", "Lnet/ccbluex/liquidbounce/api/minecraft/inventory/ISlot;", "slotNumber", "", "clickedButton", "clickType", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiContainer.class */
public interface IGuiContainer extends IGuiScreen {
    void handleMouseClick(@NotNull ISlot iSlot, int i, int i2, int i3);

    @Nullable
    IContainer getInventorySlots();
}

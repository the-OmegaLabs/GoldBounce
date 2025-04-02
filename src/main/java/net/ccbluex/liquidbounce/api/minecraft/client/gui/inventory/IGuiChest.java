package net.ccbluex.liquidbounce.api.minecraft.client.gui.inventory;

import kotlin.Metadata;
import org.jetbrains.annotations.Nullable;

/* compiled from: IGuiChest.kt */
@Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"��\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n��\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018��2\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X¦\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0014\u0010\u0006\u001a\u0004\u0018\u00010\u0007X¦\u0004¢\u0006\u0006\u001a\u0004\b\b\u0010\t¨\u0006\n"}, d2 = {"Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiChest;", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiContainer;", "inventoryRows", "", "getInventoryRows", "()I", "lowerChestInventory", "Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IIInventory;", "getLowerChestInventory", "()Lnet/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IIInventory;", "Pride"})
/* loaded from: Ordinary２－９ｃｒｋ.jar:net/ccbluex/liquidbounce/api/minecraft/client/gui/inventory/IGuiChest.class */
public interface IGuiChest extends IGuiContainer {
    int getInventoryRows();

    @Nullable
    IIInventory getLowerChestInventory();
}

/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam;
import net.ccbluex.liquidbounce.features.module.modules.render.NoSwing;
import net.ccbluex.liquidbounce.features.module.modules.render.SilentHotbarModule;
import net.ccbluex.liquidbounce.utils.SilentHotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemRenderer.class, priority = 1337)
public class MixinItemRenderer {
    @Shadow
    private Minecraft mc;
    @Shadow
    private ItemStack itemToRender;
    @Shadow
    private int equippedItemSlot;
    @Shadow
    private float equippedProgress;
    @Shadow
    private float prevEquippedProgress;

    @Redirect(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isEntityInsideOpaqueBlock()Z"))
    private boolean injectFreeCam(EntityPlayerSP instance) {
        return ! FreeCam.INSTANCE.handleEvents() && instance.isEntityInsideOpaqueBlock();
    }

    /**
     * @author bzym2
     * @reason cancel the swing animation
     */
    @Overwrite
    public void updateEquippedItem() {
        EntityPlayer entityplayer = this.mc.thePlayer;
        SilentHotbarModule module = SilentHotbarModule.INSTANCE;
        int slot = SilentHotbar.INSTANCE.renderSlot(module.handleEvents() && module.getKeepItemInHandInFirstPerson());
        ItemStack itemstack = entityplayer.inventory.getStackInSlot(slot);

        if (NoSwing.INSTANCE.handleEvents()) {
            this.equippedProgress = 1.0F;

            if (this.itemToRender == null && itemstack != null) {
                this.itemToRender = itemstack;
                this.equippedItemSlot = slot;
            } else if (this.itemToRender != null && itemstack == null) {
                this.itemToRender = null;
                this.equippedItemSlot = slot;
            } else if (this.itemToRender != null && itemstack != null) {
                if (! this.itemToRender.getIsItemStackEqual(itemstack)) {
                    this.itemToRender = itemstack;
                    this.equippedItemSlot = slot;
                }
            }
        } else {
            this.prevEquippedProgress = this.equippedProgress;
            boolean flag = false;
            if (this.itemToRender != null && itemstack != null) {
                if (! this.itemToRender.getIsItemStackEqual(itemstack)) {
                    if (! this.itemToRender.getItem().shouldCauseReequipAnimation(this.itemToRender, itemstack, this.equippedItemSlot != slot)) {
                        this.itemToRender = itemstack;
                        this.equippedItemSlot = slot;
                        return;
                    }

                    flag = true;
                }
            } else
                flag = this.itemToRender != null || itemstack != null;

            float f = 0.4F;
            float f1 = flag ? 0.0F : 1.0F;
            float f2 = MathHelper.clamp_float(f1 - this.equippedProgress, - f, f);
            this.equippedProgress += f2;
            if (this.equippedProgress < 0.1F) {
                this.itemToRender = itemstack;
                this.equippedItemSlot = slot;
            }
        }
    }
}

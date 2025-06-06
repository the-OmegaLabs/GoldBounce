package net.ccbluex.liquidbounce.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.*;
import net.minecraft.entity.*;
import net.minecraft.potion.*;

public class BlockAndBuild
{
    Minecraft mc;
    
    public BlockAndBuild(final Minecraft mc) {
        this.mc = mc;
    }
    
    public void blockbuild() {
        if (this.mc.thePlayer.getItemInUseCount() != 0 && this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.gameSettings.keyBindUseItem.isKeyDown() && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            this.swingItem((EntityLivingBase)this.mc.thePlayer);
        }
    }
    
    public void swingItem(final EntityLivingBase e) {
        if (!e.isSwingInProgress || e.swingProgressInt >= this.getArmSwingAnimationEnd(e) / 2 || e.swingProgressInt < 0) {
            e.swingProgressInt = -1;
            e.isSwingInProgress = true;
        }
    }
    
    private int getArmSwingAnimationEnd(final EntityLivingBase e) {
        return e.isPotionActive(Potion.digSpeed) ? (6 - (1 + e.getActivePotionEffect(Potion.digSpeed).getAmplifier())) : (e.isPotionActive(Potion.digSlowdown) ? (6 + (1 + e.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2) : 6);
    }
}

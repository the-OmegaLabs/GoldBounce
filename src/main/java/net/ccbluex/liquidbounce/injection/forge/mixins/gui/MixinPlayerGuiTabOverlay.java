package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.utils.CustomUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static net.ccbluex.liquidbounce.features.module.modules.misc.IRC.nameMap;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinPlayerGuiTabOverlay extends MixinGui {

//    @Inject(method = "renderPlayerlist", at = @At("HEAD"), remap = true)
//    private void updatePlayerInfo(int p_renderPlayerlist_1_, Scoreboard p_renderPlayerlist_2_, ScoreObjective p_renderPlayerlist_3_, CallbackInfo ci) {
//        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
//
//        for (NetworkPlayerInfo info : netHandler.getPlayerInfoMap()) {
//            String uuid = info.getGameProfile().getId().toString();
//            if (nameMap.containsKey(uuid)) {
//                @NotNull String pair = String.valueOf(nameMap.get(uuid));
//            }
//        }
//    }

}

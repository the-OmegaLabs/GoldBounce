/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.packetfix.mixins;

import java.io.IOException;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.IMerchant;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.village.MerchantRecipeList;
import net.ccbluex.liquidbounce.packetfix.features.hytpacket.CustomPacket;
import net.ccbluex.liquidbounce.packetfix.features.hytpacket.PacketManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {

    @Shadow
    @Final
    private static Logger logger;

    @Shadow
    private Minecraft gameController;

    @Overwrite
    public void handleCustomPayload(S3FPacketCustomPayload p_handleCustomPayload_1_) {
        // Minecraft Code
        PacketThreadUtil.checkThreadAndEnqueue(p_handleCustomPayload_1_, this, this.gameController);

        // HuaYuTing Packet
        for (CustomPacket packet : new PacketManager().packets) {
            if (Objects.equals(packet.getChannel(), p_handleCustomPayload_1_.getChannelName())) {
                packet.process(p_handleCustomPayload_1_.getBufferData());
                return;
            }
        }

        // Minecraft Code
        if ("MC|TrList".equals(p_handleCustomPayload_1_.getChannelName())) {
            PacketBuffer packetbuffer = p_handleCustomPayload_1_.getBufferData();

            try {
                int i = packetbuffer.readInt();
                GuiScreen guiscreen = this.gameController.currentScreen;
                if (guiscreen instanceof GuiMerchant && i == this.gameController.thePlayer.openContainer.windowId) {
                    IMerchant imerchant = ((GuiMerchant)guiscreen).getMerchant();
                    MerchantRecipeList merchantrecipelist = MerchantRecipeList.readFromBuf(packetbuffer);
                    imerchant.setRecipes(merchantrecipelist);
                }
            } catch (IOException var10) {
                logger.error("Couldn't load trade info", var10);
            } finally {
                packetbuffer.release();
            }
        } else if ("MC|Brand".equals(p_handleCustomPayload_1_.getChannelName())) {
            this.gameController.thePlayer.setClientBrand(p_handleCustomPayload_1_.getBufferData().readStringFromBuffer(32767));
        } else if ("MC|BOpen".equals(p_handleCustomPayload_1_.getChannelName())) {
            ItemStack itemstack = this.gameController.thePlayer.getCurrentEquippedItem();
            if (itemstack != null && itemstack.getItem() == Items.written_book) {
                this.gameController.displayGuiScreen(new GuiScreenBook(this.gameController.thePlayer, itemstack, false));
            }
        }

    }
}

package net.ccbluex.liquidbounce.utils.packet

import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos

object sendOffHandUseItem {

    val mc = Minecraft.getMinecraft()

    fun sendOffHandUseItem() {
        mc.netHandler.addToSendQueue(
            C08PacketPlayerBlockPlacement(BlockPos(-1, -2, -1), 255, null, 0.0f, 0.0f, 0.0f)
        )
    }
}
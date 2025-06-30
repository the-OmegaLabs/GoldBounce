/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.floatValue
import net.minecraft.network.play.server.S3FPacketCustomPayload

object AntiBlind : Module("AntiBlind", Category.RENDER, gameDetecting = false, hideModule = false) {
    val confusionEffect by _boolean("Confusion", true)
    val pumpkinEffect by _boolean("Pumpkin", true)
    val fireEffect by floatValue("FireAlpha", 0.3f, 0f..1f)
    val bossHealth by _boolean("BossHealth", true)
    private val bookPage by _boolean("BookPage", true)
    val achievements by _boolean("Achievements", true)
    val scoreboard by _boolean("Scoreboard", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (!bookPage) return

        val packet = event.packet
        if (packet is S3FPacketCustomPayload && packet.channelName == "MC|BOpen") event.cancelEvent()
    }
}
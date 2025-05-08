/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.special

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload

object ClientFixes : MinecraftInstance(), Listenable {

    var fmlFixesEnabled = true

    var blockFML = true

    var blockProxyPacket = true

    var blockPayloadPackets = true

    var blockResourcePackExploit = true

    var clientBrand = "Vanilla"

    var possibleBrands = arrayOf(
        "Vanilla",
        "Forge",
        "LunarClient",
        "CheatBreaker",
        "Geyser",
        "LabyMod"
    )

    @EventTarget
    fun onPacket(event: PacketEvent) = runCatching {
        val packet = event.packet

        if (mc.isIntegratedServerRunning || !fmlFixesEnabled) {
            return@runCatching
        }

        when {
            blockProxyPacket && packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket" -> {
                event.cancelEvent()
                return@runCatching
            }

            packet is C17PacketCustomPayload -> {
                if (blockPayloadPackets && !packet.channelName.startsWith("MC|")) {
                    event.cancelEvent()
                } else if (packet.channelName == "MC|Brand") {
                    packet.data = PacketBuffer(Unpooled.buffer()).writeString(
                        when (clientBrand) {
                            "Vanilla" -> "vanilla"
                            "LunarClient" -> "lunarclient:v2.18.2-2452"
                            "CheatBreaker" -> "CB"
                            "Geyser" -> "geyser"
                            "LabyMod" -> "labymod"
                            else -> {
                                // do nothing
                                return@runCatching
                            }
                        }
                    )
                }
            }
        }
    }.onFailure {
        LOGGER.error("Failed to handle packet on client fixes.", it)
    }

    

}
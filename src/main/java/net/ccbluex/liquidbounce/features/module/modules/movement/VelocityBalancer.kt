/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement // 或者 .movement

import com.google.common.collect.Queues
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.server.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

object VelocityBalancer : Module("VelocityBalancer", Category.MOVEMENT) {

    private val packetDelay: IntegerValue = IntegerValue("PacketDelayTicks", 2, 0..200)

    private val delayedPacketQueue: ConcurrentLinkedQueue<PacketSnapshot> = Queues.newConcurrentLinkedQueue()

    private data class PacketSnapshot(val packet: Packet<*>, val time: Long)

    override fun onEnable() {
        clear(false)
    }

    override fun onDisable() {
        clear(true)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // 切换世界时，安全地清空队列
        clear(true)
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        val centerX = sr.scaledWidth / 2
        val centerY = sr.scaledHeight / 2

        val delayTicks = packetDelay.get()
        val delayMillis = delayTicks * 50L

        val now = System.currentTimeMillis()

        if (delayedPacketQueue.isEmpty()) return

        val firstPacketTime = delayedPacketQueue.peek()?.time ?: return
        val remainingMillis = delayMillis - (now - firstPacketTime)
        val remainingTicks = (remainingMillis / 50L).coerceAtLeast(0)

        val info1 = "VelocityQueue: ${delayedPacketQueue.size}"
        val info2 = "NextSend: $remainingTicks ticks"

        mc.fontRendererObj.drawStringWithShadow(
            info1,
            centerX - mc.fontRendererObj.getStringWidth(info1) / 2f,
            centerY + 60f,
            0xFFFFFF
        )
        mc.fontRendererObj.drawStringWithShadow(
            info2,
            centerX - mc.fontRendererObj.getStringWidth(info2) / 2f,
            centerY + 70f,
            0xFFFFFF
        )
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.netHandler == null) {
            return
        }

        if (event.eventType.stateName != "RECEIVE") {
            return
        }

        val packet = event.packet

        when (packet) {
            is S08PacketPlayerPosLook, is S40PacketDisconnect, is S00PacketKeepAlive, is S32PacketConfirmTransaction -> {
                return
            }

            is S06PacketUpdateHealth -> {
                if (packet.health <= 0) {
                    clear(true)
                    return
                }
            }

            is S29PacketSoundEffect -> {
                if (packet.soundName == "game.player.hurt") {
                    return
                }
            }

        }
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            val threshold = 1000

            val absMotionX = abs(packet.motionX)
            val absMotionY = abs(packet.motionY)
            val absMotionZ = abs(packet.motionZ)

            if (absMotionX < threshold && absMotionY < threshold && absMotionZ < threshold) {
                return
            }

            chat("Delayed Velocity: (${packet.motionX}, ${packet.motionY}, ${packet.motionZ})")
            event.cancelEvent()
            delayedPacketQueue.add(PacketSnapshot(packet, System.currentTimeMillis()))
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null || mc.netHandler == null) {
            return
        }
        processPackets(false)
    }

    private fun processPackets(force: Boolean) {
        if (mc.netHandler == null) {
            delayedPacketQueue.clear()
            return
        }

        val delayMillis = packetDelay.get() * 50L
        val iterator = delayedPacketQueue.iterator()

        while (iterator.hasNext()) {
            val snapshot = iterator.next()
            if (force || System.currentTimeMillis() - snapshot.time >= delayMillis) {
                try {
                    @Suppress("UNCHECKED_CAST") (snapshot.packet as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                iterator.remove()
            }
        }
    }

    /**
     * 清理模块状态。
     * @param handlePackets - 如果为 true，则在清空队列前处理所有剩余数据包。
     */
    private fun clear(handlePackets: Boolean) {
        if (handlePackets) {
            processPackets(true)
        }
        delayedPacketQueue.clear()
    }
}
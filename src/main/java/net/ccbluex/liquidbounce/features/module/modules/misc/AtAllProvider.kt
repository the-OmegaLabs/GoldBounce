/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value._boolean
import net.minecraft.network.play.client.C01PacketChatMessage
import java.util.concurrent.LinkedBlockingQueue

object AtAllProvider :
    Module("AtAllProvider", Category.MISC, subjective = true, gameDetecting = false, hideModule = false) {

    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 1000, 0..20000) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay)
    }
    private val maxDelay by maxDelayValue

    private val minDelay by object : IntegerValue("MinDelay", 500, 0..20000) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay)

        override fun isSupported() = !maxDelayValue.isMinimal()
    }

    private val retry by _boolean("Retry", false)
    private val sendQueue = LinkedBlockingQueue<String>()
    private val retryQueue = mutableListOf<String>()
    private val msTimer = MSTimer()
    private var delay = randomDelay(minDelay, maxDelay)

    override fun onDisable() {
        synchronized(sendQueue) {
            sendQueue.clear()
        }
        synchronized(retryQueue) {
            retryQueue.clear()
        }

        super.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!msTimer.hasTimePassed(delay))
            return

        try {
            synchronized(sendQueue) {
                if (sendQueue.isEmpty()) {
                    if (!retry || retryQueue.isEmpty())
                        return
                    else
                        sendQueue += retryQueue
                }

                mc.thePlayer.sendChatMessage(sendQueue.take())
                msTimer.reset()

                delay = randomDelay(minDelay, maxDelay)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C01PacketChatMessage) {
            val message = event.packet.message

            if ("@a" in message) {
                synchronized(sendQueue) {
                    for (playerInfo in mc.netHandler.playerInfoMap) {
                        val playerName = playerInfo?.gameProfile?.name

                        if (playerName == mc.thePlayer.name)
                            continue

                        // Replace out illegal characters
                        val filteredName = playerName?.replace("[^a-zA-Z0-9_]", "")?.let {
                            message.replace("@a", it)
                        }

                        sendQueue += filteredName
                    }
                    if (retry) {
                        synchronized(retryQueue) {
                            retryQueue.clear()
                            retryQueue += sendQueue
                        }
                    }
                }
                event.cancelEvent()
            }
        }
    }
}
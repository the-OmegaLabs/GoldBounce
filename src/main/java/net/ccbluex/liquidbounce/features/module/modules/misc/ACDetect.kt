/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 * @author RtxOP
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.value.boolean
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S32PacketConfirmTransaction

object ACDetect : Module("ACDetect", Category.MISC) {
    private val debug by boolean("Debug", true)

    private val actionNumbers = mutableListOf<Int>()
    private var check = false
    private var ticksPassed = 0

    @EventTarget
    fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is S32PacketConfirmTransaction -> {
                if (check) {
                    actionNumbers.add(packet.actionNumber.toInt())

                    if (debug) {
                        chat("ID: ${packet.actionNumber}")
                    }

                    if (actionNumbers.size >= 5) {
                        analyzeActionNumbers()
                        check = false
                    }
                    ticksPassed = 0
                }
            }

            is S01PacketJoinGame -> {
                reset()
                check = true
            }
        }
    }
    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (check) ticksPassed++
        if (ticksPassed > 40 && check) {
            hud.addNotification(Notification("§3No Anticheat present.", 3000F))
            check = false
            actionNumbers.clear()
        }
    }

    override fun onEnable() {
        reset()
    }

    private fun analyzeActionNumbers() {
        if (actionNumbers.size < 3) {
            return
        }

        val differences = mutableListOf<Int>()
        for (i in 1 until actionNumbers.size) {
            differences.add(actionNumbers[i] - actionNumbers[i - 1])
        }

        val allSame = differences.all { it == differences[0] }
        if (allSame) {
            val step = differences[0]
            val first = actionNumbers.first()

            val detectedAC = when (step) {
                1 -> when (first) {
                    in -23772..-23762 -> "Vulcan"
                    in 95..105 -> "Matrix"
                    in -20005..-19995 -> "Matrix"
                    in -32773..-32762 -> "Grizzly"
                    else -> "Verus"
                }

                -1 -> when {
                    first < -3000 -> "Intave"
                    first in -5..0 -> "Grim"
                    first in -3005..-2995 -> "Karhu"
                    else -> "Polar"
                }

                else -> null
            }

            detectedAC?.let {
                hud.addNotification(Notification( "§3Anticheat detected: §a${it}", 3000F))
                actionNumbers.clear()
                return
            }
        }

        // Polar
        if (differences.size >= 2) {
            val firstDiff = differences[0]
            val secondDiff = differences[1]
            val remainingDiffs = differences.drop(2)

            if (firstDiff >= 100 && secondDiff == -1 && remainingDiffs.all { it == -1 }) {
                hud.addNotification(Notification("§3Anticheat detected: §aPolar", 3000F))
                actionNumbers.clear()
                return
            }
        }

        // Intave zero handling
        val firstAction = actionNumbers.firstOrNull()
        if (firstAction != null && firstAction < -3000 && actionNumbers.any { it == 0 }) {
            hud.addNotification(Notification("§3Anticheat detected: §aIntave", 3000F))
            actionNumbers.clear()
            return
        }

        hud.addNotification(Notification("§3Anticheat detected: §aUnknown", 3000F))
        if (debug) {
            chat("§3Action Numbers: ${actionNumbers.joinToString()}")
            chat("§3Differences: ${differences.joinToString()}")
        }
        actionNumbers.clear()
    }

    private fun reset() {
        actionNumbers.clear()
        ticksPassed = 0
        check = false
    }
}
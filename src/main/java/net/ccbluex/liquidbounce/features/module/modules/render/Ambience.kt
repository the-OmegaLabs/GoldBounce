/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S2BPacketChangeGameState

object Ambience : Module("Atmosphere", Category.RENDER, gameDetecting = false, hideModule = false) {

    private val timeMode by choices("Mode", arrayOf("None", "Normal", "Custom"), "Custom")
    private val customWorldTime by int("Time", 19000, 0..24000) { timeMode == "Custom" }
    private val changeWorldTimeSpeed by int("TimeSpeed", 150, 10..500) { timeMode == "Normal" }

    private val weatherMode by choices("WeatherMode", arrayOf("None", "Sun", "Rain", "Thunder"), "None")
    private val weatherStrength by float("WeatherStrength", 1f, 0f..1f)
    { weatherMode == "Rain" || weatherMode == "Thunder" }

    private var i = 0L

    override fun onDisable() {
        i = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (timeMode.lowercase()) {
            "normal" -> {
                i += changeWorldTimeSpeed
                i %= 24000
                mc.theWorld.worldTime = i
            }

            "custom" -> {
                mc.theWorld.worldTime = customWorldTime.toLong()
            }
        }

        val strength = weatherStrength.coerceIn(0F, 1F)

        when (weatherMode.lowercase()) {
            "sun" -> {
                mc.theWorld.setRainStrength(0f)
                mc.theWorld.setThunderStrength(0f)
            }

            "rain" -> {
                mc.theWorld.setRainStrength(strength)
                mc.theWorld.setThunderStrength(0f)
            }

            "thunder" -> {
                mc.theWorld.setRainStrength(strength)
                mc.theWorld.setThunderStrength(strength)
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (timeMode != "None" && packet is S03PacketTimeUpdate)
            event.cancelEvent()

        if (weatherMode != "None" && packet is S2BPacketChangeGameState) {
            if (packet.gameState in 7..8) { // change weather packet
                event.cancelEvent()
            }
        }
    }
}
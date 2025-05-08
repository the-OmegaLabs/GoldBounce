/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils.timing

class TickTimer {
    private var tick = 0
    fun update() {
        tick++
    }

    fun reset() {
        tick = 0
    }
    fun getTickPassed(): Int {
        return tick
    }
    fun hasTimePassed(ticks: Int) = tick >= ticks
}

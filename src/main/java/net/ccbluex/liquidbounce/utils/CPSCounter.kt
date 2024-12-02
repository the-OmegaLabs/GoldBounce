/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.ClientUtils.runTimeTicks

/**
 * @author superblaubeere27
 */
object CPSCounter {
    private const val MAX_CPS = 50
    private val TIMESTAMP_BUFFERS = Array(MouseButton.values().size) { RollingArrayLongBuffer(MAX_CPS) }

    /**
     * Registers a mouse button click
     *
     * @param button The clicked button
     */
    fun registerClick(button: MouseButton) = TIMESTAMP_BUFFERS[button.ordinal].add(runTimeTicks.toLong())

    /**
     * Gets the count of clicks that have occurred in the last 1000ms
     *
     * @param button The mouse button
     * @return The CPS
     */
    fun getCPS(button: MouseButton, timeStampsSince: Int = runTimeTicks - 20) =
        TIMESTAMP_BUFFERS[button.ordinal].getTimestampsSince(timeStampsSince.toLong())

    enum class MouseButton { LEFT, MIDDLE, RIGHT }
}

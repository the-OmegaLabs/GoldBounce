/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils.kotlin

@Suppress("ControlFlowWithEmptyBody")
object CoroutineUtils {
	fun waitUntil(condition: () -> Boolean) {
		while (!condition()) {}
	}
}
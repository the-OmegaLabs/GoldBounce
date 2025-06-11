/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

object BPSCalculation : Module("BPSCalculation", Category.SETTINGS, gameDetecting = false, hideModule = false, canBeEnabled = false) {
    val bpsLimitEnabled by BoolValue("BPSLimitEnabled", false)
    val bpsLimit by FloatValue("BPSLimit", 15f, 0f..30f) {bpsLimitEnabled}
    val debugMode by BoolValue("Debug", false)
}

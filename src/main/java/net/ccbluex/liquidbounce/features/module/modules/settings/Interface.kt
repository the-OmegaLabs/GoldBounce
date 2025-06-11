package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

object Interface : Module("Interface", Category.SETTINGS, canBeEnabled = false) {
    val overrideRoundedRectShadow = BoolValue("OverrideRoundedRectShadow", false)
    val overrideGlow = BoolValue("Shadow = Glow",false) {overrideRoundedRectShadow.get()}
    val overrideStrength = IntegerValue("ShadowStrenght", 15, 0..20) {overrideRoundedRectShadow.get()}
}
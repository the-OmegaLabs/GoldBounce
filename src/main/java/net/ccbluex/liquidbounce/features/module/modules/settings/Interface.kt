package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.choices

object Interface : Module("Interface", Category.SETTINGS, canBeEnabled = false) {
    val xibao = choices("XibaoMode",arrayOf("XiBao","BeiBao"),"XiBao")
//    val overrideRoundedRectShadow = BoolValue("别打开我", false)
//    val overrideGlow = BoolValue("别打开我2",false) {overrideRoundedRectShadow.get()}
//    val overrideStrength = IntegerValue("别打开我3", 15, 0..20) {overrideRoundedRectShadow.get()}
}
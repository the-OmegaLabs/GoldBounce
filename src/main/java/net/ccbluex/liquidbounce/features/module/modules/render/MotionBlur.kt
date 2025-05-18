package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.float

object MotionBlur : Module("MotionBlur", Category.RENDER) {
    val blurAmount by float("BlurAmount", 7f, 0.0f..10.0f)
}
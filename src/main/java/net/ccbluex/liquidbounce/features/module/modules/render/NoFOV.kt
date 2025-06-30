/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.floatValue

object NoFOV : Module("NoFOV", Category.RENDER, gameDetecting = false, hideModule = false) {
    val fov by floatValue("FOV", 1f, 0f..1.5f)
}

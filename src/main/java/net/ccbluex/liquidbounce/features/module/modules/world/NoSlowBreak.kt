/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean

object NoSlowBreak : Module("NoSlowBreak", Category.WORLD, gameDetecting = false, hideModule = false) {
    val air by _boolean("Air", true)
    val water by _boolean("Water", false)
}

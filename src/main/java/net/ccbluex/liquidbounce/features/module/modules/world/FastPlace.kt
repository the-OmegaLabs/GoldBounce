/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.intValue

object FastPlace : Module("FastPlace", Category.WORLD, hideModule = false) {
    val speed by intValue("Speed", 0, 0..4)
    val onlyBlocks by _boolean("OnlyBlocks", true)
    val facingBlocks by _boolean("OnlyWhenFacingBlocks", true)
}

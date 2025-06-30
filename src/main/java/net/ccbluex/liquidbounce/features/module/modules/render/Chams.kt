/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean

object Chams : Module("Chams", Category.RENDER, hideModule = false) {
    val targets by _boolean("Targets", true)
    val chests by _boolean("Chests", true)
    val items by _boolean("Items", true)
}

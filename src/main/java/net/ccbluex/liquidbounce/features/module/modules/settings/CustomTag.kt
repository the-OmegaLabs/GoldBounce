/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.TextValue

object CustomTag : Module("CustomTag", Category.SETTINGS, gameDetecting = false, hideModule = false) {
    val custom = TextValue("text", "————————————————空岛高性能模式————————————————")
}

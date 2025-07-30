package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.StuckUtils

object Freeze : Module("Freeze", Category.MOVEMENT) {
    override fun onEnable() {
        StuckUtils.enable()
    }

    override fun onDisable() {
        StuckUtils.disable()
    }
}

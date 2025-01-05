package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.skid.fpsmaster.*

object RawInput : Module("RawInput", Category.MISC) {
    override fun onEnable(){
        RawInputMod().start()
    }
    override fun onDisable(){
        RawInputMod().stop()
    }
}
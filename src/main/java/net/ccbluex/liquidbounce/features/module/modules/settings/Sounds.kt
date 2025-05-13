package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.asyncPlay
import net.ccbluex.liquidbounce.utils.getWAVS
import net.ccbluex.liquidbounce.utils.playMP3
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

object Sounds : Module("Sounds", Category.SETTINGS) {
    val enableSounds = ListValue("Enable", getWAVS("/assets/minecraft/liquidbounce/sounds/Enable").toTypedArray(),"Mac")
    val disableSounds = ListValue("Disable", getWAVS("/assets/minecraft/liquidbounce/sounds/Disable").toTypedArray(),"Mac")
    val infoSounds = ListValue("Info", getWAVS("/assets/minecraft/liquidbounce/sounds/Info").toTypedArray(),"Chord")
    val startupSounds = ListValue("Startup", getWAVS("/assets/minecraft/liquidbounce/sounds/Startup").toTypedArray(),"MIUI")
    fun playEnableSound(){
        playMP3("/assets/minecraft/liquidbounce/sounds/Enable/${enableSounds.get()}.mp3")
    }
    fun playDisableSound(){
        playMP3("/assets/minecraft/liquidbounce/sounds/Disable/${disableSounds.get()}.mp3")
    }
    fun playInfoSound(){
        playMP3("/assets/minecraft/liquidbounce/sounds/Info/${infoSounds.get()}.mp3")
    }
    fun playStartupSound(){
        playMP3("/assets/minecraft/liquidbounce/sounds/Startup/${startupSounds.get()}.mp3")
    }
}
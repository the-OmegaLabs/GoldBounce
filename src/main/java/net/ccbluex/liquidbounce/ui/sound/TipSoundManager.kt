package net.ccbluex.liquidbounce.ui.sound

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSound: TipSoundPlayer
    var disableSound: TipSoundPlayer

    init {
        // 确保sounds目录存在
        if (!LiquidBounce.fileManager.soundsDir.exists()) {
            LiquidBounce.fileManager.soundsDir.mkdirs() // 创建目录
        }

        val enableSoundFile = File(LiquidBounce.fileManager.soundsDir, "enable.wav")
        val disableSoundFile = File(LiquidBounce.fileManager.soundsDir, "disable.wav")

        // 如果声音文件不存在，则解压默认文件
        if (!enableSoundFile.exists()) {
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/liquidbounce/sounds/enable.wav")
        }
        if (!disableSoundFile.exists()) {
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/liquidbounce/sounds/disable.wav")
        }

        // 初始化声音播放器
        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
    }
}

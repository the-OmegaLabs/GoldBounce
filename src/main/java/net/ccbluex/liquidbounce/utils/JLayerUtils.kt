package net.ccbluex.liquidbounce.utils

import javazoom.jl.player.Player
import net.ccbluex.liquidbounce.ui.client.GuiMiniGame
import java.io.BufferedInputStream
import java.io.InputStream
import kotlin.concurrent.thread

fun playMP3(resourcePath: String) {
    thread(start = true) {
        try {
            val inputStream: InputStream = GuiMiniGame::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("音频资源未找到: $resourcePath")
            val bufferedStream = BufferedInputStream(inputStream)
            val player = Player(bufferedStream)
            player.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

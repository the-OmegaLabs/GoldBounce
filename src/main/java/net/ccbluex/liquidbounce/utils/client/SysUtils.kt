package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class SysUtils {
    fun copyToGameDir(filePath: String,objectPath: String) {
        val isFileInDir = File(FileManager.dir, objectPath)
        if (!isFileInDir.exists()) {
            val inputStream: InputStream = LiquidBounce::class.java.classLoader.getResourceAsStream("assets/minecraft/liquidbounce/${filePath}") ?: throw IllegalStateException("$filePath not found in resources")
            Files.copy(inputStream, isFileInDir.toPath(), StandardCopyOption.REPLACE_EXISTING)
            inputStream.close()
            LOGGER.info("Copied $filePath to ${isFileInDir.absolutePath}")
        }
    }
    fun copyToFontDir(filePath: String) {
        val isFileInDir = File(FileManager.fontsDir, filePath)
        if (!isFileInDir.exists()) {
            val inputStream: InputStream = LiquidBounce::class.java.classLoader.getResourceAsStream("assets/minecraft/liquidbounce/font/${filePath}") ?: throw IllegalStateException("$filePath not found in resources")
            Files.copy(inputStream, isFileInDir.toPath(), StandardCopyOption.REPLACE_EXISTING)
            inputStream.close()
            LOGGER.info("Copied $filePath to ${isFileInDir.absolutePath}")
        }
    }
    fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }

}
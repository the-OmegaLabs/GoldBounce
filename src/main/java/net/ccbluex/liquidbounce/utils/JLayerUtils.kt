package net.ccbluex.liquidbounce.utils

import javazoom.jl.player.Player
import net.ccbluex.liquidbounce.features.module.modules.settings.Sounds
import net.ccbluex.liquidbounce.ui.client.GuiMiniGame
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URLDecoder
import java.util.jar.JarFile
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException
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

private fun validateSoundPath(path: String) {
    if (getMP3S(path).isEmpty()) {
        LOGGER.warn("声音目录 [$path] 存在但未找到WAV文件，请检查：")
        LOGGER.warn("- 文件扩展名是否为小写 .mp3")
        LOGGER.warn("- 文件实际路径是否包含中文/特殊字符")
        LOGGER.warn("- IDE中是否正确标记资源目录")
    }
}

fun asyncPlay(resourcePath: String) {
    thread(start = true) {
        try {
            val audioStream = GuiMiniGame::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("资源未找到: $resourcePath")

            val audioInputStream = AudioSystem.getAudioInputStream(BufferedInputStream(audioStream))
            val clip = AudioSystem.getClip()

            clip.open(audioInputStream)
            clip.start()

            while (clip.isRunning) {
                Thread.sleep(100)
            }

            clip.close()
            audioInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun playWavSound(path: String) {
    try {
        val audioStream = AudioSystem.getAudioInputStream(
            BufferedInputStream(
                Sounds::class.java.getResourceAsStream(path)
            )
        )
        val clip = AudioSystem.getClip()
        clip.open(audioStream)
        clip.start()
    } catch (e: UnsupportedAudioFileException) {
        println("不支持的音频格式: $path")
        e.printStackTrace()
    } catch (e: IOException) {
        println("文件加载失败: $path")
        e.printStackTrace()
    }
}

fun getWAVS(resourcePath: String): List<String> {
    val resourceDir = if (resourcePath.endsWith("/")) resourcePath else "$resourcePath/"
    val mp3Files = mutableListOf<String>()

    try {
        // 修复点1：使用正确的资源路径格式
        val normalizedPath = resourceDir.removePrefix("/")
        val resourceUrl = GuiMiniGame::class.java.classLoader.getResource(normalizedPath)
            ?: return emptyList()

        when (resourceUrl.protocol) {
            "jar" -> {
                val jarPath = resourceUrl.path.substringBefore("!")
                    .replace("file:", "")
                    .replace(Regex("(?i)%20"), " ") // 处理空格转义

                JarFile(URLDecoder.decode(jarPath, "UTF-8")).use { jar ->
                    jar.entries().iterator().forEach { entry ->
                        if (!entry.isDirectory &&
                            entry.name.startsWith(normalizedPath) &&
                            entry.name.equals("$normalizedPath${entry.name.substringAfterLast('/')}", true) &&
                            entry.name.endsWith(".wav", true)
                        ) {
                            val baseName = entry.name
                                .substringAfterLast('/')
                                .substringBeforeLast('.')
                            mp3Files.add(baseName)
                        }
                    }
                }
            }

            "file" -> {
                val fileDir = File(URLDecoder.decode(resourceUrl.toURI().path, "UTF-8"))
                if (fileDir.exists() && fileDir.isDirectory) {
                    fileDir.walk()
                        .filter { it.isFile && it.extension.equals("wav", true) }
                        .forEach { mp3Files.add(it.nameWithoutExtension) }
                }
            }
        }
    } catch (e: Exception) {
        LOGGER.warn("加载音频资源失败: ${e.message}") // 改为警告级别日志
    }

    return mp3Files.distinct() // 添加去重
}

fun getMP3S(resourcePath: String): List<String> {
    val resourceDir = if (resourcePath.endsWith("/")) resourcePath else "$resourcePath/"
    val mp3Files = mutableListOf<String>()

    try {
        // 修复点1：使用正确的资源路径格式
        val normalizedPath = resourceDir.removePrefix("/")
        val resourceUrl = GuiMiniGame::class.java.classLoader.getResource(normalizedPath)
            ?: return emptyList()

        when (resourceUrl.protocol) {
            "jar" -> {
                val jarPath = resourceUrl.path.substringBefore("!")
                    .replace("file:", "")
                    .replace(Regex("(?i)%20"), " ") // 处理空格转义

                JarFile(URLDecoder.decode(jarPath, "UTF-8")).use { jar ->
                    jar.entries().iterator().forEach { entry ->
                        if (!entry.isDirectory &&
                            entry.name.startsWith(normalizedPath) &&
                            entry.name.equals("$normalizedPath${entry.name.substringAfterLast('/')}", true) &&
                            entry.name.endsWith(".mp3", true)
                        ) {
                            val baseName = entry.name
                                .substringAfterLast('/')
                                .substringBeforeLast('.')
                            mp3Files.add(baseName)
                        }
                    }
                }
            }

            "file" -> {
                val fileDir = File(URLDecoder.decode(resourceUrl.toURI().path, "UTF-8"))
                if (fileDir.exists() && fileDir.isDirectory) {
                    fileDir.walk()
                        .filter { it.isFile && it.extension.equals("mp3", true) }
                        .forEach { mp3Files.add(it.nameWithoutExtension) }
                }
            }
        }
    } catch (e: Exception) {
        LOGGER.warn("加载音频资源失败: ${e.message}") // 改为警告级别日志
    }

    return mp3Files.distinct() // 添加去重
}

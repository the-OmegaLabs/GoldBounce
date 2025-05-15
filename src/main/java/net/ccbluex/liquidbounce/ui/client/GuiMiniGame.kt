package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import javazoom.jl.player.Player
import java.io.BufferedInputStream
import java.io.InputStream
import kotlin.concurrent.thread

class GuiMiniGame(private val prevGui: GuiScreen) : GuiScreen() {

//    private var playerX = 0f
//    private val playerSpeed = 5f
//    private var score = 0
//    private var isGameOver = false
//    private val fallingObjects = mutableListOf<FallingObject>()
//    private val objectTextures = arrayOf(
//        ResourceLocation("liquidbounce/logo_large.png"),
//        ResourceLocation("liquidbounce/icon_64x64.png"),
//        ResourceLocation("liquidbounce/taco/1.png"),
//        ResourceLocation("liquidbounce/custom_hud_icon.png")
//    )
//    private val spawnRate = 40
//    private var spawnCounter = 0
//    private val GIF_WIDTH = 133f
//    private val GIF_HEIGHT = 86f
//    private var musicPlayer: Player? = null
//    private var musicThread: Thread? = null
//    private var backgroundMusicPlaying = false

//    override fun initGui() {
//        playerX = width / 2f - GIF_WIDTH / 2
//        buttonList.add(GuiButton(1, width / 2 - 100, height - 60, "Back"))
//        resetGame()
//    }

//    private fun resetGame() {
//        stopMusic()
//        fallingObjects.clear()
//        score = 0
//        isGameOver = false
//        backgroundMusicPlaying = true
//        // 音乐:电警喵
//        playMP3("/assets/minecraft/liquidbounce/chuha.mp3")
//    }
//
//    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
//        drawDefaultBackground()
//
//        if (!isGameOver) {
//            updateGame()
//        }
//
//        RenderUtils.drawImage(
//            ResourceLocation("liquidbounce/maodie/maodie.png"),
//            playerX.toInt(),
//            (height - 86).toInt(),
//            133,
//            86
//        )
//
//        fallingObjects.forEach { obj ->
//            RenderUtils.drawImage(obj.texture, obj.x.toInt(), obj.y.toInt(), obj.size, obj.size)
//        }
//
//        Fonts.font40.drawString("Score: $score", 10f, 10f, 0x000000) // 黑色字体
//
//        if (isGameOver) {
//            Fonts.font40.drawCenteredString("Game Over! Click to restart", width / 2f, height / 2f, 0xFF0000)
//        }
//
//        super.drawScreen(mouseX, mouseY, partialTicks)
//    }
//
//    private fun updateGame() {
//        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
//            playerX -= playerSpeed
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
//            playerX += playerSpeed
//        }
//        playerX = playerX.coerceIn(0f, width - GIF_WIDTH)
//
//        if (spawnCounter++ >= spawnRate) {
//            spawnCounter = 0
//            fallingObjects.add(
//                FallingObject(
//                    texture = objectTextures.random(),
//                    x = (Math.random() * (width - 50)).toFloat(),
//                    y = 0f,
//                    speed = (Math.random() * 3 + 2).toFloat(),
//                    size = (Math.random() * 30 + 30).toInt()
//                )
//            )
//        }
//
//        fallingObjects.forEach { it.y += it.speed }
//
//        fallingObjects.removeAll { obj ->
//            val collision = checkPixelCollision(
//                playerX, (height - GIF_HEIGHT).toFloat(),
//                obj.x, obj.y,
//                GIF_WIDTH, GIF_HEIGHT,
//                obj.size.toFloat(), obj.size.toFloat()
//            )
//
//            if (collision) {
//                if (!isGameOver) {
//                    stopMusic()
//                    playMP3("/assets/minecraft/liquidbounce/baby.mp3") // 死亡音效
//                }
//                isGameOver = true
//                true
//            } else {
//                obj.y > height
//            }
//        }
//
//        score++
//    }
//
//    override fun keyTyped(typedChar: Char, keyCode: Int) {
//        // 保留回退用途，不处理移动
//    }
//    fun getMusicPlayer(): Player? {
//        return musicPlayer
//    }
//
//    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
//        if (isGameOver) {
//            resetGame()
//        }
//        super.mouseClicked(mouseX, mouseY, mouseButton)
//    }
//
//    override fun actionPerformed(button: GuiButton) {
//        if (button.id == 1) {
//            stopMusic()
//            mc.displayGuiScreen(prevGui)
//        }
//    }
//
//    override fun onGuiClosed() {
//        stopMusic()
//        super.onGuiClosed()
//    }
//
//    fun stopMusic() {
//        try {
//            musicPlayer?.let {
//                println("Closing music player.")
//                it.close()
//                musicPlayer = null
//            }
//        } catch (e: Exception) {
//            println("Error while stopping music:")
//            e.printStackTrace()
//        }
//    }
//
//
//    private fun playMP3(resourcePath: String) {
//        musicThread = thread(start = true) {
//            try {
//                val stream: InputStream = GuiMiniGame::class.java.getResourceAsStream(resourcePath)
//                    ?: throw IllegalArgumentException("音频资源未找到: $resourcePath")
//                val bufferedStream = BufferedInputStream(stream)
//                musicPlayer = Player(bufferedStream)
//                musicPlayer?.play()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private data class FallingObject(
//        val texture: ResourceLocation,
//        var x: Float,
//        var y: Float,
//        val speed: Float,
//        val size: Int
//    )
//
//    private fun checkPixelCollision(
//        x1: Float, y1: Float, x2: Float, y2: Float,
//        w1: Float, h1: Float, w2: Float, h2: Float
//    ): Boolean {
//        return x1 < x2 + w2 && x1 + w1 > x2 &&
//                y1 < y2 + h2 && y1 + h1 > y2
//    }
}

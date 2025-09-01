package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.playMP3
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.util.ResourceLocation
import java.awt.Color

class GuiMainMenu : GuiScreen() {

    companion object {
        private var played = false
    }

    private val lancerW = 74f
    private val lancerH = 62f
    private var xPos = -lancerW
    private var vx = 100f
    private var ax = 200f
    private var lastTime = 0L

    private val expFrames = 17
    private var expOn = false
    private var expFrame = 1
    private var lastExpTime = 0L
    private val expInterval = 80L

    private var running = false
    private var done = false
    private var delayStart = 0L
    private val delayMs = 1000L
    private var delayPassed = false
    private var doExchange = false
    override fun initGui() {
        super.initGui()
        val h0 = height / 4 + 48
        val bw = 98
        val bh = 20
        val sp = 24
        buttonList.run {
            add(GuiButton(1, width/2-100, h0, bw, bh, "Singleplayer"))
            add(GuiButton(2, width/2+2, h0, bw, bh, "Multiplayer"))
            add(GuiButton(100, width/2-100, h0+sp, bw, bh, "AltManager"))
            add(GuiButton(103, width/2+2, h0+sp, bw, bh, "Mods"))
            add(GuiButton(101, width/2-100, h0+sp*2, bw*2+4, bh, "Server"))
            add(GuiButton(102, width/2-100, h0+sp*3, bw*2+4, bh, "Hack"))
            add(GuiButton(0, width/2-100, h0+sp*4, bw, bh, "Settings"))
            add(GuiButton(4, width/2+2, h0+sp*4, bw, bh, "Exit"))
        }
        if (!played) {
            running = false
            done = false
            delayPassed = false
            delayStart = System.currentTimeMillis()
            xPos = -lancerW
            vx = 100f
            ax = 200f
            lastTime = 0L
            expOn = false
            expFrame = 1
            lastExpTime = 0L
        }
    }

    override fun drawScreen(mx: Int, my: Int, pt: Float) {
        drawImage(ResourceLocation("liquidbounce/background.png"), -mx, -my, width*2, height*2)
        val now = System.currentTimeMillis()
        if (!played && !delayPassed) {
            if (now - delayStart >= delayMs) {
                delayPassed = true
                running = true
                lastTime = now
            }
        }
        if (running && !done) {
            val dt = ((now - lastTime).coerceAtLeast(0L)) / 1000f
            if (!expOn) {
                vx += ax*dt
                xPos += vx*dt
                val collideX = width/2f - 100f
                if (xPos + lancerW >= collideX) {
                    expOn = true
                    playMP3("/assets/minecraft/liquidbounce/sounds/explosion.mp3")
                    doExchange = true
                    lastExpTime = now
                    vx += 150f
                }
            } else {
                vx += ax*dt
                xPos += vx*dt
            }
            lastTime = now
            if (expOn && expFrame<=expFrames) {
                if (lastExpTime==0L) lastExpTime=now
                if (now - lastExpTime >= expInterval) {
                    expFrame++
                    lastExpTime = now
                }
            }
            val logoTop = height/8f
            val logoHt = 58f
            val centerY = logoTop + logoHt/2f
            val yL = (centerY - lancerH/2f).toInt()
            drawImage(
                ResourceLocation("liquidbounce/icons/lancer/${frameIndex()}.png"),
                xPos.toInt(), yL, lancerW.toInt(), lancerH.toInt()
            )
            if (expOn && expFrame in 1..expFrames) {
                val ew = 71f
                val eh = 98f
                val yE = (centerY - eh/2f).toInt()
                val xE = (width/2f - ew/2f).toInt()
                drawImage(
                    ResourceLocation("liquidbounce/icons/explosion/$expFrame.png"),
                    xE, yE, ew.toInt(), eh.toInt()
                )
            }
            if (expOn && expFrame>expFrames) {
                done = true
                played = true
            }
        }
        if (played) {
            val bw = 92
            val bh = 120
            val y0 = (height/8f - 10f).toInt()
            drawImage(
                ResourceLocation("liquidbounce/icons/Berdly.png"),
                (width/2f - bw/2f).toInt(), y0, bw, bh
            )
        } else if ((!running && delayPassed && !doExchange) || done) {
            drawImage(
                ResourceLocation("liquidbounce/logo_large.png"),
                width/2-100, height/8, 199, 58
            )
        } else if (!played) {
            drawImage(
                ResourceLocation("liquidbounce/logo_large.png"),
                width/2-100, height/8, 199, 58
            )
        }
        drawRoundedBorderRect(width/2f-115, height/4f+35, width/2f+115, height/4f+175, 2f, Integer.MIN_VALUE, Integer.MIN_VALUE, 3f)
        GlowUtils.drawGlow(width/2f-115, height/4f+35, 230f, 140f, 20, Color.BLACK)
        Fonts.fontNoto35.drawCenteredString("b10", width/2f+148f, height/8f+Fonts.font35.fontHeight, 0xffffff, true)
        super.drawScreen(mx, my, pt)
    }

    private fun frameIndex(): Int {
        val step = lancerW/6f
        val moved = xPos + lancerW
        if (moved<=0f) return 1
        return ((moved/step).toInt()%6 + 1).coerceIn(1,6)
    }

    override fun mouseClicked(mx: Int, my: Int, btn: Int) = super.mouseClicked(mx, my, btn)

    override fun actionPerformed(b: GuiButton) {
        when (b.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            101 -> mc.displayGuiScreen(GuiServerStatus(this))
            102 -> mc.displayGuiScreen(GuiClientConfiguration(this))
            103 -> mc.displayGuiScreen(GuiModsMenu(this))
        }
    }
}

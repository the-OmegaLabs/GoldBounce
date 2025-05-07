package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.*

object WaterMark : Module("WaterMark", Category.RENDER) {

    // 新增药丸状态相关属性
    private data class PillState(
        var content: String = "", 
        var progress: Float = 0f, 
        var active: Boolean = false, 
        var timer: ScheduledFuture<*>? = null,
        var progressBar: Float = 0f // 新增进度条百分比
    )

    private val pillState = PillState()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val pillAnimationSpeed = 0.1f
    private val pillColor = Color(76, 175, 80)
    private val pillTextColor = Color.WHITE

    private var posX = 0
    private var posY = 0
    private val bgColor = Color(0, 0, 0, 120)
    private val textColor = Color.WHITE

    private var animationProgress = 0f
    private var animationSpeed = 0.05f
    private var isAnimating = true
    private var pulseTime = 0f

    private val shadowEnabled = boolean("别打开我", false)
    private val shadowStrength = float("ShadowStrength", 5f, 1f..10f)
    private val showMemory = boolean("Show Memory", false)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        posX = sr.scaledWidth / 2
        posY = sr.scaledHeight / 10

        val fps = Minecraft.getDebugFPS()
        val ping = mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID)?.responseTime ?: 0
        val memoryUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
        val memoryMax = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        val watermarkText = buildString {
            append("Obai | ${mc.session.username} | ${fps}fps | ${ping}ms")
            if (showMemory.get()) append(" | RAM: ${memoryUsed}/${memoryMax}MB")
        }

        val logoWidth = 20
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText)
        val totalWidth = textWidth + logoWidth + 20
        val height = 30

        if (isAnimating) {
            animationProgress = (animationProgress + animationSpeed).coerceAtMost(1f)
            if (animationProgress >= 1f) isAnimating = false
        }

        val easedProgress = easeOutBack(animationProgress)
        val animatedWidth = totalWidth * easedProgress

        pulseTime += 0.05f
        val pulseWidth = 20 + 10 * sin(pulseTime)

        if (shadowEnabled.get()) {
            ShadowUtils.shadow(
                strength = shadowStrength.get(),
                drawMethod = {
                    RenderUtils.drawRoundedRect(
                        (posX - animatedWidth / 2).toFloat(),
                        posY.toFloat(),
                        (posX + animatedWidth / 2).toFloat(),
                        (posY + height).toFloat(),
                        bgColor.rgb,
                        15f
                    )
                    RenderUtils.drawRoundedRect(
                        (posX - animatedWidth / 2).toFloat(),
                        (posY + height - 3).toFloat(),
                        (posX - animatedWidth / 2 + pulseWidth).toFloat(),
                        (posY + height).toFloat(),
                        Color(255, 255, 255, 80).rgb,
                        2f
                    )
                },
                cutMethod = {
                    RenderUtils.drawRoundedRect(
                        (posX - animatedWidth / 2).toFloat() - 5,
                        posY.toFloat() - 5,
                        (posX + animatedWidth / 2).toFloat() + 5,
                        (posY + height).toFloat() + 5,
                        Color.WHITE.rgb,
                        15f
                    )
                }
            )
        } else {
            RenderUtils.drawRoundedRect(
                (posX - animatedWidth / 2).toFloat(),
                posY.toFloat(),
                (posX + animatedWidth / 2).toFloat(),
                (posY + height).toFloat(),
                bgColor.rgb,
                15f
            )
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
        RenderUtils.drawCircle((posX - animatedWidth / 2 + 15).toFloat(), (posY + height / 2).toFloat(), 8f, 0, 360)

        Fonts.fontHonor40.drawString(
            watermarkText,
            (posX - animatedWidth / 2 + logoWidth + 10).toFloat(),
            (posY + (height - Fonts.fontHonor40.FONT_HEIGHT) / 2).toFloat(),
            textColor.rgb
        )

        val scaffoldModule = ModuleManager.getModule(Scaffold::class.java)
        if (scaffoldModule.state == true) {
            val blocksCount = InventoryUtils.blocksAmount()
            val stateText = "Blocks:$blocksCount"
            val stateWidth = Fonts.fontHonor40.getStringWidth(stateText) + 30
            val stateHeight = 30

            val slideOffset = if (isAnimating) 0f else easeOutBack(1f - animationProgress) * stateWidth
            val stateX = posX - animatedWidth / 2 - stateWidth - 10 + slideOffset
            val stateY = posY

            RenderUtils.drawRoundedRect(
                stateX.toFloat(),
                stateY.toFloat(),
                (stateX + stateWidth).toFloat(),
                (stateY + stateHeight).toFloat(),
                Color(30, 144, 255, 255).rgb,
                15f
            )

            Fonts.fontHonor40.drawString(
                stateText,
                (stateX + 10f).toFloat(),
                (stateY + (stateHeight - Fonts.fontHonor40.FONT_HEIGHT) / 2f).toFloat(),
                Color.WHITE.rgb
            )
        }

        // 新增药丸渲染逻辑
        if (pillState.content.isNotEmpty() || pillState.active) {
            val pillText = pillState.content
            val textWidth = Fonts.fontHonor40.getStringWidth(pillText)
            val targetWidth = if (pillState.content.isNotEmpty()) textWidth + 40 else 0
            val maxWidth = max(targetWidth, 0)
            
            pillState.progress = (pillState.progress + pillAnimationSpeed * if (pillState.content.isNotEmpty()) 1f else -1f).coerceIn(0f, 1f)
            pillState.active = pillState.progress > 0
            
            if (pillState.active) {
                val pillWidth = maxWidth * easeInOutQuint(pillState.progress) // 改为使用EaseIn-Out动画
                val pillX = posX + animatedWidth/2 + 10
                val pillY = posY + height/2 - 15
                
                // 绘制药丸背景（添加进度条）
                RenderUtils.drawRoundedRect(
                    pillX.toFloat(),
                    pillY.toFloat(),
                    (pillX + pillWidth).toFloat(),
                    (pillY + 30).toFloat(),
                    pillColor.rgb,
                    15f
                )
                
                // 绘制进度条
                val progressWidth = pillWidth * pillState.progressBar
                RenderUtils.drawRect(
                    pillX.toFloat(),
                    (pillY + 28).toFloat(),
                    (pillX + progressWidth).toFloat(),
                    (pillY + 30).toFloat(),
                    Color(255, 255, 255, 150).rgb
                )
                
                // 绘制药丸文字
                if (pillWidth > 40) {
                    Fonts.fontHonor40.drawString(
                        pillText,
                        pillX + 20f,
                        pillY + (30 - Fonts.fontHonor40.FONT_HEIGHT)/2f,
                        pillTextColor.rgb
                    )
                }
            }
        }
        
        GL11.glDisable(GL11.GL_BLEND)
    }

    // 新增进度设置函数
    fun setPillProgress(percentage: Float) {
        pillState.progressBar = percentage.coerceIn(0f, 1f)
    }

    // 修改后的设置内容函数（添加进度重置参数）
    fun setPillContent(text: String, autoCloseSeconds: Int = 3, resetProgress: Boolean = true) {
        pillState.timer?.cancel(true)
        
        pillState.content = text
        if (resetProgress) {
            pillState.progressBar = 0f
        }
        
        if (text.isNotEmpty()) {
            pillState.timer = scheduler.schedule({
                setPillContent("")
            }, autoCloseSeconds.toLong(), TimeUnit.SECONDS)
        }
    }

    override fun onDisable() {
        animationProgress = 0f
        isAnimating = true
    }

    // 修改后的动画缓动函数
    private fun easeInOutQuint(x: Float): Float {
        return if (x < 0.5f) 16f * x * x * x * x * x else 1f - (-2f * x + 2f).pow(5) / 2f
    }

    private fun easeOutBack(x: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }
}

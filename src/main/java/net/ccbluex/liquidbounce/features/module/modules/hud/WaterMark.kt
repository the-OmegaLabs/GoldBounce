package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils.easeOutBack
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.value.TextValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.disableBlend
import net.minecraft.client.renderer.GlStateManager.disableLighting
import net.minecraft.client.renderer.GlStateManager.enableAlpha
import net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting
import net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting
import net.minecraft.item.ItemBlock
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import java.awt.Color
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

object WaterMark : Module("WaterMark", Category.HUD) {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    private var posX = 0
    private var posY = 0
    private val bgColor = Color(0, 0, 0, 120)
    private val textColor = Color.WHITE

    private var animatedBlocks = 0f
    private var animationProgress = 0f
    private const val ANIM_SPEED = 0.2f
    private val animationSpeed = 0.05f
    private var isAnimating = true
    private var pulseTime = 0f
    private var uiAnimProgress = 0f
    private var currentWidth: Float = 0f
    private var currentHeight: Float = 0f
    private var lastStateChangeTime = 0L
    private enum class State { NONE, Normal, Scaffolding }
    private var currentState = State.NONE
    private const val ANIM_DURATION = 300L // 动画时长300ms
    val showMemory = boolean("ShowMemory", false)
    private val shadowEnabled = boolean("Shadow", false)
    val shadowStrengh = int("ShadowStrength", 20, 1..20)
    private val clientName = TextValue("ClientName", "Obai")
    private fun easeOutElastic(x: Float): Float {
        val c4 = (2 * Math.PI) / 3
        return when (x) {
            0f -> 0f
            1f -> 1f
            else -> (2.0.pow((-10 * x).toDouble()) * sin((x * 10 - 0.75) * c4) + 1).toFloat()
        }
    }

    private fun easeInOutQuad(x: Float): Float {
        return if (x < 0.5f) 2 * x * x else 1 - (-2 * x + 2).pow(2) / 2
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        val now = System.currentTimeMillis()

        // 状态检测和动画进度计算
        val isScaffolding = LiquidBounce.moduleManager.getModule(Scaffold::class.java).state
        val newState = if (isScaffolding) State.Scaffolding else State.Normal
        if (newState != currentState) {
            currentState = newState
            lastStateChangeTime = now
        }
        uiAnimProgress = ((now - lastStateChangeTime) / ANIM_DURATION.toFloat()).coerceIn(0f, 1f)

        // 计算目标尺寸
        val (targetWidth, targetHeight) = when (currentState) {
            State.Scaffolding -> calculateScaffoldSize(sr)
            else -> calculateNormalSize(sr)
        }

        // 应用弹性动画
        currentWidth = lerp(currentWidth, targetWidth, uiAnimProgress).toFloat()
        currentHeight = lerp(currentHeight, targetHeight, uiAnimProgress).toFloat()

        // 统一背景绘制
        drawBackground(sr, currentWidth, currentHeight)

        when (currentState) {
            State.Scaffolding -> drawScaffoldUI(sr, currentWidth, currentHeight)
            else -> drawNormalUI(sr, currentWidth, currentHeight)
        }
    }
    private fun lerp(start: Float, end: Float, progress: Float): Double {
        return start + (end - start) * easeOutBack(progress.toDouble())
    }

    private fun shouldUpdateState(): Boolean {
        val isScaffolding = LiquidBounce.moduleManager.getModule(Scaffold::class.java).state
        val currentState = if (isScaffolding) State.Scaffolding else State.Normal
        return currentState != State.NONE
    }
    private fun calculateNormalSize(sr: ScaledResolution): Pair<Float, Float> {
        val watermarkText = buildString {
            append("${clientName.get()} | ${mc.session.username} | ${Minecraft.getDebugFPS()}fps")
            if (showMemory.get()) append(" | RAM: ${getUsedMemory()}/${getMaxMemory()}MB")
        }
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText).toFloat()
        return Pair(textWidth + 40f, 30f)
    }
    private fun drawNormalUI(sr: ScaledResolution, width: Float, height: Float) {
        val centerX = sr.scaledWidth / 2f
        val posY = sr.scaledHeight / 9f

        // 基于当前宽度重新计算位置
        val logoWidth = 20
        val watermarkText = buildString {
            append("${clientName.get()} | ${mc.session.username} | ${Minecraft.getDebugFPS()}fps")
            if (showMemory.get()) append(" | RAM: ${getUsedMemory()}/${getMaxMemory()}MB")
        }
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText)

        // 修复偏移：使用当前宽度计算位置
        val contentWidth = logoWidth + 10 + textWidth
        val offsetX = (width - contentWidth) / 2

        // 图标绘制
        RenderUtils.drawImage(
            ResourceLocation("liquidbounce/obai.png"),
            (centerX - width/2 + offsetX).toInt(),
            (posY + height/2 - 9).toInt(),
            18, 18
        )

        // 文本绘制
        Fonts.fontHonor40.drawString(
            watermarkText,
            centerX - width/2 + offsetX + logoWidth + 5,
            posY + height/2 - Fonts.fontHonor40.FONT_HEIGHT/2,
            textColor.rgb
        )
    }

    private fun drawScaffoldUI(sr: ScaledResolution, width: Float, height: Float) {
        val startX = (sr.scaledWidth - width) / 2
        val startY = sr.scaledHeight / 9f

        val containerPadding = 8f
        val elementSpacing = 4f
        val progressWidth = 120f  // 调整进度条宽度
        val iconSize = 20f

        val stack = mc.thePlayer?.inventory?.getStackInSlot(SilentHotbar.currentSlot)
        val blockAmount = stack?.stackSize ?: 0
        val countText = "$blockAmount blocks"
        val textWidth = Fonts.font40.getStringWidth(countText)

        // 动态调整右侧间距
        val rightPadding = containerPadding + (width - (iconSize + elementSpacing*2 + progressWidth + textWidth)) / 2

        // 物品图标
        if (stack?.item is ItemBlock) {
            glPushMatrix()
            enableGUIStandardItemLighting()
            mc.renderItem.renderItemAndEffectIntoGUI(
                stack,
                (startX + containerPadding).toInt(),
                (startY + (height - 16)/2).toInt()
            )
            disableStandardItemLighting()
            glPopMatrix()
        }

        // 进度条背景
        val progressBarY = startY + height/2 - 2
        drawRoundedRect(
            startX + containerPadding + iconSize + elementSpacing,
            progressBarY,
            startX + containerPadding + iconSize + elementSpacing + progressWidth,
            progressBarY + 4f,
            Color(0, 0, 0, 100).rgb,
            2f
        )

        // 进度条前景（带平滑动画）
        val targetProgress = blockAmount / 64f
        animatedBlocks += (targetProgress - animatedBlocks) * 0.15f
        val animatedWidth = progressWidth * animatedBlocks
        drawRoundedRect(
            startX + containerPadding + iconSize + elementSpacing,
            progressBarY,
            startX + containerPadding + iconSize + elementSpacing + animatedWidth,
            progressBarY + 4f,
            Color(255, 255, 255, 200).rgb,
            2f
        )

        // 文本绘制（修复右侧间距）
        Fonts.font40.drawString(
            countText,
            startX + width - rightPadding - textWidth,
            startY + height/2 - Fonts.font40.FONT_HEIGHT/2,
            textColor.rgb
        )
    }
    private fun calculateScaffoldSize(sr: ScaledResolution): Pair<Float, Float> {
        val stack = mc.thePlayer?.inventory?.getStackInSlot(SilentHotbar.currentSlot)
        val textWidth = Fonts.font40.getStringWidth("${stack?.stackSize ?: 0} blocks").toFloat()
        return Pair(200f + textWidth, 36f)
    }
    private fun drawBackground(sr: ScaledResolution, width: Float, height: Float) {
        val startX = (sr.scaledWidth - width) / 2
        val startY = sr.scaledHeight / 9f

        // 修复圆角：保持15f圆角
        RenderUtils.drawRoundedRect(
            startX, startY,
            startX + width, startY + height,
            Color(bgColor.red, bgColor.green, bgColor.blue, (bgColor.alpha * uiAnimProgress).toInt()).rgb,
            15f
        )

        // 阴影绘制
        if (shadowEnabled.get()) {
            GlowUtils.drawGlow(
                startX, startY,
                width, height,
                (shadowStrengh.get() * uiAnimProgress).toInt(),
                bgColor
            )
        }
    }
    private fun getUsedMemory() = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    private fun getMaxMemory() = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    override fun onDisable() {
        animationProgress = 0f
        isAnimating = true
    }

}
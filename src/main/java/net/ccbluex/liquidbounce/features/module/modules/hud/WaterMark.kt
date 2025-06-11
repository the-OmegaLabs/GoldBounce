package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils水影加加
import net.ccbluex.liquidbounce.utils.render.EaseUtils.easeOutBack
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float
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

    private var animatedBlocks = 0f
    private var animationProgress = 0f
    private const val ANIM_SPEED = 0.5f
    private val animationSpeed = 0.05f
    private var isAnimating = true
    private var pulseTime = 0f
    private var uiAnimProgress = 0f
    private var currentWidth: Float = 0f
    private var currentHeight: Float = 0f
    private var lastStateChangeTime = 0L

    private enum class State { NONE, Normal, Scaffolding }

    private var currentState = State.NONE
    private val ANIM_DURATION = int("AnimationDuration", 300, 0..1000)
    val normalMode = ListValue("RenderMode", arrayOf("Opai", "Opal"), "Opai")
    val textColorR = int("TextColorR", 255, 0..255)
    private val textColorG = int("TextColorG", 255, 0..255)
    private val textColorB = int("TextColorB", 255, 0..255)
    val showMemory = boolean("ShowMemory", false) { normalMode.get() == "Opai" }
    val showLatency = boolean("ShowLatency", true) { normalMode.get() == "Opai" }
    private val shadowEnabled = boolean("Shadow", false) { normalMode.get() == "Opai"}
    val shadowStrengh = int("ShadowStrength", 20, 1..20) { normalMode.get() == "Opai"}
    private val clientName = TextValue("ClientName", "Obai")
    private var animWidth = 0f
    private var animHeight = 0f
    // Opai模式专用值
    private val opaiColorR = int("Opal-R", 255, 0..255) { normalMode.get() == "Opal" }
    private val opaiColorG = int("Opal-G", 255, 0..255) { normalMode.get() == "Opal" }
    private val opaiColorB = int("Opal-B", 255, 0..255) { normalMode.get() == "Opal" }
    private val opaiShadow = boolean("Opal-Shadow", false) { normalMode.get() == "Opal" }
    private val opaiShadowStrength = int("Opal-ShadowStrength", 1, 1..2) { normalMode.get() == "Opal" }
    private val opaiAnimationSpeed = float("Opal-AnimSpeed", 0.4f, 0.05f..1f) { normalMode.get() == "Opal" }

    // Opai模式常量
    private val versionNameUp = LiquidBounce.clientVersionText
    private val versionNameDown = LiquidBounce.clientBigVersionText
    private var progressBarAnimationWidth = 120f

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

    private fun updateAnimation(targetWidth: Float, targetHeight: Float) {
        animWidth = AnimationUtils水影加加.animate(
            targetWidth,
            animWidth,
            ANIM_SPEED * RenderUtils.deltaTime * 0.025F
        )

        animHeight = AnimationUtils水影加加.animate(
            targetHeight,
            animHeight,
            ANIM_SPEED * RenderUtils.deltaTime * 0.025F
        )

        currentWidth = animWidth
        currentHeight = animHeight
    }



    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = ScaledResolution(mc)
        val now = System.currentTimeMillis()

        val isScaffolding = LiquidBounce.moduleManager.getModule(Scaffold::class.java).state
        val newState = if (isScaffolding) State.Scaffolding else State.Normal
        if (newState != currentState) {
            currentState = newState
            lastStateChangeTime = now
        }
        uiAnimProgress = ((now - lastStateChangeTime) / ANIM_DURATION.get().toFloat()).coerceIn(0f, 1f)

        val (targetWidth, targetHeight) = when (currentState) {
            State.Scaffolding -> calculateScaffoldSize(sr)
            else -> calculateNormalSize(sr)
        }

        updateAnimation(targetWidth, targetHeight)

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
        return if (normalMode.get() == "Opal") {
            calculateOpaiNormalSize()
        } else {
            calculateBasicNormalSize()
        }
    }

    private fun calculateOpaiNormalSize(): Pair<Float, Float> {
        val serverip = ServerUtils.remoteIp
        val playerPing = "${mc.thePlayer.getPing()}ms"
        val textWidth = Fonts.fontHonor40.getStringWidth(clientName.get())

        val imageLen = 21F
        val containerToUiDistance = 2F
        val uiToUIDistance = 4F
        val textBar2 = max(Fonts.fontHonor40.getStringWidth(versionNameUp), Fonts.fontHonor35.getStringWidth(versionNameDown))
        val textBar3 = max(Fonts.fontHonor40.getStringWidth(serverip), Fonts.fontHonor35.getStringWidth(playerPing))

        val LineWidth = 2F

        val fastLen1 = containerToUiDistance + imageLen + uiToUIDistance
        val allLen = fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance + textBar2 + uiToUIDistance + LineWidth + uiToUIDistance + textBar3 + containerToUiDistance
        return Pair(allLen, 27f)
    }

    private fun calculateBasicNormalSize(): Pair<Float, Float> {
        val watermarkText = buildString {
            append("${clientName.get()} | ${mc.session.username} | ${Minecraft.getDebugFPS()}fps")
            if (showLatency.get()) append(" | ${mc.thePlayer.getPing()}ms")
            if (showMemory.get()) append(" | RAM: ${getUsedMemory()}/${getMaxMemory()}MB")
        }
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText).toFloat()
        return Pair(textWidth + 40f, 30f)
    }

    private fun drawNormalUI(sr: ScaledResolution, width: Float, height: Float) {
        if (normalMode.get() == "Opal") {
            drawOpaiNormalUI(sr, width, height)
        } else {
            drawBasicNormalUI(sr, width, height)
        }
    }

    private fun drawOpaiNormalUI(sr: ScaledResolution, width: Float, height: Float) {
        val startX = (sr.scaledWidth - width) / 2
        val startY = sr.scaledHeight / 9f

        val serverip = ServerUtils.remoteIp
        val playerPing = "${mc.thePlayer.getPing()}ms"
        val textWidth = Fonts.fontHonor40.getStringWidth(clientName.get())

        val colorAL = Color(opaiColorR.get(), opaiColorG.get(), opaiColorB.get(), 255)
        val imageLen = 21F
        val containerToUiDistance = 2F
        val uiToUIDistance = 4F
        val textBar2 = max(Fonts.fontHonor40.getStringWidth(versionNameUp), Fonts.fontHonor35.getStringWidth(versionNameDown))
        val textBar3 = max(Fonts.fontHonor40.getStringWidth(serverip), Fonts.fontHonor35.getStringWidth(playerPing))

        val LineWidth = 2F

        val fastLen1 = containerToUiDistance + imageLen + uiToUIDistance
        val allLen = fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance + textBar2 + uiToUIDistance + LineWidth + uiToUIDistance + textBar3 + containerToUiDistance

        // 绘制背景
        drawRoundedRect(startX, startY, startX + width, startY + height, Color(0, 0, 0, (120 * uiAnimProgress).toInt()).rgb, 13F)
        if (opaiShadow.get()) {
            drawOpaiShadow(startX, startY, width, height)
        }

        // 绘制内容
        drawImage(
            ResourceLocation("liquidbounce/obai.png"),
            (startX + containerToUiDistance + 2).toInt(),
            (startY + 4).toInt(),
            19,
            19,
            colorAL
        )

        Fonts.fontHonor40.drawString(
            clientName.get(),
            startX + fastLen1,
            startY + 9F,
            colorAL.rgb,
            false
        )

        Fonts.fontHonor40.drawString(
            "|",
            startX + fastLen1 + textWidth + uiToUIDistance - 1F,
            startY + 9F,
            Color(120, 120, 120, 250).rgb,
            false
        )

        Fonts.fontHonor40.drawString(
            versionNameUp,
            startX + fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance,
            startY + 4.5F,
            Color(255, 255, 255, 255).rgb,
            false
        )

        Fonts.fontHonor35.drawString(
            versionNameDown,
            startX + fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance,
            startY + 14F,
            Color(170, 170, 170, 170).rgb,
            false
        )

        Fonts.fontHonor40.drawString(
            "|",
            startX + fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance + textBar2 + uiToUIDistance - 1F,
            startY + 9F,
            Color(120, 120, 120, 250).rgb,
            false
        )

        Fonts.fontHonor40.drawString(
            serverip,
            startX + fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance + textBar2 + uiToUIDistance + LineWidth + uiToUIDistance,
            startY + 4.5F,
            Color(255, 255, 255, 255).rgb,
            false
        )

        Fonts.fontHonor35.drawString(
            playerPing,
            startX + fastLen1 + textWidth + uiToUIDistance + LineWidth + uiToUIDistance + textBar2 + uiToUIDistance + LineWidth + uiToUIDistance,
            startY + 14F,
            Color(170, 170, 170, 170).rgb,
            false
        )
    }

    private fun drawBasicNormalUI(sr: ScaledResolution, width: Float, height: Float) {
        val centerX = sr.scaledWidth / 2f
        val posY = sr.scaledHeight / 9f
        val textColor = Color(textColorR.get(), textColorG.get(), textColorB.get())
        // 基于当前宽度重新计算位置
        val logoWidth = 20
        val watermarkText = buildString {
            append("${clientName.get()} | ${mc.session.username} | ${Minecraft.getDebugFPS()}fps")
            if (showLatency.get()) append(" | ${mc.thePlayer.getPing()}ms")
            if (showMemory.get()) append(" | RAM: ${getUsedMemory()}/${getMaxMemory()}MB")
        }
        val textWidth = Fonts.fontHonor40.getStringWidth(watermarkText)

        val contentWidth = logoWidth + 10 + textWidth
        val offsetX = (width - contentWidth) / 2
        var currentX = centerX - width / 2 + offsetX + logoWidth + 5
        // 图标绘制
        RenderUtils.drawImage(
            ResourceLocation("liquidbounce/obai.png"),
            (centerX - width / 2 + offsetX).toInt(),
            (posY + height / 2 - 9).toInt(),
            18, 18, textColor
        )
        Fonts.fontHonor40.drawString(
            clientName.get(),
            currentX,
            posY + height/2 - Fonts.fontHonor40.FONT_HEIGHT/2,
            textColor.rgb
        )
        currentX += Fonts.fontHonor40.getStringWidth(clientName.get() + " | ")
        Fonts.fontHonor40.drawString(
            "| ${mc.session.username} | ${Minecraft.getDebugFPS()}fps" +
                    (if (showLatency.get()) " | ${mc.thePlayer.getPing()}ms" else "") +
                    (if (showMemory.get()) " | RAM: ${getUsedMemory()}/${getMaxMemory()}MB" else ""),
            currentX - 3,
            posY + height/2 - Fonts.fontHonor40.FONT_HEIGHT/2,
            Color.WHITE.rgb
        )
    }

    private fun drawScaffoldUI(sr: ScaledResolution, width: Float, height: Float) {
        drawBasicScaffoldUI(sr, width, height)
    }

    private fun drawBasicScaffoldUI(sr: ScaledResolution, width: Float, height: Float) {
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
        val rightPadding = containerPadding + (width - (iconSize + elementSpacing * 2 + progressWidth + textWidth)) / 2

        // 物品图标
        if (stack?.item is ItemBlock) {
            glPushMatrix()
            enableGUIStandardItemLighting()
            mc.renderItem.renderItemAndEffectIntoGUI(
                stack,
                (startX + containerPadding).toInt(),
                (startY + (height - 16) / 2).toInt()
            )
            disableStandardItemLighting()
            glPopMatrix()
        }

        // 进度条背景
        val progressBarY = startY + height / 2 - 2
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
            startY + height / 2 - Fonts.font40.FONT_HEIGHT / 2,
            Color.WHITE.rgb
        )
    }

    private fun calculateScaffoldSize(sr: ScaledResolution): Pair<Float, Float> {
        return run {
            val stack = mc.thePlayer?.inventory?.getStackInSlot(SilentHotbar.currentSlot)
            val textWidth = Fonts.font40.getStringWidth("${stack?.stackSize ?: 0} blocks").toFloat()
            Pair(200f + textWidth, 36f)
        }
    }

    private fun drawBackground(sr: ScaledResolution, width: Float, height: Float) {
        val screenWidth = sr.scaledWidth.toFloat()
        // 固定顶部Y坐标为原始位置（屏幕1/9处）
        val fixedTopY = sr.scaledHeight / 9f

        // 计算动态底部Y坐标
        val dynamicBottomY = fixedTopY + animHeight

        // 绘制主背景（保持顶部固定）
        RenderUtils.drawRoundedRect(
            (screenWidth - animWidth) / 2, // 保持水平居中
            fixedTopY,                     // 固定顶部
            (screenWidth + animWidth) / 2, // 左右扩展
            dynamicBottomY,                // 向下拉伸
            bgColor.rgb,                   // 使用固定透明度
            15f
        )

        // 阴影系统调整
        when {
            normalMode.get() == "Opai" && shadowEnabled.get() -> {
                GlowUtils.drawGlow(
                    (screenWidth - animWidth)/2 - 5,
                    fixedTopY,
                    animWidth + 10,
                    animHeight,
                    shadowStrengh.get(),
                    bgColor
                )
            }

            normalMode.get() == "Opal" && opaiShadow.get() -> {
                GlowUtils.drawGlow(
                    (screenWidth - animWidth)/2,
                    fixedTopY,
                    animWidth,
                    animHeight + 10,
                    opaiShadowStrength.get() * 13,
                    Color(255, 255, 255, 80)
                )
            }
        }
    }


    private fun drawOpaiShadow(startX: Float, startY: Float, width: Float, height: Float) {
        GlowUtils.drawGlow(
            startX, startY,
            width, height,
            (opaiShadowStrength.get() * 13F).toInt(),
            Color(0, 0, 0, (120 * uiAnimProgress).toInt()))
    }

    private fun getUsedMemory() =
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)

    private fun getMaxMemory() = Runtime.getRuntime().maxMemory() / (1024 * 1024)
    override fun onDisable() {
        animationProgress = 0f
        isAnimating = true
    }
}
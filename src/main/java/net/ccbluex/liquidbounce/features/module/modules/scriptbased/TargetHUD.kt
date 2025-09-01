package net.ccbluex.liquidbounce.features.module.modules.scriptbased

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.animations.ContinualAnimation
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.floatValue
import net.ccbluex.liquidbounce.value.intValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumChatFormatting.BOLD
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.*


object TargetHUD : Module("TargetHUD", Category.HUD, hideModule = false) {

    // General Settings
    private val hudStyle by ListValue(
        "Style", arrayOf(
            "Flux",
            "Arc",
            "Compact",
            "Moon4",
            "Novoline",
            "戶籍",
            "Chill",
            "Myau",
            "RavenB4",
            "Naven",
            "Layer",
            "Southside",
            "Animated"
        ), "Flux"
    )
    private val posX by intValue("PosX", 0, -400..400) { hudStyle != "Layer" }
    private val posY by intValue("PosY", 0, -400..400) { hudStyle != "Layer" }
    private val animSpeed by floatValue("AnimationSpeed", 0.1F, 0.01F..0.5F)

    // Flux Settings
    private val fluxColorMode by ListValue(
        "Flux-Color",
        arrayOf("Custom", "Health", "Rainbow"),
        "Health"
    ) { hudStyle == "Flux" }
    private val fluxColorRed by intValue("Flux-Red", 0, 0..255) { hudStyle == "Flux" && fluxColorMode == "Custom" }
    private val fluxColorGreen by intValue(
        "Flux-Green",
        120,
        0..255
    ) { hudStyle == "Flux" && fluxColorMode == "Custom" }
    private val fluxColorBlue by intValue("Flux-Blue", 255, 0..255) { hudStyle == "Flux" && fluxColorMode == "Custom" }

    // Arc Settings
    private val arcRainbow by _boolean("Arc-Rainbow", true) { hudStyle == "Arc" }
    private val arcColorRed by intValue("Arc-Red", 255, 0..255) { hudStyle == "Arc" && !arcRainbow }
    private val arcColorGreen by intValue("Arc-Green", 255, 0..255) { hudStyle == "Arc" && !arcRainbow }
    private val arcColorBlue by intValue("Arc-Blue", 255, 0..255) { hudStyle == "Arc" && !arcRainbow }

    // Myau Settings
    private val rainbow by _boolean("Myau-Rainbow", true) { hudStyle == "Myau" }
    private val borderRed by intValue("Myau-Border-Red", 255, 0..255) { hudStyle == "Myau" }
    private val borderGreen by intValue("Myau-Border-Green", 255, 0..255) { hudStyle == "Myau" }
    private val borderBlue by intValue("Myau-Border-Blue", 255, 0..255) { hudStyle == "Myau" }
    private val showAvatar by _boolean("Myau-Show-Avatar", true) { hudStyle == "Myau" }

    // RavenB4 Settings
    val barColorR by intValue("RavenB4-BarColorR", 255, 0..255) { hudStyle == "RavenB4" }
    private val barColorG by intValue("RavenB4-BarColorG", 255, 0..255) { hudStyle == "RavenB4" }
    private val barColorB by intValue("RavenB4-BarColorB", 255, 0..255) { hudStyle == "RavenB4" }
    private val animSpeedRB4 by intValue("RavenB4-AnimSpeed", 3, 1..10) { hudStyle == "RavenB4" }

    // Moon4 Settings
    private val moon4BarColorR by intValue("Moon4-BarR", 70, 0..255) { hudStyle == "Moon4" }
    private val moon4BarColorG by intValue("Moon4-BarG", 130, 0..255) { hudStyle == "Moon4" }
    private val moon4BarColorB by intValue("Moon4-BarB", 255, 0..255) { hudStyle == "Moon4" }
    private val moon4BGColorR by intValue("Moon4-BGR", 30, 0..255) { hudStyle == "Moon4" }
    private val moon4BGColorG by intValue("Moon4-BGG", 30, 0..255) { hudStyle == "Moon4" }
    private val moon4BGColorB by intValue("Moon4-BGB", 30, 0..255) { hudStyle == "Moon4" }
    private val moon4BGColorA by intValue("Moon4-BGA", 180, 0..255) { hudStyle == "Moon4" }
    private val moon4AnimSpeed by intValue("Moon4-AnimSpeed", 4, 1..10) { hudStyle == "Moon4" }

    // State Variables
    private val decimalFormat = DecimalFormat("0.0", DecimalFormatSymbols(Locale.ENGLISH))
    private var target: EntityLivingBase? = null
    private var lastTarget: EntityLivingBase? = null
    override var hue = 0.0f

    // Wave样式设置项
    private val waveColor by ListValue(
        "Wave-Color",
        arrayOf("Health", "Rainbow", "Custom"),
        "Health"
    ) { hudStyle == "Wave" }
    private val waveCustomColor = Color(0, 150, 255)

    // Pulse样式设置项
    private val pulseSpeed by floatValue("Pulse-Speed", 0.05F, 0.01F..0.2F) { hudStyle == "Pulse" }
    private val pulseThickness by floatValue("Pulse-Thickness", 4F, 1F..10F) { hudStyle == "Pulse" }

    // Neon样式设置项
    private val neonGlow by _boolean("Neon-Glow", true) { hudStyle == "Neon" }
    private val neonColor = Color(255, 50, 255)

    // Animation States
    private var easingHealth = 0F
    private var moon4EasingHealth = 0F
    private var southsideEasingHealth = 0F
    private var slideIn = 0F
    private var damageHealth = 0F
    private val animate = ContinualAnimation()
    private var prevHealth = -1f
    private var avatarScale = 1f
    private var avatarTargetScale = 1f
    private var avatarTintAlpha = 0f // 0..1
    private var isHitAnimating = false
    private var hitAnimTimer = 0f
    private val hitAnimDuration = 500f // 毫秒
    override fun onEnable() {
        easingHealth = 0F
        moon4EasingHealth = 0F
        southsideEasingHealth = 0f
        slideIn = 0F
        damageHealth = 0f
        hue = 0.0f
        target = null
        lastTarget = null
        prevHealth = -1f
        avatarScale = 1f
        avatarTargetScale = 1f
        avatarTintAlpha = 0f
        isHitAnimating = false
        hitAnimTimer = 0f
    }

    private fun updateSouthsideEasingHealth(targetHealth: Float, maxHealth: Float) {
        val changeAmount = abs(southsideEasingHealth - targetHealth)
        var speed = 0.02f * deltaTime

        if (changeAmount > 5) {
            speed *= 2.0f
        } else if (changeAmount > 2) {
            speed *= 1.5f
        }

        if (abs(southsideEasingHealth - targetHealth) < 0.1f) {
            southsideEasingHealth = targetHealth
        } else if (southsideEasingHealth > targetHealth) {
            southsideEasingHealth -= min(speed * 1.2f, southsideEasingHealth - targetHealth)
        } else {
            southsideEasingHealth += min(speed, targetHealth - southsideEasingHealth)
        }
        southsideEasingHealth = southsideEasingHealth.coerceAtMost(maxHealth)
    }

    private fun renderLayer() {
        KillAura.target

    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val kaTarget = KillAura.target

        // Update target logic
        if (kaTarget != null && kaTarget is EntityPlayer && !AntiBot.isBot(kaTarget)) {
            target = kaTarget
        } else if (mc.currentScreen is GuiChat) {
            target = mc.thePlayer
        } else if (target != null && (KillAura.target == null || !target!!.isEntityAlive || AntiBot.isBot(target!!))) {
            target = null
        }

        // Handle target change for animations
        if (target != lastTarget) {
            if (lastTarget != null) { // Smooth out previous target
                easingHealth = lastTarget!!.health
                damageHealth = lastTarget!!.health
            } else if (target != null) { // Instantly set for new target
                easingHealth = target!!.health
                damageHealth = target!!.health
            }
            // Instantly set health for animated styles to prevent animating from old target
            if (target != null) {
                moon4EasingHealth = target!!.health
                southsideEasingHealth = target!!.health
            }
        }

        lastTarget = target

        // Update global animations
        hue += 0.05f * deltaTime * 0.1f
        if (hue > 1F) hue = 0F

        slideIn = lerp(slideIn, if (target != null) 1F else 0F, animSpeed)

        if (slideIn < 0.01F && target == null) return

        val sr = ScaledResolution(mc)

        // Centralized positioning
        val x = sr.scaledWidth / 2F + posX
        val y = sr.scaledHeight / 2F + posY

        when (hudStyle.lowercase(Locale.getDefault())) {
            // New Styles
            "flux" -> renderFluxHUD(x, y)
            "arc" -> renderArcHUD(x, y)
            "compact" -> renderCompactHUD(x, y)
            "moon4" -> renderMoon4HUD(x, y)
            "novoline" -> renderNovolineHUD(sr)
            "戶籍" -> render0x01a4HUD(sr)
            "chill" -> renderChillHUD(sr)
            "myau" -> renderMyauHUD(sr)
            "ravenb4" -> renderRavenB4HUD(sr)
            "naven" -> renderNavenHUD(sr)
            "wave" -> renderWaveHUD(x, y)
            "pulse" -> renderPulseHUD(x, y)
            "neon" -> renderNeonHUD(x, y)
            "layer" -> renderLayerHUD()
            "southside" -> renderSouthsideHUD(x, y)
            "animated" -> renderAnimatedHUD(x, y)
        }
    }
    private fun renderSouthsideHUD(x: Float, y: Float) {
        val entity = TargetHUD.target
            ?: TargetHUD.lastTarget ?: return

        val health = entity.health
        val maxHealth = entity.maxHealth
        val healthPercent = (health / maxHealth).coerceIn(0f, 1f)

        // Update easing health
        TargetHUD.updateSouthsideEasingHealth(health, maxHealth)
        val easingHealthPercent = (TargetHUD.southsideEasingHealth / maxHealth).coerceIn(0f, 1f)

        val name = entity.name
        val width = Fonts.font40.getStringWidth(name) + 75f
        val presentWidth = easingHealthPercent * width

        GlStateManager.pushMatrix()

        // Animation
        val animOutput = TargetHUD.slideIn
        GlStateManager.translate((x + width / 2) * (1 - animOutput).toDouble(), (y + 20) * (1 - animOutput).toDouble(), 0.0)
        GlStateManager.scale(animOutput, animOutput, animOutput)

        // Background
        RenderUtils.drawRect(x, y, x + width, y + 40, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawRect(x, y, x + presentWidth, y + 40, Color(230, 230, 230, 100).rgb)

        // Vertical health indicator
        val healthColor = when {
            healthPercent > 0.5 -> Color(63, 157, 4, 150)
            healthPercent > 0.25 -> Color(255, 144, 2, 150)
            else -> Color(168, 1, 1, 150)
        }
        RenderUtils.drawRect(x, y + 12.5f, x + 3, y + 27.5f, healthColor.rgb)

        // Head
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, x.toInt() + 7, y.toInt() + 7, 26, 26, Color.WHITE)
        } ?: RenderUtils.drawRect(x + 6, y + 6, x + 34, y + 34, Color.BLACK.rgb)


        // Text
        Fonts.font40.drawString(name, x + 40, y + 7, Color(200, 200, 200, 255).rgb)
        Fonts.font40.drawString("${health.toInt()} HP", x + 40, y + 22, Color(200, 200, 200, 255).rgb)

        // Held Item
        val itemStack = entity.heldItem
        val itemX = x + Fonts.font40.getStringWidth(name) + 50
        if (itemStack != null) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(itemX, y + 12, 0f)
            GlStateManager.scale(1.5f, 1.5f, 1.5f) // Make item bigger
            RenderHelper.enableGUIStandardItemLighting()
            mc.renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0)
            RenderHelper.disableStandardItemLighting()
            GlStateManager.popMatrix()
        } else {
            Fonts.font40.drawString("?", x + Fonts.font40.getStringWidth(name) + 55, y + 11, Color(200, 200, 200, 255).rgb)
        }

        GlStateManager.popMatrix()
    }
    private fun renderAnimatedHUD(x: Float, y: Float) {
        val entity = target ?: lastTarget ?: return

        // Size and layout - 调整以实现更紧凑的设计
        val width = 190f
        val height = 52f
        val padding = 8f
        val avatarSize = 36f
        val left = x
        val top = y

        // Update health easing
        easingHealth = lerp(easingHealth, entity.health, animSpeed * 1.5f)

        // Initialize prevHealth if needed
        if (prevHealth < 0f) prevHealth = entity.health

        // Detect hit: health decreased
        if (entity.health < prevHealth && !isHitAnimating) {
            isHitAnimating = true
            hitAnimTimer = 0f
            avatarTargetScale = 0.85f
            // tint alpha will be driven in animation update
        }

        // Update hit animation state
        if (isHitAnimating) {
            hitAnimTimer += deltaTime
            val progress = (hitAnimTimer / hitAnimDuration).coerceIn(0f, 1f)

            when {
                progress < 0.4f -> {
                    avatarTargetScale = 0.85f
                    val desiredTint = 1f
                    avatarTintAlpha = lerp(avatarTintAlpha, desiredTint, 0.25f)
                }
                progress < 0.75f -> {
                    avatarTargetScale = 1.12f // quick bounce
                    val desiredTint = 0.6f
                    avatarTintAlpha = lerp(avatarTintAlpha, desiredTint, 0.25f)
                }
                else -> {
                    avatarTargetScale = 1f
                    avatarTintAlpha = lerp(avatarTintAlpha, 0f, 0.25f)
                }
            }

            if (progress >= 1f) {
                isHitAnimating = false
                avatarTargetScale = 1f
                avatarTintAlpha = 0f
                hitAnimTimer = 0f
            }
        } else {
            // Ensure avatar returns to normal if no animation
            avatarTargetScale = 1f
            avatarTintAlpha = lerp(avatarTintAlpha, 0f, 0.12f)
        }

        // Smoothly interpolate avatar scale
        avatarScale = lerp(avatarScale, avatarTargetScale, 0.22f)

        // Draw HUD with slide-in animation
        GlStateManager.pushMatrix()
        GlStateManager.translate(left, top, 0f)
        GlStateManager.scale(slideIn, slideIn, 1f)

        // Subtle inner shadow/background for contrast
        RenderUtils.drawRoundedRect(4f, 4f, width - 4f, height - 4f, Color(245, 245, 245).rgb, 6f)

        // Draw avatar with scaling and tint overlay
        val ax = padding
        val ay = (height - avatarSize) / 2f

        GlStateManager.pushMatrix()
        GlStateManager.translate(ax, ay, 0f)
        // Apply scaling from the center of the avatar
        GlStateManager.translate(avatarSize / 2f, avatarSize / 2f, 0f)
        GlStateManager.scale(avatarScale, avatarScale, 1f)
        GlStateManager.translate(-avatarSize / 2f, -avatarSize / 2f, 0f)

        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, 0, 0, avatarSize.toInt(), avatarSize.toInt(), Color.WHITE)
        }
        GlStateManager.popMatrix()

        // Tint overlay (red) to indicate hit
        if (avatarTintAlpha > 0.01f) {
            val tint = Color(255, 50, 50, (avatarTintAlpha * 140).toInt())
            RenderUtils.drawRoundedRect(ax, ay, ax + avatarSize, ay + avatarSize, tint.rgb, 6f)
        }

        // --- Repositioned Name and Health Bar for compact layout ---
        val nameX = ax + avatarSize + 10f // 减小头像和文字之间的间距
        val barH = 10f // 略微减小生命条的高度
        val nameFontHeight = Fonts.font40.FONT_HEIGHT.toFloat() // 获取字体高度
        val contentBlockHeight = nameFontHeight + 4f + barH // 计算文字+间隙+血条的总高度
        val contentBlockY = (height - contentBlockHeight) / 2f // 计算该内容块的垂直居中起始Y坐标

        // Name above health bar
        val nameY = contentBlockY
        Fonts.font40.drawString(entity.name, nameX, nameY, Color(30, 30, 30).rgb)

        // Health bar (rounded)
        val barX = nameX
        val barY = nameY + nameFontHeight + 4f // 将生命条紧跟在名字下方，并保留一个小的间隙
        val barW = width - barX - padding

        // Background of bar (light)
        RenderUtils.drawRoundedRect(barX, barY, barX + barW, barY + barH, Color(0, 0, 0, 70).rgb, 6f)

        // Foreground: eased health
        val healthPercent = (easingHealth / entity.maxHealth).coerceIn(0f, 1f)
        val fillW = barW * healthPercent
        val barColor = ColorUtils.healthColor(easingHealth, entity.maxHealth)

        RenderUtils.drawRoundedRect(barX, barY, barX + fillW, barY + barH, barColor.rgb, 6f)

        GlStateManager.popMatrix()

        // Update prevHealth for next frame
        prevHealth = entity.health
    }

    private fun renderLayerHUD() {
        val entity = target ?: lastTarget ?: return
        val sr = ScaledResolution(mc)

        val targetHealth = target?.health ?: 0f
        easingHealth = lerp(easingHealth, targetHealth, animSpeed)
        val maxHealth = entity.maxHealth.takeIf { it > 0 } ?: 20f
        val healthPercent = (easingHealth / maxHealth).coerceIn(0f, 1f)

        val healthBarWidth = sr.scaledWidth * healthPercent

        val overlayColor = Color(255, 80, 80, (120 * slideIn).toInt())
        if (healthBarWidth > 0) {
            RenderUtils.drawRect(0f, 0f, healthBarWidth, sr.scaledHeight.toFloat(), overlayColor.rgb)
        }
    }

    private fun renderMoon4HUD(x: Float, y: Float) {
        val entity = target ?: lastTarget ?: return

        // Animate towards the current target's health, or towards 0 if no target.
        val currentHealth = target?.health ?: 0f
        moon4EasingHealth += ((currentHealth - moon4EasingHealth) / 2.0F.pow(10.0F - moon4AnimSpeed)) * deltaTime

        val mainColor = Color(moon4BarColorR, moon4BarColorG, moon4BarColorB)
        val bgColor = Color(moon4BGColorR, moon4BGColorG, moon4BGColorB, moon4BGColorA)

        val boldName = "$BOLD${entity.name}"
        val healthInt = entity.health.toInt()
        val percentText = "$BOLD${healthInt}HP"

        val nameLength = (Fonts.fontSF40.getStringWidth(boldName)).coerceAtLeast(
            Fonts.fontSF35.getStringWidth(percentText)
        ).toFloat() + 20F

        val healthPercent = (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        val barWidth = healthPercent * (nameLength - 2F)
        val animateThingy =
            (moon4EasingHealth.coerceIn(entity.health, entity.maxHealth) / entity.maxHealth) * (nameLength - 2F)

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0F)
        GlStateManager.scale(slideIn, slideIn, slideIn)

        // Backgrounds
        RenderUtils.drawRoundedRect(-2F, -2F, 3F + nameLength + 36F, 2F + 36F, bgColor.rgb, 3f)
        RenderUtils.drawRoundedRect(-1F, -1F, 2F + nameLength + 36F, 1F + 36F, Color(0, 0, 0, 50).rgb, 3f)

        // Head with Stencil
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let { playerInfo ->
            Stencil.write(false)
            glDisable(GL_TEXTURE_2D)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            RenderUtils.drawRoundedRect(1f, 0.5f, 1f + 35f, 0.5f + 35f, Color.WHITE.rgb, 7F)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            Stencil.erase(true)
            Target().drawHead(playerInfo.locationSkin, 1, 0, 35, 35, Color.WHITE)
            Stencil.dispose()
        }

        // Text
        Fonts.fontSF40.drawStringWithShadow(boldName, 2F + 36F, 2F, -1)
        Fonts.fontSF35.drawStringWithShadow(percentText, 38F, 15F, Color.WHITE.rgb)

        // Health Bar
        RenderUtils.drawRoundedRect(37F, 23F, 37F + nameLength, 33f, Color(0, 0, 0, 100).rgb, 3f)
        if (moon4EasingHealth > entity.health) {
            RenderUtils.drawRoundedRect(38F, 24F, 38F + animateThingy, 32f, mainColor.darker().rgb, 3f)
        }
        RenderUtils.drawRoundedRect(38F, 24F, 38F + barWidth, 32f, mainColor.rgb, 3f)

        GlStateManager.popMatrix()
    }

    private fun renderFluxHUD(x: Float, y: Float) {
        val entity = target ?: lastTarget ?: return
        val width = 140F
        val height = 46F

        // Update animations
        easingHealth = lerp(easingHealth, entity.health, animSpeed)

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0F)
        GlStateManager.scale(slideIn, slideIn, slideIn)
        GlStateManager.translate(-x, -y, 0F)

        // Background
        RenderUtils.drawRoundedRect(x, y, x + width, y + height, Color(25, 25, 25, (200 * slideIn).toInt()).rgb, 4F)

        // Head
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, (x + 6).toInt(), (y + 6).toInt(), 34, 34, Color.WHITE)
        }

        // Name
        Fonts.font40.drawString(entity.name, x + 46, y + 8, Color.WHITE.rgb)

        // Health Bar
        val healthPercent = (easingHealth / entity.maxHealth).coerceIn(0F, 1F)
        val healthBarWidth = (width - 52) * healthPercent
        val barColor = when (fluxColorMode) {
            "Custom" -> Color(fluxColorRed, fluxColorGreen, fluxColorBlue)
            "Rainbow" -> Color.getHSBColor(hue, 0.7f, 0.9f)
            else -> ColorUtils.healthColor(easingHealth, entity.maxHealth)
        }

        RenderUtils.drawRect(x + 46, y + 22, x + width - 6, y + 30, Color(45, 45, 45).rgb)
        RenderUtils.drawRect(x + 46, y + 22, x + 46 + healthBarWidth, y + 30, barColor.rgb)

        // Health Text
        val healthText = decimalFormat.format(easingHealth)
        Fonts.font35.drawString(healthText, x + 48, y + 23, Color.WHITE.rgb)

        // Distance Text
        val distance = mc.thePlayer.getDistanceToEntity(entity)
        val distanceText = "${decimalFormat.format(distance)}m"
        // 您可以在这里添加一个图标
        // drawIcon(x + 46, y + 33, icon_path)
        Fonts.font35.drawString(distanceText, x + 46, y + 33, Color(200, 200, 200).rgb)

        GlStateManager.popMatrix()
    }

    private fun renderWaveHUD(x: Float, y: Float) {
        val entity = target ?: return
        val width = 160F
        val height = 50F

        // 动态健康值缓存
        easingHealth = lerp(easingHealth, entity.health, animSpeed)

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0F)
        GlStateManager.scale(slideIn, slideIn, 1F)

        // 背景层
        RenderUtils.drawRoundedRect(0F, 0F, width, height, Color(15, 15, 15, 220).rgb, 6F)

        // 头像绘制
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, 5, 5, 40, 40, Color.WHITE)
        }

        // 名称文本
        Fonts.font40.drawString(entity.name, 50F, 8F, Color.WHITE.rgb)

        // 波浪健康条
        easingHealth / entity.maxHealth
        val waveHeight = 12F
        val waveAmplitude = 2F
        val waveFrequency = 0.1F

        // 健康条背景
        RenderUtils.drawRect(50F, 25F, width - 10F, 35F, Color(30, 30, 30).rgb)

        // 动态波浪绘制
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glBegin(GL_TRIANGLE_STRIP)
        val barWidth = width - 60F
        val color = when (waveColor) {
            "Health" -> ColorUtils.healthColor(easingHealth, entity.maxHealth)
            "Rainbow" -> Color.getHSBColor((hue * 3) % 1, 0.8f, 0.9f)
            else -> waveCustomColor
        }

        for (i in 0..barWidth.toInt()) {
            val progress = i / barWidth
            val offset =
                sin((progress * waveFrequency + System.currentTimeMillis() * 0.001) * 2 * PI).toFloat() * waveAmplitude
            val waveY = 25F + waveHeight + offset

            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 0.8f)
            glVertex2f(50F + i, 25F + waveHeight)
            glVertex2f(50F + i, waveY)
        }
        glEnd()

        // 健康数值显示
        val healthText = "${decimalFormat.format(easingHealth)}HP"
        Fonts.font35.drawString(healthText, width - Fonts.font35.getStringWidth(healthText) - 5F, 27F, Color.WHITE.rgb)

        GlStateManager.popMatrix()
    }

    private fun renderArcHUD(x: Float, y: Float) {
        val entity = target ?: lastTarget ?: return
        val size = 50F

        // Update animations
        easingHealth = lerp(easingHealth, entity.health, animSpeed)

        GlStateManager.pushMatrix()
        val scale = slideIn.pow(0.5f)
        GlStateManager.translate(x + size / 2, y + size / 2, 0F)
        GlStateManager.scale(scale, scale, scale)
        GlStateManager.translate(-(x + size / 2), -(y + size / 2), 0F)

        // Draw Head clipped in a circle
        RenderUtils.withClipping({ drawCircle(x + size / 2, y + size / 2, size / 2 - 3, Color.WHITE.rgb) }, {
            mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
                Target().drawHead(
                    it.locationSkin,
                    x.toInt() + 3,
                    y.toInt() + 3,
                    (size - 6).toInt(),
                    (size - 6).toInt(),
                    Color.WHITE
                )
            }
        })

        // Health Arc
        val healthPercent = (easingHealth / entity.maxHealth).coerceIn(0F, 1F)
        val arcColor =
            if (arcRainbow) Color.getHSBColor(hue, 0.6f, 1f) else Color(arcColorRed, arcColorGreen, arcColorBlue)

        // Background Arc
        drawCircleArc(
            x + size / 2,
            y + size / 2,
            size / 2 - 1.5F,
            3F,
            0F,
            360F,
            Color(40, 40, 40, (200 * slideIn).toInt())
        )
        // Foreground Arc
        if (healthPercent > 0) {
            drawCircleArc(x + size / 2, y + size / 2, size / 2 - 1.5F, 3F, -90F, 360F * healthPercent, arcColor)
        }

        // Text Info
        val textX = x + size + 5
        val nameColor = Color(255, 255, 255, (255 * slideIn).toInt()).rgb
        val healthColor = Color(200, 200, 200, (255 * slideIn).toInt()).rgb
        Fonts.font40.drawString(entity.name, textX, y + 8, nameColor)
        Fonts.font35.drawString("HP: ${decimalFormat.format(easingHealth)}", textX, y + 24, healthColor)

        GlStateManager.popMatrix()
    }

    private fun renderCompactHUD(x: Float, y: Float) {
        val entity = target ?: lastTarget ?: return
        val width = 120F
        val height = 18F

        // Update animations
        if (target != null) {
            easingHealth = lerp(easingHealth, entity.health, animSpeed * 1.5f)
            if (abs(entity.health - damageHealth) > 0.1f && easingHealth < damageHealth) {
                // Damage flash effect, damageHealth catches up to easingHealth
                damageHealth = lerp(damageHealth, easingHealth, animSpeed * 0.5f)
            } else {
                damageHealth = easingHealth
            }
        } else {
            // When target is lost, both bars fade out
            easingHealth = lerp(easingHealth, 0f, animSpeed)
            damageHealth = lerp(damageHealth, 0f, animSpeed)
        }

        if (target != null && target != lastTarget) {
            damageHealth = entity.maxHealth // Reset damage bar on new target
        }

        GlStateManager.pushMatrix()
        val scale = slideIn.pow(2f)
        GlStateManager.translate(x + width / 2, y + height / 2, 0F)
        GlStateManager.scale(1f, scale, 1f)
        GlStateManager.translate(-(x + width / 2), -(y + height / 2), 0F)

        if (scale < 0.05f) {
            GlStateManager.popMatrix()
            return
        }

        // Background
        RenderUtils.drawRect(x, y, x + width, y + height, Color(20, 20, 20, (180 * scale).toInt()).rgb)

        // Health Bars
        val healthPercent = (easingHealth / entity.maxHealth).coerceIn(0F, 1F)
        val damagePercent = (damageHealth / entity.maxHealth).coerceIn(0F, 1F)
        val barColor = ColorUtils.healthColor(easingHealth, entity.maxHealth)

        // Damage bar (the trail)
        RenderUtils.drawRect(x + 2, y + 2, x + 2 + (width - 4) * damagePercent, y + height - 2, barColor.brighter().rgb)
        // Health bar
        RenderUtils.drawRect(x + 2, y + 2, x + 2 + (width - 4) * healthPercent, y + height - 2, barColor.rgb)

        // Text on bar
        val text = "${entity.name} - ${decimalFormat.format(easingHealth)} HP"
        Fonts.font35.drawCenteredString(
            text,
            x + width / 2,
            y + height / 2 - Fonts.font35.fontHeight / 2 + 1,
            Color.WHITE.rgb,
            true
        )

        GlStateManager.popMatrix()
    }

    private fun renderPulseHUD(x: Float, y: Float) {
        val entity = target ?: return
        val size = 70F

        val pulsePhase = (System.currentTimeMillis() % 1000) / 1000f
        val pulseScale = 1f + sin(pulsePhase * 2 * PI) * 0.05f

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0F)
        GlStateManager.scale(pulseScale, pulseScale, 1.0)

        RenderUtils.drawFilledCircle(x + size / 2F, y + size / 2F, size / 2F, Color(20, 20, 20, 200))

        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(
                it.locationSkin,
                x.toInt() + 5,
                y.toInt() + 5,
                (size - 10).toInt(),
                (size - 10).toInt(),
                Color.WHITE
            )
        }

        val healthPercent = entity.health / entity.maxHealth
        val ringColor = ColorUtils.healthColor(entity.health, entity.maxHealth)

        glPushMatrix()
        glTranslatef(x + size / 2, y + size / 2, 0F)
        for (i in 0..3) {
            1.1f + i * 0.1f
            val alpha = (200 * (1 - i / 3f)).toInt()
            drawCircleArc(
                0F,
                0F,
                size / 2 + i * 3,
                pulseThickness,
                -90F,
                360F * healthPercent,
                Color(ringColor.red, ringColor.green, ringColor.blue, alpha)
            )
        }
        glPopMatrix()

        Fonts.font40.drawCenteredString(entity.name, x + size / 2, y + size + 5F, Color.WHITE.rgb)
        Fonts.font35.drawCenteredString(
            "${decimalFormat.format(entity.health)}m",
            x + size / 2,
            y + size + 20F,
            Color(200, 200, 200).rgb
        )

        GlStateManager.popMatrix()
    }

    private fun renderNeonHUD(x: Float, y: Float) {
        val entity = target ?: return
        val width = 180F
        val height = 60F

        easingHealth = lerp(easingHealth, entity.health, animSpeed * 2)

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0F)
        GlStateManager.scale(slideIn, slideIn, 1F)
        RenderUtils.drawRoundedRect(0F, 0F, width, height, Color(10, 10, 10, 180).rgb, 8F)

        if (neonGlow) {
            for (i in 1..3) {
                val glowSize = i * 2F
                RenderUtils.drawRoundedRect(
                    -glowSize,
                    -glowSize,
                    width + glowSize,
                    height + glowSize,
                    Color(neonColor.red, neonColor.green, neonColor.blue, 30 / i).rgb,
                    8F + glowSize
                )
            }
        }

        // 头像绘制
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, 8, 8, 44, 44, Color.WHITE)
        }

        // 名称文本
        Fonts.font40.drawString(entity.name, 60F, 12F, Color.WHITE.rgb)

        // 霓虹健康条
        val healthPercent = easingHealth / entity.maxHealth
        val barHeight = 12F
        val barX = 60F
        val barY = 28F

        // 背景条
        RenderUtils.drawRoundedRect(barX, barY, width - 20F, barY + barHeight, Color(30, 30, 30).rgb, 4F)

        // 前景条（霓虹效果）
        val gradientWidth = (width - 80F) * healthPercent
        RenderUtils.drawRoundedRect(
            barX, barY, barX + gradientWidth, barY + barHeight, neonColor.brighter().rgb, 4F
        )

        // 健康数值
        val healthText = "${decimalFormat.format(easingHealth)} / ${entity.maxHealth}"
        Fonts.font35.drawString(
            healthText,
            width - Fonts.font35.getStringWidth(healthText) - 15F,
            barY + 2F,
            neonColor.brighter().rgb
        )

        // 距离显示
        val distance = mc.thePlayer.getDistanceToEntity(entity)
        val distanceText = "${decimalFormat.format(distance)}m"
        Fonts.font35.drawString(distanceText, barX, barY + barHeight + 5F, Color(200, 200, 200).rgb)

        GlStateManager.popMatrix()
    }

    // Helper function to draw an arc, can be moved to RenderUtils later
    private fun drawCircleArc(
        x: Float,
        y: Float,
        radius: Float,
        lineWidth: Float,
        startAngle: Float,
        endAngle: Float,
        color: Color
    ) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(lineWidth)

        glColor4f(color.red / 255F, color.green / 255F, color.blue / 255F, color.alpha / 255F)

        glBegin(GL_LINE_STRIP)
        for (i in (startAngle / 360 * 100).toInt()..(endAngle / 360 * 100).toInt()) {
            val angle = (i / 100.0 * 360.0 * (PI / 180)).toFloat()
            glVertex2f(x + sin(angle) * radius, y + cos(angle) * radius)
        }
        glEnd()

        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawCircle(x: Float, y: Float, radius: Float, color: Int) {
        val side = (radius * 2).toInt()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_POLYGON_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        RenderUtils.glColor(Color(color))
        for (i in 0..side) {
            val angle = i * (Math.PI * 2) / side
            glVertex2d(x + sin(angle) * radius, y + cos(angle) * radius)
        }
        glEnd()
        glDisable(GL_POLYGON_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    override fun onDisable() {
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }

    override val tag get() = hudStyle

    private fun lerp(start: Float, end: Float, speed: Float): Float =
        start + (end - start) * speed * (deltaTime / (1000F / 60F))

    private fun getRainbowColor(): Color = Color.getHSBColor(hue, 1f, 1f)

    // --- Original HUD Render Functions ---
    // (The original functions from your code are placed below, unchanged)

    private fun updateRavenB4Anim(targetHealth: Float) {
        easingHealth += ((targetHealth - easingHealth) / 2.0F.pow(10.0F - animSpeedRB4)) * deltaTime
    }

    private fun renderRavenB4HUD(sr: ScaledResolution) {
        val entity = target ?: return
        val x = sr.scaledWidth / 2F + posX
        val y = sr.scaledHeight / 2F + posY
        val font = Fonts.minecraftFont
        val hp = decimalFormat.format(entity.health)
        val hplength = font.getStringWidth(hp)
        val length = font.getStringWidth(entity.displayName.formattedText)
        val barColor = Color(barColorR, barColorG, barColorB)
        val totalWidth = x + length + hplength + 23F
        val totalHeight = y + 35F

        GlStateManager.pushMatrix()
        updateRavenB4Anim(entity.health)
        RenderUtils.drawRoundedGradientOutlineCorner(x, y, totalWidth, totalHeight, 2F, 8F, barColor.rgb, barColor.rgb)
        RenderUtils.drawRoundedRect(x, y, totalWidth, totalHeight, Color(0, 0, 0, 100).rgb, 4F)
        GlStateManager.enableBlend()
        font.drawStringWithShadow(entity.displayName.formattedText, x + 6F, y + 8F, Color.WHITE.rgb)

        val winOrLose = if (entity.health < mc.thePlayer.health) "W" else "L"
        val wlColor = if (winOrLose == "W") Color(0, 255, 0).rgb else Color(139, 0, 0).rgb
        font.drawStringWithShadow(winOrLose, x + length + hplength + 11.6F, y + 8F, wlColor)

        font.drawStringWithShadow(
            hp,
            x + length + 8F,
            y + 8F,
            ColorUtils.reAlpha(ColorUtils.healthColor(entity.health, entity.maxHealth), 255).rgb
        )

        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        RenderUtils.drawRoundedRect(
            x + 5.0F,
            y + 29.55F,
            x + length + hplength + 18F,
            y + 25F,
            Color(0, 0, 0, 110).rgb,
            2F
        )
        RenderUtils.drawRoundedGradientRectCorner(
            x + 5F,
            y + 25F,
            x + 5F + (easingHealth / entity.maxHealth) * (length + hplength + 13F),
            y + 29.5F,
            4F,
            barColor.rgb,
            barColor.rgb
        )
        GlStateManager.popMatrix()
    }

    private fun renderNavenHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val x = sr.scaledWidth / 2f + posX
        val y = sr.scaledHeight / 2f + posY
        val width = 130f
        val height = 50f

        RenderUtils.drawRoundedRect(x, y, x + width, y + height, Color(10, 10, 30, 120).rgb, 5f)

        mc.netHandler.getPlayerInfo(entity.uniqueID)?.locationSkin?.let {
            Target().drawHead(it, x.toInt() + 7, y.toInt() + 7, 30, 30, Color.WHITE)
        }

        val barX1 = x + 5f
        val barY1 = y + height - 10f
        val barX2 = x + width - 5f
        val barY2 = barY1 + 3f
        RenderUtils.drawRoundedRect(barX1, barY1, barX2, barY2, Color(0, 0, 0, 200).rgb, 2f)

        val healthPercent = entity.health / entity.maxHealth
        val fillX2 = barX1 + (barX2 - barX1) * healthPercent
        RenderUtils.drawRoundedRect(barX1, barY1, fillX2, barY2, Color(160, 42, 42).rgb, 2f)

        Fonts.font35.drawString(entity.name, x + 40f, y + 10f, Color.WHITE.rgb)
        Fonts.font32.drawString("Health: ${"%.2f".format(entity.health)}", x + 40f, y + 22f, Color.WHITE.rgb)
        Fonts.font32.drawString(
            "Distance: ${"%.2f".format(entity.getDistanceToEntity(mc.thePlayer))}",
            x + 40f,
            y + 30f,
            Color.WHITE.rgb
        )
    }

    private fun renderNovolineHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val x = sr.scaledWidth / 2 + this.posX
        val y = sr.scaledHeight / 2 + this.posY
        val width = (38 + Fonts.font35.getStringWidth(entity.name)).coerceAtLeast(118).toFloat()

        RenderUtils.drawRect(x.toFloat(), y.toFloat(), x + width + 14f, y + 44f, Color(0, 0, 0, 120).rgb)

        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, x + 3, y + 3, 30, 30, Color.WHITE)
        }

        Fonts.font35.drawString(entity.name, x + 34.5f, y + 4f, Color.WHITE.rgb)
        Fonts.font35.drawString("Health: ${"%.1f".format(entity.health)}", x + 34.5f, y + 14f, Color.WHITE.rgb)
        Fonts.font35.drawString(
            "Distance: ${"%.1f".format(mc.thePlayer.getDistanceToEntity(entity))}m",
            x + 34.5f,
            y + 24f,
            Color.WHITE.rgb
        )

        RenderUtils.drawRect(x + 2.5f, y + 35.5f, x + width + 11.5f, y + 37.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(
            x + 3f,
            y + 36f,
            x + 3f + (entity.health / entity.maxHealth) * (width + 8f),
            y + 37f,
            ColorUtils.healthColor(entity.health, entity.maxHealth)
        )

        RenderUtils.drawRect(x + 2.5f, y + 39.5f, x + width + 11.5f, y + 41.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(
            x + 3f,
            y + 40f,
            x + 3f + (entity.totalArmorValue / 20f) * (width + 8f),
            y + 41f,
            Color(77, 128, 255).rgb
        )
    }

    private fun renderMyauHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val x = sr.scaledWidth / 2F + posX
        val y = sr.scaledHeight / 2F + posY
        val nameWidth = Fonts.font35.getStringWidth(entity.name)
        val hudWidth = maxOf(80f, nameWidth + 20f)
        val hudHeight = 25f
        val avatarSize = hudHeight

        if (rainbow) {
            hue += 0.0005f
            if (hue > 1f) hue = 0f
        }
        val borderColor = if (rainbow) getRainbowColor() else Color(borderRed, borderGreen, borderBlue)
        val healthBarColor = if (rainbow) getRainbowColor() else Color(
            maxOf(borderRed - 50, 0),
            maxOf(borderGreen - 50, 0),
            maxOf(borderBlue - 50, 0)
        )

        val totalWidth = if (showAvatar) hudWidth + avatarSize else hudWidth

        RenderUtils.drawRect(x - 1, y - 1, x + totalWidth + 1, y, borderColor.rgb)
        RenderUtils.drawRect(x - 1, y + hudHeight, x + totalWidth + 1, y + hudHeight + 1, borderColor.rgb)
        RenderUtils.drawRect(x - 1, y, x, y + hudHeight, borderColor.rgb)
        RenderUtils.drawRect(x + totalWidth, y, x + totalWidth + 1, y + hudHeight, borderColor.rgb)
        RenderUtils.drawRect(x, y, x + totalWidth, y + hudHeight, Color(0, 0, 0, 100).rgb) // Background

        if (showAvatar) {
            mc.netHandler.getPlayerInfo(entity.uniqueID)?.locationSkin?.let {
                Target().drawHead(it, x.toInt(), y.toInt(), avatarSize.toInt(), avatarSize.toInt(), Color.WHITE)
            }
        }

        val textX = if (showAvatar) x + avatarSize + 3 else x + 3
        Fonts.font35.drawString(entity.name, textX, y + 1, Color.WHITE.rgb)
        val healthText = String.format("%.1f", entity.health)
        Fonts.font35.drawString(healthText, textX, y + 11, healthBarColor.rgb)
        Fonts.font35.drawString(
            "\u2764",
            textX + Fonts.font35.getStringWidth(healthText) + 2,
            y + 11,
            healthBarColor.rgb
        )

        val barY = y + 21
        val barWidth = hudWidth - 5f
        RenderUtils.drawRect(textX, barY, textX + barWidth, barY + 3, Color(64, 64, 64).rgb)
        val targetFill = (entity.health / entity.maxHealth) * barWidth
        easingHealth = lerp(easingHealth, targetFill, 0.1f)
        RenderUtils.drawRect(textX, barY, textX + easingHealth, barY + 3, healthBarColor.rgb)

        val playerHealth = mc.thePlayer.health
        val (winLoss, wlColor) = when {
            playerHealth > entity.health -> "W" to Color(0, 255, 0)
            playerHealth < entity.health -> "L" to Color(255, 0, 0)
            else -> "D" to Color(255, 255, 0)
        }
        Fonts.font35.drawString(winLoss, x + totalWidth - Fonts.font35.getStringWidth(winLoss) - 1, y + 1, wlColor.rgb)

        val diff = playerHealth - entity.health
        val diffText = if (diff > 0) "+${"%.1f".format(diff)}" else String.format("%.1f", diff)
        val diffColor = when {
            diff > 0 -> Color(0, 255, 0)
            diff < 0 -> Color(255, 0, 0)
            else -> Color(255, 255, 0)
        }
        Fonts.font35.drawString(
            diffText,
            maxOf(x + totalWidth - Fonts.font35.getStringWidth(diffText) - 1, textX),
            y + 11,
            diffColor.rgb
        )
    }

    private fun render0x01a4HUD(sr: ScaledResolution) {
        val entity = target ?: return
        val x = sr.scaledWidth / 2 + this.posX
        val y = sr.scaledHeight / 2 + this.posY

        RenderUtils.drawRect(x + 11F, y - 15F, x + 150F, y + 90F, Color(30, 30, 30, 200).rgb)
        Fonts.font35.drawString("PLC 全国人口档案查询系统", x + 15f, y - 5f, Color.WHITE.rgb)
        Fonts.font35.drawString("姓名: ${entity.name}", x + 15f, y + 5f, Color.WHITE.rgb)
        Fonts.font35.drawString(
            "健康: ${entity.health.toInt()}/${entity.maxHealth.toInt()}",
            x + 15f,
            y + 25f,
            Color.WHITE.rgb
        )
        Fonts.font35.drawString("资产: ${entity.totalArmorValue}", x + 15f, y + 45f, Color.WHITE.rgb)
        Fonts.font35.drawString("身份证: ${entity.entityId}", x + 15f, y + 65f, Color.WHITE.rgb)
    }

    private fun renderChillHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val x = sr.scaledWidth / 2 + this.posX
        val y = sr.scaledHeight / 2 + this.posY

        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name)
            .coerceAtLeast(Fonts.font72.getStringWidth("%.1f".format(entity.health)))).coerceAtLeast(120F)

        RenderUtils.drawRoundedRect(x.toFloat(), y.toFloat(), x + tWidth, y + 48F, Color(0, 0, 0, 120).rgb, 7F)

        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, x + 4, y + 4, 30, 30, Color.WHITE)
        }

        Fonts.font40.drawString(entity.name, x + 38F, y + 6F, Color.WHITE.rgb)
        Fonts.font72.drawString("%.1f".format(entity.health), x + 38F, y + 17F, Color.WHITE.rgb)

        val healthPercent = entity.health / entity.maxHealth
        RenderUtils.drawRect(x + 4F, y + 38F, x + tWidth - 4F, y + 44F, Color.DARK_GRAY.rgb)
        RenderUtils.drawRect(
            x + 4F,
            y + 38F,
            x + 4F + (tWidth - 8F) * healthPercent,
            y + 44F,
            ColorUtils.healthColor(entity.health, entity.maxHealth)
        )
    }
}
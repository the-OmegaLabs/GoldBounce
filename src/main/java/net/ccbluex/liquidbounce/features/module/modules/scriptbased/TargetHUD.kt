package net.ccbluex.liquidbounce.features.module.modules.scriptbased

import net.ccbluex.liquidbounce.bzym.PoseStack
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.Fonts.font32
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.utils.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.animations.impl.EaseBackIn
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.extensions.safeDiv
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.inventory.isEmpty
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawHead
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.withClipping
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object TargetHUD : Module("TargetHUD", Category.HUD, hideModule = false) {

    // 配置系统
    private val hudStyle by ListValue(
        "Style",
        arrayOf(
            "Novoline",
            "户籍",
            "Chill",
            "Myau",
            "RavenB4",
            "Naven"
        ),
        "Novoline"
    )
    var easingHealth = 0F
    private val posX by int("PosX", 0, -400..400)
    private val posY by int("PosY", 0, -400..400)
    private val textColor = Color.WHITE
    private val bgColor = Color(0, 0, 0, 120)
    val rainbow by boolean("Rainbow", true) { hudStyle == "Myau" }
    private val borderRed by int("Border Red", 255, 0..255) { hudStyle == "Myau" }
    private val borderGreen by int("Border Green", 255, 0..255) { hudStyle == "Myau" }
    private val borderBlue by int("Border Blue", 255, 0..255) { hudStyle == "Myau" }
    private val showAvatar by boolean("Show Avatar", true) { hudStyle == "Myau" }

    // 状态跟踪
    val barColorR by int("BarColorR", 255, 0..255) { hudStyle == "RavenB4" }
    private val barColorG by int("BarColorG", 255, 0..255) { hudStyle == "RavenB4" }
    private val barColorB by int("BarColorB", 255, 0..255) { hudStyle == "RavenB4" }
    private val roundedRectRadius by float("Rounded-Radius", 3F, 0F..5F)

    private val borderStrength by float("Border-Strength", 3F, 1F..5F)

    private val backgroundMode by choices("Background-ColorMode", arrayOf("Custom", "Rainbow"), "Custom")

    // 修改后的颜色配置部分
    private val backgroundColorR by int(
        "Background-Color-R",
        0,
        0..255
    ) { hudStyle == "LiquidNew" && backgroundMode == "Custom" }
    private val backgroundColorG by int(
        "Background-Color-G",
        0,
        0..255
    ) { hudStyle == "LiquidNew" && backgroundMode == "Custom" }
    private val backgroundColorB by int(
        "Background-Color-B",
        0,
        0..255
    ) { hudStyle == "LiquidNew" && backgroundMode == "Custom" }
    private val backgroundColorA by int(
        "Background-Color-A",
        150,
        0..255
    ) { hudStyle == "LiquidNew" && backgroundMode == "Custom" }

    private val healthBarColor1R by int("HealthBar-Gradient1-R", 3, 0..255) { hudStyle == "LiquidNew" }
    private val healthBarColor1G by int("HealthBar-Gradient1-G", 65, 0..255) { hudStyle == "LiquidNew" }
    private val healthBarColor1B by int("HealthBar-Gradient1-B", 252, 0..255) { hudStyle == "LiquidNew" }
    private val healthBarColor1A by int("HealthBar-Gradient1-A", 255, 0..255) { hudStyle == "LiquidNew" }

    private val healthBarColor2R by int("HealthBar-Gradient2-R", 3, 0..255) { hudStyle == "LiquidNew" }
    private val healthBarColor2G by int("HealthBar-Gradient2-G", 252, 0..255) { hudStyle == "LiquidNew" }
    private val healthBarColor2B by int("HealthBar-Gradient2-B", 236, 0..255) { hudStyle == "LiquidNew" }
    private val healthBarColor2A by int("HealthBar-Gradient2-A", 255, 0..255) { hudStyle == "LiquidNew" }

    private val borderColorR by int("Border-Color-R", 0, 0..255) { hudStyle == "LiquidNew" }
    private val borderColorG by int("Border-Color-G", 0, 0..255) { hudStyle == "LiquidNew" }
    private val borderColorB by int("Border-Color-B", 0, 0..255) { hudStyle == "LiquidNew" }
    private val borderColorA by int("Border-Color-A", 255, 0..255) { hudStyle == "LiquidNew" }

    private val textColorLR by int("TextColor-R", 255, 0..255) { hudStyle == "LiquidNew" }
    private val textColorLG by int("TextColor-G", 255, 0..255) { hudStyle == "LiquidNew" }
    private val textColorLB by int("TextColor-B", 255, 0..255) { hudStyle == "LiquidNew" }
    private val textColorLA by int("TextColor-A", 255, 0..255) { hudStyle == "LiquidNew" }

    // 封装为Color对象
    private val backgroundColor = Color(
        backgroundColorR,
        backgroundColorG,
        backgroundColorB,
        backgroundColorA
    )

    private val healthBarColor1 = Color(
        healthBarColor1R,
        healthBarColor1G,
        healthBarColor1B,
        healthBarColor1A
    )

    private val healthBarColor2 = Color(
        healthBarColor2R,
        healthBarColor2G,
        healthBarColor2B,
        healthBarColor2A
    )

    private val borderColor = Color(
        borderColorR,
        borderColorG,
        borderColorB,
        borderColorA
    )

    private val textColorL = Color(
        textColorLR,
        textColorLG,
        textColorLB,
        textColorLA
    )

    private val roundHealthBarShape by boolean("RoundHealthBarShape", true)


    private val rainbowX by float(
        "Rainbow-X",
        -1000F,
        -2000F..2000F
    ) { backgroundMode == "Rainbow" && hudStyle == "LiquidNew" }
    private val rainbowY by float(
        "Rainbow-Y",
        -1000F,
        -2000F..2000F
    ) { backgroundMode == "Rainbow" && hudStyle == "LiquidNew" }

    private val titleFont by font("TitleFont", Fonts.font40) { hudStyle == "LiquidNew" }
    private val healthFont by font("HealthFont", Fonts.font32) { hudStyle == "LiquidNew" }
    private val textShadow by boolean("TextShadow", false) { hudStyle == "LiquidNew" }
    val targetTimer: MSTimer = MSTimer()
    private val fadeSpeed by float("FadeSpeed", 2F, 1F..9F) { hudStyle == "LiquidNew" }
    private val absorption by boolean("Absorption", true) { hudStyle == "LiquidNew" }
    private val healthFromScoreboard by boolean("HealthFromScoreboard", true) { hudStyle == "LiquidNew" }

    private val animation by choices("Animation", arrayOf("Smooth", "Fade"), "Fade") { hudStyle == "LiquidNew" }
    private val animationSpeed by float("AnimationSpeed", 0.2F, 0.05F..1F) { hudStyle == "LiquidNew" }
    private val vanishDelay by int("VanishDelay", 300, 0..500) { hudStyle == "LiquidNew" }

    private val animSpeedRB4 by int("AnimSpeed", 3, 1..10) { hudStyle == "RavenB4" }
    private var target: EntityPlayer? = null
    private var currentHealthBarFillWidth = 1.0f
    override var hue = 0.0f
    private val playerHeads = ConcurrentHashMap<String, Target>()
    private const val HUD_WIDTH = 160
    private const val HUD_HEIGHT = 60
    var lastUpdateTime: Long = System.currentTimeMillis()
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        KillAura.target?.let { if (it.isMob()) return }
        val sr = ScaledResolution(mc)
        target =
            KillAura.target as EntityPlayer?
        if (target == null) return
        // 抗机器人检测
        if (state && AntiBot.isBot(target!!)) return

        when (hudStyle.lowercase()) {
            "novoline" -> renderNovolineHUD(sr)
            "户籍" -> render0x01a4HUD(sr)
            "chill" -> renderChillHUD(sr)
            "myau" -> renderMyauHUD(sr)
            "ravenb4" -> renderRavenB4HUD(sr)
            "naven" -> renderNavenHUD(sr)
            "liquidnew" -> renderNewLiquidHUD(sr)
            "modern" -> drawModernTargetHUD(sr)
        }
    }

    private fun renderAfucizusHUD(sr: ScaledResolution) {
        val X = sr.scaledWidth / 2F + posX
        val Y = sr.scaledHeight / 2F + posY

    }
    override fun onEnable() {
        currentHealthBarFillWidth = 1.0f
        hue = 0.0f
        targetTimer.reset()
    }

    val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    val decimalFormat2 = DecimalFormat("##0.0", DecimalFormatSymbols(Locale.ENGLISH))
    val decimalFormat3 = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))
    fun updateAnim(targetHealth: Float) {
        easingHealth += ((targetHealth - easingHealth) / 2.0F.pow(10.0F - animSpeedRB4)) * RenderUtils.deltaTime
    }
    private fun drawModernTargetHUD(sr: ScaledResolution){
        val x = sr.scaledWidth / 2F + posX
        val y = sr.scaledHeight / 2F + posY
        val poseStack = PoseStack()
        val fontManager = Fonts
        val animation = EaseBackIn(500, 1.0, 1.8f)
        if (target != null) {
            lastTarget = target
        }

        if (!targetTimer.hasTimePassed(50000) && lastTarget != null && target == null) {
            target = lastTarget as EntityPlayer?
        }


        // 血量
        val health = target?.health?.toInt() ?: 0
        val maxHealth = target?.maxHealth ?: 20
        val healthPresent: Float = if (target != null) health / target!!.maxHealth else 0f


        // 更新缓动血量
        updateEasingHealth(health.toFloat(), maxHealth.toFloat())


        // 名字
        val name: String? = if (target != null) target!!.getName().toString() else "Player"


        // 宽度
        val width: Int = name?.let { fontManager.font24.getStringWidth(it) }?.plus(75) ?: 0
        val presentWidth = min(healthPresent, 1f) * width


        // 动画
        poseStack.pushPose()
        poseStack.translate(
            (x + width.toDouble() / 2) * (1 - animation.getOutput()),
            (y + 20) * (1 - animation.getOutput()),
            0.0
        )
        poseStack.scale(animation.getOutput() as Float, animation.getOutput() as Float, 0F)


        // 绘制背景
        RenderUtils.drawRect(x, y, width.toFloat(), 40F, Color(0, 0, 0, 100).getRGB())
        RenderUtils.drawRect(x, y, presentWidth.toFloat(), 40F, Color(230, 230, 230, 100).getRGB())


        // 垂直血条指示
        val healthColor = if (healthPresent > 0.5) Color(63, 157, 4, 150) else (if (healthPresent > 0.25) Color(
            255,
            144,
            2,
            150
        ) else Color(168, 1, 1, 150))
        RenderUtils.drawRect(x, y + 12.5f, 3F, 15F, healthColor.getRGB())


        // 绘制头像
        try {
            if (target != null) {
                val player: AbstractClientPlayer = target as AbstractClientPlayer
                RenderUtils.drawPlayerHead(x.toInt() + 7, y.toInt() + 7, 26, 26, player)
            } else {
                RenderUtils.drawRect(x + 6, y + 6, 28F, 28F, Color.BLACK.getRGB())
            }
        } catch (e: Exception) {
            RenderUtils.drawRect(x + 6, y + 6, 28F, 28F, Color.BLACK.getRGB())
        }


        // 绘制文字
        name?.let { fontManager.font24.drawString(it, x + 40, y + 7, Color(200, 200, 200, 255).rgb) }
        fontManager.font18.drawString(
            "$health HP",
            x + 40,
            y + 22,
            Color(200, 200, 200, 255).getRGB()
        )


        // 绘制物品
        if (target != null && !target!!.heldItem.isEmpty()) {
            name?.let { x + fontManager.font24.getStringWidth(it) + 50 }?.let {
                RenderUtils.renderItemIcon(
                    it.toInt(),
                    y.toInt() + 12,
                    target!!.heldItem
                )
            }
        } else {
            name?.let {
                fontManager.font30.drawString(
                    "?",
                    x + fontManager.font24.getStringWidth(it) + 55,
                    y + 11,
                    Color(200, 200, 200, 255).getRGB()
                )
            }
        }


        // 结束绘制
        poseStack.popPose()
    }
    private fun updateEasingHealth(targetHealth: Float, maxHealth: Float) {
        val currentTime = System.currentTimeMillis()
        val deltaTime: Long = currentTime - lastUpdateTime
        lastUpdateTime = currentTime

        val changeAmount: Float = abs(easingHealth - targetHealth)
        val baseSpeed = 0.02f
        var speed = baseSpeed * deltaTime

        if (changeAmount > 5) {
            speed *= 2.0f
        } else if (changeAmount > 2) {
            speed *= 1.5f
        }

        if (abs(easingHealth - targetHealth) < 0.1) {
            easingHealth = targetHealth
        } else if (easingHealth > targetHealth) {
            easingHealth -= min(
                speed * 1.2f,
                easingHealth - targetHealth
            )
        } else {
            easingHealth += min(
                speed,
                targetHealth - easingHealth
            )
        }
        easingHealth =
            min(easingHealth, maxHealth)
    }
    private fun renderRavenB4HUD(sr: ScaledResolution) {
        val X = sr.scaledWidth / 2F + posX
        val Y = sr.scaledHeight / 2F + posY
        val entity = target!!
        val font = Fonts.minecraftFont
        val hp = decimalFormat2.format(entity.health)
        val hplength = font.getStringWidth(decimalFormat2.format(entity.health))
        val length = font.getStringWidth(entity.displayName.formattedText)
        val barColor = Color(barColorR, barColorG, barColorB)
        GlStateManager.pushMatrix()
        updateAnim(entity.health)
        RenderUtils.drawRoundedGradientOutlineCorner(
            X,
            Y,
            X + length + hplength + 23F,
            Y + 35F,
            2F, 8F,
            barColor.rgb,
            barColor.rgb
        )
        RenderUtils.drawRoundedRect(X, Y, X + length + hplength + 23F, Y + 35F, Color(0, 0, 0, 100).rgb, 4F)
        GlStateManager.enableBlend()
        font.drawStringWithShadow(
            entity.displayName.formattedText,
            X + 6F,
            Y + 8F,
            Color(255, 255, 255, 255).rgb
        )
        val winorlose = if (entity.health < mc.thePlayer.health) "W" else "L"
        font.drawStringWithShadow(
            winorlose,
            X + length + hplength + 11.6F,
            Y + 8F, (if (winorlose == "W") Color(0, 255, 0).rgb else Color(139, 0, 0).rgb)
        )
        font.drawStringWithShadow(
            hp,
            X + length + 8F,
            Y + 8F,
            ColorUtils.reAlpha(BlendUtils.getHealthColor(entity.health, entity.maxHealth), 255).rgb
        )
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        RenderUtils.drawRoundedRect(
            X + 5.0F,
            Y + 29.55F,
            X + length + hplength + 18F,
            Y + 25F,
            Color(0, 0, 0, 110).rgb,
            2F
        )
        RenderUtils.drawRoundedGradientRectCorner(
            X + 5F,
            Y + 25F,
            X + 8F + (entity.health / 20) * (length + hplength + 10F),
            Y + 29.5F,
            4F,
            barColor.rgb,
            barColor.rgb
        )
        GlStateManager.popMatrix()

    }

    private var width = 0f
    private var height = 0f

    private var lastTarget: EntityLivingBase? = null
    private val isRendered
        get() = width > 0f || height > 0f

    private var alphaText = 0
    private var alphaBackground = 0
    private var alphaBorder = 0

    private val isAlpha
        get() = alphaBorder > 0 || alphaBackground > 0 || alphaText > 0

    private var delayCounter = 0
    private var easingHurtTime = 0F
    private fun renderNewLiquidHUD(sr: ScaledResolution) {
        val baseX = sr.scaledWidth / 2F + posX // 基准X坐标
        val baseY = sr.scaledHeight / 2F + posY // 基准Y坐标

        val smoothMode = animation == "Smooth"
        val fadeMode = animation == "Fade"

        val killAuraTarget = KillAura.target.takeIf { it is EntityPlayer }

        val shouldRender = handleEvents() && killAuraTarget != null || mc.currentScreen is GuiChat
        val target = killAuraTarget ?: if (delayCounter >= vanishDelay && !isRendered) {
            mc.thePlayer
        } else {
            lastTarget ?: mc.thePlayer
        }

        val stringWidth = (40f + (target.name?.let(titleFont::getStringWidth) ?: 0)).coerceAtLeast(118F)

        if (shouldRender) {
            delayCounter = 0
        } else if (isRendered || isAlpha) {
            delayCounter++
        }

        if (shouldRender || isRendered || isAlpha) {
            val targetHealth = getHealth(target!!, healthFromScoreboard, absorption)
            val maxHealth = target.maxHealth + if (absorption) target.absorptionAmount else 0F

            easingHealth += (targetHealth - easingHealth) / 2f.pow(10f - fadeSpeed) * deltaTime
            easingHealth = easingHealth.coerceIn(0f, maxHealth)
            val targetHurtTime = if (target.isEntityAlive()) target.hurtTime.toFloat() else 0F
            easingHurtTime = (easingHurtTime..targetHurtTime).lerpWith(RenderUtils.deltaTimeNormalized().toFloat())

            if (target != lastTarget || abs(easingHealth - targetHealth) < 0.01) {
                easingHealth = targetHealth
            }

            if (smoothMode) {
                val targetWidth = if (shouldRender) stringWidth else if (delayCounter >= vanishDelay) 0f else width
                width = AnimationUtil.base(width.toDouble(), targetWidth.toDouble(), animationSpeed.toDouble())
                    .toFloat().coerceAtLeast(0f)

                val targetHeight = if (shouldRender) 36f else if (delayCounter >= vanishDelay) 0f else height
                height = AnimationUtil.base(height.toDouble(), targetHeight.toDouble(), animationSpeed.toDouble())
                    .toFloat().coerceAtLeast(0f)
            } else {
                width = stringWidth
                height = 36f

                val targetText =
                    if (shouldRender) textColor.alpha else if (delayCounter >= vanishDelay) 0f else alphaText
                alphaText =
                    AnimationUtil.base(alphaText.toDouble(), targetText.toDouble(), animationSpeed.toDouble())
                        .toInt()

                val targetBackground = if (shouldRender) {
                    backgroundColor.alpha
                } else if (delayCounter >= vanishDelay) {
                    0f
                } else alphaBackground

                alphaBackground = AnimationUtil.base(
                    alphaBackground.toDouble(), targetBackground.toDouble(), animationSpeed.toDouble()
                ).toInt()

                val targetBorder = if (shouldRender) {
                    borderColor.alpha
                } else if (delayCounter >= vanishDelay) {
                    0f
                } else alphaBorder

                alphaBorder =
                    AnimationUtil.base(alphaBorder.toDouble(), targetBorder.toDouble(), animationSpeed.toDouble())
                        .toInt()
            }

            val backgroundCustomColor = backgroundColor.withAlpha(
                if (fadeMode) alphaBackground else backgroundColor.alpha
            ).rgb
            val borderCustomColor = borderColor.withAlpha(
                if (fadeMode) alphaBorder else borderColor.alpha
            ).rgb
            val textCustomColor = textColor.withAlpha(
                if (fadeMode) alphaText else textColor.alpha
            ).rgb

            val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
            val rainbowX = 1f safeDiv rainbowX
            val rainbowY = 1f safeDiv rainbowY

            glPushMatrix()
            glTranslatef(baseX, baseY, 0f) // 应用基准坐标偏移

            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            if (fadeMode && isAlpha || smoothMode && isRendered || delayCounter < vanishDelay) {
                val width = width.coerceAtLeast(0F)
                val height = height.coerceAtLeast(0F)

                RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                    drawRoundedBorderRect(
                        0F,
                        0F,
                        width,
                        height,
                        borderStrength,
                        if (backgroundMode == "Rainbow") 0 else backgroundCustomColor,
                        borderCustomColor,
                        roundedRectRadius
                    )
                }

                val healthBarTop = 24F
                val healthBarHeight = 8F
                val healthBarStart = 36F
                val healthBarTotal = (width - 39F).coerceAtLeast(0F)
                val currentWidth = (easingHealth / maxHealth).coerceIn(0F, 1F) * healthBarTotal

                // 背景条
                val backgroundBar = {
                    drawRoundedRect(
                        healthBarStart,
                        healthBarTop,
                        healthBarStart + healthBarTotal,
                        healthBarTop + healthBarHeight,
                        Color.BLACK.rgb,
                        6F,
                    )
                }

                if (roundHealthBarShape) {
                    backgroundBar()
                }

                // 主血条
                withClipping(main = {
                    if (roundHealthBarShape) {
                        drawRoundedRect(
                            healthBarStart,
                            healthBarTop,
                            healthBarStart + currentWidth,
                            healthBarTop + healthBarHeight,
                            0,
                            6F
                        )
                    } else {
                        backgroundBar()
                    }
                }, toClip = {
                    drawGradientRect(
                        healthBarStart.toInt(),
                        healthBarTop.toInt(),
                        healthBarStart.toInt() + currentWidth.toInt(),
                        healthBarTop.toInt() + healthBarHeight.toInt(),
                        healthBarColor1.rgb,
                        healthBarColor2.rgb,
                        0f
                    )
                })

                // 血量百分比
                val healthPercentage = (easingHealth / maxHealth * 100).toInt()
                val percentageText = "$healthPercentage%"
                val textWidth = healthFont.getStringWidth(percentageText)
                val calcX = healthBarStart + currentWidth - textWidth
                val textX = max(healthBarStart, calcX)
                val textY = healthBarTop - Fonts.font32.fontHeight / 2 - 2F
                healthFont.drawString(percentageText, textX, textY, textCustomColor, textShadow)

                val shouldRenderBody =
                    (fadeMode && alphaText + alphaBackground + alphaBorder > 100) || (smoothMode && width + height > 100)

                if (shouldRenderBody) {
                    // 玩家头像
                    val renderer = mc.renderManager.getEntityRenderObject<Entity>(target)
                    if (renderer != null) {
                        val entityTexture = (target as? EntityPlayer)?.let {
                            mc.netHandler.getPlayerInfo(it.uniqueID)?.locationSkin
                        }

                        glPushMatrix()
                        val scale = 1 - easingHurtTime / 10f
                        val f1 = (0.7F..1F).lerpWith(scale) * scale
                        val color = ColorUtils.interpolateColor(Color.RED.rgb, Color.WHITE.rgb, scale)
                        val centerX1 = (4f..32f).lerpWith(0.5F)
                        val midY = (4f..28f).lerpWith(0.5F)

                        glTranslatef(centerX1, midY, 0f)
                        glScalef(f1, f1, f1)
                        glTranslatef(-centerX1, -midY, 0f)

                        if (entityTexture != null) {
                            withClipping(main = {
                                drawRoundedRect(4f, 4f, 32f, 32f, 0, roundedRectRadius)
                            }, toClip = {
                                drawHead(
                                    entityTexture, 4, 4, 8f, 8f, 8, 8, 28, 28, 64F, 64F, Color(color)
                                )
                            })
                        }
                        glPopMatrix()
                    }

                    // 玩家名称
                    target.name?.let {
                        titleFont.drawString(it, healthBarStart, 6F, textCustomColor, textShadow)
                    }
                }
            }

            glPopMatrix()
        }

        lastTarget = target
    }


    private fun renderNavenHUD(sr: ScaledResolution) {
        // 把坐标都转成 Float
        val x2 = sr.scaledWidth / 2f + posX
        val y2 = sr.scaledHeight / 2f + posY
        val width = 130f
        val height = 50f

        // 背景
        RenderUtils.drawRoundedRect(
            x2, y2,
            x2 + width, y2 + height,
            Color(10, 10, 30, 120).rgb, 5f
        )

        // 头像
        mc.netHandler.getPlayerInfo(target!!.uniqueID)?.locationSkin?.let {
            Target().drawHead(
                it, x2.toInt() + 7, y2.toInt() + 7,
                30, 30,
                Color.WHITE
            )
        }

        // 血条背景
        val barX1 = x2 + 5f
        val barY1 = y2 + height - 10f
        val barX2 = x2 + width - 5f
        val barY2 = barY1 + 3f
        RenderUtils.drawRoundedRect(barX1, barY1, barX2, barY2, Color(0, 0, 0, 200).rgb, 2f)

        // 血条填充
        val healthPercent = target!!.health / target!!.maxHealth
        val fillX2 = barX1 + (barX2 - barX1) * healthPercent
        RenderUtils.drawRoundedRect(barX1, barY1, fillX2, barY2, Color(160, 42, 42).rgb, 2f)

        // 名称 & 生命 & 距离
        font35.drawString(target!!.name, x2 + 40f, y2 + 10f, Color.WHITE.rgb)
        font32.drawString("Health: ${"%.2f".format(target!!.health)}", x2 + 40f, y2 + 22f, Color.WHITE.rgb)
        font32.drawString(
            "Distance: ${"%.2f".format(target!!.getDistanceToEntity(mc.thePlayer))}",
            x2 + 40f,
            y2 + 30f,
            Color.WHITE.rgb
        )
    }


    private fun renderNovolineHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY

        // 新增Novoline样式实现
        val width = (38 + Fonts.font35.getStringWidth(entity.name)).coerceAtLeast(118).toFloat()

        // 绘制背景
        RenderUtils.drawRect(posX.toFloat(), posY.toFloat(), posX + width + 14f, posY + 44f, bgColor.rgb)

        // 绘制玩家头部
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, posX + 3, posY + 3, 30, 30, Color.WHITE)
        }

        // 绘制文本信息
        Fonts.font35.drawString(entity.name, posX + 34.5f, posY + 4f, textColor.rgb)
        Fonts.font35.drawString("Health: ${"%.1f".format(entity.health)}", posX + 34.5f, posY + 14f, textColor.rgb)
        Fonts.font35.drawString(
            "Distance: ${"%.1f".format(mc.thePlayer.getDistanceToEntity(entity))}m",
            posX + 34.5f, posY + 24f, textColor.rgb
        )

        // 绘制生命条
        RenderUtils.drawRect(posX + 2.5f, posY + 35.5f, posX + width + 11.5f, posY + 37.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(
            posX + 3f, posY + 36f,
            posX + 3f + (entity.health / entity.maxHealth) * (width + 8f),
            posY + 37f, ColorUtils.healthColor(entity.health, entity.maxHealth)
        )

        // 绘制护甲条
        RenderUtils.drawRect(posX + 2.5f, posY + 39.5f, posX + width + 11.5f, posY + 41.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(
            posX + 3f, posY + 40f,
            posX + 3f + (entity.totalArmorValue / 20f) * (width + 8f),
            posY + 41f, Color(77, 128, 255).rgb
        )
    }

    private fun renderMyauHUD(sr: ScaledResolution) {
        val entity = target ?: return
        // 位置
        val x = sr.scaledWidth / 2F + posX
        val y = sr.scaledHeight / 2F + posY
        // 名称宽度
        val name = entity.name
        val nameWidth = Fonts.font35.getStringWidth(name)
        val hudWidth = maxOf(80f, nameWidth + 20f)
        val hudHeight = 25f
        val avatarSize = hudHeight

        // 更新彩虹色
        if (rainbow) {
            hue += 0.0005f
            if (hue > 1f) hue = 0f
        }
        val borderColor = if (rainbow) getRainbowColor() else Color(borderRed, borderGreen, borderBlue)
        val healthBarColor = if (rainbow) getRainbowColor() else Color(
            maxOf(borderRed - 50, 0), maxOf(borderGreen - 50, 0), maxOf(borderBlue - 50, 0)
        )

        // 计算总宽度
        val totalWidth = if (showAvatar) hudWidth + avatarSize else hudWidth

        // 边框
        RenderUtils.drawRect(x - 1, y - 1, x + totalWidth + 1, y, borderColor.rgb)
        RenderUtils.drawRect(x - 1, y + hudHeight, x + totalWidth + 1, y + hudHeight + 1, borderColor.rgb)
        RenderUtils.drawRect(x - 1, y, x, y + hudHeight, borderColor.rgb)
        RenderUtils.drawRect(x + totalWidth, y, x + totalWidth + 1, y + hudHeight, borderColor.rgb)
        // 背景透明
        RenderUtils.drawRect(x, y, x + totalWidth, y + hudHeight, Color(0, 0, 0, 0).rgb)

        // 头像
        if (showAvatar) {
            mc.netHandler.getPlayerInfo(entity.uniqueID)?.locationSkin?.let {
                Target().drawHead(
                    it,
                    x.toInt(), y.toInt(), avatarSize.toInt(), avatarSize.toInt(), Color.WHITE
                )
            }
        }

        // 文本
        val textX = if (showAvatar) x + avatarSize + 3 else x + 3
        Fonts.font35.drawString(name, textX, y + 1, Color.WHITE.rgb)
        val healthText = String.format("%.1f", entity.health)
        Fonts.font35.drawString(healthText, textX, y + 11, healthBarColor.rgb)
        // 心符号
        val heartX = textX + Fonts.font35.getStringWidth(healthText) + 2
        Fonts.font35.drawString("\u2764", heartX, y + 11, healthBarColor.rgb)

        // 血条背景
        val barY = y + 21
        val barWidth = hudWidth - 5f
        RenderUtils.drawRect(textX, barY, textX + barWidth, barY + 3, Color(64, 64, 64).rgb)
        // 插值
        val targetFill = (entity.health / entity.maxHealth) * barWidth
        currentHealthBarFillWidth = lerp(currentHealthBarFillWidth, targetFill, 0.1f)
        // 血条填充
        RenderUtils.drawRect(textX, barY, textX + currentHealthBarFillWidth, barY + 3, healthBarColor.rgb)

        // 胜负指示
        val playerHealth = mc.thePlayer.health
        val (winLoss, wlColor) = when {
            playerHealth > entity.health -> Pair("W", Color(0, 255, 0))
            playerHealth < entity.health -> Pair("L", Color(255, 0, 0))
            else -> Pair("D", Color(255, 255, 0))
        }
        val wlX = x + totalWidth - Fonts.font35.getStringWidth(winLoss) - 1
        Fonts.font35.drawString(winLoss, wlX, y + 1, wlColor.rgb)

        // 差值
        val diff = playerHealth - entity.health
        val diffText = if (diff > 0) "+${"%.1f".format(diff)}" else String.format("%.1f", diff)
        val diffColor = when {
            diff > 0 -> Color(0, 255, 0)
            diff < 0 -> Color(255, 0, 0)
            else -> Color(255, 255, 0)
        }
        val diffX = maxOf(x + totalWidth - Fonts.font35.getStringWidth(diffText) - 1, textX)
        Fonts.font35.drawString(diffText, diffX, y + 11, diffColor.rgb)
    }

    private fun lerp(start: Float, end: Float, speed: Float): Float = start + (end - start) * speed
    private fun getRainbowColor(): Color = Color.getHSBColor(hue, 1f, 1f)
    private fun render0x01a4HUD(sr: ScaledResolution) {
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY

        RenderUtils.drawRect(
            posX + 11F, posY - 15F, posX + 130F, posY + 90F,
            Color(30, 30, 30, 200).rgb
        )
        Fonts.font35.drawString("PLC 全国人口档案查询系统", posX + 15, posY - 5, Color.WHITE.rgb)
        Fonts.font35.drawString("姓名:${target!!.gameProfile.name}", posX + 15, posY + 5, Color.WHITE.rgb)
        Fonts.font35.drawString(
            "健康:${target!!.health.toInt()}/${target!!.maxHealth.toInt()}",
            posX + 15,
            posY + 25,
            Color.WHITE.rgb
        )
        Fonts.font35.drawString("资产:${target!!.totalArmorValue}", posX + 15, posY + 45, Color.WHITE.rgb)
        Fonts.font35.drawString("身份证:${target!!.gameProfile.id}", posX + 15, posY + 65, Color.WHITE.rgb)

    }


    // Moon样式实现
    private fun renderMoonHUD(sr: ScaledResolution) {
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY


        // 数据面板
        RenderUtils.drawRect(posX + 11, posY + 5, posX + 116, posY + 34, Color(0, 0, 0, 100).rgb)
        Fonts.font35.drawString(target!!.name, posX + 41, posY + 8, Color.WHITE.rgb)

        // 动态生命条
        val healthPercent = target!!.health / target!!.maxHealth
        RenderUtils.drawRect(
            posX + 42F, posY + 26F,
            (posX + 42F) + (72 * healthPercent), posY + 27F,
            ColorUtils.healthColor(target!!.health, target!!.maxHealth)
        )
    }

    // Smoke样式实现
    private fun renderSmokeHUD(sr: ScaledResolution) {
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY

        // 主面板
        RenderUtils.drawRect(posX, posY + 5, posX + 158, posY + 52, Color(20, 20, 20).rgb)

        // 动态生命指示器
        val healthPercent = target!!.health / target!!.maxHealth
        RenderUtils.drawRect(
            posX + 3F, posY + 47F,
            (posX + 3F) + (152 * healthPercent), posY + 40F,
            ColorUtils.healthColor(target!!.health, target!!.maxHealth)
        )
        val color = if (target!!.hurtTime > 0) Color(
            255,
            (255 + -(target!!.hurtTime * 20)).coerceAtMost(255),
            (255 + -(target!!.hurtTime * 20)).coerceAtMost(255)
        ) else Color.WHITE
        // 实体头像
        mc.netHandler.getPlayerInfo(target!!.uniqueID)?.let {
            Target().drawHead(it.locationSkin, posX + 3, posY + 9, 28, 28, color)
        }
    }

    private fun renderChillHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY

        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name)
            .coerceAtLeast(Fonts.font72.getStringWidth("%.1f".format(entity.health)))).coerceAtLeast(120F)

        // 绘制背景
        RenderUtils.drawRoundedRect(posX.toFloat(), posY.toFloat(), posX + tWidth, posY + 48F, bgColor.rgb, 7F)

        // 绘制玩家头部
        mc.netHandler.getPlayerInfo(entity.uniqueID)?.let {
            Target().drawHead(it.locationSkin, posX.toInt() + 4, posY.toInt() + 4, 30, 30, Color.WHITE)
        }

        // 绘制名称和生命值
        Fonts.font40.drawString(entity.name, posX + 38F, posY + 6F, textColor.rgb)
        Fonts.font72.drawString("%.1f".format(entity.health), posX + 38F, posY + 17F, textColor.rgb)

        // 绘制生命条
        val healthPercent = entity.health / entity.maxHealth
        RenderUtils.drawRect(posX + 4F, posY + 38F, posX + tWidth - 4F, posY + 44F, Color.DARK_GRAY.rgb)
        RenderUtils.drawRect(
            posX + 4F, posY + 38F, posX + 4F + (tWidth - 8F) * healthPercent, posY + 44F,
            ColorUtils.healthColor(entity.health, entity.maxHealth)
        )
    }

    override fun onDisable() {
        // 清理渲染状态
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }

    override val tag
        get() = hudStyle
}
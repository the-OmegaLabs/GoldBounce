package net.ccbluex.liquidbounce.features.module.modules.scriptbased

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

object TargetHUD : Module("TargetHUD", Category.SCRIPT, hideModule = false) {

    // 配置系统
    private val hudStyle by ListValue(
        "Style",
        arrayOf(
            "Health",
            "Novoline",
            "Smoke",
            "Moon",
            "户籍",
        ),
        "Health"
    )
    private val posX by int("PosX", 0, -400..400)
    private val posY by int("PosY", 0, -400..400)
    private val textColor = Color.WHITE
    private val bgColor = Color(0, 0, 0, 120)

    // 状态跟踪
    private var target: EntityPlayer? = null
    private const val HUD_WIDTH = 160
    private const val HUD_HEIGHT = 60

    @EventTarget
    fun onRender2D(event: Render2DEvent) {

        val sr = ScaledResolution(mc)
        target =
            KillAura.target as EntityPlayer?
        if (target == null) return

        // 抗机器人检测
        if (state && AntiBot.isBot(target!!)) return

        when (hudStyle.lowercase()) {
            "health" -> renderHealthHUD(sr)
            "novoline" -> renderNovolineHUD(sr)
            "smoke" -> renderSmokeHUD(sr)
            "moon" -> renderMoonHUD(sr)
            "户籍" -> render0x01a4HUD(sr)
        }
    }

    private fun renderHealthHUD(sr: ScaledResolution) {
        val centerX = sr.scaledWidth / 2 + posX
        val centerY = sr.scaledHeight / 2 + posY

        // 背景
        RenderUtils.drawRect(centerX - 60, centerY - 30, centerX + 60, centerY + 30, bgColor.rgb)

        // 名称
        Fonts.font35.drawCenteredString(target!!.name, centerX.toFloat(), (centerY - 25).toFloat(), textColor.rgb)

        // 生命条
        val healthPercent = target!!.health / target!!.maxHealth
        RenderUtils.drawRect(centerX - 50, centerY + 10, centerX + 50, centerY + 15, Color.DARK_GRAY.rgb)
        RenderUtils.drawRect(
            centerX - 50F, centerY + 10F,
            (centerX - 50F) + (100 * healthPercent).toFloat(), centerY + 15F,
            ColorUtils.healthColor(target!!.health, target!!.maxHealth)
        )
    }


    private var lastHealth = 0f
    private var healthAnimationProgress = 0f
    private var critAnimationProgress = 0f
    private var nameColor = Color.WHITE

    private fun renderNovolineHUD(sr: ScaledResolution) {
        val centerX = sr.scaledWidth / 2 + posX
        val centerY = sr.scaledHeight / 2 + posY

        // 绘制背景
        RenderUtils.drawRect(centerX - 50, centerY - 15, centerX + 50, centerY + 15, Color(40, 40, 40).rgb)

        // 处理名字颜色变化
        if (target!!.hurtTime > 0) {
            nameColor = Color(255, 0, 0) // 受伤时变为红色
        } else {
            // 渐变为白色
            nameColor = Color(
                (nameColor.red + (255 - nameColor.red) * 0.1f).toInt(),
                (nameColor.green + (255 - nameColor.green) * 0.1f).toInt(),
                (nameColor.blue + (255 - nameColor.blue) * 0.1f).toInt()
            )
        }

        // 绘制名字
        Fonts.font35.drawCenteredString(target!!.name, centerX.toFloat(), centerY - 10F, nameColor.rgb)

        // 处理血条动画
        val currentHealth = target!!.health
        if (currentHealth != lastHealth) {
            healthAnimationProgress = 1f // 开始动画
            lastHealth = currentHealth
        }

        val healthPercent = (currentHealth / target!!.maxHealth).coerceIn(0f..1f)
        val animatedHealth = if (healthAnimationProgress > 0) {
            healthAnimationProgress -= 0.05f
            lerp(lastHealth / target!!.maxHealth, healthPercent, 1f - healthAnimationProgress)
        } else {
            healthPercent
        }

        // 绘制血条背景
        RenderUtils.drawRect(centerX - 45, centerY + 5, centerX + 45, centerY + 8, Color(30, 30, 30).rgb)

        // 绘制血条
        RenderUtils.drawRect(
            centerX - 45, centerY + 5,
            (centerX - 45) + (90 * animatedHealth).toInt(), centerY + 8,
            Color(129, 95, 149).rgb
        )

        // 处理暴击提示动画
        if (target!!.hurtTime > 0 && target!!.hurtResistantTime == 0) {
            critAnimationProgress = 1f // 开始暴击动画
        }

        if (critAnimationProgress > 0) {
            critAnimationProgress -= 0.05f
            val critColor = Color(255, 0, 0, (255 * critAnimationProgress).toInt())
            Fonts.font35.drawCenteredString("CRIT!", centerX.toFloat(), centerY - 20F, critColor.rgb)
        }
    }

    private fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    private fun render0x01a4HUD(sr: ScaledResolution) {
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY

        RenderUtils.drawRect(
            posX + 11F, posY - 15F, posX + 130F, posY + 90F,
            Color(30, 30, 30, 200).rgb
        )
        Fonts.font35.drawString("PLC 全国人口档案查询系统", posX + 15, posY - 5, Color.WHITE.rgb)
        Fonts.font35.drawString("姓名:${target!!.gameProfile.name}", posX + 15, posY + 5, Color.WHITE.rgb)
        Fonts.font35.drawString("健康:${target!!.health.toInt()}/${target!!.maxHealth.toInt()}", posX + 15, posY + 25, Color.WHITE.rgb)
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

    override fun onDisable() {
        // 清理渲染状态
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }
    override val tag
        get() = hudStyle
}
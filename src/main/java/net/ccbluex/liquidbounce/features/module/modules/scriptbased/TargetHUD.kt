package net.ccbluex.liquidbounce.features.module.modules.scriptbased

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.render.WaterMark
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import java.awt.Color
import javax.annotation.Resource

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
            "Chill"
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
        KillAura.target?.let { if (it.isMob()) return }
        val sr = ScaledResolution(mc)
        target =
            KillAura.target as EntityPlayer?
        if (target == null) return
        // 抗机器人检测
        if (state && AntiBot.isBot(target!!)) return
        WaterMark.setContent("/icons/sword.png", "TargetHUD",  "正在攻击 ${target!!.name}")

        when (hudStyle.lowercase()) {
            "health" -> renderHealthHUD(sr)
            "novoline" -> renderNovolineHUD(sr)
            "smoke" -> renderSmokeHUD(sr)
            "moon" -> renderMoonHUD(sr)
            "户籍" -> render0x01a4HUD(sr)
            "chill" -> renderChillHUD(sr)
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
        Fonts.font35.drawString("Distance: ${"%.1f".format(mc.thePlayer.getDistanceToEntity(entity))}m", 
            posX + 34.5f, posY + 24f, textColor.rgb)
        
        // 绘制生命条
        RenderUtils.drawRect(posX + 2.5f, posY + 35.5f, posX + width + 11.5f, posY + 37.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(posX + 3f, posY + 36f, 
            posX + 3f + (entity.health / entity.maxHealth) * (width + 8f), 
            posY + 37f, ColorUtils.healthColor(entity.health, entity.maxHealth))
        
        // 绘制护甲条
        RenderUtils.drawRect(posX + 2.5f, posY + 39.5f, posX + width + 11.5f, posY + 41.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(posX + 3f, posY + 40f, 
            posX + 3f + (entity.totalArmorValue / 20f) * (width + 8f), 
            posY + 41f, Color(77, 128, 255).rgb)
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

    private fun renderChillHUD(sr: ScaledResolution) {
        val entity = target ?: return
        val posX = sr.scaledWidth / 2 + this.posX
        val posY = sr.scaledHeight / 2 + this.posY
        
        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name).coerceAtLeast(Fonts.font72.getStringWidth("%.1f".format(entity.health)))).coerceAtLeast(120F)
        
        // 绘制背景
        RenderUtils.drawRoundedRect(posX.toFloat(), posY.toFloat(), posX + tWidth, posY + 48F,bgColor.rgb, 7F)
        
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
        RenderUtils.drawRect(posX + 4F, posY + 38F, posX + 4F + (tWidth - 8F) * healthPercent, posY + 44F, 
            ColorUtils.healthColor(entity.health, entity.maxHealth))
    }

    override fun onDisable() {
        // 清理渲染状态
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }
    override val tag
        get() = hudStyle
}
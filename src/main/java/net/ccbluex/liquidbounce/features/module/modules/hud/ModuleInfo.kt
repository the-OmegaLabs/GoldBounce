package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.LiquidWalk
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import org.lwjgl.opengl.GL11.*

/**
 * HUD 信息模块，渲染自定义文本元素，自下而上排列，支持优先级和动态补全
 */
object ModuleInfo : Module("ModuleInfo", Category.HUD, hideModule = false) {
    // 基准偏移，参照屏幕左下角
    private val offsetX by FloatValue("OffsetX", 2f, 0f..500f)
    private val offsetY by FloatValue("OffsetY", 2f, 0f..500f)

    // 文本元素数据类
    private data class TextElement(
        val id: String,
        var content: String,
        var color: Color,
        var priority: Int,
        // 新增动画属性
        var animationProgress: Float = 0f, // 动画进度 0~1
        var lastUpdateTime: Long = System.currentTimeMillis()
    )

    // 存储所有文本元素
    private val elements = mutableListOf<TextElement>()

    /**
     * 添加或更新文本元素
     * @param id 唯一标识
     * @param content 文本内容
     * @param color 文本颜色
     * @param priority 排序优先级，数值越大越靠上
     */
    fun setTextElement(id: String, content: String, color: Color, priority: Int) {
        val existing = elements.find { it.id == id }
        if (existing != null) {
            existing.content = content
            existing.color = color
            existing.priority = priority
        } else {
            elements.add(TextElement(id, content, color, priority))
        }
    }

    /**
     * 删除指定文本元素
     * @param id 唯一标识
     */
    fun removeTextElement(id: String) {
        elements.removeAll { it.id == id }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val currentTime = System.currentTimeMillis()
        
        elements.forEach { element ->
            val deltaTime = (currentTime - element.lastUpdateTime).coerceAtMost(50L)
            val targetProgress = if (element.content.isNotEmpty()) 1f else 0f
            val animationSpeed = 0.02f * deltaTime // 控制动画速度
            
            element.animationProgress = when {
                element.animationProgress < targetProgress -> 
                    (element.animationProgress + animationSpeed).coerceAtMost(1f)
                else -> 
                    (element.animationProgress - animationSpeed).coerceAtLeast(0f)
            }
            element.lastUpdateTime = currentTime
        }
        
        if (Speed.state) {
            if (mc.thePlayer.onGround) {
                removeTextElement("speed1")
                setTextElement("speed1","Speed", Color(255,255,255),5)
            } else {
                removeTextElement("speed1")
                setTextElement("speed1","Speed↓", Color(255,70,70),5)
            }
        } else {
            removeTextElement("speed1")
        }
        if (LiquidWalk.state) {
            if (!mc.thePlayer.isInLiquid){
                removeTextElement("liquidwalk")
                setTextElement("liquidwalk","LiquidWalk", Color(255,255,255),5)
            } else {
                removeTextElement("liquidwalk")
                setTextElement("liquidwalk","LiquidWalk↓", Color(255,70,70),5)
            }
        } else {
            removeTextElement("liquidwalk")
        }
        if (NoSlow.state) {
            if (!mc.thePlayer.isBlocking){
                removeTextElement("nslow")
                setTextElement("nslow","NS", Color(255,255,255),6)
            } else {
                removeTextElement("nslow")
                setTextElement("nslow","NS", Color(255,70,70),6)
            }
        } else {
            removeTextElement("nslow")
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val resolution = ScaledResolution(mc)
        val baseX = offsetX.toInt()
        val baseY = resolution.scaledHeight - offsetY.toInt()

        // 根据优先级排序，小的先渲染（更底部）
        val sorted = elements.sortedBy { it.priority }

        // 字体高度
        val fr = Fonts.fontNoto35
        val lineHeight = fr.FONT_HEIGHT + 2

        // 从下向上渲染
        sorted.forEachIndexed { index, elem ->
            val y = baseY - index * lineHeight
            // 修改开始：添加与CPSHudCounter一致的背景样式
            val textWidth = fr.getStringWidth(elem.content)
            // 绘制圆角背景
            RenderUtils.drawRoundedRect(
                baseX.toFloat() - 5F, 
                y.toFloat() - 6F, 
                baseX.toFloat() + textWidth + 5F, 
                y.toFloat() + fr.FONT_HEIGHT + 4F, 
                Color(0, 0, 0, 120).rgb, 
                5F
            )
            // 绘制发光效果
            GlowUtils.drawGlow(
                baseX.toFloat() - 5F,
                y.toFloat() - 6F,
                textWidth + 10F,
                fr.FONT_HEIGHT + 10F,
                8,
                Color(0, 0, 0, 120)
            )
            // 调整文字位置（带内边距）
            fr.drawStringWithShadow(elem.content, baseX.toFloat() + 5F, y.toFloat() + 6F, elem.color.rgb)
            // 修改结束
            
            // 应用裁剪区域实现展开动画
            glEnable(GL_SCISSOR_TEST)
            val animatedWidth = (elem.animationProgress * textWidth).toInt()
            val screenScale = resolution.scaleFactor
            
            glScissor(
                (baseX * screenScale).toInt(),
                ((resolution.scaledHeight - y) * screenScale).toInt() - (fr.FONT_HEIGHT * screenScale).toInt(),
                (animatedWidth * screenScale).toInt(),
                (fr.FONT_HEIGHT * screenScale).toInt()
            )
            
            glDisable(GL_SCISSOR_TEST)
        }
    }
}

package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.gui.Gui
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min

fun drawSmoothRoundedRect(
    x1: Float, y1: Float, x2: Float, y2: Float,
    colorInt: Int, radius: Float,
    segments: Int = 16  // 圆角细分程度
) {
    // 拆解 ARGB
    val a  = ((colorInt ushr 24) and 0xFF) / 255f
    val r  = ((colorInt ushr 16) and 0xFF) / 255f
    val g  = ((colorInt ushr 8)  and 0xFF) / 255f
    val b  = ( colorInt         and 0xFF) / 255f

    // 确保坐标有序
    val lx = min(x1, x2)
    val rx = maxOf(x1, x2)
    val ty = min(y1, y2)
    val by = maxOf(y1, y2)

    // 最小半径（不能超过宽高一半）
    val rad = min(radius, min((rx - lx), (by - ty)) * 0.5f)
    if (rad < 1f) {
        // 半径太小就直接用普通 drawRect
        Gui.drawRect(lx.toInt(), ty.toInt(), rx.toInt(), by.toInt(), colorInt)
        return
    }

    // 固定屏幕坐标系：左上原点，Y 轴向下
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    glDisable(GL_TEXTURE_2D)

    // 1) 核心圆角：半径 rad-1，完全不透明
    drawCornerRect(lx, ty, rx, by, r, g, b, a, rad - 1f, segments)

    // 2) 手动抗锯齿——外围 1px 的边和角
    val outer = rad
    val inner = rad - 1f

    // a) 四条直边的 1px 抗锯齿矩形
    // 上
    drawEdgeRect(
        lx + outer, ty + inner, rx - outer, ty + outer,
        r, g, b, a, 0f, 1f
    )
    // 下
    drawEdgeRect(
        lx + outer, by - outer, rx - outer, by - inner,
        r, g, b, a, 1f, 0f
    )
    // 左
    drawEdgeRect(
        lx + inner, ty + outer, lx + outer, by - outer,
        r, g, b, a, 0f, 1f
    )
    // 右
    drawEdgeRect(
        rx - outer, ty + outer, rx - inner, by - outer,
        r, g, b, a, 1f, 0f
    )

    // b) 四个圆角的 1px 抗锯齿扇带
    fun aaCorner(cx: Float, cy: Float, startAngle: Float) {
        glBegin(GL_TRIANGLE_STRIP)
        for (i in 0..segments) {
            val t = i / segments.toFloat()
            val ang = startAngle + t * (Math.PI / 2)
            val cosA = cos(ang).toFloat()
            val sinA = sin(ang).toFloat()
            // 内圈：完全不透明
            glColor4f(r, g, b, a)
            glVertex2f(cx + cosA * inner, cy + sinA * inner)
            // 外圈：透明
            glColor4f(r, g, b, 0f)
            glVertex2f(cx + cosA * outer, cy + sinA * outer)
        }
        glEnd()
    }
    // 左上
    aaCorner(lx + rad, ty + rad, Math.PI.toFloat())
    // 右上
    aaCorner(rx - rad, ty + rad, (-Math.PI/2).toFloat())
    // 右下
    aaCorner(rx - rad, by - rad, 0f)
    // 左下
    aaCorner(lx + rad, by - rad, (Math.PI/2).toFloat())

    // 恢复状态
    glEnable(GL_TEXTURE_2D)
    glDisable(GL_BLEND)
}

// 画实心圆角矩形（片段 + 四个扇面），不带抗锯齿
private fun drawCornerRect(
    lx: Float, ty: Float, rx: Float, by: Float,
    r: Float, g: Float, b: Float, a: Float,
    rad: Float, segments: Int
) {
    // 中心矩形
    Gui.drawRect(
        (lx + rad).toInt(), (ty + rad).toInt(),
        (rx - rad).toInt(), (by - rad).toInt(),
        ((a*255).toInt() shl 24) or ((r*255).toInt() shl 16) or ((g*255).toInt() shl 8) or (b*255).toInt()
    )
    // 边矩形
    Gui.drawRect(lx.toInt(),        (ty + rad).toInt(), (lx + rad).toInt(), (by - rad).toInt(), colorInt(r,g,b,a))
    Gui.drawRect((rx - rad).toInt(), (ty + rad).toInt(), rx.toInt(),       (by - rad).toInt(), colorInt(r,g,b,a))
    Gui.drawRect((lx + rad).toInt(), ty.toInt(),        (rx - rad).toInt(), (ty + rad).toInt(), colorInt(r,g,b,a))
    Gui.drawRect((lx + rad).toInt(), (by - rad).toInt(), (rx - rad).toInt(), by.toInt(),        colorInt(r,g,b,a))

    // 四个圆角扇面
    fun corner(cx: Float, cy: Float, startAngle: Float) {
        glBegin(GL_TRIANGLE_FAN)
        glColor4f(r, g, b, a)
        glVertex2f(cx, cy)
        for (i in 0..segments) {
            val ang = startAngle + (i / segments.toFloat()) * (Math.PI/2)
            glVertex2f(
                cx + cos(ang).toFloat() * rad,
                cy + sin(ang).toFloat() * rad
            )
        }
        glEnd()
    }
    corner(lx + rad, ty + rad, Math.PI.toFloat())
    corner(rx - rad, ty + rad, (-Math.PI/2).toFloat())
    corner(rx - rad, by - rad, 0f)
    corner(lx + rad, by - rad, (Math.PI/2).toFloat())
}

private fun drawEdgeRect(
    x1: Float, y1: Float, x2: Float, y2: Float,
    r: Float, g: Float, b: Float, a: Float,
    alphaInner: Float, alphaOuter: Float
) {
    glBegin(GL_TRIANGLE_STRIP)
    glColor4f(r, g, b, a * alphaInner)
    glVertex2f(x1, y1)
    glVertex2f(x2, y1)
    glColor4f(r, g, b, a * alphaOuter)
    glVertex2f(x1, y2)
    glVertex2f(x2, y2)
    glEnd()
}

// 方便生成 colorInt
private fun colorInt(r: Float, g: Float, b: Float, a: Float): Int {
    return (( (a*255).toInt() and 0xFF) shl 24) or
            (( (r*255).toInt() and 0xFF) shl 16) or
            (( (g*255).toInt() and 0xFF) shl  8) or
            ( (b*255).toInt() and 0xFF)
}

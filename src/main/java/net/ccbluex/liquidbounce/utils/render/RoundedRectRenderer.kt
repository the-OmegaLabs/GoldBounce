package net.ccbluex.liquidbounce.utils.render

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import java.nio.FloatBuffer
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.LWJGLException

object RoundedRectRenderer {
    private var programId = 0
    private var locPos    = -1
    private var locSize   = -1
    private var locRadius = -1
    private var locColor  = -1
    // Broken
    private fun initShader() {
        if (programId != 0) return

        val vertSrc = """
            #version 120
            void main() {
                gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
            }
        """.trimIndent()

        val fragSrc = """
            #version 120
            uniform vec2 u_pos;
            uniform vec2 u_size;
            uniform float u_radius;
            uniform vec4 u_color;
            void main() {
                // 计算当前像素到矩形左下角的偏移
                vec2 p = gl_FragCoord.xy - u_pos;
                // corner 表示去除圆角后的矩形半尺寸
                vec2 corner = u_size * 0.5 - vec2(u_radius);
                // 求到中点的偏移再减去 corner 区域
                vec2 d = abs(p - u_size * 0.5) - corner;
                float outside = length(max(d, vec2(0.0)));
                float inside  = min(max(d.x, d.y), 0.0);
                float dist = outside + inside;
                // 用 0.5 像素做平滑区间
                float alpha = clamp(0.5 - dist, 0.0, 1.0);
                gl_FragColor = vec4(u_color.rgb, u_color.a * alpha);
            }
        """.trimIndent()

        // 编译顶点着色器
        val vs = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vs, vertSrc)
        glCompileShader(vs)
        checkShaderCompile(vs)

        // 编译片段着色器
        val fs = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fs, fragSrc)
        glCompileShader(fs)
        checkShaderCompile(fs)

        // 链接程序
        programId = glCreateProgram().also { pid ->
            glAttachShader(pid, vs)
            glAttachShader(pid, fs)
            glLinkProgram(pid)
            checkProgramLink(pid)
        }

        locPos    = glGetUniformLocation(programId, "u_pos")
        locSize   = glGetUniformLocation(programId, "u_size")
        locRadius = glGetUniformLocation(programId, "u_radius")
        locColor  = glGetUniformLocation(programId, "u_color")
    }

    private fun checkShaderCompile(shader: Int) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            // 使用返回 String 的重载
            val infoLog = glGetShaderInfoLog(shader, 1024)
            error("Shader compile failed:\n$infoLog")
        }
    }

    private fun checkProgramLink(program: Int) {
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            // 使用返回 String 的重载
            val infoLog = glGetProgramInfoLog(program, 1024)
            error("Program link failed:\n$infoLog")
        }
    }

    fun drawRoundedRect(x1: Float, y1: Float, x2: Float, y2: Float, colorInt: Int, radius: Float) {
        initShader()

        val a = ((colorInt ushr 24) and 0xFF) / 255.0f
        val r = ((colorInt ushr 16) and 0xFF) / 255.0f
        val g = ((colorInt ushr 8)  and 0xFF) / 255.0f
        val b = (colorInt and 0xFF)         / 255.0f

        val (nx1, ny1, nx2, ny2) = if (x1 <= x2) {
            Quad(x1, y1, x2, y2)
        } else {
            Quad(x2, y2, x1, y1)
        }

        val width  = nx2 - nx1
        val height = ny2 - ny1

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glUseProgram(programId)
        glUniform2f(locPos, nx1, ny1)
        glUniform2f(locSize, width, height)
        glUniform1f(locRadius, radius)
        glUniform4f(locColor, r, g, b, a)

        glBegin(GL_QUADS)
        glVertex2f(nx1, ny1)
        glVertex2f(nx2, ny1)
        glVertex2f(nx2, ny2)
        glVertex2f(nx1, ny2)
        glEnd()

        glUseProgram(0)
        glDisable(GL_BLEND)
    }

    private data class Quad(val x1: Float, val y1: Float, val x2: Float, val y2: Float)
}

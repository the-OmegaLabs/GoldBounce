package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

/**
 * A utility object for rendering operations, including SVG path drawing from ResourceLocations.
 */
object SvgUtils {

    // Cache to store parsed SVG paths to avoid reading and parsing files repeatedly.
    private val pathCache = mutableMapOf<ResourceLocation, List<Pair<Float, Float>>>()

    /**
     * Draws a shape defined by an SVG file located at the given ResourceLocation.
     * This function supports basic commands: M, L, H, V, Z (and their lowercase counterparts).
     * It automatically scales the path to fit within the specified width and height.
     *
     * @param location The ResourceLocation of the .svg file.
     * @param x The x-coordinate for the top-left corner of the drawing area.
     * @param y The y-coordinate for the top-left corner of the drawing area.
     * @param width The width of the drawing area.
     * @param height The height of the drawing area.
     * @param color The color to draw the path with.
     */
    fun drawSvg(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, color: Color) {
        // Get parsed points from cache or load from file
        val points = pathCache.getOrPut(location) {
            try {
                val rawPath = readSvgFromResource(location)
                parseSvgPath(rawPath)
            } catch (e: IOException) {
                println("[SvgUtils] Could not read SVG from: $location. Error: ${e.message}")
                emptyList()
            }
        }

        if (points.isEmpty()) return

        // Calculate the original bounding box of the SVG path
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (p in points) {
            minX = min(minX, p.first)
            minY = min(minY, p.second)
            maxX = max(maxX, p.first)
            maxY = max(maxY, p.second)
        }

        val pathWidth = maxX - minX
        val pathHeight = maxY - minY

        if (pathWidth <= 0 || pathHeight <= 0) return

        // Calculate scale factors
        val scaleX = width / pathWidth
        val scaleY = height / pathHeight

        glPushMatrix()
        glTranslated(x.toDouble(), y.toDouble(), 0.0)
        glScalef(scaleX, scaleY, 1.0f)
        glTranslated(-minX.toDouble(), -minY.toDouble(), 0.0)

        // Set up OpenGL for smooth line drawing
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        // Set color
        GlStateManager.color(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )
        
        // Draw the path as a filled shape
        glBegin(GL_TRIANGLE_FAN)
        points.forEach { (px, py) ->
            glVertex2f(px, py)
        }
        glEnd()

        // Restore OpenGL state
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GlStateManager.color(1f, 1f, 1f, 1f) // Reset color

        glPopMatrix()
    }

    /**
     * Reads the content of a resource file as a string.
     */
    @Throws(IOException::class)
    private fun readSvgFromResource(location: ResourceLocation): String {
        val inputStream = Minecraft.getMinecraft().resourceManager.getResource(location).inputStream
        return inputStream.bufferedReader().use { it.readText() }
    }

    /**
     * Parses an SVG path string into a list of 2D points.
     */
    private fun parseSvgPath(path: String): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        var lastX = 0f
        var lastY = 0f

        // Basic cleanup: replace commas with spaces and handle multiple spaces.
        val cleanedPath = path.replace(",", " ").replace(Regex("\\s+"), " ")
        
        try {
            val parts = cleanedPath.split(" ").filter { it.isNotBlank() }
            var i = 0
            while (i < parts.size) {
                val command = parts[i].first()

                if (!command.isLetter()) {
                    i++
                    continue
                }
                
                val isRelative = command.isLowerCase()

                try {
                    when (command.uppercaseChar()) {
                        'M' -> { // MoveTo
                            val newX = parts[i + 1].toFloat()
                            val newY = parts[i + 2].toFloat()
                            lastX = if (isRelative) lastX + newX else newX
                            lastY = if (isRelative) lastY + newY else newY
                            points.add(lastX to lastY)
                            i += 3
                        }
                        'L' -> { // LineTo
                            val newX = parts[i + 1].toFloat()
                            val newY = parts[i + 2].toFloat()
                            lastX = if (isRelative) lastX + newX else newX
                            lastY = if (isRelative) lastY + newY else newY
                            points.add(lastX to lastY)
                            i += 3
                        }
                        'H' -> { // Horizontal LineTo
                            val newX = parts[i + 1].toFloat()
                            lastX = if (isRelative) lastX + newX else newX
                            points.add(lastX to lastY)
                            i += 2
                        }
                        'V' -> { // Vertical LineTo
                            val newY = parts[i + 1].toFloat()
                            lastY = if (isRelative) lastY + newY else newY
                            points.add(lastX to lastY)
                            i += 2
                        }
                        'Z' -> { // ClosePath
                            i++
                        }
                        else -> i++
                    }
                } catch (e: IndexOutOfBoundsException) {
                    println("[SvgUtils] Malformed SVG command '$command' in path. Skipping.")
                    break
                }
            }
        } catch (e: Exception) {
            println("[SvgUtils] Error parsing SVG path: ${e.message}")
            return emptyList()
        }
        return points
    }
}

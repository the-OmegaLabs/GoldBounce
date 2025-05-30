/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.ui.font

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.file.FileManager.fontsDir
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.gui.FontRenderer
import java.awt.Font
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.inputStream

object Fonts : MinecraftInstance() {

    @FontDetails(fontName = "Minecraft Font")
    val minecraftFont: FontRenderer = mc.fontRendererObj

    @FontDetails(fontName = "Product Sans Medium", fontSize = 16)
    lateinit var font16: GameFontRenderer

    @FontDetails(fontName = "Product Sans Medium", fontSize = 18)
    lateinit var font18: GameFontRenderer

    @FontDetails(fontName = "Product Sans Medium", fontSize = 32)
    lateinit var font32: GameFontRenderer

    @FontDetails(fontName = "Product Sans Medium", fontSize = 35)
    lateinit var font35: GameFontRenderer

    @FontDetails(fontName = "Product Sans Medium", fontSize = 40)
    lateinit var font40: GameFontRenderer

    @FontDetails(fontName = "Product Sans Medium", fontSize = 72)
    lateinit var font72: GameFontRenderer

    @FontDetails(fontName = "Honor Sans Regular", fontSize = 35)
    lateinit var fontHonor35: GameFontRenderer

    @FontDetails(fontName = "Honor Sans Regular", fontSize = 40)
    lateinit var fontHonor40: GameFontRenderer

    @FontDetails(fontName = "Noto Sans SC Regular", fontSize = 35)
    lateinit var fontNoto35: GameFontRenderer

    @FontDetails(fontName = "Noto Sans SC Regular", fontSize = 60)
    lateinit var fontNoto60: GameFontRenderer

    @FontDetails(fontName = "Noto Sans SC Regular", fontSize = 80)
    lateinit var fontNoto80: GameFontRenderer

    @FontDetails(fontName = "Product Sans Bold", fontSize = 40)
    lateinit var fontBold40: GameFontRenderer

    @FontDetails(fontName = "Product Sans Bold", fontSize = 35)
    lateinit var fontBold35: GameFontRenderer

    @FontDetails(fontName = "Product Sans Bold", fontSize = 180)
    lateinit var fontBold180: GameFontRenderer

    @FontDetails(fontName = "Helvetica Display Medium", fontSize = 30)
    lateinit var fontHD30: GameFontRenderer

    @FontDetails(fontName = "Helvetica Display Medium", fontSize = 35)
    lateinit var fontHD35: GameFontRenderer

    @FontDetails(fontName = "Helvetica Display Medium", fontSize = 40)
    lateinit var fontHD40: GameFontRenderer

    @FontDetails(fontName = "Helvetica Display Medium", fontSize = 45)
    lateinit var fontHD45: GameFontRenderer

    @FontDetails(fontName = "Helvetica Display Medium", fontSize = 50)
    lateinit var fontHD50: GameFontRenderer

    @FontDetails(fontName = "SF UI Display Medium", fontSize = 35)
    lateinit var fontSF35: GameFontRenderer

    @FontDetails(fontName = "SF UI Display Medium", fontSize = 40)
    lateinit var fontSF40: GameFontRenderer

    @FontDetails(fontName = "SF UI Display Medium", fontSize = 45)
    lateinit var fontSF45: GameFontRenderer

    @FontDetails(fontName = "notification60", fontSize = 30)
    lateinit var notification60: GameFontRenderer

    private val CUSTOM_FONT_RENDERERS = hashMapOf<FontInfo, FontRenderer>()

    fun loadFonts() {
        val l = System.currentTimeMillis()
        LOGGER.info("Loading Fonts.")

        downloadFonts()
        font16 = GameFontRenderer(getFont("Product Sans Regular.ttf", 16))
        font18 = GameFontRenderer(getFont("Product Sans Regular.ttf", 18))
        font32 = GameFontRenderer(getFont("Product Sans Regular.ttf", 32))
        font35 = GameFontRenderer(getFont("Product Sans Regular.ttf", 35))
        font40 = GameFontRenderer(getFont("Product Sans Regular.ttf", 40))
        font72 = GameFontRenderer(getFont("Product Sans Regular.ttf", 72))
        fontHD30 = GameFontRenderer(getFont("HelveticaMedium.ttf", 30))
        fontHD35 = GameFontRenderer(getFont("HelveticaMedium.ttf", 35))
        fontHD40 = GameFontRenderer(getFont("HelveticaMedium.ttf", 40))
        fontHD45 = GameFontRenderer(getFont("HelveticaMedium.ttf", 45))
        fontHD50 = GameFontRenderer(getFont("HelveticaMedium.ttf", 50))
        fontSF35 = GameFontRenderer(getFont("SF-UI-Display-Medium.ttf", 35))
        fontSF40 = GameFontRenderer(getFont("SF-UI-Display-Medium.ttf", 40))
        fontSF45 = GameFontRenderer(getFont("SF-UI-Display-Medium.ttf", 45))
        fontBold35 = GameFontRenderer(getFont("Product Sans Bold.ttf", 35))
        fontBold40 = GameFontRenderer(getFont("Product Sans Bold.ttf", 40))
        fontHonor40 = GameFontRenderer(getFont("Product Sans Bold.ttf", 40))
        fontHonor35 = GameFontRenderer(getFont("Product Sans Bold.ttf", 35))
        fontNoto35 = GameFontRenderer(getFont("NotoSansSC-Regular.ttf", 35))
        fontNoto60 = GameFontRenderer(getFont("NotoSansSC-Regular.ttf", 60))
        fontNoto80 = GameFontRenderer(getFont("NotoSansSC-Regular.ttf", 80))
        fontBold180 = GameFontRenderer(getFont("Product Sans Bold.ttf", 180))
        notification60 = GameFontRenderer(getFont("iconnovo.ttf", 30))

        try {
            CUSTOM_FONT_RENDERERS.clear()
            val fontsFile = File(fontsDir, "fonts.json")
            if (fontsFile.exists()) {
                val jsonElement = JsonParser().parse(fontsFile.bufferedReader())
                if (jsonElement is JsonNull) return
                val jsonArray = jsonElement as JsonArray
                for (element in jsonArray) {
                    if (element is JsonNull) return
                    val fontObject = element as JsonObject
                    val font = getFont(fontObject["fontFile"].asString, fontObject["fontSize"].asInt)
                    CUSTOM_FONT_RENDERERS[FontInfo(font)] = GameFontRenderer(font)
                }
            } else {
                fontsFile.createNewFile()

                fontsFile.writeText(PRETTY_GSON.toJson(JsonArray()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        LOGGER.info("Loaded Fonts. (" + (System.currentTimeMillis() - l) + "ms)")
    }

    private fun downloadFonts() {
        try {
            val outputFile = File(fontsDir, "roboto.zip")
            if (!outputFile.exists()) {
                LOGGER.info("Downloading fonts...")
                LOGGER.error("Download font module is not available now")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getFontRenderer(name: String, size: Int): FontRenderer {
        for (field in Fonts::class.java.declaredFields) {
            try {
                field.isAccessible = true
                val obj = field[null]
                if (obj is FontRenderer) {
                    val fontDetails = field.getAnnotation(FontDetails::class.java)
                    if (fontDetails.fontName == name && fontDetails.fontSize == size) return obj
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return CUSTOM_FONT_RENDERERS.getOrDefault(FontInfo(name, size), minecraftFont)
    }

    fun getFontDetails(fontRenderer: FontRenderer): FontInfo? {
        for (field in Fonts::class.java.declaredFields) {
            try {
                field.isAccessible = true
                val obj = field[null]
                if (obj == fontRenderer) {
                    val fontDetails = field.getAnnotation(FontDetails::class.java)
                    return FontInfo(fontDetails.fontName, fontDetails.fontSize)
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        for ((key, value) in CUSTOM_FONT_RENDERERS) {
            if (value === fontRenderer) return key
        }
        return null
    }

    val fonts: List<FontRenderer>
        get() {
            val fonts = mutableListOf<FontRenderer>()
            for (fontField in Fonts::class.java.declaredFields) {
                try {
                    fontField.isAccessible = true
                    val fontObj = fontField[null]
                    if (fontObj is FontRenderer) fonts += fontObj
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
            fonts += CUSTOM_FONT_RENDERERS.values
            return fonts
        }

    fun getFont(fontName: String, size: Int) =
        try {
            val inputStream = File(fontsDir, fontName).inputStream()
            var awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream)
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size.toFloat())
            inputStream.close()
            awtClientFont
        } catch (e: Exception) {
            e.printStackTrace()
            Font("default", Font.PLAIN, size)
        }

    private fun extractZip(zipFile: String, outputFolder: String) {
        val buffer = ByteArray(1024)
        try {
            val folder = File(outputFolder)
            if (!folder.exists()) folder.mkdir()
            val zipInputStream = ZipInputStream(Paths.get(zipFile).inputStream())
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                val newFile = File(outputFolder + File.separator + zipEntry.name)
                File(newFile.parent).mkdirs()
                val fileOutputStream = newFile.outputStream()
                var i: Int
                while (zipInputStream.read(buffer).also { i = it } > 0) fileOutputStream.write(buffer, 0, i)
                fileOutputStream.close()
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.closeEntry()
            zipInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class FontInfo(val name: String?, val fontSize: Int) {

        constructor(font: Font) : this(font.name, font.size)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false

            val fontInfo = other as FontInfo

            return fontSize == fontInfo.fontSize && name == fontInfo.name
        }

        override fun hashCode(): Int {
            var result = name?.hashCode() ?: 0
            result = 31 * result + fontSize
            return result
        }
    }
}
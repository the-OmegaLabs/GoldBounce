package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.floatValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import kotlin.math.sin

object Halo : Module("BigCirclesOnYourHead", Category.RENDER) {

    private val characterChoices = arrayOf(
        "alice", "shiroko", "reisa", "hoshino", "azusa", "iori", "izuna", "kayoko","shengya","sunzheng"
    )
    private val characterIndex by choices("Character", characterChoices, "alice")
    private val onlySelf by _boolean("OnlySelf", true)
    private val trackCamera by _boolean("TrackCamera", true)
    private val size by floatValue("Scale", 0.5f, 0.1f..2f)
    private val offsetX by floatValue("OffsetX", 0f, -2f..2f)
    private val offsetY by floatValue("OffsetY", 0.4f, -2f..2f)
    private val offsetZ by floatValue("OffsetZ", 0f, -2f..2f)
    private val floatRange by floatValue("FloatRange", 0.05f, 0f..1f)
    private val floatSpeed by floatValue("FloatSpeed", 0.5f, 0.01f..5f)
    private val flipEnabled by _boolean("FlipEnabled", true)
    private val flipX by floatValue("FlipX", 0f, -180f..180f)
    private val flipY by floatValue("FlipY", 5f, -180f..180f)
    private val flipZ by floatValue("FlipZ", 0f, -180f..180f)

    private val texture: ResourceLocation
        get() {
            val name = characterIndex.toString().lowercase()
            return ResourceLocation("liquidbounce/halo/$name.png")
        }

    override fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        val mc = mc
        val world = mc.theWorld ?: return
        val partialTicks = event.partialTicks.toDouble()

        val players = if (!onlySelf) world.playerEntities else listOfNotNull(mc.thePlayer)

        val time = world.totalWorldTime + partialTicks
        val floatY = sin(time * floatSpeed) * floatRange

        try {
            mc.textureManager.bindTexture(texture)
        } catch (t: Throwable) {
            return
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GlStateManager.pushMatrix()

        try {
            GlStateManager.disableLighting()
            GlStateManager.disableCull()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.depthMask(false)
            GlStateManager.disableDepth()
            GlStateManager.color(1f, 1f, 1f, 1f)

            val renderManager = mc.renderManager

            for (entityPlayer in players) {
                if (entityPlayer.isSpectator || !entityPlayer.isEntityAlive) continue

                val interpX = entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * partialTicks
                val interpY = entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * partialTicks
                val interpZ = entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * partialTicks

                val baseY = entityPlayer.getEyeHeight().toDouble() + offsetY + floatY
                val hx = interpX + offsetX
                val hy = interpY + baseY
                val hz = interpZ + offsetZ

                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    (hx - renderManager.viewerPosX).toFloat(),
                    (hy - renderManager.viewerPosY).toFloat(),
                    (hz - renderManager.viewerPosZ).toFloat()
                )

                val scale = size.toDouble()
                GlStateManager.scale(scale.toFloat(), scale.toFloat(), scale.toFloat())

                val viewY = renderManager.playerViewY
                val viewX = renderManager.playerViewX

                GlStateManager.rotate(-viewY, 0f, 1f, 0f)
                if (trackCamera) {
                    GlStateManager.rotate(viewX, 1f, 0f, 0f)
                }

                if (flipEnabled) {
                    GlStateManager.rotate(flipX, 1f, 0f, 0f)
                    GlStateManager.rotate(flipY, 0f, 1f, 0f)
                    GlStateManager.rotate(flipZ, 0f, 0f, 1f)
                }

                val half = 0.5f
                val tess = Tessellator.getInstance()
                val wr = tess.worldRenderer
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
                wr.pos((-half).toDouble(), (-half).toDouble(), 0.0).tex(0.0, 1.0).color(1f, 1f, 1f, 1f).endVertex()
                wr.pos(half.toDouble(), (-half).toDouble(), 0.0).tex(1.0, 1.0).color(1f, 1f, 1f, 1f).endVertex()
                wr.pos(half.toDouble(), half.toDouble(), 0.0).tex(1.0, 0.0).color(1f, 1f, 1f, 1f).endVertex()
                wr.pos((-half).toDouble(), half.toDouble(), 0.0).tex(0.0, 0.0).color(1f, 1f, 1f, 1f).endVertex()
                tess.draw()

                GlStateManager.popMatrix()
            }
        } finally {

            try {
                GlStateManager.color(1f, 1f, 1f, 1f)
            } catch (_: Throwable) {
            }

            try {
                GlStateManager.enableDepth()
                GlStateManager.depthMask(true)
                GlStateManager.disableBlend()
                GlStateManager.enableCull()
                GlStateManager.enableLighting()
            } catch (_: Throwable) {
            }

            GlStateManager.popMatrix()
            GL11.glPopAttrib()
        }
    }

    override val tag: String
        get() = characterIndex.toString()
}

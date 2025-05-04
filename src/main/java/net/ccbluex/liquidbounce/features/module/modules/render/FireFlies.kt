package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.fireflies.FireFilesUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.math.MathUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils
import net.ccbluex.liquidbounce.utils.skid.moonlight.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object FireFlies : Module(name = "金液", category = Category.RENDER) {
    private val darkImprint: BoolValue = BoolValue("DarkImprint", false)
    private val lighting: BoolValue = BoolValue("Lighting", false)
    private val spawnDelay: FloatValue = FloatValue("SpawnDelay", 3.0f, 1.0f..10.0f)
    private val FIRE_PARTS_LIST = ArrayList<FirePart>()
    private val FIRE_PART_TEX = ResourceLocation("liquidbounce/firepart.png")
    private val tessellator: Tessellator = Tessellator.getInstance()
    private val buffer: WorldRenderer = tessellator.worldRenderer

    private val maxPartAliveTime: Long
        get() = 6000L

    private val partColor: Int
        get() = rainbow(0).rgb

    private fun getRandom(min: Double, max: Double): Double {
        return MathUtils.randomizeDouble(min, max)
    }

    private fun generateVecForPart(rangeXZ: Double, rangeY: Double): Vec3 {
        var pos = mc.thePlayer.positionVector.addVector(
            getRandom(-rangeXZ, rangeXZ),
            getRandom(-rangeY / 2.0, rangeY), getRandom(-rangeXZ, rangeXZ)
        )
        for (i in 0..29) {
            pos = mc.thePlayer.positionVector.addVector(
                getRandom(-rangeXZ, rangeXZ),
                getRandom(-rangeY / 2.0, rangeY), getRandom(-rangeXZ, rangeXZ)
            )
        }
        return pos
    }

    private fun setupGLDrawsFireParts(partsRender: Runnable) {
        val glX = mc.renderManager.viewerPosX
        val glY = mc.renderManager.viewerPosY
        val glZ = mc.renderManager.viewerPosZ
        GL11.glPushMatrix()
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)
        mc.entityRenderer.disableLightmap()
        GL11.glEnable(3042)
        GL11.glLineWidth(1.0f)
        GL11.glEnable(3553)
        GL11.glDisable(2896)
        GL11.glShadeModel(7425)
        GL11.glDisable(3008)
        GL11.glDisable(2884)
        GL11.glDepthMask(false)
        GL11.glTranslated(-glX, -glY, -glZ)
        partsRender.run()
        GL11.glTranslated(glX, glY, glZ)
        GL11.glDepthMask(true)
        GL11.glEnable(2884)
        GL11.glEnable(3008)
        GL11.glLineWidth(1.0f)
        GL11.glShadeModel(7424)
        GL11.glEnable(3553)
        GlStateManager.resetColor()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GL11.glPopMatrix()
    }

    private fun bindResource(toBind: ResourceLocation) {
        mc.textureManager.bindTexture(toBind)
    }

    private fun drawBindedTexture(x: Float, y: Float, x2: Float, y2: Float, c: Int) {
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        buffer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).color(
            ColorUtils.getRedFromColor(c),
            ColorUtils.getGreenFromColor(c),
            ColorUtils.getBlueFromColor(c),
            ColorUtils.getAlphaFromColor(c)
        ).endVertex()
        buffer.pos(x.toDouble(), y2.toDouble(), 0.0).tex(0.0, 1.0).color(
            ColorUtils.getRedFromColor(c),
            ColorUtils.getGreenFromColor(c),
            ColorUtils.getBlueFromColor(c),
            ColorUtils.getAlphaFromColor(c)
        ).endVertex()
        buffer.pos(x2.toDouble(), y2.toDouble(), 0.0).tex(1.0, 1.0).color(
            ColorUtils.getRedFromColor(c),
            ColorUtils.getGreenFromColor(c),
            ColorUtils.getBlueFromColor(c),
            ColorUtils.getAlphaFromColor(c)
        ).endVertex()
        buffer.pos(x2.toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).color(
            ColorUtils.getRedFromColor(c),
            ColorUtils.getGreenFromColor(c),
            ColorUtils.getBlueFromColor(c),
            ColorUtils.getAlphaFromColor(c)
        ).endVertex()
        tessellator.draw()
    }

    private fun drawPart(part: FirePart, pTicks: Float) {
        val color = this.partColor
        if (darkImprint.get()) {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            this.drawSparkPartsList(color, part, pTicks)
            this.drawTrailPartsList(color, part)
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)
        } else {
            this.drawSparkPartsList(color, part, pTicks)
            this.drawTrailPartsList(color, part)
        }
        val pos = part.getRenderPosVec(pTicks)
        GL11.glPushMatrix()
        GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord)
        GL11.glNormal3d(1.0, 1.0, 1.0)
        GL11.glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        GL11.glRotated(
            mc.renderManager.playerViewX.toDouble(),
            if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0,
            0.0,
            0.0
        )
        GL11.glScaled(-0.1, -0.1, 0.1)
        var scale = 7.0f
        this.drawBindedTexture(-scale / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, color)
        if (lighting.get()) {
            //this.drawBindedTexture(-(scale *= 8.0f) / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, ColorUtils.applyOpacity(ColorUtils.darker(color, 0.2f), (float)ColorUtils.getAlphaFromColor(color) / 5.0f));
            this.drawBindedTexture(
                -(3.0f.let { scale *= it; scale }) / 2.0f,
                -scale / 2.0f,
                scale / 2.0f,
                scale / 2.0f,
                ColorUtils.applyOpacity(
                    ColorUtils.darker(color, 0.2f),
                    ColorUtils.getAlphaFromColor(color).toFloat() / 7.0f
                )
            )
        }
        GL11.glPopMatrix()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer != null && mc.thePlayer.ticksExisted == 1) {
            FIRE_PARTS_LIST.forEach(Consumer { obj: FirePart -> obj.setToRemove() })
        }
        FIRE_PARTS_LIST.forEach(Consumer { obj: FirePart -> obj.updatePart() })
        FIRE_PARTS_LIST.removeIf { obj: FirePart -> obj.toRemove }
        if (mc.thePlayer.ticksExisted % (spawnDelay.get() + 1.0f).toInt() == 0) {
            FIRE_PARTS_LIST.add(
                FirePart(
                    this.generateVecForPart(10.0, 4.0),
                    maxPartAliveTime.toFloat()
                )
            )
            FIRE_PARTS_LIST.add(
                FirePart(
                    this.generateVecForPart(6.0, 5.0),
                    maxPartAliveTime.toFloat()
                )
            )
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!FIRE_PARTS_LIST.isEmpty()) {
            this.setupGLDrawsFireParts {
                this.bindResource(this.FIRE_PART_TEX)
                FIRE_PARTS_LIST.forEach(Consumer { part: FirePart ->
                    this.drawPart(
                        part,
                        event.partialTicks
                    )
                })
            }
        }
    }

    private fun drawSparkPartsList(color: Int, firePart: FirePart, partialTicks: Float) {
        if (firePart.SPARK_PARTS.size < 2) {
            return
        }
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glDisable(3008)
        GL11.glEnable(2832)
        GL11.glPointSize(
            1.5f + 6.0f * MathHelper.clamp_float(
                1.0f - (getSmoothDistanceToCoord(
                    firePart.posVec.xCoord.toFloat(),
                    firePart.posVec.yCoord.toFloat() + 1.6f,
                    firePart.posVec.zCoord.toFloat(),
                    mc.thePlayer
                ) - 3.0f) / 10.0f, 0.0f, 1.0f
            )
        )
        GL11.glBegin(0)
        for (spark in firePart.SPARK_PARTS) {
            val c: Int = ColorUtils.applyOpacity(
                ColorUtils.interpolateColor(-1, color, spark.timePC().toFloat()),
                ColorUtils.getAlphaFromColor(color).toFloat() * 1f * (1.0f - spark.timePC().toFloat())
            )
            RenderUtils.glColor(c)
            GL11.glVertex3d(
                spark.getRenderPosX(partialTicks),
                spark.getRenderPosY(partialTicks),
                spark.getRenderPosZ(partialTicks)
            )
        }
        GL11.glEnd()
        GlStateManager.resetColor()
        GL11.glEnable(3008)
        GL11.glEnable(3553)
    }

    fun getSmoothDistanceToCoord(x: Float, y: Float, z: Float, entityLivingBase: EntityPlayerSP): Float {
        val pTicks = Minecraft.getMinecraft().timer.renderPartialTicks
        val xposme =
            entityLivingBase.lastTickPosX + (entityLivingBase.posX - entityLivingBase.lastTickPosX) * pTicks.toDouble()
        val yposme =
            entityLivingBase.lastTickPosY + (entityLivingBase.posY - entityLivingBase.lastTickPosY) * pTicks.toDouble()
        val zposme =
            entityLivingBase.lastTickPosZ + (entityLivingBase.posZ - entityLivingBase.lastTickPosZ) * pTicks.toDouble()
        val f = (xposme - x.toDouble()).toFloat()
        val f1 = (yposme - y.toDouble()).toFloat()
        val f2 = (zposme - z.toDouble()).toFloat()
        return MathHelper.sqrt_double((f * f + f1 * f1 + f2 * f2).toDouble())
    }

    private fun drawTrailPartsList(color: Int, firePart: FirePart) {
        if (firePart.TRAIL_PARTS.size < 2) {
            return
        }
        GL11.glDisable(3553)
        GL11.glLineWidth(
            1.0E-5f + 8.0f * MathHelper.clamp_float(
                1.0f - (getSmoothDistanceToCoord(
                    firePart.posVec.xCoord.toFloat(),
                    firePart.posVec.yCoord.toFloat() + 1.6f,
                    firePart.posVec.zCoord.toFloat(),
                    mc.thePlayer
                ) - 3.0f) / 20.0f, 0.0f, 1.0f
            )
        )
        GL11.glEnable(3042)
        GL11.glDisable(3008)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        var point = 0
        val pointsCount = firePart.TRAIL_PARTS.size
        GL11.glBegin(3)
        for (trail in firePart.TRAIL_PARTS) {
            var sizePC = point.toFloat() / pointsCount.toFloat()
            sizePC = (if (sizePC.toDouble() > 0.5) 1.0f - sizePC else sizePC) * 2.0f
            sizePC = if (sizePC > 1.0f) 1.0f else (max(sizePC.toDouble(), 0.0)).toFloat()
            val c: Int = ColorUtils.applyOpacity(color, ColorUtils.getAlphaFromColor(color).toFloat() * 1f * sizePC)
            RenderUtils.glColor(c)
            GL11.glVertex3d(trail.x, trail.y, trail.z)
            ++point
        }
        GL11.glEnd()
        GlStateManager.resetColor()
        GL11.glEnable(3008)
        GL11.glDisable(2848)
        GL11.glHint(3154, 4352)
        GL11.glLineWidth(1.0f)
        GL11.glEnable(3553)
    }

    private class FirePart(var posVec: Vec3, maxAlive: Float) {
        var TRAIL_PARTS: MutableList<TrailPart>
        var SPARK_PARTS: MutableList<SparkPart> = ArrayList()
        var prevPos: Vec3
        var alphaPC: FireFilesUtils = FireFilesUtils(0.0f, 1.0f, 0.02f)
        var msChangeSideRate: Int = this.msChangeSideRate()
        var moveYawSet: Float = getRandom(0.0, 360.0).toFloat()
        var speed: Float = getRandom(0.1, 0.25).toFloat()
        var yMotion: Float = getRandom(-0.075, 0.1).toFloat()
        var moveYaw: Float = this.moveYawSet
        var maxAlive: Float
        var startTime: Long = 0
        var rateTimer: Long = System.currentTimeMillis().also { this.startTime = it }

        var toRemove: Boolean = false

        init {
            this.prevPos = posVec
            this.maxAlive = maxAlive
            TRAIL_PARTS = ArrayList()
        }

        val timePC: Float
            get() = MathHelper.clamp_float(
                (System.currentTimeMillis() - this.startTime).toFloat() / this.maxAlive,
                0.0f,
                1.0f
            )

        fun setAlphaPCTo(to: Float) {
            alphaPC.to = to
        }

        fun getAlphaPC(): Float {
            return alphaPC.get_Anim()
        }

        fun getRenderPosVec(pTicks: Float): Vec3 {
            val pos = this.posVec
            return pos.addVector(
                -(prevPos.xCoord - pos.xCoord) * pTicks.toDouble(),
                -(prevPos.yCoord - pos.yCoord) * pTicks.toDouble(),
                -(prevPos.zCoord - pos.zCoord) * pTicks.toDouble()
            )
        }

        fun updatePart() {
            if (System.currentTimeMillis() - this.rateTimer >= msChangeSideRate.toLong()) {
                this.msChangeSideRate = this.msChangeSideRate()
                this.rateTimer = System.currentTimeMillis()
                this.moveYawSet = getRandom(0.0, 360.0).toFloat()
            }
            this.moveYaw = MathUtils.lerp(this.moveYaw, this.moveYawSet, 0.065f)
            val motionX =
                -(sin(Math.toRadians(moveYaw.toDouble())).toFloat()) * (1.005f.let { this.speed /= it; this.speed })
            val motionZ = cos(Math.toRadians(moveYaw.toDouble())).toFloat() * this.speed
            this.prevPos = this.posVec
            val scaleBox = 0.1f
            val delente = if (!mc.theWorld.getCollisionBoxes(
                    AxisAlignedBB(
                        posVec.xCoord - (scaleBox / 2.0f).toDouble(),
                        posVec.yCoord,
                        posVec.zCoord - (scaleBox / 2.0f).toDouble(),
                        posVec.xCoord + (scaleBox / 2.0f).toDouble(),
                        posVec.yCoord + scaleBox.toDouble(), posVec.zCoord + (scaleBox / 2.0f).toDouble()
                    )
                ).isEmpty()
            ) 0.3f else 1.0f
            this.posVec = posVec.addVector(
                (motionX / delente).toDouble(),
                ((1.02f.let { this.yMotion /= it; this.yMotion }) / delente).toDouble(),
                (motionZ / delente).toDouble()
            )
            if (this.timePC >= 1.0f) {
                this.setAlphaPCTo(0.0f)
                if (this.getAlphaPC() < 0.003921569f) {
                    this.setToRemove()
                }
            }
            TRAIL_PARTS.add(TrailPart(this, 400))
            if (!TRAIL_PARTS.isEmpty()) {
                TRAIL_PARTS.removeIf { obj: TrailPart -> obj.toRemove() }
            }
            for (i in 0..1) {
                SPARK_PARTS.add(SparkPart(this, 300))
            }
            SPARK_PARTS.forEach(Consumer { obj: SparkPart -> obj.motionSparkProcess() })
            if (!SPARK_PARTS.isEmpty()) {
                SPARK_PARTS.removeIf { obj: SparkPart -> obj.toRemove() }
            }
        }

        fun setToRemove() {
            this.toRemove = true
        }

        fun msChangeSideRate(): Int {
            return getRandom(300.5, 900.5).toInt()
        }
    }

    private class SparkPart(part: FirePart, var maxTime: Int) {
        var posX: Double = part.posVec.xCoord
        var posY: Double = part.posVec.yCoord
        var posZ: Double = part.posVec.zCoord
        var prevPosX: Double
        var prevPosY: Double
        var prevPosZ: Double
        var speed: Double = Math.random() / 30.0
        var radianYaw: Double = Math.random() * 360.0
        var radianPitch: Double = -90.0 + Math.random() * 180.0
        var startTime: Long = System.currentTimeMillis()

        init {
            this.prevPosX = this.posX
            this.prevPosY = this.posY
            this.prevPosZ = this.posZ
        }

        fun timePC(): Double {
            return MathHelper.clamp_float(
                (System.currentTimeMillis() - this.startTime).toFloat() / maxTime.toFloat(),
                0.0f,
                1.0f
            ).toDouble()
        }

        fun toRemove(): Boolean {
            return this.timePC() == 1.0
        }

        fun motionSparkProcess() {
            val radYaw = Math.toRadians(this.radianYaw)
            this.prevPosX = this.posX
            this.prevPosY = this.posY
            this.prevPosZ = this.posZ
            this.posX += sin(radYaw) * this.speed
            this.posY += cos(Math.toRadians(this.radianPitch - 90.0)) * this.speed
            this.posZ += cos(radYaw) * this.speed
        }

        fun getRenderPosX(partialTicks: Float): Double {
            return this.prevPosX + (this.posX - this.prevPosX) * partialTicks.toDouble()
        }

        fun getRenderPosY(partialTicks: Float): Double {
            return this.prevPosY + (this.posY - this.prevPosY) * partialTicks.toDouble()
        }

        fun getRenderPosZ(partialTicks: Float): Double {
            return this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks.toDouble()
        }
    }

    private class TrailPart(part: FirePart, var maxTime: Int) {
        var x: Double = part.posVec.xCoord
        var y: Double = part.posVec.yCoord
        var z: Double = part.posVec.zCoord
        var startTime: Long = System.currentTimeMillis()

        val timePC: Float
            get() = MathHelper.clamp_float(
                (System.currentTimeMillis() - this.startTime).toFloat() / maxTime.toLong(),
                0.0f,
                1.0f
            )

        fun toRemove(): Boolean {
            return this.timePC == 1.0f
        }
    }
    override val tag
        get() = if(darkImprint.get()) "黑的" else "白的"
}
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.settings.Debugger.transgender
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.skid.crosssine.ColorManager
import net.ccbluex.liquidbounce.utils.skid.crosssine.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object TargetStrafe : Module("TargetStrafe", Category.MOVEMENT) {
    private val thirdPersonViewValue = BoolValue("ThirdPersonView", false)
    private val renderModeValue = ListValue("RenderMode", arrayOf("Circle", "Polygon", "None"), "Polygon")
    private val lineWidthValue = FloatValue("LineWidth", 1f, 1f..10f) { !renderModeValue.equals("None") }
    private val radiusModeValue = ListValue("RadiusMode", arrayOf("Normal", "Strict"), "Normal")
    private val radiusValue = FloatValue("Radius", 0.5f, 0.1f..5.0f)
    private val ongroundValue = BoolValue("OnlyOnGround", false)
    private val holdSpaceValue = BoolValue("HoldSpace", false)
    private val onlySpeedValue = BoolValue("OnlySpeed", true)
    private val onlyFlightValue = BoolValue("OnlyFlight", true)
    private var direction = -1.0
    var targetEntity: EntityLivingBase? = null
    var isEnabled = false
    var doStrafe = true

    var callBackYaw = 0.0

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (renderModeValue.get() != "None" && canStrafe()) {
            if (targetEntity == null || !doStrafe) {
                if (transgender) chat("TargetStrafe entity$targetEntity doStrafe$doStrafe returned")
                return
            }
            val counter = intArrayOf(0)
            if (renderModeValue.get().equals("Circle", ignoreCase = true)) {
                GL11.glPushMatrix()
                GL11.glDisable(3553)
                GL11.glEnable(2848)
                GL11.glEnable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3042)
                GL11.glBlendFunc(770, 771)
                GL11.glHint(3154, 4354)
                GL11.glHint(3155, 4354)
                GL11.glHint(3153, 4354)
                GL11.glDisable(2929)
                GL11.glDepthMask(false)
                GL11.glLineWidth(lineWidthValue.get())
                GL11.glBegin(3)
                val x =
                    targetEntity!!.lastTickPosX + (targetEntity!!.posX - targetEntity!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y =
                    targetEntity!!.lastTickPosY + (targetEntity!!.posY - targetEntity!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z =
                    targetEntity!!.lastTickPosZ + (targetEntity!!.posZ - targetEntity!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                for (i in 0..359) {
                    val rainbow = Color(
                        Color.HSBtoRGB(
                            ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f).toFloat(),
                            0.7f,
                            1.0f
                        )
                    )
                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    GL11.glVertex3d(
                        x + radiusValue.get() * cos(i * 6.283185307179586 / 45.0),
                        y,
                        z + radiusValue.get() * sin(i * 6.283185307179586 / 45.0)
                    )
                }
                GL11.glEnd()
                GL11.glDepthMask(true)
                GL11.glEnable(2929)
                GL11.glDisable(2848)
                GL11.glDisable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3553)
                GL11.glPopMatrix()
            } else {
                val rad = radiusValue.get()
                GL11.glPushMatrix()
                GL11.glDisable(3553)
                GL11.glDisable(2929)
                GL11.glDepthMask(false)
                GL11.glLineWidth(lineWidthValue.get())
                GL11.glBegin(3)
                val x =
                    targetEntity!!.lastTickPosX + (targetEntity!!.posX - targetEntity!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y =
                    targetEntity!!.lastTickPosY + (targetEntity!!.posY - targetEntity!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z =
                    targetEntity!!.lastTickPosZ + (targetEntity!!.posZ - targetEntity!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                for (i in 0..10) {
                    counter[0] = counter[0] + 1
                    val rainbow = Color(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    if (rad < 0.8 && rad > 0.0) GL11.glVertex3d(
                        x + rad * cos(i * 6.283185307179586 / 3.0),
                        y,
                        z + rad * sin(i * 6.283185307179586 / 3.0)
                    )
                    if (rad < 1.5 && rad > 0.7) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 4.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 4.0)
                        )
                    }
                    if (rad < 2.0 && rad > 1.4) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 5.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 5.0)
                        )
                    }
                    if (rad < 2.4 && rad > 1.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 6.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 6.0)
                        )
                    }
                    if (rad < 2.7 && rad > 2.3) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 7.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 7.0)
                        )
                    }
                    if (rad < 6.0 && rad > 2.6) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 8.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 8.0)
                        )
                    }
                    if (rad < 7.0 && rad > 5.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 9.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 9.0)
                        )
                    }
                    if (rad < 11.0) if (rad > 6.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 10.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 10.0)
                        )
                    }
                }
                GL11.glEnd()
                GL11.glDepthMask(true)
                GL11.glEnable(2929)
                GL11.glEnable(3553)
                GL11.glPopMatrix()
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val moveFix = LiquidBounce.moduleManager[MoveFix::class.java]
        if (moveFix.state && MoveFix.mode.get() == "Bloxd") {
            return
        }

        if (doStrafe && (!ongroundValue.get() || mc.thePlayer.onGround)) {
            if (!canStrafe()) {
                isEnabled = false
                if (transgender) chat("TargetStrafe can not strafe on $targetEntity returned")
                return
            }
            var aroundVoid = false
            for (x in -1..0) for (z in -1..0)
                if (isVoid(x, z))
                    aroundVoid = true
            if (aroundVoid)
                direction *= -1
            var _1IlIll1 = 0
            if (radiusModeValue.get().equals("Strict", ignoreCase = true)) {
                _1IlIll1 = 1
            }
            if (transgender) chat("TargetStrafe trying to do MovementUtils.doTargetStrafe")
            MovementUtils.doTargetStrafe(
                targetEntity!!,
                direction.toFloat(),
                radiusValue.get(),
                event,
                _1IlIll1.toInt()
            )
            callBackYaw = RotationUtils.getRotationsEntity(targetEntity!!).yaw.toDouble()
            isEnabled = true
            if (!thirdPersonViewValue.get())
                return
            mc.gameSettings.thirdPersonView = if (canStrafe()) 3 else 0
        } else {
            isEnabled = false
            if (thirdPersonViewValue.get() && mc.gameSettings.thirdPersonView == 3) {
                mc.gameSettings.thirdPersonView = 0
            }
        }
    }

    fun canStrafe(): Boolean {
        targetEntity = if (KillAura.state) KillAura.target else null
        return state && targetEntity != null && (!holdSpaceValue.get() || mc.gameSettings.keyBindJump.isKeyDown) && (!onlySpeedValue.get() || LiquidBounce.moduleManager[Speed::class.java].state) && (!onlyFlightValue.get() || LiquidBounce.moduleManager.getModule(
            "Fly"
        )!!.state)
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        targetEntity = if (KillAura.state) KillAura.target else null
        isEnabled = canStrafe()

        if(thirdPersonViewValue.get()) {
            if(isEnabled) {
                mc.gameSettings.thirdPersonView = 3
            } else if(mc.gameSettings.thirdPersonView == 3) {
                mc.gameSettings.thirdPersonView = 0
            }
        }
    }

    private fun checkVoid(): Boolean {
        for (x in -2..2) for (z in -2..2) if (isVoid(x, z)) return true
        return false
    }

    private fun isVoid(xPos: Int, zPos: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) return true
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb = mc.thePlayer.entityBoundingBox.offset(xPos.toDouble(), -off.toDouble(), zPos.toDouble())
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
        }
        return true
    }

    override val tag get() = renderModeValue.get()
}
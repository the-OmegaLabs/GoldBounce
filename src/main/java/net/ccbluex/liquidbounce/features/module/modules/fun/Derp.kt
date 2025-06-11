package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.syncSpecialModuleRotations
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float
import net.minecraft.entity.EntityLivingBase
import kotlin.math.atan2

object Derp : Module("AntiAim", Category.FUN, subjective = true, hideModule = false) {

    private val mode by choices(
        "Mode",
        arrayOf("BigAngle", "Random", "Spinny", "AI"),  // <-- added "AI"
        "AI"
    )

    private val headless = mode == "BigAngle" || mode == "Spinny" || mode == "AI"
    private val angelSwitch by float("AngleSwitch", 30F, -180F..180F) { mode == "BigAngle" }
    private val spinny = mode == "Spinny"
    private val customPitch by float("CustomPitch", 90F, -180F..180F) { spinny }
    private val increment by float("SpinStrength", 1F, 0F..100F) { spinny }

    private var anglePhase = false

    override fun onEnable() {
        anglePhase = false
        super.onEnable()
    }

    override fun onDisable() {
        syncSpecialModuleRotations()
    }

    private fun getBigAngle(): Float {
        val result = if (anglePhase) angelSwitch else 0F
        anglePhase = !anglePhase
        return result
    }

    private fun getRandomYaw(): Float = nextFloat(-180f, 180f)
    private fun getRandomPitch(): Float = nextFloat(-90f, 90f)

    /**
     * Find the closest living entity (excluding yourself),
     * calculate the yaw needed to face it, then add 180° so
     * you’re looking directly away.
     */
    private fun getAwayFromClosestYaw(): Float {
        val playerPos = mc.thePlayer.positionVector
        // filter only living entities, not yourself
        val closest = mc.theWorld.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .filter { it !== mc.thePlayer }
            .minByOrNull { it.positionVector.distanceTo(playerPos) }
            ?: return currentRotation?.yaw ?: serverRotation.yaw

        val dx = closest.posX - playerPos.xCoord
        val dz = closest.posZ - playerPos.zCoord
        // base yaw to face target
        val targetYaw = Math.toDegrees(atan2(dz, dx)).toFloat() - 90F
        // add 180 so you look away
        return targetYaw + 180F
    }

    val rotation: Rotation
        get() {
            // start from server or current
            val base = currentRotation ?: serverRotation
            val rot = Rotation(base.yaw, base.pitch)

            // flip upside‐down head if desired

            when (mode) {
                "Spinny"   -> {
                    rot.yaw += increment
                    rot.pitch = customPitch
                }
                "BigAngle" -> rot.yaw = getAwayFromClosestYaw()+getBigAngle()
                "Random"   -> {
                    rot.pitch = getRandomPitch()
                    rot.yaw   = getRandomYaw()
                }
                "AI"       -> rot.yaw   = getAwayFromClosestYaw()
            }

            // apply sensitivity fix
            return rot.fixedSensitivity()
        }
    override val tag: String
        get() = mode
}

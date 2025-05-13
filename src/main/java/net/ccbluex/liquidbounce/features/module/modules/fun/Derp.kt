/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.syncSpecialModuleRotations
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float

object Derp : Module("AntiAim", Category.FUN, subjective = true, hideModule = false) {
    private val mode by choices(
        "Mode",
        arrayOf("BigAngle", "Random", "Spinny"), "Spinny"
    )
    private val headless = mode == "BigAngle" || mode == "Spinny"
    private val angelSwitch by float("AngleSwitch", 0F, -90F..90F) {mode == "BigAngle"}
    private val spinny = mode == "Spinny"
    private val increment by float("SpinStrength", 1F, 0F..100F) { spinny }

    override fun onDisable() {
        syncSpecialModuleRotations()
    }
    var anglePhase = false
    fun getBigAngle(): Float {
        if (anglePhase){
            anglePhase = !anglePhase
            return angelSwitch
        } else {
            anglePhase = !anglePhase
            return angelSwitch - angelSwitch
        }
    }
    fun getRandomYaw(): Float {
        return nextFloat(-180f, 0f)
    }
    fun getRandomPitch(): Float {
        return nextFloat(-90f, 90f)
    }
    val rotation: Rotation
        get() {
            val rotationToUse = currentRotation ?: serverRotation

            val rot = Rotation(rotationToUse.yaw, rotationToUse.pitch)

            if (headless)
                rot.pitch = 180F
            if (mode == "Random"){
                rot.pitch = getRandomPitch()
                rot.yaw = getRandomYaw()
            }
            rot.yaw += if (spinny) increment else if (mode == "BigAngle") getBigAngle() else 0F

            return rot.fixedSensitivity()
        }

}
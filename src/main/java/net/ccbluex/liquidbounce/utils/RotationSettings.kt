/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.withGCD
import net.ccbluex.liquidbounce.value.*
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
open class RotationSettings(owner: Module, generalApply: () -> Boolean = { true }) {

    open val rotationsValue = _boolean("Rotations", true) { generalApply() }
    open val applyServerSideValue = _boolean("ApplyServerSide", true) { rotationsActive && generalApply() }
    open val algorithmSearchSteps = intValue("AlgorithmSteps", 10, 5..20) { rotateMode.get() == "Algorithm" }
    open val algorithmPrecision = floatValue("AlgorithmPrecision", 0.5f, 0.1f..2f) { rotateMode.get() == "Algorithm" }
    open val noiseScale = floatValue("NoiseScale", 2.0f, 0.5f..5f) { rotateMode.get() == "Noise" }
    open val noiseSpeed = floatValue("NoiseSpeed", 0.5f, 0.1f..2f) { rotateMode.get() == "Noise" }
    open val noiseAdditionYaw = floatValue("NoiseAdditionYawMS", 0.0f, 0f..1000f) { rotateMode.get() == "Noise" }
    open val noiseAdditionPitch = floatValue("NoiseAdditionPitchMS", 0.0f, 0f..1000f) { rotateMode.get() == "Noise" }
    open val maxTurnSpeed = floatValue("MaxTurnSpeed", 180f, 10f..360f) { rotateMode.get() == "Noise" }
    open val improve = ListValue("NoiseImprovement",arrayOf("XY","XZ","Fallback"),"XY") { rotateMode.get() == "Noise" }
    open val simulateShortStopValue = _boolean("SimulateShortStop", false) { rotationsActive && generalApply() }
    open val rotationDiffBuildUpToStopValue = floatValue("RotationDiffBuildUpToStop", 180f, 50f..720f) { simulateShortStop }
    open val maxThresholdAttemptsToStopValue = intValue("MaxThresholdAttemptsToStop", 1, 0..5) { simulateShortStop }
    open val shortStopDurationValue = intRange("ShortStopDuration", 1..2, 1..5) { simulateShortStop }
    open val strafeValue = _boolean("Strafe", false) { rotationsActive && applyServerSide && generalApply() }
    open val strictValue = _boolean("Strict", false) { strafeValue.isActive() && generalApply() }
    open val keepRotationValue = _boolean(
        "KeepRotation", true
    ) { rotationsActive && applyServerSide && generalApply() }
    open val resetTicksValue = object : IntegerValue("ResetTicks", 1, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minimum)
        override fun isSupported() = rotationsActive && applyServerSide && generalApply()
    }

    open val legitimizeValue = _boolean("Legitimize", false) { rotationsActive && generalApply() }
    open val rotateMode = ListValue(
        "RotationMode",
        arrayOf(
            "Default",
            "Algorithm",
            "Noise"
        ),
        "Default"
    )
    open val rotationMode = rotateMode.get()
    fun getMode(): ListValue {
        return rotateMode
    }
    open val maxHorizontalAngleChangeValue: FloatValue = object : FloatValue(
        "MaxHorizontalAngleChange", 180f, 1f..180f
    ) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalAngleChange)
        override fun isSupported() = rotationsActive && generalApply()
    }

    open val minHorizontalAngleChangeValue: FloatValue = object : FloatValue(
        "MinHorizontalAngleChange", 180f, 1f..180f
    ) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalAngleChange)
        override fun isSupported() = !maxHorizontalAngleChangeValue.isMinimal() && rotationsActive && generalApply()
    }

    open val maxVerticalAngleChangeValue: FloatValue = object : FloatValue("MaxVerticalAngleChange", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minVerticalAngleChange)
        override fun isSupported() = rotationsActive && generalApply()
    }

    open val minVerticalAngleChangeValue: FloatValue = object : FloatValue("MinVerticalAngleChange", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxVerticalAngleChange)
        override fun isSupported() = !maxVerticalAngleChangeValue.isMinimal() && rotationsActive && generalApply()
    }

    open val angleResetDifferenceValue: FloatValue = object : FloatValue("AngleResetDifference", 5f.withGCD(), 0.0f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.withGCD().coerceIn(range)
        override fun isSupported() = rotationsActive && applyServerSide && generalApply()
    }

    open val minRotationDifferenceValue = FloatValue(
        "MinRotationDifference", 0f, 0f..2f
    ) { rotationsActive && generalApply() }

    // Variables for easier access
    val rotations by rotationsValue
    val applyServerSide by applyServerSideValue
    val simulateShortStop by simulateShortStopValue
    val rotationDiffBuildUpToStop by rotationDiffBuildUpToStopValue
    val maxThresholdAttemptsToStop by maxThresholdAttemptsToStopValue
    val shortStopDuration by shortStopDurationValue
    val strafe by strafeValue
    val strict by strictValue
    val keepRotation by keepRotationValue
    val resetTicks by resetTicksValue
    val legitimize by legitimizeValue
    val maxHorizontalAngleChange by maxHorizontalAngleChangeValue
    val minHorizontalAngleChange by minHorizontalAngleChangeValue
    val maxVerticalAngleChange by maxVerticalAngleChangeValue
    val minVerticalAngleChange by minVerticalAngleChangeValue
    val angleResetDifference by angleResetDifferenceValue
    val minRotationDifference by minRotationDifferenceValue

    var prioritizeRequest = false
    var immediate = false
    var instant = false

    var rotDiffBuildUp = 0f
    var maxThresholdReachAttempts = 0

    open val rotationsActive
        get() = rotations

    val horizontalSpeed
        get() = minHorizontalAngleChange..maxHorizontalAngleChange

    val verticalSpeed
        get() = minVerticalAngleChange..maxVerticalAngleChange

    fun withoutKeepRotation(): RotationSettings {
        keepRotationValue.excludeWithState()

        return this
    }

    fun updateSimulateShortStopData(diff: Float) {
        rotDiffBuildUp += diff
    }

    fun resetSimulateShortStopData() {
        rotDiffBuildUp = 0f
        maxThresholdReachAttempts = 0
    }

    fun shouldPerformShortStop(): Boolean {
        if (abs(rotDiffBuildUp) < rotationDiffBuildUpToStop || !simulateShortStop)
            return false

        if (maxThresholdReachAttempts < maxThresholdAttemptsToStop) {
            maxThresholdReachAttempts++
            return false
        }

        return true
    }

    init {
        owner.addConfigurable(this)
    }
}

class RotationSettingsWithRotationModes(
    owner: Module, listValue: ListValue, generalApply: () -> Boolean = { true },
) : RotationSettings(owner, generalApply) {

    override val rotationsValue = super.rotationsValue.apply { excludeWithState() }

    val rotationModeValue = listValue.apply { supportCondition = generalApply }

    override val rotationMode by rotationModeValue

    override val rotationsActive: Boolean
        get() = rotationMode != "Off"

    init {
        owner.addConfigurable(this)
    }
}
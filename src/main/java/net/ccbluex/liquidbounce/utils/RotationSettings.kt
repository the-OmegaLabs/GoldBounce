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

    open val rotationsValue = boolean("Rotations", true) { generalApply() }
    open val applyServerSideValue = boolean("ApplyServerSide", true) { rotationsActive && generalApply() }
    open val algorithmSearchSteps = int("AlgorithmSteps", 10, 5..20) { rotateMode.get() == "Algorithm" }
    open val algorithmPrecision = float("AlgorithmPrecision", 0.5f, 0.1f..2f) { rotateMode.get() == "Algorithm" }
    open val noiseScale = float("NoiseScale", 2.0f, 0.5f..5f) { rotateMode.get() == "Noise" }
    open val noiseSpeed = float("NoiseSpeed", 0.5f, 0.1f..2f) { rotateMode.get() == "Noise" }
    open val noiseAdditionYaw = float("NoiseAdditionYawMS", 0.0f, 0f..1000f) { rotateMode.get() == "Noise" }
    open val noiseAdditionPitch = float("NoiseAdditionPitchMS", 0.0f, 0f..1000f) { rotateMode.get() == "Noise" }
    open val maxTurnSpeed = float("MaxTurnSpeed", 180f, 10f..360f) { rotateMode.get() == "Noise" }
    open val improve = ListValue("NoiseImprovement",arrayOf("XY","XZ","Fallback"),"XY") { rotateMode.get() == "Noise" }
    open val simulateShortStopValue = boolean("SimulateShortStop", false) { rotationsActive && generalApply() }
    open val rotationDiffBuildUpToStopValue = float("RotationDiffBuildUpToStop", 180f, 50f..720f) { simulateShortStop }
    open val maxThresholdAttemptsToStopValue = int("MaxThresholdAttemptsToStop", 1, 0..5) { simulateShortStop }
    open val shortStopDurationValue = intRange("ShortStopDuration", 1..2, 1..5) { simulateShortStop }
    open val strafeValue = boolean("Strafe", false) { rotationsActive && applyServerSide && generalApply() }
    open val strictValue = boolean("Strict", false) { strafeValue.isActive() && generalApply() }
    open val keepRotationValue = boolean(
        "KeepRotation", true
    ) { rotationsActive && applyServerSide && generalApply() }
    open val resetTicksValue = object : IntegerValue("ResetTicks", 1, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minimum)
        override fun isSupported() = rotationsActive && applyServerSide && generalApply()
    }

    open val legitimizeValue = boolean("Legitimize", false) { rotationsActive && generalApply() }
    open val rotateMode = ListValue(
        "RotationMode",
        arrayOf(
            "Default",
            "Algorithm",
            "Noise",
            "FlickAndCorrect",
            "Oscillating",
            "CurvePath"
        ),
        "Default"
    )
    open val rotationMode = rotateMode.get()
    val flick_modeEnabled = boolean("Flick_Enabled", true) { rotateMode.get() == "FlickAndCorrect" }
    val flick_horizontalSpeed = floatRange("Flick_HorizontalSpeed", 120f..180f, 1f..360f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val flick_verticalSpeed = floatRange("Flick_VerticalSpeed", 80f..120f, 1f..360f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val flick_accuracy = float("Flick_Accuracy", 0.75f, 0.5f..1.0f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 0.5=甩到一半, 1.0=完美甩到
    val flick_overshootChance = float("Flick_OvershootChance", 0.4f, 0f..1f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val flick_overshootAmount = float("Flick_OvershootAmount", 5f, 0f..20f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 过冲角度
    val flick_completionThreshold = float("Flick_CompletionThreshold", 5f, 1f..20f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 角度差小于此值时，Flick阶段结束
    val flick_noiseAmount = float("Flick_NoiseAmount", 0.5f, 0f..5f) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val flick_minDurationTicks = int("Flick_MinDurationTicks", 2, 0..10) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // Flick最少持续时间
    val flick_applyLegitimize = boolean("Flick_ApplyLegitimize", false) { flick_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }

    val correct_modeEnabled = boolean("Correct_Enabled", true) { rotateMode.get() == "FlickAndCorrect" }
    val correct_delayTicks = intRange("Correct_DelayTicks", 0..2, 0..10) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 甩枪和修正间的延迟
    val correct_horizontalSpeed = floatRange("Correct_HorizontalSpeed", 10f..25f, 1f..180f) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val correct_verticalSpeed = floatRange("Correct_VerticalSpeed", 10f..25f, 1f..180f) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val correct_jitterAmount = float("Correct_JitterAmount", 0.3f, 0f..3f) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 修正时的微小抖动
    val correct_stopOnTarget = boolean("Correct_StopOnTarget", true) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 是否在瞄准后完全停止
    val correct_maxDurationTicks = int("Correct_MaxDurationTicks", 15, 5..50) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" } // 修正阶段最长持续时间
    val correct_applyLegitimize = boolean("Correct_ApplyLegitimize", true) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }
    val correct_minRotationDiff = float("Correct_MinRotationDiff", 0.1f, 0f..2f) { correct_modeEnabled.get() && rotateMode.get() == "FlickAndCorrect" }

    val osc_modeEnabled = boolean("Osc_Enabled", true) { rotateMode.get() == "Oscillating" }
    val osc_baseHorizontalSpeed = floatRange("Osc_BaseHorizontalSpeed", 30f..50f, 1f..180f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" }
    val osc_baseVerticalSpeed = floatRange("Osc_BaseVerticalSpeed", 30f..50f, 1f..180f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" }
    val osc_yawAmplitude = float("Osc_YawAmplitude", 15f, 0f..45f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 水平振幅
    val osc_pitchAmplitude = float("Osc_PitchAmplitude", 8f, 0f..45f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 垂直振幅
    val osc_frequency = float("Osc_Frequency", 0.8f, 0.1f..5f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 振荡频率
    val osc_dampingFactor = float("Osc_DampingFactor", 0.85f, 0.5f..1.0f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 阻尼因子，越小振幅衰减越快
    val osc_randomness = float("Osc_Randomness", 0.1f, 0f..1f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 振幅和频率的随机性
    val osc_settleThreshold = float("Osc_SettleThreshold", 0.5f, 0.1f..5f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 振幅小于此值时停止
    val osc_startDelayTicks = intRange("Osc_StartDelayTicks", 0..1, 0..10) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" }
    val osc_noiseAmount = float("Osc_NoiseAmount", 0.2f, 0f..5f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" }
    val osc_noiseSpeed = float("Osc_NoiseSpeed", 1.0f, 0.1f..2f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" }
    val osc_adaptiveDamping = boolean("Osc_AdaptiveDamping", true) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 越接近目标阻尼越强
    val osc_pitchDominance = float("Osc_PitchDominance", 0.7f, 0f..2f) { osc_modeEnabled.get() && rotateMode.get() == "Oscillating" } // 垂直移动相对于水平移动的比例

    val curve_modeEnabled = boolean("Curve_Enabled", true) { rotateMode.get() == "CurvePath" }
    val curve_type = ListValue("Curve_Type", arrayOf("Bezier", "CatmullRom", "Linear"), "Bezier") { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" }
    val curve_speedProfile = ListValue("Curve_SpeedProfile", arrayOf("Linear", "EaseIn", "EaseOut", "EaseInOut"), "EaseInOut") { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" }
    val curve_baseSpeed = float("Curve_BaseSpeed", 80f, 10f..360f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" }
    val curve_controlPointHorizontal = float("Curve_ControlPointHorizontal", 20f, -90f..90f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" && curve_type.get() == "Bezier" } // 控制点水平偏移
    val curve_controlPointVertical = float("Curve_ControlPointVertical", 10f, -45f..45f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" && curve_type.get() == "Bezier" } // 控制点垂直偏移
    val curve_controlPointRandomness = float("Curve_ControlPointRandomness", 5f, 0f..45f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" && curve_type.get() == "Bezier" }
    val curve_gravityFactor = float("Curve_GravityFactor", 0.2f, 0f..2f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" } // 模拟手臂下坠的弧度
    val curve_pathSteps = int("Curve_PathSteps", 20, 5..100) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" } // 路径计算的精细度
    val curve_noiseAmount = float("Curve_NoiseAmount", 0.4f, 0f..5f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" } // 沿路径的噪声
    val curve_noiseSpeed = float("Curve_NoiseSpeed", 0.9f, 0.1f..2f) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" }
    val curve_legitimize = boolean("Curve_Legitimize", true) { curve_modeEnabled.get() && rotateMode.get() == "CurvePath" }

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
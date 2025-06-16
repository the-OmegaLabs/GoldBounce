/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 *
 * --- MODIFIED VERSION WITH ADVANCED ROTATION SIMULATIONS (FINAL, INTEGRATED) ---
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.bzym.OpenSimplex2S
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.FastBow
import net.ccbluex.liquidbounce.features.module.modules.render.Rotations
import net.ccbluex.liquidbounce.features.module.modules.settings.Debugger
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextDouble
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import java.util.PriorityQueue
import javax.vecmath.Vector2f
import kotlin.math.*

object RotationUtils : MinecraftInstance(), Listenable {

    // --- Core Rotation State ---
    var targetRotation: Rotation? = null
    var currentRotation: Rotation? = null
    var serverRotation: Rotation
        get() = lastRotations[0]
        set(value) {
            lastRotations = lastRotations.toMutableList().apply { set(0, value) }
        }

    private const val MAX_CAPTURE_TICKS = 3
    var lastRotations = MutableList(MAX_CAPTURE_TICKS) { Rotation.ZERO }
        set(value) {
            val updatedList = MutableList(lastRotations.size) { Rotation.ZERO }
            for (tick in 0 until MAX_CAPTURE_TICKS) {
                updatedList[tick] = if (tick == 0) value[0] else field[tick - 1]
            }
            field = updatedList
        }

    var activeSettings: RotationSettings? = null
    var resetTicks = 0

    // --- [NEW] State Machine for Complex Rotations ---
    private var rotationProcessActive = false
    private var currentRotationPhase = "None" // e.g., "Flicking", "Correcting", "Oscillating", "Curving", "Hybrid"
    private var phaseTicks = 0

    // State for FlickAndCorrect
    private var flickIntermediateTarget: Rotation? = null
    private var correctionDelayTicks = 0

    // State for Oscillating
    private var oscillationStartTime = 0L
    private var initialYawDiff = 0f
    private var initialPitchDiff = 0f

    // State for CurvePath / Hybrid
    private var rotationPath: List<Rotation> = emptyList()
    private var pathIndex = 0

    // --- Public Main Functions ---

    /**
     * The main entry point to start a rotation process.
     * It initializes the state machine based on the selected rotation mode.
     */
    fun setTargetRotation(rotation: Rotation, options: RotationSettings, ticks: Int = options.resetTicks) {
        if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90) return
        if (!options.prioritizeRequest && activeSettings?.prioritizeRequest == true) return

        if (!options.applyServerSide) {
            currentRotation?.let {
                mc.thePlayer.rotationYaw = it.yaw
                mc.thePlayer.rotationPitch = it.pitch
            }
        }

        // Reset previous process before starting a new one
        resetRotationProcess()

        val startRotation = currentRotation ?: serverRotation
        this.targetRotation = rotation
        this.activeSettings = options
        this.rotationProcessActive = true

        val rotateMode =
            if (options is RotationSettingsWithRotationModes) options.rotationMode else options.getMode().get()
        if (Debugger.RotationDebug) {
            chat("RotationMode: $rotateMode, Target: ${rotation.yaw}, ${rotation.pitch}")
        }

        when (rotateMode) {
            "FlickAndCorrect" -> initFlickAndCorrect(startRotation, rotation, options)
            "Oscillating" -> initOscillating(startRotation, rotation, options)
            "CurvePath" -> initCurvePath(startRotation, rotation, options)
            "Hybrid" -> initHybrid(startRotation, rotation, options)
            // Legacy modes are handled as a single phase
            "Default", "Algorithm", "Noise" -> {
                currentRotationPhase = rotateMode
            }
            // Add other modes from original code
            "MouseSensitive" -> initSinglePhase("MouseSensitive")
            "Inertial" -> initSinglePhase("Inertial")
            "MicroAdjustment" -> initSinglePhase("MicroAdjustment")
            "SmoothTracking" -> initSinglePhase("SmoothTracking")
            "RandomJitter" -> initSinglePhase("RandomJitter")
            "Realistic" -> initSinglePhase("Realistic")
            "Sinusoidal" -> initSinglePhase("Sinusoidal")
            else -> {
                currentRotationPhase = "Default"
            }
        }

        resetTicks = if (!options.applyServerSide || !options.resetTicksValue.isSupported()) 1 else ticks

        if (options.immediate) {
            update()
        }
    }
    /**
     * Checks if the rotation difference is not the same as the smallest GCD angle possible.
     */
    fun canUpdateRotation(current: Rotation, target: Rotation, multiplier: Int = 1): Boolean {
        if (current == target)
            return true

        val smallestAnglePossible = getFixedAngleDelta()

        return rotationDifference(target, current).withGCD() > smallestAnglePossible * multiplier
    }
    /**
     * Search for the best point on an entity to aim at.
     * This now integrates the new rotation modes logic.
     */
    fun searchCenter(
        bb: AxisAlignedBB,
        outborder: Boolean,
        randomization: RandomizationSettings? = null,
        predict: Boolean,
        lookRange: Float,
        attackRange: Float,
        throughWallsRange: Float = 0f,
        bodyPoints: List<String> = listOf("Head", "Feet"),
        horizontalSearch: ClosedFloatingPointRange<Float> = 0f..1f,
        settings: RotationSettings
    ): Rotation? {
        val scanRange = lookRange.coerceAtLeast(attackRange)

        val max = BodyPoint.fromString(bodyPoints[0]).range.endInclusive
        val min = BodyPoint.fromString(bodyPoints[1]).range.start

        if (outborder) {
            val vec3 = bb.lerpWith(nextDouble(0.5, 1.3), nextDouble(0.9, 1.3), nextDouble(0.5, 1.3))
            return toRotation(vec3, predict).fixedSensitivity()
        }

        val eyes = mc.thePlayer.eyes
        var currRotation = Rotation.ZERO.plus(currentRotation ?: mc.thePlayer.rotation)
        var attackRotation: Pair<Rotation, Float>? = null
        var lookRotation: Pair<Rotation, Float>? = null

        randomization?.takeIf { it.randomize }?.run {
            val yawMovement = angleDifference(currRotation.yaw, serverRotation.yaw).sign.takeIf { it != 0f } ?: arrayOf(
                -1f,
                1f
            ).random()
            val pitchMovement =
                angleDifference(currRotation.pitch, serverRotation.pitch).sign.takeIf { it != 0f } ?: arrayOf(
                    -1f,
                    1f
                ).random()
            currRotation.yaw += if (Math.random() > yawRandomizationChance.random()) yawRandomizationRange.random() * yawMovement else 0f
            currRotation.pitch += if (Math.random() > pitchRandomizationChance.random()) pitchRandomizationRange.random() * pitchMovement else 0f
            currRotation.fixedSensitivity()
        }

        val (hMin, hMax) = horizontalSearch.start.toDouble() to horizontalSearch.endInclusive.toDouble()
        val rotationMode =
            if (settings is RotationSettingsWithRotationModes) settings.rotationMode else settings.getMode().get()

        // Handle modes that require a specific search algorithm
        when (rotationMode) {
            "Algorithm" -> return algorithmSearch(bb)
            "Noise" -> {
                val base = defaultSearch(bb, hMin..hMax, min..max, predict)
                return base?.let { noiseRotate(it, settings) }?.fixedSensitivity()
            }
        }

        // Default search logic for all other modes
        for (x in hMin..hMax step 0.1) {
            for (y in min..max step 0.1) {
                for (z in hMin..hMax step 0.1) {
                    val vec = bb.lerpWith(x, y, z)
                    val rotation = toRotation(vec, predict).fixedSensitivity()
                    val gcdVec = bb.calculateIntercept(
                        eyes,
                        eyes + getVectorForRotation(rotation) * scanRange.toDouble()
                    )?.hitVec ?: continue
                    val distance = eyes.distanceTo(gcdVec)

                    if (distance > scanRange || (attackRotation != null && distance > attackRange)) continue
                    if (!isVisible(gcdVec) && distance > throughWallsRange) continue

                    val rotationWithDiff = rotation to rotationDifference(rotation, currRotation)
                    if (distance <= attackRange) {
                        if (attackRotation == null || rotationWithDiff.second < attackRotation.second) attackRotation =
                            rotationWithDiff
                    } else {
                        if (lookRotation == null || rotationWithDiff.second < lookRotation.second) lookRotation =
                            rotationWithDiff
                    }
                }
            }
        }

        return attackRotation?.first ?: lookRotation?.first ?: run {
            val vec = getNearestPointBB(eyes, bb)
            val dist = eyes.distanceTo(vec)
            if (dist <= scanRange && (dist <= throughWallsRange || isVisible(vec))) toRotation(vec, predict) else null
        }
    }

    /**
     * Allows you to check if your crosshair is over your target entity
     *
     * @param targetEntity       your target entity
     * @param blockReachDistance your reach
     * @return if crosshair is over target
     */
    fun isFaced(targetEntity: Entity, blockReachDistance: Double) =
        raycastEntity(blockReachDistance) { entity: Entity -> targetEntity == entity } != null

    fun coerceBodyPoint(point: BodyPoint, minPoint: BodyPoint, maxPoint: BodyPoint): BodyPoint {
        return when {
            point.rank < minPoint.rank -> minPoint
            point.rank > maxPoint.rank -> maxPoint
            else -> point
        }
    }

    fun rotationDifference(entity: Entity) =
        rotationDifference(toRotation(entity.hitBox.center, true), mc.thePlayer.rotation)

    /**
     * Search for the best point on an entity to aim at.
     * This now integrates the new rotation modes logic.
     */
    fun searchCenter(
        bb: AxisAlignedBB,
        outborder: Boolean,
        randomization: RandomizationSettings? = null,
        predict: Boolean,
        lookRange: Float,
        attackRange: Float,
        throughWallsRange: Float = 0f,
        bodyPoints: List<String> = listOf("Head", "Feet"),
        horizontalSearch: ClosedFloatingPointRange<Float> = 0f..1f
    ): Rotation? {
        val scanRange = lookRange.coerceAtLeast(attackRange)

        val max = BodyPoint.fromString(bodyPoints[0]).range.endInclusive
        val min = BodyPoint.fromString(bodyPoints[1]).range.start

        if (outborder) {
            val vec3 = bb.lerpWith(nextDouble(0.5, 1.3), nextDouble(0.9, 1.3), nextDouble(0.5, 1.3))
            return toRotation(vec3, predict).fixedSensitivity()
        }

        val eyes = mc.thePlayer.eyes
        var currRotation = Rotation.ZERO.plus(currentRotation ?: mc.thePlayer.rotation)
        var attackRotation: Pair<Rotation, Float>? = null
        var lookRotation: Pair<Rotation, Float>? = null

        randomization?.takeIf { it.randomize }?.run {
            val yawMovement = angleDifference(currRotation.yaw, serverRotation.yaw).sign.takeIf { it != 0f } ?: arrayOf(
                -1f,
                1f
            ).random()
            val pitchMovement =
                angleDifference(currRotation.pitch, serverRotation.pitch).sign.takeIf { it != 0f } ?: arrayOf(
                    -1f,
                    1f
                ).random()
            currRotation.yaw += if (Math.random() > yawRandomizationChance.random()) yawRandomizationRange.random() * yawMovement else 0f
            currRotation.pitch += if (Math.random() > pitchRandomizationChance.random()) pitchRandomizationRange.random() * pitchMovement else 0f
            currRotation.fixedSensitivity()
        }

        val (hMin, hMax) = horizontalSearch.start.toDouble() to horizontalSearch.endInclusive.toDouble()
        // Default search logic for all other modes
        for (x in hMin..hMax step 0.1) {
            for (y in min..max step 0.1) {
                for (z in hMin..hMax step 0.1) {
                    val vec = bb.lerpWith(x, y, z)
                    val rotation = toRotation(vec, predict).fixedSensitivity()
                    val gcdVec = bb.calculateIntercept(
                        eyes,
                        eyes + getVectorForRotation(rotation) * scanRange.toDouble()
                    )?.hitVec ?: continue
                    val distance = eyes.distanceTo(gcdVec)

                    if (distance > scanRange || (attackRotation != null && distance > attackRange)) continue
                    if (!isVisible(gcdVec) && distance > throughWallsRange) continue

                    val rotationWithDiff = rotation to rotationDifference(rotation, currRotation)
                    if (distance <= attackRange) {
                        if (attackRotation == null || rotationWithDiff.second < attackRotation.second) attackRotation =
                            rotationWithDiff
                    } else {
                        if (lookRotation == null || rotationWithDiff.second < lookRotation.second) lookRotation =
                            rotationWithDiff
                    }
                }
            }
        }

        return attackRotation?.first ?: lookRotation?.first ?: run {
            val vec = getNearestPointBB(eyes, bb)
            val dist = eyes.distanceTo(vec)
            if (dist <= scanRange && (dist <= throughWallsRange || isVisible(vec))) toRotation(vec, predict) else null
        }
    }


    // --- [NEW] Initialization functions for each mode ---

    private fun initFlickAndCorrect(start: Rotation, end: Rotation, settings: RotationSettings) {
        currentRotationPhase = "Flicking"
        correctionDelayTicks = settings.correct_delayTicks.random
        val diff = angleDifferences(end, start)
        val accuracy = settings.flick_accuracy.get()
        val overshootAmount =
            if (Math.random() < settings.flick_overshootChance.get()) settings.flick_overshootAmount.get() * -diff.x.sign else 0f

        val flickYaw = start.yaw + (diff.x * accuracy) + overshootAmount
        val flickPitch = start.pitch + (diff.y * accuracy)

        flickIntermediateTarget = Rotation(flickYaw, flickPitch)
    }

    private fun initOscillating(start: Rotation, end: Rotation, settings: RotationSettings) {
        currentRotationPhase = "Oscillating"
        oscillationStartTime = System.currentTimeMillis() - settings.osc_startDelayTicks.random * 50
        val diff = angleDifferences(end, start)
        initialYawDiff = diff.x
        initialPitchDiff = diff.y
    }

    private fun initCurvePath(start: Rotation, end: Rotation, settings: RotationSettings) {
        currentRotationPhase = "Curving"
        rotationPath = generateCurvePath(start, end, settings)
        pathIndex = 0
    }

    private fun initHybrid(start: Rotation, end: Rotation, settings: RotationSettings) {
        currentRotationPhase = "Hybrid"
        rotationPath = generateCurvePath(start, end, settings) // Use curve path as base
        pathIndex = 0
    }

    private fun initSinglePhase(mode: String) {
        currentRotationPhase = mode
    }

    // --- [NEW] Handler functions for state machine ---

    private fun handleFlickPhase(current: Rotation, finalTarget: Rotation, settings: RotationSettings): Rotation {
        val flickTarget = flickIntermediateTarget!!
        val tempSettings = settings.cloneWithSpeeds(
            settings.flick_horizontalSpeed.get(),
            settings.flick_verticalSpeed.get(),
            settings.flick_applyLegitimize.get()
        )
        var nextRotation = limitAngleChange(current, flickTarget, tempSettings)

        if (settings.flick_noiseAmount.get() > 0) {
            val noise = Rotation(
                (nextFloat(-1f, 1f) * settings.flick_noiseAmount.get()),
                (nextFloat(-1f, 1f) * settings.flick_noiseAmount.get())
            )
            nextRotation += noise
        }

        val flickComplete = rotationDifference(nextRotation, flickTarget) < 2.0f
        val targetAlreadyReached =
            rotationDifference(nextRotation, finalTarget) < settings.flick_completionThreshold.get()
        val timeout = phaseTicks > settings.flick_minDurationTicks.get()

        if ((flickComplete || targetAlreadyReached || timeout) && settings.correct_modeEnabled.get()) {
            currentRotationPhase = "Correcting"
            phaseTicks = 0
            return handleCorrectPhase(nextRotation, finalTarget, settings)
        } else if (flickComplete || targetAlreadyReached) {
            rotationProcessActive = false
        }

        return nextRotation
    }

    private fun handleCorrectPhase(current: Rotation, finalTarget: Rotation, settings: RotationSettings): Rotation {
        if (phaseTicks < correctionDelayTicks) return current

        val tempSettings = settings.cloneWithSpeeds(
            settings.correct_horizontalSpeed.get(),
            settings.correct_verticalSpeed.get(),
            settings.correct_applyLegitimize.get(),
            settings.correct_minRotationDiff.get()
        )
        var nextRotation = limitAngleChange(current, finalTarget, tempSettings)

        if (settings.correct_jitterAmount.get() > 0) {
            nextRotation = simulateRandomJitter(nextRotation, settings.correct_jitterAmount.get())
        }

        val correctionComplete = rotationDifference(nextRotation, finalTarget) < 0.1f
        val timeout = phaseTicks > (settings.correct_maxDurationTicks.get() + correctionDelayTicks)

        if (correctionComplete || timeout) {
            rotationProcessActive = false
            if (settings.correct_stopOnTarget.get()) return finalTarget
        }

        return nextRotation
    }

    private fun handleOscillatingPhase(current: Rotation, finalTarget: Rotation, settings: RotationSettings): Rotation {
        val elapsedTime = (System.currentTimeMillis() - oscillationStartTime) / 1000.0f
        var damping = settings.osc_dampingFactor.get().pow(elapsedTime)

        if (settings.osc_adaptiveDamping.get()) {
            val distFactor = (rotationDifference(current, finalTarget) / max(
                1f,
                abs(initialYawDiff) + abs(initialPitchDiff)
            )).coerceIn(0f, 1f)
            damping *= (1 - distFactor * 0.5f)
        }

        val rand = 1.0f + nextFloat(-settings.osc_randomness.get(), settings.osc_randomness.get())
        val yawAmplitude = settings.osc_yawAmplitude.get() * damping * rand
        val pitchAmplitude = settings.osc_pitchAmplitude.get() * damping * rand

        val yawOffset = sin(elapsedTime * settings.osc_frequency.get()) * yawAmplitude
        val pitchOffset =
            cos(elapsedTime * settings.osc_frequency.get() + PI.toFloat() / 2f) * pitchAmplitude * settings.osc_pitchDominance.get()

        val oscillatingTarget = Rotation(finalTarget.yaw + yawOffset, finalTarget.pitch + pitchOffset)

        val tempSettings =
            settings.cloneWithSpeeds(settings.osc_baseHorizontalSpeed.get(), settings.osc_baseVerticalSpeed.get())
        var nextRotation = limitAngleChange(current, oscillatingTarget, tempSettings)

        if (settings.osc_noiseAmount.get() > 0) {
            nextRotation =
                noiseRotate(nextRotation, settings, settings.osc_noiseAmount.get(), settings.osc_noiseSpeed.get())
        }

        if ((abs(yawAmplitude) + abs(pitchAmplitude)) < settings.osc_settleThreshold.get()) {
            rotationProcessActive = false
            return limitAngleChange(current, finalTarget, tempSettings)
        }

        return nextRotation
    }

    private fun handleCurvePhase(current: Rotation, settings: RotationSettings): Rotation {
        if (pathIndex >= rotationPath.size) {
            rotationProcessActive = false
            return rotationPath.lastOrNull() ?: current
        }

        val nextPoint = rotationPath[pathIndex]
        val tempSettings = settings.cloneWithSpeeds(
            settings.curve_baseSpeed.get()..settings.curve_baseSpeed.get(),
            settings.curve_baseSpeed.get()..settings.curve_baseSpeed.get(),
            settings.curve_legitimize.get()
        )
        var nextRotation = limitAngleChange(current, nextPoint, tempSettings)

        if (settings.curve_noiseAmount.get() > 0) {
            nextRotation =
                noiseRotate(nextRotation, settings, settings.curve_noiseAmount.get(), settings.curve_noiseSpeed.get())
        }

        if (rotationDifference(nextRotation, nextPoint) < 5.0f || phaseTicks > 2) {
            pathIndex++
            phaseTicks = 0
        }

        return nextRotation
    }

    private fun handleHybridPhase(current: Rotation, settings: RotationSettings): Rotation {
        if (pathIndex >= rotationPath.size) {
            rotationProcessActive = false
            return rotationPath.lastOrNull() ?: current
        }

        val nextPoint = rotationPath[pathIndex]
        val noisyPoint = noiseRotate(nextPoint, settings)

        val tempSettings = settings.cloneWithSpeeds(
            settings.curve_baseSpeed.get()..settings.curve_baseSpeed.get(),
            settings.curve_baseSpeed.get()..settings.curve_baseSpeed.get(),
            settings.curve_legitimize.get()
        )
        val nextRotation = limitAngleChange(current, noisyPoint, tempSettings)

        if (rotationDifference(nextRotation, nextPoint) < 5.0f) {
            pathIndex++
        }

        return nextRotation
    }

    // --- [NEW] Path Generation and Noise ---

    private fun generateCurvePath(start: Rotation, end: Rotation, settings: RotationSettings): List<Rotation> {
        val path = mutableListOf<Rotation>()
        val steps = settings.curve_pathSteps.get()
        val diff = angleDifferences(end, start)
        val midPoint = Rotation(start.yaw + diff.x * 0.5f, start.pitch + diff.y * 0.5f)
        val hOffset = settings.curve_controlPointHorizontal.get() + nextFloat(
            -1f,
            1f
        ) * settings.curve_controlPointRandomness.get()
        val vOffset =
            settings.curve_controlPointVertical.get() + nextFloat(-1f, 1f) * settings.curve_controlPointRandomness.get()
        val controlPoint = Rotation(
            midPoint.yaw + hOffset,
            midPoint.pitch + vOffset - (abs(diff.x) * settings.curve_gravityFactor.get())
        )

        for (i in 0..steps) {
            var t = i.toFloat() / steps

            t = when (settings.curve_speedProfile.get()) {
                "EaseOut" -> 1 - (1 - t).pow(3)
                "EaseIn" -> t.pow(3)
                "EaseInOut" -> if (t < 0.5f) 4 * t * t * t else 1 - (-2 * t + 2).pow(3) / 2
                else -> t // Linear
            }

            val yaw = (1 - t).pow(2) * start.yaw + 2 * t * (1 - t) * controlPoint.yaw + t.pow(2) * end.yaw
            val pitch = (1 - t).pow(2) * start.pitch + 2 * t * (1 - t) * controlPoint.pitch + t.pow(2) * end.pitch

            path.add(Rotation(yaw, pitch))
        }
        return path
    }

    private fun noiseRotate(
        baseRotation: Rotation,
        settings: RotationSettings,
        scale: Float = settings.noiseScale.get(),
        speed: Float = settings.noiseSpeed.get()
    ): Rotation {
        val time = System.currentTimeMillis() / 1000.0 * speed
        val yawNoise = tryRunPerlinNoise(settings, settings.noiseAdditionYaw.get().toLong(), time) * scale
        val pitchNoise = tryRunPerlinNoise(settings, settings.noiseAdditionPitch.get().toLong(), time) * scale
        return baseRotation + Rotation(yawNoise, pitchNoise)
    }

    private fun tryRunPerlinNoise(settings: RotationSettings, addMilliseconds: Long, time: Double): Float {
        val improve = settings.improve.get()
        val scale = settings.noiseScale.get()
        return when (improve) {
            "XY" -> OpenSimplex2S.noise3_ImproveXY(
                mc.thePlayer.uniqueID.mostSignificantBits,
                time + addMilliseconds,
                time + addMilliseconds,
                time + addMilliseconds
            ) * scale

            "XZ" -> OpenSimplex2S.noise3_ImproveXZ(
                mc.thePlayer.uniqueID.mostSignificantBits,
                time + addMilliseconds,
                time + addMilliseconds,
                time + addMilliseconds
            ) * scale

            else -> OpenSimplex2S.noise2(
                mc.thePlayer.uniqueID.mostSignificantBits,
                time + addMilliseconds,
                time + addMilliseconds
            ) * scale
        }
    }

    // --- Alternative methods
    /**
     * Returns the inverted yaw angle.
     *
     * @param yaw The original yaw angle in degrees.
     * @return The yaw angle inverted by 180 degrees.
     */
    fun invertYaw(yaw: Float): Float {
        return (yaw + 180) % 360
    }

    /**
     * Any module that modifies the server packets without using the [currentRotation] should use on module disable.
     */
    fun syncSpecialModuleRotations() {
        serverRotation.let { (yaw, _) ->
            mc.thePlayer?.let {
                it.rotationYaw = yaw + angleDifference(it.rotationYaw, yaw)
                syncRotations()
            }
        }
    }

    /**
     * Allows you to check if your crosshair is over your target entity
     *
     * @param targetEntity       your target entity
     * @param blockReachDistance your reach
     * @return if crosshair is over target
     */
    fun isRotationFaced(targetEntity: Entity, blockReachDistance: Double, rotation: Rotation) = raycastEntity(
        blockReachDistance,
        rotation.yaw,
        rotation.pitch
    ) { entity: Entity -> targetEntity == entity } != null

    fun performRayTrace(blockPos: BlockPos, vec: Vec3, eyes: Vec3 = mc.thePlayer.eyes) =
        mc.theWorld?.let { blockPos.getBlock()?.collisionRayTrace(it, blockPos, eyes, vec) }

    fun isEntityHeightVisible(entity: Entity) = arrayOf(
        entity.hitBox.center.withY(entity.hitBox.maxY),
        entity.hitBox.center.withY(entity.hitBox.minY)
    ).any { isVisible(it) }

    fun isEntityHeightVisible(entity: TileEntity) = arrayOf(
        entity.renderBoundingBox.center.withY(entity.renderBoundingBox.maxY),
        entity.renderBoundingBox.center.withY(entity.renderBoundingBox.minY)
    ).any { isVisible(it) }

    fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
        val player = mc.thePlayer
        val x = posX - player.posX
        val y = posY - (player.posY + player.getEyeHeight())
        val z = posZ - player.posZ
        val dist = MathHelper.sqrt_double(x * x + z * z)
        val yaw = (atan2(z, x) * 180.0 / Math.PI - 90).toFloat()
        val pitch = (-(atan2(y, dist.toDouble()) * 180.0 / Math.PI)).toFloat()
        return Rotation(yaw, pitch)
    }

    fun getRotationsEntity(entity: EntityLivingBase): Rotation {
        return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
    }
    // --- Core Update and Reset Logic ---

    private fun update() {
        val settings = activeSettings ?: return
        val player = mc.thePlayer ?: return
        if (!InventoryUtils.serverOpenContainer && !InventoryUtils.serverOpenInventory) {
            if (rotationProcessActive && targetRotation != null) {
                phaseTicks++
                val startRotation = currentRotation ?: serverRotation
                val finalTarget = targetRotation!!

                var nextRotation: Rotation? = null

                when (currentRotationPhase) {
                    "Flicking" -> nextRotation = handleFlickPhase(startRotation, finalTarget, settings)
                    "Correcting" -> nextRotation = handleCorrectPhase(startRotation, finalTarget, settings)
                    "Oscillating" -> nextRotation = handleOscillatingPhase(startRotation, finalTarget, settings)
                    "Curving" -> nextRotation = handleCurvePhase(startRotation, settings)
                    "Hybrid" -> nextRotation = handleHybridPhase(startRotation, settings)
                    else -> { // Handle all single-phase modes
                        val modifiedTarget = when (currentRotationPhase) {
                            "MouseSensitive" -> simulateMouseSensitiveRotation(
                                startRotation,
                                finalTarget,
                                sensitivity = 0.8f
                            )

                            "Inertial" -> simulateInertialRotation(startRotation, finalTarget, inertiaFactor = 0.9f)
                            "MicroAdjustment" -> simulateMicroAdjustments(
                                finalTarget,
                                finalTarget,
                                adjustmentRange = 1.5f
                            )

                            "SmoothTracking" -> simulateSmoothTracking(startRotation, finalTarget, stepSize = 5f)
                            "RandomJitter" -> simulateRandomJitter(startRotation, jitterAmount = 0.8f)
                            "Realistic" -> simulateRealisticRotation(startRotation, finalTarget)
                            "Sinusoidal" -> generateSinusoidalRotation(startRotation, amplitude = 3f, frequency = 2f)
                            "Algorithm", "Noise", "Default" -> finalTarget // These modify the target in searchCenter
                            else -> finalTarget
                        }
                        nextRotation = limitAngleChange(startRotation, modifiedTarget, settings)
                        // For single-phase, once it's close enough, the process is done.
                        if (rotationDifference(nextRotation, finalTarget) < 0.1) {
                            rotationProcessActive = false
                        }
                    }
                }

                if (nextRotation != null) {
                    currentRotation = if (settings.applyServerSide) nextRotation.fixedSensitivity() else nextRotation
                    if (!settings.applyServerSide) nextRotation.toPlayer(player)
                }

            } else if (resetTicks == 0) {
                val distanceToPlayerRotation =
                    rotationDifference(currentRotation ?: serverRotation, player.rotation).withGCD()
                if (distanceToPlayerRotation <= settings.angleResetDifference || !settings.applyServerSide) {
                    resetRotation()
                } else {
                    currentRotation = limitAngleChange(
                        currentRotation ?: serverRotation,
                        player.rotation,
                        settings
                    ).fixedSensitivity()
                }
            }

            if (resetTicks > 0) resetTicks--
        }
    }

    private fun resetRotation() {
        resetTicks = 0
        currentRotation?.let { (yaw, _) ->
            mc.thePlayer?.let {
                it.rotationYaw = yaw + angleDifference(it.rotationYaw, yaw)
                syncRotations()
            }
        }
        targetRotation = null
        currentRotation = null
        activeSettings = null
        resetRotationProcess()
    }

    private fun resetRotationProcess() {
        rotationProcessActive = false
        currentRotationPhase = "None"
        phaseTicks = 0
        flickIntermediateTarget = null
        correctionDelayTicks = 0
        rotationPath = emptyList()
        pathIndex = 0
    }

    private fun limitAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings
    ): Rotation {
        val (hSpeed, vSpeed) = if (settings.instant) {
            180f to 180f
        } else settings.horizontalSpeed.random() to settings.verticalSpeed.random()

        return performAngleChange(
            currentRotation,
            targetRotation,
            hSpeed,
            vSpeed,
            !settings.instant && settings.legitimize,
            settings.minRotationDifference,
        )
    }

    // --- Utility and Helper Functions (Integrated from original code) ---

    @Suppress("NOTHING_TO_INLINE")
    inline fun Vec3.scale(factor: Double) = Vec3(xCoord * factor, yCoord * factor, zCoord * factor)

    fun faceBlock(blockPos: BlockPos?, throughWalls: Boolean = true): VecRotation? {
        val world = mc.theWorld ?: return null
        val player = mc.thePlayer ?: return null
        if (blockPos == null) return null
        val eyesPos = player.eyes
        val startPos = Vec3(blockPos)
        var visibleVec: VecRotation? = null
        var invisibleVec: VecRotation? = null
        for (x in 0.0..1.0) {
            for (y in 0.0..1.0) {
                for (z in 0.0..1.0) {
                    val block = blockPos.getBlock() ?: return null
                    val posVec = startPos.add(block.lerpWith(x, y, z))
                    val dist = eyesPos.distanceTo(posVec)
                    val (diffX, diffY, diffZ) = posVec - eyesPos
                    val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
                    val rotation = Rotation(
                        MathHelper.wrapAngleTo180_float(atan2(diffZ, diffX).toDegreesF() - 90f),
                        MathHelper.wrapAngleTo180_float(-atan2(diffY, diffXZ).toDegreesF())
                    ).fixedSensitivity()
                    val rotationVector = getVectorForRotation(rotation)
                    val vector = eyesPos + (rotationVector * dist)
                    val currentVec = VecRotation(posVec, rotation)
                    val raycast = world.rayTraceBlocks(eyesPos, vector, false, true, false)
                    val currentRotation = currentRotation ?: player.rotation
                    if (raycast != null && raycast.blockPos == blockPos) {
                        if (visibleVec == null || rotationDifference(
                                currentVec.rotation,
                                currentRotation
                            ) < rotationDifference(visibleVec.rotation, currentRotation)
                        ) {
                            visibleVec = currentVec
                        }
                    } else if (throughWalls) {
                        val invisibleRaycast = performRaytrace(blockPos, rotation) ?: continue
                        if (invisibleRaycast.blockPos != blockPos) continue
                        if (invisibleVec == null || rotationDifference(
                                currentVec.rotation,
                                currentRotation
                            ) < rotationDifference(invisibleVec.rotation, currentRotation)
                        ) {
                            invisibleVec = currentVec
                        }
                    }
                }
            }
        }
        return visibleVec ?: invisibleVec
    }

    private fun algorithmSearch(targetBB: AxisAlignedBB): Rotation? {
        val eyesPos = mc.thePlayer.eyes
        var bestRotation: Rotation? = null
        var minDistance = Double.MAX_VALUE
        for (yRatio in listOf(0.4, 0.5, 0.6)) {
            for (xRatio in listOf(0.2, 0.5, 0.8)) {
                for (zRatio in listOf(0.2, 0.5, 0.8)) {
                    val scanPos = targetBB.lerpWith(xRatio, yRatio, zRatio)
                    val rotation = toRotation(scanPos, true).fixedSensitivity()
                    val hitVec = calculateHitVec(rotation, eyesPos) ?: continue
                    val distance = hitVec.distanceTo(eyesPos)
                    if (distance < minDistance) {
                        minDistance = distance
                        bestRotation = rotation
                    }
                }
            }
        }
        return bestRotation
    }

    private fun defaultSearch(
        targetBB: AxisAlignedBB,
        hRange: ClosedRange<Double>,
        vRange: ClosedRange<Double>,
        predict: Boolean
    ): Rotation? {
        val eyesPos = mc.thePlayer.eyes
        var closestRot: Rotation? = null
        var closestDist = Double.MAX_VALUE
        for (y in vRange.start..vRange.endInclusive step 0.1) {
            for (x in hRange.start..hRange.endInclusive step 0.1) {
                for (z in hRange.start..hRange.endInclusive step 0.1) {
                    val pos = targetBB.lerpWith(x, y, z)
                    val rot = toRotation(pos, predict).fixedSensitivity()
                    val dist = pos.distanceTo(eyesPos)
                    if (dist < closestDist) {
                        closestDist = dist
                        closestRot = rot
                    }
                }
            }
        }
        return closestRot
    }

    private fun calculateHitVec(rotation: Rotation, startPos: Vec3 = mc.thePlayer.eyes): Vec3? {
        val endPos = startPos.add(getVectorForRotation(rotation).scale(128.0))
        return mc.theWorld.rayTraceBlocks(startPos, endPos, false, true, false)?.hitVec
    }

    fun getRotationBlock(pos: BlockPos, predict: Float): Rotation {
        val from = mc.thePlayer.getPositionEyes(predict)
        val to = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val diff = to.subtract(Vec3d(from))
        val yaw = MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diff.z, diff.x)).toFloat() - 90f)
        val pitch = MathHelper.wrapAngleTo180_float(
            (-Math.toDegrees(
                atan2(
                    diff.y,
                    sqrt(diff.x * diff.x + diff.z * diff.z)
                )
            )).toFloat()
        )
        return Rotation(yaw, pitch)
    }

    fun faceTrajectory(
        target: Entity,
        predict: Boolean,
        predictSize: Float,
        gravity: Float = 0.05f,
        velocity: Float? = null
    ): Rotation {
        val player = mc.thePlayer
        val posX =
            target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else .0) - (player.posX + if (predict) player.posX - player.prevPosX else .0)
        val posY =
            target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else .0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + (if (predict) player.posY - player.prevPosY else .0)) - player.getEyeHeight()
        val posZ =
            target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else .0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else .0)
        val posSqrt = sqrt(posX * posX + posZ * posZ)
        var finalVelocity = velocity
        if (finalVelocity == null) {
            finalVelocity = if (FastBow.handleEvents()) 1f else player.itemInUseDuration / 20f
            finalVelocity = ((finalVelocity * finalVelocity + finalVelocity * 2) / 3).coerceAtMost(1f)
        }
        val gravityModifier = 0.12f * gravity
        return Rotation(
            atan2(posZ, posX).toDegreesF() - 90f,
            -atan((finalVelocity * finalVelocity - sqrt(finalVelocity * finalVelocity * finalVelocity * finalVelocity - gravityModifier * (gravityModifier * posSqrt * posSqrt + 2 * posY * finalVelocity * finalVelocity))) / (gravityModifier * posSqrt)).toDegreesF()
        )
    }

    fun toRotation(vec: Vec3, predict: Boolean = false, fromEntity: Entity = mc.thePlayer): Rotation {
        val eyesPos = fromEntity.eyes.let {
            if (predict) it.addVector(
                fromEntity.motionX,
                fromEntity.motionY,
                fromEntity.motionZ
            ) else it
        }
        val (diffX, diffY, diffZ) = vec - eyesPos
        return Rotation(
            MathHelper.wrapAngleTo180_float(atan2(diffZ, diffX).toDegreesF() - 90f),
            MathHelper.wrapAngleTo180_float(-atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)).toDegreesF())
        )
    }

    fun performAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        hSpeed: Float,
        vSpeed: Float = hSpeed,
        legitimize: Boolean,
        minRotationDiff: Float
    ): Rotation {
        var (yawDiff, pitchDiff) = angleDifferences(targetRotation, currentRotation)
        val rotationDifference = hypot(yawDiff, pitchDiff)
        if (rotationDifference <= getFixedAngleDelta()) return currentRotation.plusDiff(targetRotation)
        val isShortStopActive = WaitTickUtils.hasScheduled(this)
        if (isShortStopActive || activeSettings?.shouldPerformShortStop() == true) {
            if (!isShortStopActive) WaitTickUtils.schedule(
                activeSettings?.shortStopDuration?.random()?.plus(1) ?: 0,
                this
            )
            activeSettings?.resetSimulateShortStopData()
            yawDiff = 0f
            pitchDiff = 0f
        }
        var (straightLineYaw, straightLinePitch) = abs(yawDiff safeDiv rotationDifference) * hSpeed to abs(pitchDiff safeDiv rotationDifference) * vSpeed
        straightLineYaw = yawDiff.coerceIn(-straightLineYaw, straightLineYaw)
        straightLinePitch = pitchDiff.coerceIn(-straightLinePitch, straightLinePitch)
        val rotationWithGCD = Rotation(straightLineYaw, straightLinePitch).fixedSensitivity()
        if (abs(rotationWithGCD.yaw) <= nextFloat(
                min(minRotationDiff, getFixedAngleDelta()),
                minRotationDiff
            )
        ) straightLineYaw = 0f
        if (abs(rotationWithGCD.pitch) < nextFloat(
                min(minRotationDiff, getFixedAngleDelta()),
                minRotationDiff
            )
        ) straightLinePitch = 0f
        if (legitimize) {
            applySlowDown(straightLineYaw, true) { straightLineYaw = it }
            applySlowDown(straightLinePitch, false) { straightLinePitch = it }
        }
        return currentRotation.plus(Rotation(straightLineYaw, straightLinePitch))
    }

    private fun applySlowDown(diff: Float, yaw: Boolean, action: (Float) -> Unit) {
        if (diff == 0f) {
            action(diff); return
        }
        val lastTick1 = angleDifferences(serverRotation, lastRotations[1]).let { if (yaw) it.x else it.y }
        val diffAbs = abs(diff)
        val range = when {
            diffAbs <= 3f -> 0.4f..0.8f + (0.2f * (1 - diffAbs / 3f)).coerceIn(0f, 1f)
            diffAbs > 50f -> 0.2f..0.55f
            diff.sign != lastTick1.sign && lastTick1.sign != 0f && diff.sign != 0f -> 0.4f..0.5f
            else -> 0.1f..0.4f
        }
        action((lastTick1..diff).lerpWith(range.random()))
    }

    fun angleDifference(a: Float, b: Float) = MathHelper.wrapAngleTo180_float(a - b)
    fun angleDifferences(target: Rotation, current: Rotation) =
        Vector2f(angleDifference(target.yaw, current.yaw), target.pitch - current.pitch)

    fun rotationDifference(a: Rotation, b: Rotation = serverRotation) =
        hypot(angleDifference(a.yaw, b.yaw), a.pitch - b.pitch)

    fun getVectorForRotation(rotation: Rotation) = getVectorForRotation(rotation.yaw, rotation.pitch)
    fun getVectorForRotation(yaw: Float, pitch: Float): Vec3 {
        val yawRad = yaw.toRadians()
        val pitchRad = pitch.toRadians()
        val f = -cos(pitchRad); return Vec3(
            (sin(-yawRad - PI.toFloat()) * f).toDouble(), sin(pitchRad).toDouble(),
            (cos(-yawRad - PI.toFloat()) * f).toDouble()
        )
    }

    fun generateSinusoidalRotation(currentRotation: Rotation, amplitude: Float, frequency: Float): Rotation {
        val deltaTime = System.currentTimeMillis() / 1000.0
        val sinusoidalYaw = currentRotation.yaw + amplitude * sin(deltaTime * frequency).toFloat()
        val sinusoidalPitch = currentRotation.pitch + amplitude * cos(deltaTime * frequency).toFloat()
        return Rotation(sinusoidalYaw, sinusoidalPitch).fixedSensitivity()
    }

    fun simulateMouseSensitiveRotation(
        currentRotation: Rotation,
        targetRotation: Rotation,
        sensitivity: Float,
        maxStep: Float = 10f
    ): Rotation {
        val yawDiff = angleDifference(targetRotation.yaw, currentRotation.yaw)
        val pitchDiff = targetRotation.pitch - currentRotation.pitch
        val yawStep = (yawDiff * sensitivity).coerceIn(-maxStep, maxStep)
        val pitchStep = (pitchDiff * sensitivity).coerceIn(-maxStep, maxStep)
        return currentRotation.plus(Rotation(yawStep, pitchStep)).fixedSensitivity()
    }

    fun simulateInertialRotation(
        currentRotation: Rotation,
        targetRotation: Rotation,
        inertiaFactor: Float = 0.9f
    ): Rotation {
        val yawDiff = angleDifference(targetRotation.yaw, currentRotation.yaw)
        val pitchDiff = targetRotation.pitch - currentRotation.pitch
        val yawStep = yawDiff * (1 - inertiaFactor)
        val pitchStep = pitchDiff * (1 - inertiaFactor)
        return currentRotation.plus(Rotation(yawStep, pitchStep)).fixedSensitivity()
    }

    fun simulateRandomJitter(currentRotation: Rotation, jitterAmount: Float = 1f): Rotation {
        val randomYawJitter = (-jitterAmount..jitterAmount).random()
        val randomPitchJitter = (-jitterAmount..jitterAmount).random()
        return currentRotation.plus(Rotation(randomYawJitter, randomPitchJitter)).fixedSensitivity()
    }

    fun simulateSmoothTracking(currentRotation: Rotation, targetRotation: Rotation, stepSize: Float = 5f): Rotation {
        val yawDiff = angleDifference(targetRotation.yaw, currentRotation.yaw)
        val pitchDiff = targetRotation.pitch - currentRotation.pitch
        val yawStep = yawDiff.coerceIn(-stepSize, stepSize)
        val pitchStep = pitchDiff.coerceIn(-stepSize, stepSize)
        return currentRotation.plus(Rotation(yawStep, pitchStep)).fixedSensitivity()
    }

    fun simulateRealisticRotation(
        currentRotation: Rotation,
        targetRotation: Rotation,
        sensitivity: Float = 0.8f,
        inertiaFactor: Float = 0.9f,
        jitterAmount: Float = 1f,
        stepSize: Float = 5f
    ): Rotation {
        val sensitiveRotation = simulateMouseSensitiveRotation(currentRotation, targetRotation, sensitivity)
        val inertialRotation = simulateInertialRotation(sensitiveRotation, targetRotation, inertiaFactor)
        val jitteredRotation = simulateRandomJitter(inertialRotation, jitterAmount)
        return simulateSmoothTracking(jitteredRotation, targetRotation, stepSize)
    }

    fun simulateMicroAdjustments(
        baseRotation: Rotation,
        targetRotation: Rotation,
        adjustmentRange: Float = 2f
    ): Rotation {
        val yawDiff = angleDifference(targetRotation.yaw, baseRotation.yaw)
        val pitchDiff = targetRotation.pitch - baseRotation.pitch
        val yawAdjust = yawDiff.coerceIn(-adjustmentRange, adjustmentRange)
        val pitchAdjust = pitchDiff.coerceIn(-adjustmentRange, adjustmentRange)
        return baseRotation.plus(Rotation(yawAdjust, pitchAdjust))
    }

    fun getFixedAngleDelta(sensitivity: Float = mc.gameSettings.mouseSensitivity) =
        (sensitivity * 0.6f + 0.2f).pow(3) * 1.2f

    fun getFixedSensitivityAngle(targetAngle: Float, startAngle: Float = 0f, gcd: Float = getFixedAngleDelta()) =
        startAngle + ((targetAngle - startAngle) / gcd).roundToInt() * gcd

    fun performRaytrace(
        blockPos: BlockPos,
        rotation: Rotation,
        reach: Float = mc.playerController.blockReachDistance
    ): MovingObjectPosition? {
        val world = mc.theWorld ?: return null
        val player = mc.thePlayer ?: return null
        val eyes = player.eyes
        return blockPos.getBlock()
            ?.collisionRayTrace(world, blockPos, eyes, eyes + (getVectorForRotation(rotation) * reach.toDouble()))
    }

    fun syncRotations() {
        mc.thePlayer?.let {
            it.prevRotationYaw = it.rotationYaw; it.prevRotationPitch = it.rotationPitch
            it.renderArmYaw = it.rotationYaw; it.renderArmPitch = it.rotationPitch
            it.prevRenderArmYaw = it.rotationYaw; it.prevRenderArmPitch = it.rotationPitch
        }
    }

    fun isVisible(vec3: Vec3) = mc.theWorld.rayTraceBlocks(mc.thePlayer.eyes, vec3) == null

    @EventTarget(priority = -1)
    fun onRotationUpdate(event: RotationUpdateEvent) {
        activeSettings?.takeIf { it.immediate }?.let { it.immediate = false; return }
        update()
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (activeSettings?.strafe == true) {
            currentRotation?.applyStrafeToPlayer(event, activeSettings!!.strict)
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet as? C03PacketPlayer ?: return
        if (!packet.rotating) {
            activeSettings?.resetSimulateShortStopData()
            return
        }
        currentRotation?.let { packet.rotation = it }
        activeSettings?.updateSimulateShortStopData(angleDifferences(packet.rotation, serverRotation).x)
    }

    enum class BodyPoint(val rank: Int, val range: ClosedFloatingPointRange<Double>) {
        HEAD(1, 0.75..0.9), BODY(0, 0.5..0.75), FEET(-1, 0.1..0.4), UNKNOWN(-2, 0.0..0.0);

        companion object {
            fun fromString(point: String): BodyPoint =
                values().find { it.name.equals(point, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

// --- [NEW] Extension function to simplify creating temporary settings for limitAngleChange ---
private fun RotationSettings.cloneWithSpeeds(
    hSpeed: ClosedFloatingPointRange<Float>,
    vSpeed: ClosedFloatingPointRange<Float>,
    legit: Boolean = this.legitimize,
    minDiff: Float = this.minRotationDifference
): RotationSettings {
    val temp = RotationSettings(LiquidBounce.moduleManager.getModule(Debugger::class.java))
    temp.minHorizontalAngleChangeValue.set(hSpeed.start)
    temp.maxHorizontalAngleChangeValue.set(hSpeed.endInclusive)
    temp.minVerticalAngleChangeValue.set(vSpeed.start)
    temp.maxVerticalAngleChangeValue.set(vSpeed.endInclusive)
    temp.legitimizeValue.set(legit)
    temp.minRotationDifferenceValue.set(minDiff)
    temp.instant = this.instant
    return temp
}
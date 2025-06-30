package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.Vec3d
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraft.util.MathHelper
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object MoveFix : Module("MoveFix", Category.MOVEMENT) {
    @JvmStatic
    val mode = ListValue("Mode", arrayOf("Grim", "Bloxd"), "Grim")
    private val silentFixValue = BoolValue("Silent", true) { mode.get() == "Grim" }

    private val spiderValue = BoolValue("Spider", true) { mode.get() == "Bloxd" }

    private var jumpfunny = 0
    private var jumpticks: Long = 0
    private val bloxdPhysics = NoaPhysics()
    private var groundTicksLocal = 0
    private var lastMotionY = 0.0
    private var wasClimbing = false

    var silentFix = false
    var doFix = false
    private var isOverwrited = false

    override fun onEnable() {
        super.onEnable()
        resetBloxdState()
    }

    override fun onDisable() {
        super.onDisable()
        doFix = false
        resetBloxdState()
    }

    private fun resetBloxdState() {
        jumpfunny = 0
        jumpticks = 0
        bloxdPhysics.reset()
        groundTicksLocal = 0
        lastMotionY = 0.0
        wasClimbing = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!isOverwrited) {
            silentFix = silentFixValue.get()
            doFix = state
        }
        val player = mc.thePlayer ?: return
        if (mode.get() == "Bloxd") {
            if (player.onGround) {
                groundTicksLocal++
            } else {
                groundTicksLocal = 0
            }
            if (groundTicksLocal > 5) {
                jumpfunny = 0
            }

            if (player.isCollidedVertically && lastMotionY > 0 && player.motionY <= 0) {
                bloxdPhysics.velocityVector.y = 0.0
                bloxdPhysics.impulseVector.y = 0.0
            }
            lastMotionY = player.motionY
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mode.get() != "Bloxd" || mc.thePlayer == null) return

        val player = mc.thePlayer!!
        val packet = event.packet
        when (packet) {
            is S12PacketEntityVelocity -> {
                if (packet.entityID == player.entityId) {
                    jumpticks = System.currentTimeMillis() + 1300
                }
            }

            is S3FPacketCustomPayload -> {
                if ("bloxd:resyncphysics" == packet.channelName) {
                    try {
                        val data: PacketBuffer = packet.bufferData
                        jumpfunny = 0
                        bloxdPhysics.impulseVector.set(0.0, 0.0, 0.0)
                        bloxdPhysics.forceVector.set(0.0, 0.0, 0.0)
                        bloxdPhysics.velocityVector.set(
                            data.readFloat().toDouble(),
                            data.readFloat().toDouble(),
                            data.readFloat().toDouble()
                        )
                    } catch (e: Exception) {
                        // Handle exception
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (mode.get() == "Grim") {
            if (doFix) {
                runStrafeFixLoop(silentFix, event)
            }
            return
        }

        if (mode.get() == "Bloxd") {
            val player = mc.thePlayer ?: return

            if (player.onGround && bloxdPhysics.velocityVector.y < 0) {
                bloxdPhysics.velocityVector.set(0.0, 0.0, 0.0)
            }

            if (player.onGround && player.motionY > 0.4199 && player.motionY < 0.4201) {
                jumpfunny = min(jumpfunny + 1, 3)
                bloxdPhysics.impulseVector.add(0.0, 8.0, 0.0)
            }

            if (spiderValue.get()) {
                if (player.isCollidedHorizontally && (abs(event.forward) > 0.005f || abs(event.strafe) > 0.005f)) {
                    bloxdPhysics.velocityVector.set(0.0, 8.0, 0.0)
                    wasClimbing = true
                } else if (wasClimbing) {
                    bloxdPhysics.velocityVector.set(0.0, 0.0, 0.0)
                    wasClimbing = false
                }
            }

            val speed = getBloxdSpeed()
            val moveDir = getBloxdMoveVec(event.forward, event.strafe, speed)

            event.cancelEvent()

            player.motionX = moveDir.x
            player.motionZ = moveDir.z
            player.motionY = if (mc.theWorld.isBlockLoaded(player.position) || player.posY <= 0) {
                bloxdPhysics.getMotionForTick().y * (1.0 / 30.0)
            } else {
                0.0
            }

            if (!mc.theWorld.isBlockLoaded(player.position) || player.posY <= 0) {
                player.motionX = 0.0
                player.motionY = 0.0
                player.motionZ = 0.0
            }
        }
    }

    private fun getBloxdSpeed(): Double {
        val player = mc.thePlayer ?: return 0.0

        if (System.currentTimeMillis() < jumpticks) {
            return 1.0
        }

        if (player.isUsingItem) {
            return 0.06
        }

        var finalSpeed = 0.26
        if (jumpfunny > 0) {
            finalSpeed += 0.025 * jumpfunny
        }
        return finalSpeed
    }

    private fun getBloxdMoveVec(forwardIn: Float, strafeIn: Float, speed: Double): Vec3d {
        val player = mc.thePlayer ?: return Vec3d(0.0, 0.0, 0.0)
        var forward = forwardIn
        var strafe = strafeIn
        val yaw = player.rotationYaw

        val sqrt = MathHelper.sqrt_float(forward * forward + strafe * strafe)
        if (sqrt >= 0.01f) {
            if (sqrt > 1.0f) { // Normalize to 1 for diagonal movement
                forward /= sqrt
                strafe /= sqrt
            }
        } else {
            return Vec3d(0.0, 0.0, 0.0)
        }

        val yawRad = yaw.toDouble().toRadians()
        val sinYaw = sin(yawRad)
        val cosYaw = cos(yawRad)

        val x = (strafe * cosYaw - forward * sinYaw) * speed
        val z = (forward * cosYaw + strafe * sinYaw) * speed

        return Vec3d(x, 0.0, z)
    }

    fun applyForceStrafe(isSilent: Boolean, runStrafeFix: Boolean) {
        silentFix = isSilent; doFix = runStrafeFix; isOverwrited = true
    }

    fun updateOverwrite() {
        isOverwrited = false; doFix = state; silentFix = silentFixValue.get()
    }

    fun runStrafeFixLoop(isSilent: Boolean, event: StrafeEvent) {
        if (event.isCancelled) {
            return
        }
        val player = mc.thePlayer ?: return
        val (yaw) = RotationUtils.targetRotation ?: return
        var strafe = event.strafe
        var forward = event.forward
        var friction = event.friction
        var factor = strafe * strafe + forward * forward
        val angleDiff =
            ((MathHelper.wrapAngleTo180_float(player.rotationYaw - yaw - 22.5f - 135.0f) + 180.0) / 45.0).toInt()
        val calcYaw = if (isSilent) {
            yaw + 45.0f * angleDiff.toFloat()
        } else yaw
        var calcMoveDir = abs(strafe).coerceAtLeast(abs(forward))
        calcMoveDir *= calcMoveDir
        val calcMultiplier = MathHelper.sqrt_float(calcMoveDir / 1.0f.coerceAtMost(calcMoveDir * 2.0f))
        if (isSilent) {
            when (angleDiff) {
                1, 3, 5, 7, 9 -> {
                    if ((abs(forward) > 0.005 || abs(strafe) > 0.005) && !(abs(forward) > 0.005 && abs(
                            strafe
                        ) > 0.005)
                    ) {
                        friction /= calcMultiplier
                    } else if (abs(forward) > 0.005 && abs(strafe) > 0.005) {
                        friction *= calcMultiplier
                    }
                }
            }
        }
        if (factor >= 1.0E-4F) {
            factor = MathHelper.sqrt_float(factor)
            if (factor < 1.0F) {
                factor = 1.0F
            }
            factor = friction / factor
            strafe *= factor
            forward *= factor
            val yawSin = MathHelper.sin((calcYaw.toRadians()))
            val yawCos = MathHelper.cos((calcYaw.toRadians()))
            player.motionX += strafe * yawCos - forward * yawSin
            player.motionZ += forward * yawCos + strafe * yawSin
        }
        event.cancelEvent()
    }

    override val tag: String
        get() = mode.get()?:""

    class MutableVec3d(var x: Double, var y: Double, var z: Double) {
        fun set(x: Double, y: Double, z: Double) {
            this.x = x; this.y = y; this.z = z
        }

        fun add(x: Double, y: Double, z: Double) {
            this.x += x; this.y += y; this.z += z
        }

        fun add(vec: MutableVec3d) {
            this.x += vec.x; this.y += vec.y; this.z += vec.z
        }

        fun mul(factor: Double) {
            this.x *= factor; this.y *= factor; this.z *= factor
        }
    }

    class NoaPhysics {
        var impulseVector = MutableVec3d(0.0, 0.0, 0.0)
        var forceVector = MutableVec3d(0.0, 0.0, 0.0)
        var velocityVector = MutableVec3d(0.0, 0.0, 0.0)
        var gravityVector = MutableVec3d(0.0, -10.0, 0.0)
        var gravityMul = 2.0
        private val mass = 1.0
        private val delta = 1.0 / 30.0

        fun reset() {
            impulseVector.set(0.0, 0.0, 0.0); forceVector.set(0.0, 0.0, 0.0); velocityVector.set(0.0, 0.0, 0.0)
        }

        fun getMotionForTick(): MutableVec3d {
            val massDiv = 1.0 / mass
            forceVector.mul(massDiv)
            forceVector.add(gravityVector)
            forceVector.mul(gravityMul)
            impulseVector.mul(massDiv)
            forceVector.mul(delta)
            impulseVector.add(forceVector)
            velocityVector.add(impulseVector)
            forceVector.set(0.0, 0.0, 0.0)
            impulseVector.set(0.0, 0.0, 0.0)
            return velocityVector
        }
    }
}
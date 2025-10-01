/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.extensions.toDegreesF
import net.ccbluex.liquidbounce.utils.extensions.toRadiansD
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance(), Listenable {

    var affectSprintOnAttack: Boolean? = null

    var speed
        get() = mc.thePlayer?.run { sqrt(motionX * motionX + motionZ * motionZ).toFloat() } ?: .0f
        set(value) {
            strafe(value)
        }

    val hasMotion
        get() = mc.thePlayer?.run { motionX != .0 || motionY != .0 || motionZ != .0 } == true

    var airTicks = 0
    fun isMoving(): Boolean {
        return isMoving(mc.thePlayer)
    }
    fun isMoving(player: EntityLivingBase?): Boolean {
        return player != null && (player.moveForward != 0f || player.moveStrafing != 0f)
    }
    fun resetMotion(y: Boolean) {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        if (y) mc.thePlayer.motionY = 0.0
    }
    fun isBlockUnder(): Boolean {
        if (mc.thePlayer == null) return false

        if (mc.thePlayer.posY < 0.0) {
            return false
        }
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb = mc.thePlayer.entityBoundingBox.offset(0.0, (-off).toDouble(), 0.0)
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true
            }
            off += 2
        }
        return false
    }
    @JvmOverloads
    fun strafe(
        speed: Float = this.speed, stopWhenNoInput: Boolean = false, moveEvent: MoveEvent? = null,
        strength: Double = 1.0,
    ) =
        mc.thePlayer?.run {
            if (!mc.thePlayer.isMoving) {
                if (stopWhenNoInput) {
                    moveEvent?.zeroXZ()
                    stopXZ()
                }

                return@run
            }

            val prevX = motionX * (1.0 - strength)
            val prevZ = motionZ * (1.0 - strength)
            val useSpeed = speed * strength

            val yaw = direction
            val x = (-sin(yaw) * useSpeed) + prevX
            val z = (cos(yaw) * useSpeed) + prevZ

            if (moveEvent != null) {
                moveEvent.x = x
                moveEvent.z = z
            }

            motionX = x
            motionZ = z
        }
    @JvmOverloads
    fun strafeBoat(
        speed: Float = this.speed, stopWhenNoInput: Boolean = false, boat: EntityBoat? = null,
        strength: Double = 1.0,
    ) =
        mc.thePlayer?.run {
            if (!mc.thePlayer.isMoving) {
                if (stopWhenNoInput) {
                    boat?.motionX = 0.0
                    boat?.motionZ = 0.0
                    stopXZ()
                }

                return@run
            }

            val prevX = motionX * (1.0 - strength)
            val prevZ = motionZ * (1.0 - strength)
            val useSpeed = speed * strength

            val yaw = direction
            val x = (-sin(yaw) * useSpeed) + prevX
            val z = (cos(yaw) * useSpeed) + prevZ

            boat?.let { it.motionX = x }
            boat?.let { it.motionZ = z }
            motionX = x
            motionZ = z
        }
    fun Vec3.strafe(
        yaw: Float = direction.toDegreesF(), speed: Double = sqrt(xCoord * xCoord + zCoord * zCoord),
        strength: Double = 1.0,
        moveCheck: Boolean = false,
    ): Vec3 {
        if (moveCheck) {
            xCoord = 0.0
            zCoord = 0.0
            return this
        }

        val prevX = xCoord * (1.0 - strength)
        val prevZ = zCoord * (1.0 - strength)
        val useSpeed = speed * strength

        val angle = Math.toRadians(yaw.toDouble())
        xCoord = (-sin(angle) * useSpeed) + prevX
        zCoord = (cos(angle) * useSpeed) + prevZ
        return this
    }

    fun forward(distance: Double) =
        mc.thePlayer?.run {
            val yaw = rotationYaw.toRadiansD()
            setPosition(posX - sin(yaw) * distance, posY, posZ + cos(yaw) * distance)
        }

    val direction
        get() = mc.thePlayer?.run {
            var yaw = rotationYaw
            var forward = 1f

            if (movementInput.moveForward < 0f) {
                yaw += 180f
                forward = -0.5f
            } else if (movementInput.moveForward > 0f)
                forward = 0.5f

            if (movementInput.moveStrafe < 0f) yaw += 90f * forward
            else if (movementInput.moveStrafe > 0f) yaw -= 90f * forward

            yaw.toRadiansD()
        } ?: 0.0

    fun isOnGround(height: Double) =
        mc.theWorld != null && mc.thePlayer != null &&
            mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.entityBoundingBox.offset(0.0, -height, 0.0)
            ).isNotEmpty()

    var serverOnGround = false

    var serverX = .0
    var serverY = .0
    var serverZ = .0

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.isCancelled)
            return

        val packet = event.packet

        if (packet is C03PacketPlayer) {
            serverOnGround = packet.onGround

            if (packet.isMoving) {
                serverX = packet.x
                serverY = packet.y
                serverZ = packet.z
            }
        }
    }
    /**
     * Calculates the player's base movement speed, including sprint and potion effects.
     * This is a self-contained replacement for the missing MovementUtils.getBaseMoveSpeed().
     * @return The base speed in blocks per tick.
     */
    fun getBaseMoveSpeed(): Float {
        var baseSpeed: Float = 0.2873F // Default sprint speed
        val player = mc.thePlayer ?: return baseSpeed

        if (player.isPotionActive(Potion.moveSpeed)) {
            val amplifier = player.getActivePotionEffect(Potion.moveSpeed).amplifier
            baseSpeed *= 1.0F + 0.2F * (amplifier + 1)
        }

        if (player.isPotionActive(Potion.moveSlowdown)) {
            val amplifier = player.getActivePotionEffect(Potion.moveSlowdown).amplifier
            baseSpeed *= 1.0F - 0.15F * (amplifier + 1)
        }

        return baseSpeed
    }


}
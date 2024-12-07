package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.RotationUtils.isVisible
import net.ccbluex.liquidbounce.utils.MCGO
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3

object GORageTriggerBot : Module("GORageTriggerBot", Category.COMBAT, hideModule = false) {

    // CPS - Attack speed
    private val maxCPSValue = object : IntegerValue("MaxCPS", 8, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(minCPS, newValue)
        }
    }

    private val maxCPS by maxCPSValue

    private val minCPS: Int by object : IntegerValue("MinCPS", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(newValue, maxCPS)
        }

        override fun isSupported() = !maxCPSValue.isMinimal()
    }

    private val range: Float by FloatValue("Range", 3.7f, 1f..32767f)

    // Target
    private var target: EntityLivingBase? = null

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0
    private var clicks = 0

    override fun onDisable() {
        target = null
        clicks = 0
        attackTimer.reset()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val player = mc.thePlayer ?: return

        // Update target
        target = getTarget(range)

        if (target == null) {
            clicks = 0
            return
        }

        // Check if target is visible and in range
        if (!isVisible(Vec3(target!!.posX, target!!.posY, target!!.posZ))) {
            clicks = 0
            target = null
            return
        }

        // Check if target is in player's crosshair (line of sight alignment)
        if (isTargetInCrosshair(target!!, player)) {
            // Attack delay
            if (attackTimer.hasTimePassed(attackDelay)) {
                clicks++
                attackTimer.reset()
                attackDelay = randomClickDelay(minCPS, maxCPS)
            }

            // Shoot
            repeat(clicks) {
                shoot(target!!)
                clicks--
            }
        }
    }

    private fun getTarget(range: Float): EntityLivingBase? {
        val player = mc.thePlayer ?: return null
        val entities = mc.theWorld!!.loadedEntityList.filterIsInstance<EntityLivingBase>()
            .filter { it != player && it.isEntityAlive && isSelected(it, true) && !Teams.isInYourTeam(it) && player.getDistanceToEntityBox(it) <= range }

        return entities.minByOrNull { player.getDistanceToEntityBox(it) }
    }

    private fun shoot(entity: EntityLivingBase) {
        MCGO().shoot()
    }

    private fun isTargetInCrosshair(target: EntityLivingBase, player: EntityPlayer): Boolean {
        // This checks if the target is in the player's crosshair
        val playerYaw = player.rotationYaw
        val playerPitch = player.rotationPitch
        val targetPosition = Vec3(target.posX, target.posY, target.posZ)

        // We check if the target is close enough to the player's viewing direction
        val targetVector = targetPosition.subtract(player.positionVector)
        val angle = Math.acos(targetVector.normalize().dotProduct(player.getLookVec()))

        // You can adjust the angle threshold here to determine how precise the target has to be
        return angle < Math.toRadians(5.0) // 5 degree tolerance
    }
}

package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.MCGO
import net.ccbluex.liquidbounce.utils.RotationUtils.isVisible
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
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
        // 获取玩家眼睛位置（更精确的视角起点）
        val playerPos = player.getPositionEyes(1f)
        // 获取目标眼睛位置（更精确的命中点）
        val targetPos = Vec3(
            target.posX,
            target.posY + target.eyeHeight,
            target.posZ
        )

        // 计算相对位置向量
        val deltaX = targetPos.xCoord - playerPos.xCoord
        val deltaY = targetPos.yCoord - playerPos.yCoord
        val deltaZ = targetPos.zCoord - playerPos.zCoord

        // 计算水平距离（XZ平面投影）
        val horizontalDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)

        // 计算目标方向的yaw角度（水平方向）
        val yawToTarget = Math.toDegrees(Math.atan2(deltaZ, deltaX)).toFloat() - 90.0f

        // 规范化角度到-180～180范围
        val normalizedYaw = (yawToTarget % 360.0f).let {
            when {
                it > 180.0f -> it - 360.0f
                it < -180.0f -> it + 360.0f
                else -> it
            }
        }

        // 计算目标方向的pitch角度（垂直方向）
        val pitchToTarget = -Math.toDegrees(Math.atan2(deltaY, horizontalDist)).toFloat()

        // 计算角度差异
        val yawDiff = Math.abs(normalizedYaw - player.rotationYaw)
        val pitchDiff = Math.abs(pitchToTarget - player.rotationPitch)

        // 处理角度环绕问题（比如从-180到180的跳跃）
        val wrappedYawDiff = if (yawDiff > 180.0f) 360.0f - yawDiff else yawDiff

        // 设置角度阈值（可根据需要调整，2度更精准）
        val yawThreshold = 2.0f
        val pitchThreshold = 2.0f

        // 双重角度校验
        return wrappedYawDiff <= yawThreshold && pitchDiff <= pitchThreshold
    }}

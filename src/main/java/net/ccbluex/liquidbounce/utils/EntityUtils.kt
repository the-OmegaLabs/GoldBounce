/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.utils

import lombok.Getter
import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.extensions.toRadiansD
import net.ccbluex.liquidbounce.utils.misc.StringUtils.contains
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin


object EntityUtils : MinecraftInstance() {


    @Getter
    var targetInvisible = false
    @Getter
    var targetPlayer = true
    @Getter
    var targetMobs = true
    @Getter
    var targetAnimals = false
    @Getter
    var targetDead = false

    private val healthSubstrings = arrayOf("hp", "health", "❤", "lives")

    fun isSelected(entity: Entity?, canAttackCheck: Boolean): Boolean {
        if (!targetAnimals && entity is EntityAnimal) return false
        if (!targetMobs && entity is EntityMob) return false
        if (entity is EntityLivingBase && (targetDead || entity.isEntityAlive) && entity != mc.thePlayer) {
            if (targetInvisible || !entity.isInvisible) {
                if (targetPlayer && entity is EntityPlayer && !entity.isMob()) {
                    if (canAttackCheck) {
                        if (isBot(entity))
                            return false

                        if (entity.isClientFriend() && !NoFriends.handleEvents())
                            return false

                        if (entity.isSpectator) return false

                        return !Teams.handleEvents() || !Teams.isInYourTeam(entity)
                    }
                    return true
                }

                return targetMobs && entity.isMob() || targetAnimals && entity.isAnimal()
            }
        }
        return false
    }

    /**
     * 检测给定角度是否指向目标实体
     *
     * @param viewer    观察者实体（通常是玩家）
     * @param target    目标实体
     * @param yaw       偏航角（角度制）
     * @param pitch     俯仰角（角度制）
     * @param maxRange  最大检测距离（方块）
     * @return 是否命中实体碰撞箱
     */
    fun isLookingAtEntity(viewer: Entity, target: Entity, yaw: Float, pitch: Float, maxRange: Double): Boolean {
        // 1. 获取观察者眼睛位置
        val eyePos = Vec3(
            viewer.posX,
            viewer.posY + viewer.eyeHeight,
            viewer.posZ
        )

        // 2. 根据角度计算视线方向向量
        val lookVec = calculateLookVector(yaw, pitch)

        // 3. 计算视线终点（起点 + 方向 * 距离）
        val endPos = eyePos.addVector(
            lookVec.xCoord * maxRange,
            lookVec.yCoord * maxRange,
            lookVec.zCoord * maxRange
        )

        // 4. 获取目标实体的碰撞箱（可能需要扩大边界）
        val targetBB = target.entityBoundingBox.expand(
            target.collisionBorderSize+0.01,  // 扩大边界避免精度问题
            target.collisionBorderSize+0.01,
            target.collisionBorderSize+0.01
        )

        // 5. 检测视线与碰撞箱的交点
        val hit = targetBB.calculateIntercept(eyePos, endPos)
        return hit != null // 有交点即表示命中
    }

    /**
     * 将偏航角和俯仰角转换为单位方向向量
     *
     * @param yaw   偏航角（角度制）
     * @param pitch 俯仰角（角度制）
     * @return 单位方向向量 (Vec3)
     */
    private fun calculateLookVector(yaw: Float, pitch: Float): Vec3 {
        // 转换为弧度
        val yawRad = yaw * Math.PI.toFloat() / 180f
        val pitchRad = pitch * Math.PI.toFloat() / 180f

        // 计算方向向量分量
        val x = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad)
        val y = -MathHelper.sin(pitchRad)
        val z = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad)

        // 返回单位向量（已归一化）
        return Vec3(x.toDouble(), y.toDouble(), z.toDouble()).normalize()
    }
    fun isLookingOnEntities(entity: Any, maxAngleDifference: Double): Boolean {
        val player = mc.thePlayer ?: return false
        val playerYaw = player.rotationYawHead
        val playerPitch = player.rotationPitch

        val maxAngleDifferenceRadians = Math.toRadians(maxAngleDifference)

        val lookVec = Vec3(
            -sin(playerYaw.toRadiansD()),
            -sin(playerPitch.toRadiansD()),
            cos(playerYaw.toRadiansD())
        ).normalize()

        val playerPos = player.positionVector.addVector(0.0, player.eyeHeight.toDouble(), 0.0)

        val entityPos = when (entity) {
            is Entity -> entity.positionVector.addVector(0.0, entity.eyeHeight.toDouble(), 0.0)
            is TileEntity -> Vec3(
                entity.pos.x.toDouble(),
                entity.pos.y.toDouble(),
                entity.pos.z.toDouble()
            )
            else -> return false
        }

        val directionToEntity = entityPos.subtract(playerPos).normalize()
        val dotProductThreshold = lookVec.dotProduct(directionToEntity)

        return dotProductThreshold > cos(maxAngleDifferenceRadians)
    }

    fun getHealth(entity: EntityLivingBase, fromScoreboard: Boolean = false, absorption: Boolean = true): Float {
        if (fromScoreboard && entity is EntityPlayer) run {
            val scoreboard = entity.worldScoreboard
            val objective = scoreboard.getValueFromObjective(entity.name, scoreboard.getObjectiveInDisplaySlot(2))

            if (healthSubstrings !in objective.objective?.displayName)
                return@run

            val scoreboardHealth = objective.scorePoints

            if (scoreboardHealth > 0)
                return scoreboardHealth.toFloat()
        }

        var health = entity.health

        if (absorption)
            health += entity.absorptionAmount

        return if (health > 0) health else 20f
    }

}
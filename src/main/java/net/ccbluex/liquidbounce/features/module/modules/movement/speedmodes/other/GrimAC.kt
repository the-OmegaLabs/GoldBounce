/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.player.Gapple
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.extensions.direction
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityMinecart
import net.minecraft.entity.projectile.EntityFishHook
import kotlin.math.cos
import kotlin.math.sin

object GrimAC : SpeedMode("EntitySpeed") {
    override fun onPlayerTick() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        if (player.moveForward == 0.0f && player.moveStrafing == 0.0f) {
            return
        }

        var collisions = 0
        val box = player.entityBoundingBox.expand(1.0, 1.0, 1.0)

        for (entity in world.loadedEntityList) {

            if (canCauseSpeed(entity)) {
                collisions++
            }
        }

        // Grim gives 0.08 leniency per entity which is customizable by speed.
        val yaw = Math.toRadians(player.direction.toDouble())
        val boost = speed * collisions
        player.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)
    }


    private fun canCauseSpeed(entity: Entity): Boolean {
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false
        val playerBox = mc.thePlayer.entityBoundingBox.expand(1.0, 1.0, 1.0)
        return (entity !is EntityArmorStand
                && entity !is EntityItem
                && entity is EntityLivingBase
                && entity.entityId != mc.thePlayer.entityId
                && playerBox.intersectsWith(entity.entityBoundingBox)
                && entity.entityId != -8
                && entity.entityId != -1337
                && !Blink.state)
    }
}

package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition

object LegitScaffold : Module("LegitScaffold", Category.WORLD) {

    private val killAuraModule = ModuleManager.getModule("KillAura")
    private val timer = MSTimer()
    private var setYaw2 = 0f
    private var checkType = "?"
    val rotationMode = "Simulate"

    override fun onDisable() {
        mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
        timer.reset()
    }
    @EventTarget
    fun onUpdate(event:UpdateEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        when (rotationMode) {
            "Simulate" -> {
                if (isFacingDirection(EnumFacing.EAST)) {
                    checkType = "EAST"
                    setYaw2 = -90f
                } else if (isFacingDirection(EnumFacing.WEST)) {
                    checkType = "WEST"
                    setYaw2 = 90f
                } else if (isFacingDirection(EnumFacing.SOUTH)) {
                    checkType = "SOUTH"
                    setYaw2 = 0f
                } else if (isFacingDirection(EnumFacing.NORTH)) {
                    checkType = "NORTH"
                    setYaw2 = 180f
                }
                val offsetResult = isPlayerOnBlockEdge(mc.thePlayer)
                var delay = 140
                val heldItem = mc.thePlayer.currentEquippedItem
                if (heldItem != null && heldItem.item is ItemBlock && mc.gameSettings.keyBindBack.pressed && mc.gameSettings.keyBindUseItem.pressed && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw
                    mc.thePlayer.rotationPitch = mc.thePlayer.rotationPitch
                    if (lookDownCheck()) {
                        setPitch(79.5085f)
                        if (mc.thePlayer.rotationYaw != setYaw2) {
                            mc.thePlayer.rotationYaw = setYaw2
                        }
                        if (mc.gameSettings.keyBindJump.pressed) {
                            delay = 30
                            resetMoveKeys()
                        }
                        if (timer.hasTimePassed(delay)) {
                            when (offsetResult) {
                                "L" -> {
                                    mc.gameSettings.keyBindLeft.pressed = true
                                    mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
                                    timer.reset()
                                }
                                "R" -> {
                                    mc.gameSettings.keyBindRight.pressed = true
                                    mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
                                    timer.reset()
                                }
                            }
                        }
                    } else {
                        // Adjust pitch based on yaw
                        if (mc.thePlayer.rotationYaw in -75.2..-13.2) {
                            mc.thePlayer.rotationYaw = -45f
                            setPitch(75.3243f)
                        }
                        if (mc.thePlayer.rotationYaw in 14.2..77.2) {
                            mc.thePlayer.rotationYaw = 45f
                            setPitch(75.3243f)
                        }
                        if (mc.thePlayer.rotationYaw in 103.2..167.2) {
                            mc.thePlayer.rotationYaw = 135f
                            setPitch(75.3243f)
                        }
                        if (mc.thePlayer.rotationYaw in -166.2..-105.2) {
                            mc.thePlayer.rotationYaw = -135f
                            setPitch(75.3243f)
                        }
                    }
                } else {
                    resetMoveKeys()
                    timer.reset()
                }
            }
        }
    }

    private fun setPitch(pitch: Float) {
        if (mc.thePlayer.rotationPitch != pitch) {
            mc.thePlayer.rotationPitch = pitch
        }
    }

    private fun resetMoveKeys() {
        if (mc.gameSettings.keyBindLeft.pressed) {
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        }
        if (mc.gameSettings.keyBindRight.pressed) {
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
        }
        if (mc.gameSettings.keyBindSneak.pressed) {
            mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
        }
    }

    private fun lookDownCheck(): Boolean {
        return mc.thePlayer.rotationPitch in 78.0..83.5085
    }

    private fun isPlayerOnBlockEdge(player: EntityPlayer): String {
        val playerBB = player.entityBoundingBox
        val blockPos = BlockPos(player.posX, Math.floor(player.posY - 0.01), player.posZ)
        val blockBB = AxisAlignedBB(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), blockPos.x.toDouble() + 1, blockPos.y.toDouble() + 1, blockPos.z.toDouble() + 1)
        val threshold = 0.1
        var offset = "?"
        if (Math.abs(playerBB.minY - blockBB.maxY) < threshold) {
            when (checkType) {
                "WEST" -> {
                    val blockCenterZ = blockPos.z.toDouble() + 0.5
                    offset = if (player.posZ > blockCenterZ) "R" else "L"
                }
                "SOUTH" -> {
                    val blockCenterX = blockPos.x.toDouble() + 0.5
                    offset = if (player.posX > blockCenterX) "R" else "L"
                }
                "NORTH" -> {
                    val blockCenterX = blockPos.x.toDouble() + 0.5
                    offset = if (player.posX < blockCenterX) "R" else "L"
                }
                "EAST" -> {
                    val blockCenterZ = blockPos.z.toDouble() + 0.5
                    offset = if (player.posZ < blockCenterZ) "R" else "L"
                }
            }
        }
        return offset
    }

    private fun isFacingDirection(direction: EnumFacing): Boolean {
        val yaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360
        val facing = EnumFacing.fromAngle(yaw.toDouble())
        return facing == direction
    }

    private fun getClosestEntity(): EntityPlayer? {
        val filteredEntities = mutableListOf<EntityPlayer>()
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityPlayer && entity != mc.thePlayer) {
                filteredEntities.add(entity)
            }
        }
        filteredEntities.sortBy { mc.thePlayer.getDistanceToEntity(it) }
        return filteredEntities.lastOrNull()
    }
}

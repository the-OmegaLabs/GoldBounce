package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.Freeze
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.intValue
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import kotlin.math.ceil
import kotlin.math.floor

object AutoSave : Module("AutoSave", Category.MOVEMENT) {

    // AutoFreeze
    private val autoFreeze by _boolean("AutoFreeze", true)
    private val freezeOnlyVoid by _boolean("FreezeOnlyVoid", true) { autoFreeze }
    private val freezeFallDistance by intValue("FreezeFallDistance", 5, 1..50) { autoFreeze }

    // AutoScaffold
    private val autoScaffold by _boolean("AutoScaffold", true)
    private val scaffoldOnlyVoid by _boolean("ScaffoldOnlyVoid", true) { autoScaffold }
    private val scaffoldVoidDistance by intValue("ScaffoldVoidDistance", 1, 1..50) { autoScaffold }
    private val scaffoldOnHit by _boolean("ScaffoldOnHit", true) { autoScaffold }
    private val scaffoldInCombat by _boolean("ScaffoldInCombat", true) { autoScaffold }

    private const val LOWEST_Y = -64
    private const val BLOCK_EDGE = 0.3
    private const val RECEIVE_HIT_TICKS = 50

    private var lastGroundY = LOWEST_Y
    private var freezeSaving = false
    private var scaffoldSaving = false
    private var wasSpectator = false
    private var receiveHitTicks = 0

    private fun reset(disable: Boolean) {
        if (disable) {
            if (freezeSaving) Freeze.state = false
            if (scaffoldSaving) Scaffold.state = false
        }

        lastGroundY = LOWEST_Y
        freezeSaving = false
        scaffoldSaving = false
        receiveHitTicks = 0
    }

    private fun aboveVoid(voidDistance: Int = -1): Boolean {
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false

        if (player.onGround) return false

        val xRange = mutableListOf(0)
        val zRange = mutableListOf(0)
        if (player.posX - floor(player.posX) <= BLOCK_EDGE) {
            xRange.add(-1)
        } else if (ceil(player.posX) - player.posX <= BLOCK_EDGE) {
            xRange.add(1)
        }
        if (player.posZ - floor(player.posZ) <= BLOCK_EDGE) {
            zRange.add(-1)
        } else if (ceil(player.posZ) - player.posZ <= BLOCK_EDGE) {
            zRange.add(1)
        }

        for (xOffset in xRange) {
            for (zOffset in zRange) {
                val minY = if (voidDistance == -1) LOWEST_Y else lastGroundY - voidDistance
                for (y in minY..lastGroundY) {
                    val block = world.getBlockState(BlockPos(player.posX.toInt() + xOffset, y, player.posZ.toInt() + zOffset)).block
                    if (block != null && block.material.isSolid) {
                        return false
                    }
                }
            }
        }

        return true
    }

    // ---------- 事件处理：使用当前实现方式的 @EventTarget 方法 ----------

    @EventTarget
    fun onWorldChange(event: WorldEvent) {
        reset(true)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            reset(true)
        }

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer?.entityId) {
            receiveHitTicks = RECEIVE_HIT_TICKS
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        if (player.capabilities.isFlying) {
            if (!wasSpectator) {
                wasSpectator = true
                reset(true)
            }
            return
        } else {
            if (wasSpectator) wasSpectator = false
        }

        if (receiveHitTicks > 0) receiveHitTicks--
        if (player.hurtTime > 0) {
            receiveHitTicks = RECEIVE_HIT_TICKS
        }

        if (player.onGround) {
            lastGroundY = player.posY.toInt() - 1
        }

        // AutoFreeze
        if (autoFreeze) {
            if (player.posY >= LOWEST_Y + 2
                && (!freezeOnlyVoid || aboveVoid())
                && !player.onGround
                && player.posY <= lastGroundY + 1 - freezeFallDistance
            ) {
                if (!freezeSaving && !Freeze.state) {
                    Freeze.state = true
                    freezeSaving = true
                }
            } else {
                if (freezeSaving) {
                    freezeSaving = false
                }
            }
        }

        // AutoScaffold
        if (autoScaffold) {
            val hitCondition = !scaffoldOnHit || receiveHitTicks > 0
            val combatCondition = !scaffoldInCombat || mc.thePlayer?.lastAttacker != null

            if (hitCondition && combatCondition &&
                aboveVoid(if (scaffoldOnlyVoid) -1 else scaffoldVoidDistance)
            ) {
                if (!scaffoldSaving && !Scaffold.state) {
                    Scaffold.state = true
                    scaffoldSaving = true
                }
            } else {
                if (scaffoldSaving) {
                    Scaffold.state = false
                    scaffoldSaving = false
                }
            }
        }
    }

    override fun onEnable() {
        reset(false)
        wasSpectator = false
    }
}

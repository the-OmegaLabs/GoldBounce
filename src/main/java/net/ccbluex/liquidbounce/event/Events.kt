/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.event

import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.utils.extensions.withY
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.util.*

/**
 * Called when player attacks other entity
 *
 * @param targetEntity Attacked entity
 */
class AttackEvent(val targetEntity: Entity?) : Event(){
    fun getTheTargetEntity(): Entity? {
        return targetEntity
    }
}

/**
 * Called when minecraft get bounding box of block
 *
 * @param blockPos block position of block
 * @param block block itself
 * @param boundingBox vanilla bounding box
 */
class BlockBBEvent(blockPos: BlockPos, val block: Block, var boundingBox: AxisAlignedBB?) : Event() {
    val x = blockPos.x
    val y = blockPos.y
    val z = blockPos.z
}

/**
 * Called when player clicks a block
 */
class ClickBlockEvent(val clickedBlock: BlockPos?, val enumFacing: EnumFacing?) : Event()

/**
 * Called when client is shutting down
 */
class ClientShutdownEvent : Event()

/**
 * Called when another entity moves
 */
data class EntityMovementEvent(val movedEntity: Entity) : Event()
class MoveMathEvent() : CancellableEvent()
/**
 * Called when player jumps
 *
 * @param motion jump motion (y motion)
 */
class JumpEvent(var motion: Float, val eventState: EventState) : CancellableEvent()

/**
 * Called when user press a key once
 *
 * @param key Pressed key
 */
class KeyEvent(val key: Int) : Event()

/**
 * Called in "onUpdateWalkingPlayer"
 *
 * @param eventState PRE or POST
 */
class MotionEvent(var x: Double, var y: Double, var z: Double, var onGround: Boolean, val eventState: EventState) : Event()
/**
 * Called when player killed other entity
 *
 * @param targetEntity Attacked entity
 */
class EntityKilledEvent(val targetEntity: EntityLivingBase) : Event()
/**
 * Called in "onLivingUpdate" when the player is using a use item.
 *
 * @param strafe the applied strafe slow down
 * @param forward the applied forward slow down
 */
class SlowDownEvent(var strafe: Float, var forward: Float) : Event()

/**
 * Called in "onLivingUpdate" when the player is sneaking.
 *
 * @param strafe the applied strafe slow down
 * @param forward the applied forward slow down
 */
class SneakSlowDownEvent(var strafe: Float, var forward: Float) : Event()

/**
 * Called in "onLivingUpdate" after the movement input update.
 *
 * @param originalInput the movement input after the update
 */
class MovementInputEvent(var originalInput: MovementInput) : Event()

/**
 * Called in "onLivingUpdate" after when the player's sprint states are updated
 */
class PostSprintUpdateEvent : Event()
/**
 * 通过注入Forge的MixinGuiIngameForge->renderGameOverlay实现
 */
class MotionBlurEvent : Event()
/**
 * Called in "moveFlying"
 */
class StrafeEvent(val strafe: Float, val forward: Float, val friction: Float) : CancellableEvent()

/**
 * Called when player moves
 *
 * @param x motion
 * @param y motion
 * @param z motion
 */
class MoveEvent(var x: Double, var y: Double, var z: Double) : CancellableEvent() {
    var isSafeWalk = false

    fun zero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    fun zeroXZ() {
        x = 0.0
        z = 0.0
    }
}

/**
 * Called when receive or send a packet
 */
class PacketEvent(val packet: Packet<*>, val eventType: EventState) : CancellableEvent()

/**
 * Called when a block tries to push you
 */
class BlockPushEvent : CancellableEvent()

/**
 * Called when screen is going to be rendered
 */
class Render2DEvent(val partialTicks: Float) : Event()

/**
 * Called when screen is going to be rendered
 */
class AfterHotbarEvent(val partialTicks: Float) : Event()

/**
 * Called when packets sent to client are processed
 */
class GameLoopEvent : Event()

/**
 * Called when world is going to be rendered
 */
class Render3DEvent(val partialTicks: Float) : Event()

/**
 * Called when blur module is open
 */
class BlurEvent() : Event()

/**
 * Called when the screen changes
 */
class ScreenEvent(val guiScreen: GuiScreen?) : Event()

/**
 * Called when the session changes
 */
class SessionEvent : Event()

/**
 * Called when player is going to step
 */
class StepEvent(var stepHeight: Float) : Event()

/**
 * Called when player step is confirmed
 */
class StepConfirmEvent : Event()

/**
 * tick... tack... tick... tack
 */
class GameTickEvent : Event()

class TickEndEvent : Event()

/**
 * tick tack for player
 */
class PlayerTickEvent(val state: EventState) : CancellableEvent()

class RotationUpdateEvent : Event()

class RotationSetEvent(var yawDiff: Float, var pitchDiff: Float) : CancellableEvent()

class CameraPositionEvent(
    private val currPos: Vec3, private val prevPos: Vec3, private val lastTickPos: Vec3,
    var result: FreeCam.PositionPair? = null,
) : Event() {
    fun withY(value: Double) {
        result = FreeCam.PositionPair(currPos.withY(value), prevPos.withY(value), lastTickPos.withY(value))
    }
}

class ClientSlotChange(var supposedSlot: Int, var modifiedSlot: Int) : Event()

/**
 * Called when minecraft player will be updated
 */
class UpdateEvent : Event()

/**
 * Called when the world changes
 */
class WorldEvent(val worldClient: WorldClient?) : Event()

/**
 * Called when window clicked
 */
class ClickWindowEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int) :
    CancellableEvent()

/**
 * Called when LiquidBounce finishes starting up
 */
class StartupEvent : Event()
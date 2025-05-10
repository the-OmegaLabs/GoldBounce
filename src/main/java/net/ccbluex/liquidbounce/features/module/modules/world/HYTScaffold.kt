package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationSettings
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.*
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

object HYTScaffold : Module("HYTScaffold",  Category.WORLD) {

    // Values
    private val hideJump by BoolValue("HideJump", false)
    private val keepYValue by BoolValue("Keep Y", false)
    private val bedwars by BoolValue("BedWars", false)
    private val tellyTicks by FloatValue("TellyTicks", 2.9f, 0.5f..8f)
    private val swing by BoolValue("Swing", true)
    private val sprintVal by BoolValue("Sprint", false)
    private val watchdog by BoolValue("Watchdog", false)
    private val adStrafe by BoolValue("ADStrafe", false)
    private val tower by BoolValue("Tower", false)
    private val eagle by BoolValue("Eagle", false)
    private val safeWalk by BoolValue("Safe walk", false)
    private val fakeSlot by BoolValue("FakeSlot", false)
    private val telly by BoolValue("Telly", true)
    private val upValue by BoolValue("Up", false) { telly && !keepYValue }
    private val esp by BoolValue("ESP", true)
    private val rotateSettings = RotationSettings(this)
    // State
    private var prevSlot = 0
    private var slot = -1
    private var keepYCoord = 0.0
    private var data: BlockPos? = null
    private var placeFacing: EnumFacing? = null
    var canTellyPlace = false
    private var offGroundTicks = 0
    private var towerTick = 0
    private val timer = net.ccbluex.liquidbounce.utils.timing.MSTimer()

    override fun onEnable() {
        prevSlot = mc.thePlayer.inventory.currentItem
        mc.thePlayer.isSprinting = sprintVal
        canTellyPlace = false
        data = null
        slot = -1
    }

    override fun onDisable() {
        mc.thePlayer.inventory.currentItem = prevSlot
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, false)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        offGroundTicks = if (mc.thePlayer.onGround) 0 else offGroundTicks + 1

        // Tower
        if (tower && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) doTower()

        // Slot select
        slot = findBlockSlot()
        if (slot >= 0) {
            mc.thePlayer.inventory.currentItem = slot
            if (fakeSlot) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(slot))
        }

        // Up/jump for keepY
        if (upValue || keepYValue) {
            if (mc.thePlayer.onGround && net.ccbluex.liquidbounce.utils.MovementUtils.isMoving()) mc.thePlayer.jump()
        }

        // Keep Y
        if (mc.thePlayer.onGround) keepYCoord = floor(mc.thePlayer.posY - 1)

        // Compute data
        findBlock()

        // Place logic
        place()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            // Rotation for place
            data?.let { pos ->
                val rot = RotationUtils.getRotationBlock(pos, 0f).fixedSensitivity()
                RotationUtils.setTargetRotation(rot, rotateSettings)
            }
            // Safe Walk
            if (safeWalk && mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= 0.15
                mc.thePlayer.motionZ *= 0.15
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer && telly) {
            event.packet.onGround = false
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        data?.let { pos -> if (esp) {RenderUtils.drawBlockBox(pos, Color.RED, false)} }
    }

    private fun findBlock() {
        // 统一转换为 Int
        data = BlockPos(
            mc.thePlayer.posX.toInt(),
            (if (keepYValue) keepYCoord else mc.thePlayer.posY - 1).toInt(),
            mc.thePlayer.posZ.toInt()
        )
        if (data?.let { BlockUtils.getBlock(it) } === Blocks.air) data = null
    }


    private fun place() {
        if (!telly) return
        canTellyPlace = offGroundTicks >= tellyTicks.toInt()
        if (!canTellyPlace || data == null || !timer.hasTimePassed(0)) return

        val pos = data!!
        val side = getPlaceSide(pos) ?: return
        sendPackets(
            C08PacketPlayerBlockPlacement(pos, side.opposite.index, mc.thePlayer.heldItem, 0f, 0f, 0f),
            C0APacketAnimation()
        )
        if (swing) mc.thePlayer.swingItem()
        timer.reset()
    }

    private fun getPlaceSide(pos: BlockPos): EnumFacing? {
        return listOf(
            EnumFacing.UP to pos.add(0, 1, 0),
            EnumFacing.EAST to pos.add(1, 0, 0),
            EnumFacing.WEST to pos.add(-1, 0, 0),
            EnumFacing.SOUTH to pos.add(0, 0, 1),
            EnumFacing.NORTH to pos.add(0, 0, -1)
        ).firstOrNull { (_, bp) ->
            BlockUtils.getBlock(bp) === Blocks.air && bp.distanceSqToCenter(
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) < 4.0
        }?.first
    }

    private fun findBlockSlot(): Int {
        return (0..8).firstOrNull { i ->
            mc.thePlayer.inventory.getStackInSlot(i)?.item is ItemBlock
        } ?: -1
    }

    private fun BlockPos.distanceSqToCenter(x: Double, y: Double, z: Double): Double {
        val cx = this.x + 0.5
        val cy = this.y + 0.5
        val cz = this.z + 0.5
        return (x - cx).pow(2) + (y - cy).pow(2) + (z - cz).pow(2)
    }

    private fun doTower() {
        with(mc.thePlayer) {
            motionY = 0.41965
            motionX = motionX.coerceAtMost(0.265)
            motionZ = motionZ.coerceAtMost(0.265)
        }
        when (towerTick++) {
            1 -> mc.thePlayer.motionY = 0.33
            2 -> mc.thePlayer.motionY = 1.0 - (mc.thePlayer.posY % 1.0)
            else -> towerTick = 0
        }
    }
}
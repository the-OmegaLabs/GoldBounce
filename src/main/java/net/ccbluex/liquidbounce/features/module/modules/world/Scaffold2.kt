package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.skid.lbnew.MathUtil
import net.ccbluex.liquidbounce.utils.Vec3d
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationSettings
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.block.state
import net.ccbluex.liquidbounce.utils.block.toVec
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.minus
import net.ccbluex.liquidbounce.utils.extensions.onPlayerRightClick
import net.ccbluex.liquidbounce.utils.skid.lbnew.FallingPlayer
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.*
import net.minecraftforge.event.ForgeEventFactory
import kotlin.math.abs
import kotlin.math.max

object Scaffold2 : Module("QuickMacroScaffold", Category.WORLD) {

    private val invalidBlocks = listOf(
        Blocks.command_block, Blocks.barrier, Blocks.enchanting_table, Blocks.anvil, Blocks.brewing_stand,
        Blocks.water, Blocks.lava, Blocks.bed, Blocks.tnt, Blocks.portal, Blocks.chest, Blocks.trapped_chest,
        Blocks.furnace, Blocks.crafting_table, Blocks.web, Blocks.ladder, Blocks.cake, Blocks.dragon_egg,
        Blocks.end_portal_frame, Blocks.tripwire_hook
    )

    private val fullSprint = BoolValue("FullSprint", false)
    private val switchBack = BoolValue("SwitchBack", true)
    private val bw = BoolValue("BedWars", false)
    private val rotationSettings = RotationSettings(this).withoutKeepRotation()

    private var baseY = -1
    private var bigVelocityTick = 0
    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null
    private var canPlace = true
    private var rotateCount = 0

    init {
        EventManager.registerListener(this)
    }

    @EventTarget
    fun onMoveInput(event: MovementInputEvent) {
        val player = mc.thePlayer ?: return
        if (player.onGround && event.originalInput.moveForward > 0.0f && !mc.gameSettings.keyBindJump.isKeyDown) {
            event.originalInput.jump = true
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == player.entityId) {
            val strength = Vec3d(packet.motionX / 8000.0, 0.0, packet.motionZ / 8000.0).length()
            if (strength >= 1.5) {
                chat("你也是要飞了: $strength")
                bigVelocityTick = 60
            }
        }
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val playerPos = BlockPos(player)
        val state = world.getBlockState(playerPos)
        if (state.block != Blocks.air && !state.block.isReplaceable(world, playerPos)) return
        if (player.ticksExisted <= 5) return

        if (bigVelocityTick > 0) bigVelocityTick--
        if (player.onGround && bigVelocityTick <= 30) bigVelocityTick = 0

        val motion = max(abs(player.motionX), abs(player.motionZ))
        if (!fullSprint.get()) place(true)
        if (!fullSprint.get() && motion <= 0.4) {
            if ((abs(player.motionX) < 0.03 || abs(player.motionZ) < 0.03) && !player.onGround && getAirTicks(player) <= 2) return
        }

        if (baseY != -1 && baseY <= player.posY.toInt() - 1 && bigVelocityTick <= 0 && !player.onGround && mc.gameSettings.keyBindJump.isKeyDown) {
            baseY = player.posY.toInt() - 1
        }

        findBlock()

        canPlace = !(mc.gameSettings.keyBindJump.isKeyDown && getAirTicks(player) >= 2)
        if (mc.gameSettings.keyBindJump.isKeyDown && !canPlace) return

        blockPos?.let {
            val falling = player.motionY < -0.1
            val reachable = if (falling) {
                val fall = FallingPlayer(player)
                fall.calculate(2)
                it.y <= fall.y
            } else true

            if (reachable && bigVelocityTick <= 0 && fullSprint.get()) {
                if (rotateCount <= 8) {
                    val rotation = RotationUtils.getRotationBlock(it, 0f)
                    rotateCount++
                    sendLookPacket(rotation)
                    place(false)
                }
            } else {
                rotateCount = 0
                val rotation = RotationUtils.getRotationBlock(it, 1f)
                RotationUtils.setTargetRotation(rotation, rotationSettings)
            }
        }
    }

    private fun place(rotate: Boolean) {
        if (!canPlace || blockPos == null || enumFacing == null) return

        val vec3 = getVec3(blockPos!!, enumFacing!!).toVec3()
        mc.thePlayer?.let { player ->
            mc.thePlayer.onPlayerRightClick(
                blockPos!!,
                enumFacing!!,
                getVec3(blockPos!!, enumFacing!!).toVec3()
            )
            player.swingItem()
            blockPos = null

            if (rotate) {
                val rot = Rotation(player.rotationYaw, player.rotationPitch)
                RotationUtils.setTargetRotation(rot, rotationSettings)
            }
        }
    }

    private fun findBlock() {
        val player = mc.thePlayer ?: return
        val baseVec = Vec3d(player.getPositionEyes(2f))
        val base = BlockPos(baseVec.x, baseY + 0.1, baseVec.z)
        val baseX = base.x
        val baseZ = base.z
        if (baseY == -1) baseY = player.posY.toInt()
        if (checkBlock(baseVec, base)) return

        for (d in 1..6) {
            if (checkBlock(baseVec, BlockPos(baseX, baseY - d, baseZ))) return
            for (x in 1..d) {
                for (z in 0..(d - x)) {
                    val y = d - x - z
                    for (rev1 in 0..1) {
                        for (rev2 in 0..1) {
                            val dx = if (rev1 == 0) x else -x
                            val dz = if (rev2 == 0) z else -z
                            if (checkBlock(baseVec, BlockPos(baseX + dx, baseY - y, baseZ + dz))) return
                        }
                    }
                }
            }
        }

        chat("没找到")
    }

    private fun checkBlock(baseVec: Vec3d, pos: BlockPos): Boolean {
        val world = mc.theWorld ?: return false
        val blockState = world.getBlockState(pos)
        if (blockState.block !is BlockAir) return false

        val center = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        for (face in EnumFacing.values()) {
            val hit = center.add(Vec3d(face.directionVec).scale(0.5))
            val baseBlock = pos.offset(face)
            val relevant = hit.subtract(baseVec)
            if (relevant.lengthSquared() <= 20.25 && relevant.dotProduct(Vec3d(face.directionVec)) >= 0.0) {
                blockPos = BlockPos(baseBlock)
                enumFacing = face.opposite
                return true
            }
        }
        return false
    }

    private fun getVec3(pos: BlockPos, face: EnumFacing): Vec3d {
        var x = pos.x + 0.5
        var y = pos.y + 0.5
        var z = pos.z + 0.5

        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += MathUtil.getRandomInRange(-0.3, 0.3)
            z += MathUtil.getRandomInRange(-0.3, 0.3)
        } else {
            y += MathUtil.getRandomInRange(-0.3, 0.3)
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) z += MathUtil.getRandomInRange(-0.3, 0.3)
        if (face == EnumFacing.NORTH || face == EnumFacing.SOUTH) x += MathUtil.getRandomInRange(-0.3, 0.3)

        return Vec3d(x, y, z)
    }

    private fun isValid(item: Item?): Boolean {
        return item is ItemBlock && !invalidBlocks.contains(item.block)
    }

    private fun getAirTicks(player: EntityPlayerSP): Int {
        // Placeholder: depends on environment implementation
        return 0
    }

    private fun sendLookPacket(rotation: Rotation) {
        mc.thePlayer?.let {
            val packet = C03PacketPlayer.C05PacketPlayerLook(rotation.yaw, rotation.pitch, it.onGround)
            mc.netHandler?.addToSendQueue(packet)
        }
    }

    override fun onEnable() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        blockPos = null
        baseY = -1
        canPlace = true
        bigVelocityTick = 0
        SilentHotbar.resetSlot(this)

        val heldItem = player.heldItem
        if (heldItem?.item !is ItemBlock) {
            InventoryUtils.findBlockInHotbar()?.let {
                SilentHotbar.selectSlotSilently(this, it)
            }
        }
    }

    override fun onDisable() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        mc.gameSettings.keyBindJump.pressed = false
        SilentHotbar.resetSlot(this)
    }
}

package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MovementInputEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.onPlayerRightClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.skid.lbnew.FallingPlayer
import net.ccbluex.liquidbounce.utils.skid.lbnew.MathUtil
import net.ccbluex.liquidbounce.value.boolean
import net.minecraft.block.BlockAir
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3i
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs

object Scaffold2 : Module("Scaffold2", Category.WORLD) {

    private val invalidBlocks = listOf(
        Blocks.enchanting_table,
        Blocks.chest,
        Blocks.ender_chest,
        Blocks.trapped_chest,
        Blocks.anvil,
        Blocks.sand,
        Blocks.web,
        Blocks.torch,
        Blocks.crafting_table,
        Blocks.furnace,
        Blocks.waterlily,
        Blocks.dispenser,
        Blocks.stone_pressure_plate,
        Blocks.wooden_pressure_plate,
        Blocks.noteblock,
        Blocks.dropper,
        Blocks.tnt,
        Blocks.standing_banner,
        Blocks.wall_banner,
        Blocks.redstone_torch
    )

    private val fullSprint by boolean("FullSprint", false)
    private val switchBack by boolean("SwitchBack", true)
    private val bw by boolean("BedWars", false)
    private val rotationSettings = RotationSettings(this).withoutKeepRotation()

    var baseY = -1
    var bigVelocityTick = 0

    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null
    private var canPlace = false
    private var rotateCount = 0


    override fun onEnable() {
        mc.theWorld ?: return
        val player = mc.thePlayer ?: return

        blockPos = null
        baseY = -1
        canPlace = true
        bigVelocityTick = 0

        SilentHotbar.resetSlot(this)
        if (player.heldItem?.item !is ItemBlock) {
            InventoryUtils.findBlockInHotbar()?.let {
                SilentHotbar.selectSlotSilently(requester = this, slot = it)
            }
        }
    }

    override fun onDisable() {
        mc.theWorld ?: return
        mc.thePlayer ?: return
        mc.gameSettings.keyBindSneak.pressed = false
        SilentHotbar.resetSlot(this)
    }

    // Event Handlers
//    private val onRender2D = handler<Render2DEvent> { event ->
//        val sr = ScaledResolution(mc)
//        val count = getBlockCount()
//        val text = "Blocks: §7$count"
//        mc.fontRenderer.drawStringWithShadow(
//            text,
//            sr.scaledWidth / 2f - mc.fontRenderer.getStringWidth(text) / 2,
//            sr.scaledHeight / 2f - 30,
//            -1
//        )
//    }
    @EventTarget
    private fun onMoveInput(event: MovementInputEvent){
        if (mc.thePlayer.onGround && event.originalInput.moveForward > 0 && !mc.gameSettings.keyBindJump.isKeyDown) {
            event.originalInput.jump = true
        }
    }
    @EventTarget
    private fun onPacket(event: PacketEvent){
        if (event.packet is S12PacketEntityVelocity && event.packet.entityID == mc.thePlayer.entityId) {
            val strength = Vec3d(event.packet.getMotionX() / 8000.0, 0.0, event.packet.getMotionZ() / 8000.0).length()
            if (strength >= 1.5) {
                chat("你也是要飞了: $strength")
                bigVelocityTick = 60
            }
        }
    }
    @EventTarget
    private fun onTick(event: TickEvent)  {
        val playerPos: BlockPos = BlockPos(mc.thePlayer)
        val state: IBlockState = mc.theWorld.getBlockState(playerPos)
        if (state.block !== Blocks.air && state.block.isPassable(mc.theWorld, playerPos)) return// 开法阵了
        if (mc.thePlayer.ticksExisted <= 5) return
        if (bigVelocityTick > 0) {
            bigVelocityTick--
        }
        if (mc.thePlayer.onGround && bigVelocityTick <= 30) {
            bigVelocityTick = 0
        }
        val motion: Double = Math.max(mc.thePlayer.motionX, mc.thePlayer.motionZ)
        if (!fullSprint) {
            place(true)
        }

        //        if (!mc.gameSettings.keyBindJump.isKeyDown() && !bw) {
        if (!fullSprint && motion <= 0.4) {
            if (abs(mc.thePlayer.motionX) < 0.03 || abs(mc.thePlayer.motionZ) < 0.03) {
                if (!mc.thePlayer.onGround && mc.thePlayer.airTime <= 2) return
            } else {
                if (!mc.thePlayer.onGround && mc.thePlayer.airTime <= 1) return
            }
        }

        //        }
        if (baseY == -1 || baseY > mc.thePlayer.posY.toInt() - 1 || bigVelocityTick > 0 || mc.thePlayer.onGround || mc.gameSettings.keyBindJump.isKeyDown) {
            baseY = mc.thePlayer.posY.toInt() - 1
        }

        this.findBlock()
//        if (!InventoryUtil.switchBlock()) return@handler
        canPlace = !mc.gameSettings.keyBindJump.isKeyDown || mc.thePlayer.airTime >= 2
        if (mc.gameSettings.keyBindJump.isKeyDown && !canPlace) {
            return
        }
        if (blockPos != null) {
            var reachable = true
            if (mc.thePlayer.motionY < -0.1) {
                val fallingPlayer: FallingPlayer = FallingPlayer(mc.thePlayer)
                fallingPlayer.calculate(2)
                if (blockPos!!.y > fallingPlayer.y) {
                    reachable = false
                }
            }
            if ((!reachable || bigVelocityTick > 0 || fullSprint) && rotateCount <= 8) {
                val rotation: Rotation = RotationUtils.getRotationBlock(blockPos!!, 0F)
                //                ChatUtil.info("working");
                StuckUtils.stuck()
                rotateCount++
                PacketUtils.sendPacket(
                    C03PacketPlayer.C05PacketPlayerLook(
                        rotation.yaw, rotation.pitch, mc.thePlayer.onGround
                    )
                )
                place(false)
                StuckUtils.stopStuck()
            } else {
                val rotation: Rotation = RotationUtils.getRotationBlock(blockPos!!, 1F)
                rotateCount = 0
                RotationUtils.setTargetRotation(rotation, rotationSettings)
            }
        }
        @EventTarget
        fun onMotion(event: MotionEvent)  {
            if (mc.thePlayer.heldItem?.item !is ItemBlock) {
                InventoryUtils.findBlockInHotbar()?.let {
                    SilentHotbar.selectSlotSilently(requester = this, slot = it)
                }
            }
        }
    }


    fun place(rotate: Boolean) {
        if (!canPlace) {
            return
        }

        if (blockPos != null) {
            mc.thePlayer.onPlayerRightClick(
                    blockPos!!,
                    enumFacing!!,
                    getVec3(blockPos!!, enumFacing!!).toVec3()
            )
            mc.thePlayer.swingItem()

            blockPos = null
            if (rotate) {
                RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), rotationSettings)
            }
        }
    }
    private val EntityPlayer.airTime: Int
    get() = if (onGround) 0 else (this as EntityLivingBase).hurtTime
    private fun findBlock() {
        val baseVec: Vec3d = Vec3d(mc.thePlayer.getPositionEyes(2f))
        //        BlockPos base = new BlockPos(baseVec.x, baseY + 0.1f, baseVec.z);
        val base: BlockPos = BlockPos(baseVec.x, baseY + 0.1, baseVec.z)
        val baseX = base.x
        val baseZ = base.z
//        if (mc.theWorld.getBlockState(base).block.isFullCube) return
        if (checkBlock(baseVec, base)) {
            chat("1")
            return
        }
        for (d in 1..6) {
            if (checkBlock(
                    baseVec, BlockPos(
                        baseX,
                        baseY - d,
                        baseZ
                    )
                )
            ) {
                chat("2")
                return
            }
            for (x in 1..d) {
                for (z in 0..d - x) {
                    val y = d - x - z
                    for (rev1 in 0..1) {
                        for (rev2 in 0..1) {
                            if (checkBlock(
                                    baseVec, BlockPos(
                                        baseX + (if (rev1 == 0) x else -x),
                                        baseY - y,
                                        baseZ + (if (rev2 == 0) z else -z)
                                    )
                                )
                            ) {
                                chat("3")
                                return
                            }
                        }
                    }
                }
            }
        }
        chat("没找到")
    }

    private fun checkBlock(baseVec: Vec3d, pos: BlockPos): Boolean {
        if (mc.theWorld.getBlockState(pos).block !is BlockAir) {
            chat("不是空气 跳过")
            return false
        }
        val center = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        for (脸 in EnumFacing.values()) {
            val hit = center.add(Vec3d(脸.directionVec).scale(0.5))
            val baseBlock: Vec3i = pos.add(脸.directionVec)
//            if (!mc.theWorld.getBlockState(BlockPos(baseBlock.x, baseBlock.y, baseBlock.z)).block.isBlockNormalCube) {
//                chat("1")
//                continue
//            }
            val relevant = hit.subtract(baseVec)
            if (relevant.lengthSquared() <= 4.5 * 4.5 && relevant.dotProduct(
                    Vec3d(脸.directionVec)
                ) >= 0
            ) {
                blockPos = BlockPos(baseBlock)
                enumFacing = 脸.opposite
                chat("[Scaffold] $pos")
                return true
            }
        }
        return false
    }

    private fun getVec3(pos: BlockPos, face: EnumFacing): Vec3d {
        var x = pos.x.toDouble() + 0.5
        var y = pos.y.toDouble() + 0.5
        var z = pos.z.toDouble() + 0.5
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += MathUtil.getRandomInRange(0.3, -0.3)
            z += MathUtil.getRandomInRange(0.3, -0.3)
        } else {
            y += MathUtil.getRandomInRange(0.3, -0.3)
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtil.getRandomInRange(0.3, -0.3)
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += MathUtil.getRandomInRange(0.3, -0.3)
        }
        return Vec3d(x, y, z)
    }

    private fun isValid(item: Item): Boolean {
        return item is ItemBlock && !invalidBlocks.contains(item.getBlock())
    }

}
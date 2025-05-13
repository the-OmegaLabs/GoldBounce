/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world.scaffolds

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.settings.Debugger
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold.searchMode
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold.shouldGoDown
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.ReflectionUtil
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.blocksAmount
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.int
import net.minecraft.init.Blocks.air
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.potion.Potion
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.truncate
import kotlin.text.get

object Tower : MinecraftInstance(), Listenable {

    val towerModeValues = choices(
        "TowerMode",
        arrayOf(
            "None",
            "Jump",
            "MotionJump",
            "Motion",
            "ConstantMotion",
            "MotionTP",
            "Packet",
            "Teleport",
            "AAC3.3.9",
            "AAC3.6.4",
            "Vulcan2.9.0",
            "Pulldown",
            "BlocksMC",
            "Hypixel",
            "NCP"
        ),
        "None"
    )
    private val motionBlocksMC = FloatValue("BlocksMC-Motion", 1F, 0.1F..1F) { towerModeValues.equals("BlocksMC") || towerModeValues.equals("TestBlocksMC") }
    private val motionSpeedEffectBlocksMC = FloatValue("BlocksMC-SpeedEffect-Motion", 1F, 0.1F..1F) { towerModeValues.equals("BlocksMC") || towerModeValues.equals("TestBlocksMC") }
    val stopWhenBlockAboveValues = boolean("StopWhenBlockAbove", false) { towerModeValues.get() != "None" }

    val onJumpValues = boolean("TowerOnJump", true) { towerModeValues.get() != "None" }
    val notOnMoveValues = boolean("TowerNotOnMove", false) { towerModeValues.get() != "None" }

    // Jump mode
    val jumpMotionValues = FloatValue("JumpMotion", 0.42f, 0.3681289f..0.79f) { towerModeValues.get() == "MotionJump" }
    val jumpDelayValues = int(
        "JumpDelay",
        0,
        0..20
    ) { towerModeValues.get() == "MotionJump" || towerModeValues.get() == "Jump" }

    // Constant Motion values
    val constantMotionValues = FloatValue(
        "ConstantMotion",
        0.42f,
        0.1f..1f
    ) { towerModeValues.get() == "ConstantMotion" }
    val constantMotionJumpGroundValues = FloatValue(
        "ConstantMotionJumpGround",
        0.79f,
        0.76f..1f
    ) { towerModeValues.get() == "ConstantMotion" }
    val constantMotionJumpPacketValues = boolean("JumpPacket", true) { towerModeValues.get() == "ConstantMotion" }

    // Pull-down
    val triggerMotionValues = FloatValue("TriggerMotion", 0.1f, 0.0f..0.2f) { towerModeValues.get() == "Pulldown" }
    val dragMotionValues = FloatValue("DragMotion", 1.0f, 0.1f..1.0f) { towerModeValues.get() == "Pulldown" }

    // Teleport
    val teleportHeightValues = FloatValue("TeleportHeight", 1.15f, 0.1f..5f) { towerModeValues.get() == "Teleport" }
    val teleportDelayValues = int("TeleportDelay", 0, 0..20) { towerModeValues.get() == "Teleport" }
    val teleportGroundValues = boolean("TeleportGround", true) { towerModeValues.get() == "Teleport" }
    val teleportNoMotionValues = boolean("TeleportNoMotion", false) { towerModeValues.get() == "Teleport" }

    var isTowering = false

    // Mode stuff
    private val tickTimer = TickTimer()
    private var jumpGround = 0.0
    private var fallTicks = 0
    private var sent = false
    private var wasOnGround = false
    private var checkGround = false
    // Handle motion events
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (isTowering && towerModeValues.get() == "BlocksMC"){
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                event.x *= motionSpeedEffectBlocksMC.get()
                event.z *= motionSpeedEffectBlocksMC.get()
            } else {
                event.x *= motionBlocksMC.get()
                event.z *= motionBlocksMC.get()
            }
        }
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        val player = mc.thePlayer ?: return

        isTowering = false
        if (eventState == EventState.PRE) {
            if (player.onGround) {
                fallTicks = 0
                wasOnGround = true
            } else {
                // 使用 0.001 容差防止梯子/蛛网误判
                if (player.motionY < -0.001 && !wasOnGround) {
                    fallTicks++
                }
                wasOnGround = false
            }
        }
        if (towerModeValues.get() == "None" || notOnMoveValues.get() && player.isMoving ||
            onJumpValues.get() && !mc.gameSettings.keyBindJump.isKeyDown
        ) {
            if (Debugger.towerDbg) chat("退出Towering - 模式:${towerModeValues.get()} 移动:${player.isMoving} 跳跃键:${mc.gameSettings.keyBindJump.isKeyDown}")
            return
        }

        isTowering = true
        if (Debugger.towerDbg) chat("进入Towering模式 - 当前模式:${towerModeValues.get()} | Speed: ${Speed.state} | Fly: ${Fly.state}")

        if (eventState == EventState.POST) {
            if (Debugger.towerDbg) chat("开始执行 move()")

            tickTimer.update()

            if (!stopWhenBlockAboveValues.get() || getBlock(BlockPos(player).up(2)) == air) {
                move()
            }

            val blockPos = BlockPos(player).down()
            if (blockPos.getBlock() == air) {
                Scaffold.search(blockPos, !shouldGoDown, searchMode == "Area")
            }
        }
    }


    // Handle jump events
    @EventTarget
    fun onJump(event: JumpEvent) {
        if (onJumpValues.get()) {
            if (Scaffold.scaffoldMode == "GodBridge" && (Scaffold.jumpAutomatically) || !Scaffold.shouldJumpOnInput)
                return
            if (towerModeValues.get() == "None" || towerModeValues.get() == "Jump")
                return
            if (notOnMoveValues.get() && mc.thePlayer.isMoving)
                return
            if (Speed.state || Fly.state)
                return

            if (Debugger.towerDbg) chat("取消跳跃事件 - 当前模式:${towerModeValues.get()} 移动状态:${mc.thePlayer.isMoving}")
            event.cancelEvent()
        }
    }

    // Send jump packets, bypasses Hypixel.
    private fun fakeJump() {
        mc.thePlayer?.isAirBorne = true
        mc.thePlayer?.triggerAchievement(StatList.jumpStat)
    }


    /**
     * Move player
     */
    private fun move() {
        val player = mc.thePlayer ?: return

        val amount = blocksAmount()
        if (amount <= 0) {
            if (Debugger.towerDbg) chat("停止Towering - 方块数量不足 ($amount)")
            return
        }

        if (Debugger.towerDbg) chat("当前执行Tower模式: ${towerModeValues.get().lowercase()}")

        when (towerModeValues.get().lowercase()) {
            "blocksmc" -> {
                val player = mc.thePlayer ?: return

                if (Debugger.towerDbg) chat("BlocksMC 模式运行 - 地面状态: ${player.onGround}, motionY: ${player.motionY}")

                MovementUtils.strafe()

                if (player.onGround) {
                    player.motionY = 0.42
                    fakeJump()
                    if (Debugger.towerDbg) chat("BlocksMC 触发跳跃模拟")

                    // 可选：根据药水加速增强跳跃高度
                    if (player.isPotionActive(Potion.moveSpeed)) {
                        val amplifier = player.getActivePotionEffect(Potion.moveSpeed).amplifier
                        player.motionY += 0.08 * amplifier
                    }

                } else if (player.motionY < 0.1 && player.motionY > -0.1) {
                    player.motionY = -0.0784000015258789
                    if (Debugger.towerDbg) chat("BlocksMC 下降修正")
                }

                // 检查脚下是否有方块支撑
                val blockBelow = BlockPos(player.posX, player.posY - 1.4, player.posZ)
                if (getBlock(blockBelow) == air && Debugger.towerDbg) {
                    chat("⚠ BlocksMC 需要脚下有方块支撑！")
                }
            }

            "jump" -> {
                if (Debugger.towerDbg) chat("Jump模式触发 - 地面状态:${player.onGround} 计时器:${tickTimer.hasTimePassed(jumpDelayValues.get())}")
                if (player.onGround && tickTimer.hasTimePassed(jumpDelayValues.get())) {
                    fakeJump()
                    player.tryJump()
                } else if (!player.onGround) {
                    player.isAirBorne = false
                    tickTimer.reset()
                }

            }
            "motion" -> {
                if (Debugger.towerDbg) chat("Motion模式更新 - Y轴速度:${player.motionY}")
                if (player.onGround) {
                    fakeJump()
                    player.motionY = 0.42
                } else if (player.motionY < 0.1) {
                    player.motionY = -0.3
                }

            }
            // Old Name (Jump)
            "motionjump" -> if (player.onGround && tickTimer.hasTimePassed(jumpDelayValues.get())) {
                fakeJump()
                player.motionY = jumpMotionValues.get().toDouble()
                tickTimer.reset()
            }

            "motiontp" -> if (player.onGround) {
                fakeJump()
                player.motionY = 0.42
            } else if (player.motionY < 0.23) {
                player.setPosition(player.posX, truncate(player.posY), player.posZ)
            }

            "packet" -> if (player.onGround && tickTimer.hasTimePassed(2)) {
                fakeJump()
                sendPackets(
                    C04PacketPlayerPosition(
                        player.posX,
                        player.posY + 0.42,
                        player.posZ,
                        false
                    ),
                    C04PacketPlayerPosition(
                        player.posX,
                        player.posY + 0.753,
                        player.posZ,
                        false
                    )
                )
                player.setPosition(player.posX, player.posY + 1.0, player.posZ)
                tickTimer.reset()
            }

            "teleport" -> {
                if (Debugger.towerDbg) chat("传送模式状态 - 地面:${player.onGround} 延迟:${teleportDelayValues.get()}")
                if (teleportNoMotionValues.get()) {
                    player.motionY = 0.0
                }
                if ((player.onGround || !teleportGroundValues.get()) && tickTimer.hasTimePassed(
                        teleportDelayValues.get()
                    )
                ) {
                    fakeJump()
                    player.setPositionAndUpdate(
                        player.posX, player.posY + teleportHeightValues.get(), player.posZ
                    )
                    tickTimer.reset()
                }
            }

            "constantmotion" -> {
                if (player.onGround) {
                    if (constantMotionJumpPacketValues.get()) {
                        fakeJump()
                    }
                    jumpGround = player.posY
                    player.motionY = constantMotionValues.get().toDouble()
                }
                if (player.posY > jumpGround + constantMotionJumpGroundValues.get()) {
                    if (constantMotionJumpPacketValues.get()) {
                        fakeJump()
                    }
                    player.setPosition(
                        player.posX, truncate(player.posY), player.posZ
                    ) // TODO: toInt() required?
                    player.motionY = constantMotionValues.get().toDouble()
                    jumpGround = player.posY
                }
            }

            "pulldown" -> {
                if (!player.onGround && player.motionY < triggerMotionValues.get()) {
                    player.motionY = -dragMotionValues.get().toDouble()
                } else {
                    fakeJump()
                }
            }

            // Credit: @localpthebest / Nextgen
            "vulcan2.9.0" -> {
                if (player.ticksExisted % 10 == 0) {
                    // Prevent Flight Flag
                    player.motionY = -0.1
                    return
                }

                fakeJump()

                if (player.ticksExisted % 2 == 0) {
                    player.motionY = 0.7
                } else {
                    player.motionY = if (player.isMoving) 0.42 else 0.6
                }
            }

            "aac3.3.9" -> {
                if (player.onGround) {
                    fakeJump()
                    player.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (player.motionY < 0) {
                    player.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
            }

            "aac3.6.4" -> if (player.ticksExisted % 4 == 1) {
                player.motionY = 0.4195464
                player.setPosition(player.posX - 0.035, player.posY, player.posZ)
            } else if (player.ticksExisted % 4 == 0) {
                player.motionY = -0.5
                player.setPosition(player.posX + 0.035, player.posY, player.posZ)
            }
            "ncp" -> {
                if (player.onGround) {
                    jumpGround = player.posY
                    player.motionY = 0.42
                    fakeJump()
                    if (Debugger.towerDbg) chat("NCP模式触发跳跃")
                }
                if (player.posY > jumpGround + 0.79) {
                    player.setPosition(
                        player.posX,
                        truncate(player.posY),
                        player.posZ
                    )
                    player.motionY = 0.42
                    jumpGround = player.posY
                    if (Debugger.towerDbg) chat("NCP高度修正")
                }
            }

            "hypixel" -> {
                when {
                    player.onGround -> {
                        checkGround = true
                        if (Debugger.towerDbg) chat("Hypixel地面检测")
                    }
                    checkGround && fallTicks >= 18 -> {
                        isTowering = false
                        if (Debugger.towerDbg) chat("Hypixel停止塔模式")
                    }
                    else -> {
                        when (fallTicks % 3) {
                            0 -> {
                                val speedBoost = if (player.isPotionActive(Potion.moveSpeed)) {
                                    0.08 * (player.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)
                                } else 0.0

                                MovementUtils.strafe(0.22f + speedBoost.toFloat())
                                player.motionY = 0.42
                                fakeJump()
                            }
                            1 -> player.motionY = 0.33
                            2 -> player.motionY = (player.posY + 1.0) - player.posY
                        }
                        if (Debugger.towerDbg) chat("Hypixel阶段处理 ${fallTicks % 3}")
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return

        val packet = event.packet
        if (packet is C08PacketPlayerBlockPlacement) {
            // c08 item override to solve issues in scaffold and some other modules, maybe bypass some anticheat in future\
            ReflectionUtil.setFieldValue(packet, "stack", mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem])
            // illegal facing checks
            ReflectionUtil.setFieldValue(
                packet,
                "facingX",
                (ReflectionUtil.getFieldValue<Float>(packet, "facingX")).coerceIn(-1.0f..1.0f)
            )
            ReflectionUtil.setFieldValue(
                packet,
                "facingY",
                (ReflectionUtil.getFieldValue<Float>(packet, "facingY")).coerceIn(-1.0f..1.0f)
            )
            ReflectionUtil.setFieldValue(
                packet,
                "facingZ",
                (ReflectionUtil.getFieldValue<Float>(packet, "facingZ")).coerceIn(-1.0f..1.0f)
            )
            if (towerModeValues.equals("BlocksMC") && isTowering) {
                if (mc.thePlayer.motionY > -0.0784000015258789) {
                    if (packet.position.equals(
                            BlockPos(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY - 1.4,
                                mc.thePlayer.posZ
                            )
                        )
                    ) {
                        mc.thePlayer.motionY = -0.0784000015258789
                    }
                }
            }
        }
        if (towerModeValues.get() == "Vulcan2.9.0" && packet is C04PacketPlayerPosition &&
            !player.isMoving && player.ticksExisted % 2 == 0
        ) {
            packet.x += 0.1
            packet.z += 0.1
        }
    }

    override fun handleEvents() = Scaffold.handleEvents()
}

/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager.getModule
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.component3
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemAppleGold
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

object Criticals : Module("Criticals", Category.COMBAT, hideModule = false) {

    val mode by choices(
        "Mode",
        arrayOf(
            "Packet",
            "NCPPacket",
            "BMCPacket",
            "BlocksMC",
            "BlocksMC2",
            "NoGround",
            "Hop",
            "TPHop",
            "Jump",
            "LowJump",
            "CustomMotion",
            "Visual",
            "AutoFreeze",
            "HuaYuTing",
            "BMCSmart",
            "BMCBlatant",
            "Stuck"
        ),
        "Packet"
    )
    private var attacks = 0
    private var attacking = false
    val delay by int("Delay", 0, 0..500)
    private val hurtTime by int("HurtTime", 10, 0..10)
    private val customMotionY by float("Custom-Y", 0.2f, 0.01f..0.42f) { mode == "CustomMotion" }
    private val lookValue = BoolValue("UseC06Packet", false) { mode == "BMCPacket" }
    var stuckEnabled = false
    val msTimer = MSTimer()
    var offGroundTicks = 0
    var tick = 0
    var onGroundTicks = 0

    // Variables for Stuck mode
    private var skiptick = 0
    private var gappleNoGround = false

    override fun onEnable() {
        attacks = 0
        resetStuck()
        gappleNoGround = false

        if (mode == "NoGround")
            mc.thePlayer.tryJump()
        if (mode.equals("Stuck", ignoreCase = true)) {
            // As in the original onWorldLoad
            skiptick = 2
        }
    }

    override fun onDisable() {
        resetStuck()
        gappleNoGround = false
        // Reset state for AutoFreeze mode
        if (stuckEnabled) {
            getModule("Freeze")?.let { it.state = false }
            stuckEnabled = false
        }
    }

    fun sendCriticalPacket(
        xOffset: Double = 0.0,
        yOffset: Double = 0.0,
        zOffset: Double = 0.0,
        ground: Boolean
    ) {
        val x = mc.thePlayer.posX + xOffset
        val y = mc.thePlayer.posY + yOffset
        val z = mc.thePlayer.posZ + zOffset
        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(x, y, z, ground))
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState.stateName == "PRE"){
            if (mc.thePlayer.hurtTime > 0) {
                this.tick = 10;
            }
            --this.tick;
        }
        if (mode == "HuaYuTing") {
            if (event.eventState.stateName == "PRE") {
                if (mode == "HuaYuTing") {
                    if (KillAura.target != null && attacking) {
                        if (mc.thePlayer.fallDistance > 0 || offGroundTicks > 3) {
                            event.onGround = false
                        }
                    } else {
                        attacking = false
                    }
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            onGroundTicks++
            offGroundTicks = 0
        } else {
            offGroundTicks++
            onGroundTicks = 0;
        }
        if (mode == "AutoFreeze") {
            if (KillAura.target != null && mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
            if (mc.thePlayer.fallDistance > 0) {
                getModule("Freeze")?.let { it.state = true }
                stuckEnabled = true
            }
            if (KillAura.target == null && stuckEnabled) {
                getModule("Freeze")?.let { it.state = false }
                stuckEnabled = false
            }
        }

        // Stuck mode logic
        if (mode.equals("Stuck", ignoreCase = true)) {
            if (skiptick > 0) {
                // This is the core of the "stuck" logic.
                // By preventing gravity from applying on the client, while still sending packets,
                // we create a desync where the server sees the player as "stuck" midair.
                // This keeps the player in a "falling" state, enabling vanilla criticals.
                mc.thePlayer.motionY = 0.0
                skiptick--
                return // Skip the rest of the logic for this tick to maintain the "stuck" state
            }

            // Handle the gappleNoGround flag, which prevents crits after eating a gapple in the air
            if (isEatingGapple() && !mc.thePlayer.onGround) {
                gappleNoGround = true
            }
            if (!isEatingGapple() && gappleNoGround && mc.thePlayer.onGround) {
                gappleNoGround = false
            }

            val target = KillAura.target
            val aura = getModule(KillAura::class.java)

            // Reset if no target, aura is off, or crit conditions are impossible
            if (target == null || aura?.state != true || cantCrit(target)) {
                resetStuck()
                return
            }

            // Auto-jump to initiate the "falling" state when on ground and close to the target
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.getDistanceToEntity(target) <= 2.0f) {
                mc.thePlayer.jump()
            }

            // When falling and close to the target, increment skiptick to "freeze" the player on the next tick
            if (mc.thePlayer.motionY < 0.0 && !mc.thePlayer.onGround && mc.thePlayer.getDistanceToEntity(target) <= 2.0f) {
                skiptick++
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val thePlayer = mc.thePlayer ?: return
            val entity = event.targetEntity

            if (!thePlayer.onGround || thePlayer.isOnLadder || thePlayer.isInWeb || thePlayer.isInWater ||
                thePlayer.isInLava || thePlayer.ridingEntity != null || entity.hurtTime > hurtTime ||
                handleEvents() || !msTimer.hasTimePassed(delay.toLong())
            )
                return

            val (x, y, z) = thePlayer

            when (mode.lowercase()) {
                "packet" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.0625, z, true),
                        C04PacketPlayerPosition(x, y, z, false)
                    )
                    thePlayer.onCriticalHit(entity)
                }

                "ncppacket" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.11, z, false),
                        C04PacketPlayerPosition(x, y + 0.1100013579, z, false),
                        C04PacketPlayerPosition(x, y + 0.0000013579, z, false)
                    )
                    mc.thePlayer.onCriticalHit(entity)
                }

                "bmcpacket" -> {
                    sendCriticalPacket(yOffset = 0.0825080378093, ground = false)
                    sendCriticalPacket(yOffset = 0.0215634532004, ground = false)
                    sendCriticalPacket(yOffset = 0.1040220332227, ground = false)
                }

                "blocksmc" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.001091981, z, true),
                        C04PacketPlayerPosition(x, y, z, false)
                    )
                }
                "bmcsmart" -> {
                    attacks++
                    if (attacks > 4) {
                        attacks = 0

                        sendCriticalPacket(yOffset = 0.001, ground = true)
                        sendCriticalPacket(ground = false)
                    }
                }
                "blocksmc2" -> {
                    if (thePlayer.ticksExisted % 4 == 0) {
                        sendPackets(
                            C04PacketPlayerPosition(x, y + 0.0011, z, true),
                            C04PacketPlayerPosition(x, y, z, false)
                        )
                    }
                }

                "hop" -> {
                    thePlayer.motionY = 0.1
                    thePlayer.fallDistance = 0.1f
                    thePlayer.onGround = false
                }

                "tphop" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.02, z, false),
                        C04PacketPlayerPosition(x, y + 0.01, z, false)
                    )
                    thePlayer.setPosition(x, y + 0.01, z)
                }

                "jump" -> thePlayer.motionY = 0.42
                "lowjump" -> thePlayer.motionY = 0.3425
                "custommotion" -> thePlayer.motionY = customMotionY.toDouble()
                "visual" -> thePlayer.onCriticalHit(entity)


            }

            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mode == "HuaYuTing") {
            if (packet is C02PacketUseEntity) {
                val wrapped = packet

                if (wrapped.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    attacking = true
                }
            }
        }
        if (mode == "BMCBlatant") {
            if (packet is C02PacketUseEntity && (packet as C02PacketUseEntity).getAction() == C02PacketUseEntity.Action.ATTACK) {
                if (mc.thePlayer.onGround && onGroundTicks % 2 == 0 && this.tick > 0) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0522, mc.thePlayer.posZ);
                    MovementUtils.strafe(0.2F);
                }
            }
        }
        if (packet is C03PacketPlayer && mode == "NoGround")
            packet.onGround = false

    }

    private fun isEatingGapple(): Boolean {
        if (mc.thePlayer?.isUsingItem != true) return false
        val item = mc.thePlayer.itemInUse?.item ?: return false
        return item is ItemAppleGold
    }

    private fun cantCrit(targetEntity: EntityLivingBase): Boolean {
        val player = mc.thePlayer
        return player.isOnLadder || player.isInWeb || player.isInWater || player.isInLava || player.ridingEntity != null || targetEntity.hurtTime > hurtTime || targetEntity.health <= 0.0f || isEatingGapple() || gappleNoGround
    }

    private fun resetStuck() {
        skiptick = 0
    }

    override val tag
        get() = mode
}
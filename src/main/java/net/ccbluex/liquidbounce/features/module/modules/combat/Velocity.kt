/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.utils.MovementUtils.isOnGround
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.realMotionX
import net.ccbluex.liquidbounce.utils.realMotionY
import net.ccbluex.liquidbounce.utils.realMotionZ
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockSoulSand
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.play.server.S3BPacketScoreboardObjective
import net.minecraft.network.play.server.S3CPacketUpdateScore
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard
import net.minecraft.util.*
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.world.WorldSettings
import scala.sys.BooleanProp
import kotlin.math.*
import kotlin.random.Random


object Velocity : Module("Velocity", Category.COMBAT) {

    /**
     * OPTIONS
     */
    private val mode by choices(
        "Mode", arrayOf(
            "Simple", "Advanced", "AAC", "AACPush", "AACZero", "AACv4",
            "Reverse", "SmoothReverse", "Jump", "Glitch", "Legit",
            "GhostBlock", "Vulcan", "S32Packet", "MatrixReduce",
            "IntaveReduce", "GrimReduce", "Delay", "GrimC03", "GrimCombat", "Hypixel", "HypixelAir",
            "Click", "BlocksMC", "IntaveA", "IntaveB", "Polar", "Block", "Prediction"
        ), "Simple"
    )
    private val GrimReduceFactor by floatValue("Factor", 0.6f, 0.0f..1.0f) { mode == "GrimReduce" }
    private val GrimMinHurtTime by intValue("MinHurtTime", 5, 0..10) { mode == "GrimReduce" }
    private val GrimMaxHurtTime by intValue("MaxHurtTime", 10, 0..20) { mode == "GrimReduce" }
    private val GrimOnGround by _boolean("OnlyGround",false) { mode == "GrimReduce" }
    private val horizontal by floatValue("Horizontal", 0F, -1F..1F) { mode in arrayOf("Simple", "AAC", "Legit") }
    private val vertical by floatValue("Vertical", 0F, -1F..1F) { mode in arrayOf("Simple", "Legit") }
    private val horizontalGround by intValue("HorizontalOnGround", 0, 0..100) { mode == "Advanced" }
    private val verticalGround by intValue("VerticalOnGround", 0, 0..100) { mode == "Advanced" }
    private val horizontalInAir by intValue("HorizontalInAir", 0, 0..100) { mode == "Advanced" }
    private val verticalInAir by intValue("VerticalInAir", 0, 0..100) { mode == "Advanced" }
    // Block
    private val renderSlot by _boolean("RenderSlot", true) { mode == "Block" }
    // Reverse
    private val reverseStrength by floatValue("ReverseStrength", 1F, 0.1F..1F) { mode == "Reverse" }
    private val reverse2Strength by floatValue("SmoothReverseStrength", 0.05F, 0.02F..0.1F) { mode == "SmoothReverse" }

    private val onLook by _boolean("onLook", false) { mode in arrayOf("Reverse", "SmoothReverse") }
    private val range by floatValue("Range", 3.0F, 1F..5.0F) {
        onLook && mode in arrayOf("Reverse", "SmoothReverse")
    }
    private val maxAngleDifference by floatValue("MaxAngleDifference", 45.0f, 5.0f..90f) {
        onLook && mode in arrayOf("Reverse", "SmoothReverse")
    }

    // AAC Push
    private val aacPushXZReducer by floatValue("AACPushXZReducer", 2F, 1F..3F) { mode == "AACPush" }
    private val aacPushYReducer by _boolean("AACPushYReducer", true) { mode == "AACPush" }

    // AAC v4
    private val aacv4MotionReducer by floatValue("AACv4MotionReducer", 0.62F, 0F..1F) { mode == "AACv4" }

    // Legit
    private val legitDisableInAir by _boolean("DisableInAir", true) { mode == "Legit" }

    // Chance
    private val chance by intValue("Chance", 100, 0..100) { mode == "Jump" || mode == "Legit" }

    // Jump
    private val jumpCooldownMode by choices("JumpCooldownMode", arrayOf("Ticks", "ReceivedHits"), "Ticks")
    { mode == "Jump" }
    private val ticksUntilJump by intValue("TicksUntilJump", 4, 0..20)
    { jumpCooldownMode == "Ticks" && mode == "Jump" }
    private val hitsUntilJump by intValue("ReceivedHitsUntilJump", 2, 0..5)
    { jumpCooldownMode == "ReceivedHits" && mode == "Jump" }

    // Ghost Block
    private val hurtTimeRange by intRange("HurtTime", 1..9, 1..10) {
        mode == "GhostBlock"
    }
    var polarHurtTime = Random.nextInt(8, 10)
    // Delay
    private val spoofDelay by intValue("SpoofDelay", 500, 0..5000) { mode == "Delay" }
    var delayMode = false

    // IntaveReduce
    private val reduceFactor by floatValue("Factor", 0.6f, 0.6f..1f) { mode == "IntaveReduce" || mode == "IntaveA" }
    private val hurtTime by intValue("HurtTime", 9, 1..10) { mode == "IntaveReduce" }

    private val pauseOnExplosion by _boolean("PauseOnExplosion", true)
    private val ticksToPause by intValue("TicksToPause", 20, 1..50) { pauseOnExplosion }

    // TODO: Could this be useful in other modes? (Jump?)
    // Limits
    private val limitMaxMotionValue = _boolean("LimitMaxMotion", false) { mode == "Simple" }
    private val maxXZMotion by floatValue("MaxXZMotion", 0.4f, 0f..1.9f) { limitMaxMotionValue.isActive() }
    private val maxYMotion by floatValue("MaxYMotion", 0.36f, 0f..0.46f) { limitMaxMotionValue.isActive() }
    //0.00075 is added silently

    // Vanilla XZ limits
    // Non-KB: 0.4 (no sprint), 0.9 (sprint)
    // KB 1: 0.9 (no sprint), 1.4 (sprint)
    // KB 2: 1.4 (no sprint), 1.9 (sprint)
    // Vanilla Y limits
    // 0.36075 (no sprint), 0.46075 (sprint)

    private val clicks by intRange("Clicks", 3..5, 1..20) { mode == "Click" }
    private val hurtTimeToClick by intValue("HurtTimeToClick", 10, 0..10) { mode == "Click" }
    private val whenFacingEnemyOnly by _boolean("WhenFacingEnemyOnly", true) { mode == "Click" }
    private val ignoreBlocking by _boolean("IgnoreBlocking", false) { mode == "Click" }
    private val clickRange by floatValue("ClickRange", 3f, 1f..6f) { mode == "Click" }
    private val swingMode by choices("SwingMode", arrayOf("Off", "Normal", "Packet"), "Normal") { mode == "Click" }

    private val grimrange by floatValue("Range", 3.5f, 0f..6f) { mode == "GrimCombat" }
    private val attackCountValue by intValue("Attack Counts", 5, 1..16) { mode == "GrimCombat" }

    // pit 调成攻击发包调成6

    private val fireCheckValue by _boolean("FireCheck", false) { mode == "GrimCombat" }
    private val waterCheckValue by _boolean("WaterCheck", false) { mode == "GrimCombat" }
    private val fallCheckValue by _boolean("FallCheck", false) { mode == "GrimCombat" }
    private val consumecheck by _boolean("ConsumableCheck", false) { mode == "GrimCombat" }
    private val raycastValue by _boolean("Ray cast", false) { mode == "GrimCombat" }

    /**
     * VALUES
     */
    private val velocityTimer = MSTimer()
    private var hasReceivedVelocity = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Jump
    private var limitUntilJump = 0

    // IntaveReduce
    private var intaveTick = 0
    private var lastAttackTime = 0L
    private var intaveDamageTick = 0

    // Delay
    private val packets = LinkedHashMap<Packet<*>, Long>()

    // Grim
    private var timerTicks = 0

    // Vulcan
    private var transaction = false

    // Hypixel
    private var absorbedVelocity = false

    // Pause On Explosion
    private var pauseTicks = 0

    // Prediction
    var buffering = false
    var releasing = false
    var bufferedPackets : MutableList<Packet<*>> = mutableListOf()
    var kbYawDeg : Float = 0.0F
    var kbX : Double = 0.0
    var kbZ : Double = 0.0
    var forceJumpOnce = false
    var ticksSinceBufferStart : Int = 0

    //    Grim
    var velocityInput: Boolean = false
    private const val grim_1_17Velocity = false
    private var attacked = false
    private var reduceXZ = 1.00000
    private const val flags = 0
    var velX = 0
    var velY = 0
    var velZ = 0
    override val tag
        get() = if (mode == "Simple" || mode == "Legit") {
            val horizontalPercentage = (horizontal * 100).toInt()
            val verticalPercentage = (vertical * 100).toInt()

            "$horizontalPercentage% $verticalPercentage%"
        } else mode

    override fun onDisable() {
        pauseTicks = 0
        mc.thePlayer?.speedInAir = 0.02F
        timerTicks = 0
        reset()
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {

    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInLiquid || thePlayer.isInWeb || thePlayer.isDead)
            return

        when (mode.lowercase()) {

            "glitch" -> {
                thePlayer.noClip = hasReceivedVelocity

                if (thePlayer.hurtTime == 7)
                    thePlayer.motionY = 0.4

                hasReceivedVelocity = false
            }

            "reverse" -> {
                val nearbyEntity = getNearestEntityInRange()

                if (!hasReceivedVelocity)
                    return

                if (nearbyEntity != null) {
                    if (!thePlayer.onGround) {
                        if (onLook && !EntityUtils.isLookingOnEntities(nearbyEntity, maxAngleDifference.toDouble())) {
                            return
                        }

                        speed *= reverseStrength
                    } else if (velocityTimer.hasTimePassed(80))
                        hasReceivedVelocity = false
                }
            }

            "smoothreverse" -> {
                val nearbyEntity = getNearestEntityInRange()

                if (hasReceivedVelocity) {
                    if (nearbyEntity == null) {
                        thePlayer.speedInAir = 0.02F
                        reverseHurt = false
                    } else {
                        if (onLook && !EntityUtils.isLookingOnEntities(nearbyEntity, maxAngleDifference.toDouble())) {
                            hasReceivedVelocity = false
                            thePlayer.speedInAir = 0.02F
                            reverseHurt = false
                        } else {
                            if (thePlayer.hurtTime > 0) {
                                reverseHurt = true
                            }

                            if (!thePlayer.onGround) {
                                thePlayer.speedInAir = if (reverseHurt) reverse2Strength else 0.02F
                            } else if (velocityTimer.hasTimePassed(80)) {
                                hasReceivedVelocity = false
                                thePlayer.speedInAir = 0.02F
                                reverseHurt = false
                            }
                        }
                    }
                }
            }

            "aac" -> if (hasReceivedVelocity && velocityTimer.hasTimePassed(80)) {
                thePlayer.motionX *= horizontal
                thePlayer.motionZ *= horizontal
                //mc.thePlayer.motionY *= vertical ?
                hasReceivedVelocity = false
            }

            "aacv4" ->
                if (thePlayer.hurtTime > 0 && !thePlayer.onGround) {
                    val reduce = aacv4MotionReducer
                    thePlayer.motionX *= reduce
                    thePlayer.motionZ *= reduce
                }

            "aacpush" -> {
                if (jump) {
                    if (thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (thePlayer.hurtTime > 0 && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0)
                        thePlayer.onGround = true

                    // Reduce Y
                    if (thePlayer.hurtResistantTime > 0 && aacPushYReducer && !handleEvents())
                        thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducer

                    thePlayer.motionX /= reduce
                    thePlayer.motionZ /= reduce
                }
            }

            "aaczero" ->
                if (thePlayer.hurtTime > 0) {
                    if (!hasReceivedVelocity || thePlayer.onGround || thePlayer.fallDistance > 2F)
                        return

                    thePlayer.motionY -= 1.0
                    thePlayer.isAirBorne = true
                    thePlayer.onGround = true
                } else
                    hasReceivedVelocity = false

            "legit" -> {
                if (legitDisableInAir && !isOnGround(0.5))
                    return

                if (mc.thePlayer.maxHurtResistantTime != mc.thePlayer.hurtResistantTime || mc.thePlayer.maxHurtResistantTime == 0)
                    return

                if (nextInt(endExclusive = 100) < chance) {
                    val horizontal = horizontal / 100f
                    val vertical = vertical / 100f

                    thePlayer.motionX *= horizontal.toDouble()
                    thePlayer.motionZ *= horizontal.toDouble()
                    thePlayer.motionY *= vertical.toDouble()
                }
            }

            "intavereduce" -> {
                if (!hasReceivedVelocity) return
                intaveTick++

                if (mc.thePlayer.hurtTime == 2) {
                    intaveDamageTick++
                    if (thePlayer.onGround && intaveTick % 2 == 0 && intaveDamageTick <= 10) {
                        thePlayer.tryJump()
                        intaveTick = 0
                    }
                    hasReceivedVelocity = false
                }
            }

            "hypixel" -> {
                if (hasReceivedVelocity && thePlayer.onGround) {
                    absorbedVelocity = false
                }
            }

            "hypixelair" -> {
                if (hasReceivedVelocity) {
                    if (thePlayer.onGround) {
                        thePlayer.tryJump()
                    }
                    hasReceivedVelocity = false
                }
            }

            "grimcombat" -> {
                if (attacked) {
                        //The velocity mode 1.8.9 ok!
                        if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                            mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10)
                            mc.thePlayer.isSprinting = false
                        }
                }

            }

            "intavea" -> {
                if (hasReceivedVelocity) {
                    intaveTick++
                    if (thePlayer.hurtTime == 2) {
                        intaveDamageTick++
                        if (thePlayer.onGround && intaveTick % 2 == 0 && intaveDamageTick <= 10) {
                            thePlayer.tryJump()
                            intaveTick = 0
                        }
                        hasReceivedVelocity = false
                    }
                }
            }

            "intaveb" -> {
                val target = getNearestEntityInRange()
                if (target != null) {
                    if (thePlayer.hurtTime > 0) {
                        thePlayer.isSprinting = false
                        if (thePlayer.hurtTime <= 6) {
                            val yaw = MathHelper.wrapAngleTo180_float(currentRotation?.yaw ?: thePlayer.rotationYaw)
                            thePlayer.motionX = -sin(yaw * (Math.PI / 180)) * 0.02
                            thePlayer.motionZ = cos(yaw * (Math.PI / 180)) * 0.02
                        }
                    }
                }
            }

            "polar" -> {

                if (thePlayer.hurtTime == polarHurtTime) {
                    thePlayer.tryJump()
                    polarHurtTime = nextInt(8, 10)
                }
            }
        }
    }

    /**
     * @see net.minecraft.entity.player.EntityPlayer.attackTargetEntityWithCurrentItem
     * Lines 1035 and 1058
     *
     * Minecraft only applies motion slow-down when you are sprinting and attacking, once per tick.
     * An example scenario: If you perform a mouse double-click on an entity, the game will only accept the first attack.
     *
     * This is where we come in clutch by making the player always sprint before dropping
     *
     * [clicks] amount of hits on the target [entity]
     *
     * We also explicitly-cast the player as an [Entity] to avoid triggering any other things caused from setting new sprint status.
     *
     * @see net.minecraft.client.entity.EntityPlayerSP.setSprinting
     * @see net.minecraft.entity.EntityLivingBase.setSprinting
     */
    @EventTarget
    fun onGameTick(event: GameTickEvent){
        val thePlayer = mc.thePlayer ?: return
        if ((thePlayer.hurtTime in GrimMinHurtTime..GrimMaxHurtTime) && mode == "GrimReduce") {
            lastAttackTime = System.currentTimeMillis()
        }
        mc.theWorld ?: return
        when (mode) {
            "Prediction" -> {
                if (!buffering) {
                    ticksSinceBufferStart = 0
                    return
                }

                ticksSinceBufferStart++

                val playerYaw = wrapTo180(mc.thePlayer.rotationYaw.toDouble())
                val diff = abs(playerYaw - kbYawDeg)

                if ((diff <= 20 || diff >= 340) || mc.thePlayer.onGround || ticksSinceBufferStart > 25){
                    releasing = true
                    buffering = false

                    for (packet in bufferedPackets){
                        if (!isStatusPacket(packet)) {
                            // 操你妈傻逼Kotlin
                            @Suppress("UNCHECKED_CAST")
                            (packet as Packet<NetHandlerPlayClient>).processPacket(mc.netHandler)
                        }
                    }
                    bufferedPackets.clear()
                    releasing = false
                    ticksSinceBufferStart = 0
                }
            }
        }
        if (mode != "Click" || thePlayer.hurtTime != hurtTimeToClick || ignoreBlocking && (thePlayer.isBlocking || KillAura.blockStatus))
            return

        var entity = mc.objectMouseOver?.entityHit

        if (entity == null) {
            if (whenFacingEnemyOnly) {
                var result: Entity? = null

                RaycastUtils.runWithModifiedRaycastResult(
                    currentRotation ?: thePlayer.rotation,
                    clickRange.toDouble(),
                    0.0
                ) {
                    result = it.entityHit?.takeIf { EntityUtils.isSelected(it, true) }
                }

                entity = result
            } else getNearestEntityInRange(clickRange)?.takeIf { EntityUtils.isSelected(it, true) }
        }

        entity ?: return

        val swingHand = {
            when (swingMode.lowercase()) {
                "normal" -> thePlayer.swingItem()
                "packet" -> sendPacket(C0APacketAnimation())
            }
        }

        repeat(clicks.random()) {
            thePlayer.attackEntityWithModifiedSprint(entity, true) { swingHand() }
        }
    }
    @EventTarget
    fun onAttack(event: AttackEvent) {
        val player = mc.thePlayer ?: return

        when (mode.lowercase()) {
            "intavea" -> {
                if (player.hurtTime == hurtTime && System.currentTimeMillis() - lastAttackTime <= 8000) {
                    player.motionX *= reduceFactor
                    player.motionZ *= reduceFactor
                }

                lastAttackTime = System.currentTimeMillis()
            }

            "intavereduce" -> {
                if (player.hurtTime == hurtTime && System.currentTimeMillis() - lastAttackTime <= 8000) {
                    player.motionX *= reduceFactor
                    player.motionZ *= reduceFactor
                }

                lastAttackTime = System.currentTimeMillis()
            }

            "grimcombat" -> {
                if (attacked) {
                        mc.netHandler.networkManager.sendPacket(C0APacketAnimation())
                        mc.netHandler.networkManager.sendPacket(
                            C02PacketUseEntity(
                                event.targetEntity,
                                C02PacketUseEntity.Action.ATTACK
                            )
                        )
                }
            }
        }
    }

    private fun checkAir(blockPos: BlockPos): Boolean {
        val world = mc.theWorld ?: return false

        if (!world.isAirBlock(blockPos)) {
            return false
        }

        timerTicks = 20

        sendPackets(
            C03PacketPlayer(true),
            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, DOWN)
        )

        world.setBlockToAir(blockPos)

        return true
    }

    // TODO: Recode
    private fun getDirection(): Double {
        var moveYaw = mc.thePlayer.rotationYaw
        when {
            mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing == 0f -> {
                moveYaw += if (mc.thePlayer.moveForward > 0) 0 else 180
            }

            mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing != 0f -> {
                if (mc.thePlayer.moveForward > 0) moveYaw += if (mc.thePlayer.moveStrafing > 0) -45 else 45 else moveYaw -= if (mc.thePlayer.moveStrafing > 0) -45 else 45
                moveYaw += if (mc.thePlayer.moveForward > 0) 0 else 180
            }

            mc.thePlayer.moveStrafing != 0f && mc.thePlayer.moveForward == 0f -> {
                moveYaw += if (mc.thePlayer.moveStrafing > 0) -90 else 90
            }
        }
        return Math.floorMod(moveYaw.toInt(), 360).toDouble()
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.theWorld == null || mc.netHandler == null) {
            return
        }
        val thePlayer = mc.thePlayer ?: return

        val packet = event.packet

        if (!handleEvents())
            return

        if (pauseTicks > 0) {
            pauseTicks--
            return
        }

        if (event.isCancelled)
            return

        if (event.eventType.stateName == "RECEIVE") {
            if (releasing) {
                return
            }

            if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
                if (packet.motionY > 0 || mc.thePlayer.fallDistance <= 14 || mc.thePlayer.hurtTime <= 1) {
                    forceJumpOnce = true
                }

                if (!mc.thePlayer.onGround && mc.thePlayer.hurtTime > 4){
                    buffering = true
                    ticksSinceBufferStart = 0
                    kbX = packet.motionX / 8000.0
                    kbZ = packet.motionZ / 8000.0
                    kbYawDeg = wrapTo180(atan2deg(kbZ, kbX)).toFloat()

                    event.cancelEvent()
                    bufferedPackets.add(packet)
                    return
                }
            }

            if (buffering && (packet is S27PacketExplosion || packet is S08PacketPlayerPosLook)) {
                event.cancelEvent()
                bufferedPackets.add(packet)
                return
            }
            if (buffering && packet is NetHandlerPlayServer && !excludedSpecial(packet)) {
                event.cancelEvent()
                bufferedPackets.add(packet)
            }
        }
        // Also from Phantom Injection
        // 感谢dev发我的src。我爱你。
        if (mode == "Block") {
            if (packet is S12PacketEntityVelocity) {
                if (packet.entityID == thePlayer.entityId) {
                    val x = packet.motionX / 8000.0
                    val y = packet.motionY / 8000.0
                    val z = packet.motionZ / 8000.0

                    val vec3 = Vec3(x, y, z)
                    val vec2 = RotationUtils.aimToPoint(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks),vec3)
                    val yaw = RotationUtils.shortestYaw(thePlayer.rotationYaw, vec2.x)
                    mc.thePlayer.rotationYaw = yaw
                }
            }
        }
        if (mode == "GrimReduce") {
            if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer?.entityId) {
                val player = mc.thePlayer ?: return
                if (GrimOnGround && !player.onGround) return

                if (player.hurtTime in GrimMinHurtTime..GrimMaxHurtTime) {
                    packet.motionX = (packet.motionX * GrimReduceFactor).toInt()
                    packet.motionY = (packet.motionY * GrimReduceFactor).toInt()
                    packet.motionZ = (packet.motionZ * GrimReduceFactor).toInt()
                }
            } else if (packet is S27PacketExplosion) {
                event.cancelEvent()
            }
        }
        if (mode.lowercase() == "grimcombat") {
            if (mc.thePlayer.isDead) return
            if (mc.currentScreen is GuiGameOver) return
            if (mc.playerController.currentGameType === WorldSettings.GameType.SPECTATOR) return
            if (mc.thePlayer.isOnLadder) return
            if (mc.thePlayer.isBurning && fireCheckValue) return
            if (mc.thePlayer.isInWater && waterCheckValue) return
            if (mc.thePlayer.fallDistance > 1.5 && fallCheckValue) return
            if (mc.thePlayer.isEating && consumecheck) return
            if (soulSandCheck()) return
            if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                //            chat("触发反击退但还没攻击")
                val s12 = (event.packet as S12PacketEntityVelocity)
                val horizontalStrength =
                    kotlin.math.sqrt(s12.getMotionX().toDouble().pow(2) + s12.getMotionZ().toDouble().pow(2))
                if (horizontalStrength <= 1000) return
                val mouse = mc.objectMouseOver
                velocityInput = true
                var entity: Entity? = null
                reduceXZ = 1.0

                if (mouse.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mouse.entityHit is EntityLivingBase && mc.thePlayer.getDistanceToEntityBox(
                        mouse.entityHit
                    ) <= KillAura.range
                ) {
                    entity = mouse.entityHit
                }

                if (entity == null && !raycastValue) {
                    val target: Entity? = KillAura.target
                    if (target != null && mc.thePlayer.getDistanceToEntityBox(target) <= grimrange) {
                        entity = KillAura.target
                    }
                }

                val state = mc.thePlayer.serverSprintState
                if (entity != null) {
                    if (!state) {
                        sendPackets(C0BPacketEntityAction(mc.thePlayer, START_SPRINTING))
                    }
                    val count = attackCountValue
                    for (i in 1..count) {
                            mc.netHandler.networkManager.sendPacket(C0APacketAnimation())
                            mc.netHandler.networkManager.sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
                    }
                    if (!state) {
                        sendPackets(C0BPacketEntityAction(mc.thePlayer, STOP_SPRINTING))
                    }
                    velX = event.packet.motionX
                    velY = event.packet.motionY
                    velZ = event.packet.motionZ
                    attacked = true
                    event.cancelEvent()
                }
            }
        }


        if ((packet is S12PacketEntityVelocity && thePlayer.entityId == packet.entityID && packet.motionY > 0 && (packet.motionX != 0 || packet.motionZ != 0))
            || (packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                    && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0))
        ) {
            velocityTimer.reset()

            if (pauseOnExplosion && packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0)
            ) {
                pauseTicks = ticksToPause
            }

            when (mode.lowercase()) {
                "simple" -> handleVelocity(event)

                "advanced" -> handleVelocity(event)

                "aac", "reverse", "smoothreverse", "aaczero", "ghostblock", "intavereduce" -> hasReceivedVelocity = true

                "jump" -> {
                    // TODO: Recode and make all velocity modes support velocity direction checks
                    var packetDirection = 0.0
                    when (packet) {
                        is S12PacketEntityVelocity -> {
                            if (packet.entityID != thePlayer.entityId) return

                            val motionX = packet.motionX.toDouble()
                            val motionZ = packet.motionZ.toDouble()

                            packetDirection = atan2(motionX, motionZ)
                        }

                        is S27PacketExplosion -> {
                            val motionX = thePlayer.motionX + packet.field_149152_f
                            val motionZ = thePlayer.motionZ + packet.field_149159_h

                            packetDirection = atan2(motionX, motionZ)
                        }
                    }
                    val degreePlayer = getDirection()
                    val degreePacket = Math.floorMod(packetDirection.toDegrees().toInt(), 360).toDouble()
                    var angle = abs(degreePacket + degreePlayer)
                    val threshold = 120.0
                    angle = Math.floorMod(angle.toInt(), 360).toDouble()
                    val inRange = angle in 180 - threshold / 2..180 + threshold / 2
                    if (inRange)
                        hasReceivedVelocity = true
                }

                "glitch" -> {
                    if (!thePlayer.onGround)
                        return

                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "matrixreduce" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        packet.motionX = (packet.getMotionX() * 0.33).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.33).toInt()

                        if (thePlayer.onGround) {
                            packet.motionX = (packet.getMotionX() * 0.86).toInt()
                            packet.motionZ = (packet.getMotionZ() * 0.86).toInt()
                        }
                    }
                }

                // Credit: @LiquidSquid / Ported from NextGen
                "blocksmc" -> {
                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        hasReceivedVelocity = true
                        event.cancelEvent()

                        sendPacket(C0BPacketEntityAction(thePlayer, START_SNEAKING))
                        sendPacket(C0BPacketEntityAction(thePlayer, STOP_SNEAKING))
                    }
                }

                "grimc03" -> {
                    // Checks to prevent from getting flagged (BadPacketsE)
                    if (thePlayer.isMoving) {
                        hasReceivedVelocity = true
                        event.cancelEvent()
                    }
                }

                "hypixel" -> {
                    hasReceivedVelocity = true
                    if (!thePlayer.onGround) {
                        if (!absorbedVelocity) {
                            event.cancelEvent()
                            absorbedVelocity = true
                            return
                        }
                    }

                    if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
                        packet.motionX = (thePlayer.motionX * 8000).toInt()
                        packet.motionZ = (thePlayer.motionZ * 8000).toInt()
                    }
                }

                "hypixelair" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "vulcan" -> {
                    event.cancelEvent()
                }

                "s32packet" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }
                
                "prediction" -> {
                }

            }
        }

        if (mode == "BlocksMC" && hasReceivedVelocity) {
            if (packet is C0BPacketEntityAction) {
                hasReceivedVelocity = false
                event.cancelEvent()
            }
        }

        if (mode == "Vulcan") {
            if (handleEvents() && Disabler.verusCombat && (!Disabler.onlyCombat || Disabler.isOnCombat)) return

            if (packet is S32PacketConfirmTransaction) {
                event.cancelEvent()
                sendPacket(
                    C0FPacketConfirmTransaction(
                        if (transaction) 1 else -1,
                        if (transaction) -1 else 1,
                        transaction
                    ), false
                )
                transaction = !transaction
            }
        }

        if (mode == "S32Packet" && packet is S32PacketConfirmTransaction) {

            if (!hasReceivedVelocity)
                return

            event.cancelEvent()
            hasReceivedVelocity = false
        }
    }

    /**
     * Tick Event (Abuse Timer Balance)
     */
    @EventTarget
    fun onTick(event: GameTickEvent) {
        val player = mc.thePlayer ?: return
        if (mode == "Block") {
            if (mc.thePlayer.hurtTime > 5) {
                val block = InventoryUtils.findBlockInHotbar()
                if (block != null) {
                    SilentHotbar.selectSlotSilently(
                        this,
                        block,
                        immediate = true,
                        render = renderSlot,
                        resetManually = true
                    )
                }
                var i = 60
                while (i >= 45) {
                    mc.thePlayer.rotationPitch = i.toFloat()
                    val mop = mc.objectMouseOver
                    if (mop?.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        mc.playerController.onPlayerRightClick(
                            mc.thePlayer,
                            mc.theWorld,
                            mc.thePlayer.heldItem,
                            mop.blockPos,
                            mop.sideHit,
                            mop.hitVec
                        )
                    }
                    i -= 5
                }
            } else {
                RotationUtils.resetRotation()
            }

        }
        if (mode != "GrimC03")
            return

        // Timer Abuse (https://github.com/CCBlueX/LiquidBounce/issues/2519)
        if (timerTicks > 0 && mc.timer.timerSpeed <= 1) {
            val timerSpeed = 0.8f + (0.2f * (20 - timerTicks) / 20)
            mc.timer.timerSpeed = timerSpeed.coerceAtMost(1f)
            --timerTicks
        } else if (mc.timer.timerSpeed <= 1) {
            mc.timer.timerSpeed = 1f
        }

        if (hasReceivedVelocity) {
            val pos = BlockPos(player.posX, player.posY, player.posZ)

            if (checkAir(pos))
                hasReceivedVelocity = false
        }
    }

    /**
     * Delay Mode
     */
    @EventTarget
    fun onDelayPacket(event: PacketEvent){
        val packet = event.packet

        if (event.isCancelled)
            return

        if (mode == "Delay") {
            if (packet is S32PacketConfirmTransaction || packet is S12PacketEntityVelocity) {

                event.cancelEvent()

                // Delaying packet like PingSpoof
                synchronized(packets) {
                    packets[packet] = System.currentTimeMillis()
                }
            }
            delayMode = true
        } else {
            delayMode = false
        }
    }

    /**
     * Reset on world change
     */
    @EventTarget
    fun onWorld(event: WorldEvent){
        packets.clear()
        if (mode == "GrimReduce") lastAttackTime = 0L
    }
    @EventTarget
    fun onGameLoop(event: GameLoopEvent){
        if (mode == "Delay")
            sendPacketsByOrder(false)
    }

    private fun sendPacketsByOrder(velocity: Boolean) {
        synchronized(packets) {
            packets.entries.removeAll { (packet, timestamp) ->
                if (velocity || timestamp <= System.currentTimeMillis() - spoofDelay) {
                    PacketUtils.schedulePacketProcess(packet)
                    true
                } else false
            }
        }
    }

    private fun reset() {
        sendPacketsByOrder(true)

        packets.clear()

        velocityInput = false
        attacked = false
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val thePlayer = mc.thePlayer

        if (thePlayer == null || thePlayer.isInLiquid || thePlayer.isInWeb)
            return

        when (mode.lowercase()) {
            "aacpush" -> {
                jump = true

                if (!thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }

            "aaczero" ->
                if (thePlayer.hurtTime > 0)
                    event.cancelEvent()
        }
    }
    @EventTarget
    fun onStrafe(event: StrafeEvent){
        val player = mc.thePlayer ?: return

        if (mode == "Jump" && hasReceivedVelocity) {
            if (!player.isJumping && nextInt(endExclusive = 100) < chance && shouldJump() && player.isSprinting && player.onGround && player.hurtTime == 9) {
                player.tryJump()
                limitUntilJump = 0
            }
            hasReceivedVelocity = false
            return
        }

        when (jumpCooldownMode.lowercase()) {
            "ticks" -> limitUntilJump++
            "receivedhits" -> if (player.hurtTime == 9) limitUntilJump++
        }
    }
    @EventTarget
    fun onBlockBB(event: BlockBBEvent){
        val player = mc.thePlayer ?: return

        if (mode == "GhostBlock") {
            if (hasReceivedVelocity) {
                if (player.hurtTime in hurtTimeRange) {
                    // Check if there is air exactly 1 level above the player's Y position
                    if (event.block is BlockAir && event.y == mc.thePlayer.posY.toInt() + 1) {
                        event.boundingBox = AxisAlignedBB(
                            event.x.toDouble(),
                            event.y.toDouble(),
                            event.z.toDouble(),
                            event.x + 1.0,
                            event.y + 1.0,
                            event.z + 1.0
                        )
                    }
                } else if (player.hurtTime == 0) {
                    hasReceivedVelocity = false
                }
            }
        }
    }
    @EventTarget
    fun onMovementInput(event: MovementInputEvent) {
        if (forceJumpOnce) {
            event.originalInput.jump = true
            forceJumpOnce = false
        }
        if (buffering || releasing) {
            forceJumpOnce = false
        }
    }
    private fun shouldJump() = when (jumpCooldownMode.lowercase()) {
        "ticks" -> limitUntilJump >= ticksUntilJump
        "receivedhits" -> limitUntilJump >= hitsUntilJump
        else -> false
    }

    private fun handleVelocity(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            // Always cancel event and handle motion from here
            event.cancelEvent()
            when (mode) {
                "Simple" -> {
                    if (horizontal == 0f && vertical == 0f)
                        return

                    // Don't modify player's motionXZ when horizontal value is 0
                    if (horizontal != 0f) {
                        var motionX = packet.realMotionX
                        var motionZ = packet.realMotionZ

                        if (limitMaxMotionValue.get()) {
                            val distXZ = sqrt(motionX * motionX + motionZ * motionZ)

                            if (distXZ > maxXZMotion) {
                                val ratioXZ = maxXZMotion / distXZ

                                motionX *= ratioXZ
                                motionZ *= ratioXZ
                            }
                        }

                        mc.thePlayer.motionX = motionX * horizontal
                        mc.thePlayer.motionZ = motionZ * horizontal
                    }

                    // Don't modify player's motionY when vertical value is 0
                    if (vertical != 0f) {
                        var motionY = packet.realMotionY

                        if (limitMaxMotionValue.get())
                            motionY = motionY.coerceAtMost(maxYMotion + 0.00075)

                        mc.thePlayer.motionY = motionY * vertical
                    }
                }

                "Advanced" -> {
                    if (mc.thePlayer.onGround) {
                        if (horizontalGround == 0 && verticalGround == 0) {
                            return
                        }
                        if (horizontalGround != 0) {
                            val motionX = packet.realMotionX
                            val motionZ = packet.realMotionZ
                            mc.thePlayer.motionX = motionX * (horizontalGround / 100F)
                            mc.thePlayer.motionZ = motionZ * (horizontalGround / 100F)
                        }
                        if (verticalGround != 0) {
                            val motionY = packet.realMotionY
                            mc.thePlayer.motionY = motionY * (verticalGround / 100F)
                        }
                    } else {
                        if (horizontalInAir == 0 && verticalInAir == 0) {
                            return
                        }
                        if (horizontalInAir != 0) {
                            val motionX = packet.realMotionX
                            val motionZ = packet.realMotionZ
                            mc.thePlayer.motionX = motionX * (horizontalInAir / 100F)
                            mc.thePlayer.motionZ = motionZ * (horizontalInAir / 100F)
                        }
                        if (verticalInAir != 0) {
                            val motionY = packet.realMotionY
                            mc.thePlayer.motionY = motionY * (verticalInAir / 100F)
                        }
                    }
                }
            }

        } else if (packet is S27PacketExplosion) {
            if (mode == "Simple") {
                // Don't cancel explosions, modify them, they could change blocks in the world
                if (horizontal != 0f && vertical != 0f) {
                    packet.field_149152_f = 0f
                    packet.field_149153_g = 0f
                    packet.field_149159_h = 0f

                    return
                }

                // Unlike with S12PacketEntityVelocity explosion packet motions get added to player motion, doesn't replace it
                // Velocity might behave a bit differently, especially LimitMaxMotion
                packet.field_149152_f *= horizontal // motionX
                packet.field_149153_g *= vertical // motionY
                packet.field_149159_h *= horizontal // motionZ

                if (limitMaxMotionValue.get()) {
                    val distXZ =
                        sqrt(packet.field_149152_f * packet.field_149152_f + packet.field_149159_h * packet.field_149159_h)
                    val distY = packet.field_149153_g
                    val maxYMotion = maxYMotion + 0.00075f

                    if (distXZ > maxXZMotion) {
                        val ratioXZ = maxXZMotion / distXZ

                        packet.field_149152_f *= ratioXZ
                        packet.field_149159_h *= ratioXZ
                    }

                    if (distY > maxYMotion) {
                        packet.field_149153_g *= maxYMotion / distY
                    }
                }
            } else if (mode == "Advanced") {
                return
            }
        }
    }

    private fun getNearestEntityInRange(range: Float = this.range): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList.filter {
            EntityUtils.isSelected(it, true) && player.getDistanceToEntityBox(it) <= range
        }.minByOrNull { player.getDistanceToEntityBox(it) }
    }


    fun soulSandCheck(): Boolean {
        val par1AxisAlignedBB = Minecraft.getMinecraft().thePlayer.entityBoundingBox.contract(
            0.001, 0.001,
            0.001
        )
        val var4 = MathHelper.floor_double(par1AxisAlignedBB.minX)
        val var5 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0)
        val var6 = MathHelper.floor_double(par1AxisAlignedBB.minY)
        val var7 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0)
        val var8 = MathHelper.floor_double(par1AxisAlignedBB.minZ)
        val var9 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0)
        for (var11 in var4 until var5) {
            for (var12 in var6 until var7) {
                for (var13 in var8 until var9) {
                    val pos = BlockPos(var11, var12, var13)
                    val var14 = Minecraft.getMinecraft().theWorld.getBlockState(pos).block
                    if (var14 is BlockSoulSand) {
                        return true
                    }
                }
            }
        }
        return false
    }
}

fun atan2deg(y: Double, x: Double): Double {
    return Math.toDegrees(atan2(y, x))
}
fun wrapTo180(angle: Double): Double {
    var a = angle % 360.0
    if (a >= 180.0) a -= 360.0
    if (a < -180.0) a += 360.0
    return a
}
fun excludedSpecial(packet: Packet<*>): Boolean{
    if (packet is S3BPacketScoreboardObjective || packet is S3CPacketUpdateScore || packet is S3DPacketDisplayScoreboard) return false
    return true
}
fun isStatusPacket(packet: Packet<*>): Boolean {
    val className = packet.javaClass.name
    return className.startsWith("net.minecraft.network.status.server.")
}

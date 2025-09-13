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
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.MovementUtils.isOnGround
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockSoulSand
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import net.minecraft.network.play.server.*
import net.minecraft.util.*
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.world.WorldSettings
import kotlin.math.*
import kotlin.random.Random


object Velocity : Module("Velocity", Category.COMBAT) {

    /**
     * OPTIONS
     */
    private val mode by choices(
        "Mode", arrayOf(
            "Simple",
            "Advanced",
            "AAC",
            "AACPush",
            "AACZero",
            "AACv4",
            "Reverse",
            "SmoothReverse",
            "Jump",
            "Glitch",
            "Legit",
            "GhostBlock",
            "Vulcan",
            "S32Packet",
            "MatrixReduce",
            "IntaveReduce",
            "Delay",
            "GrimC03",
            "GrimCombat",
            "Hypixel",
            "HypixelAir",
            "Click",
            "BlocksMC",
            "IntaveA",
            "IntaveB",
            "Polar",
            "Block",
            "Prediction"
        ), "Simple"
    )
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
    private val jumpCooldownMode by choices(
        "JumpCooldownMode",
        arrayOf("Ticks", "ReceivedHits"),
        "Ticks"
    ) { mode == "Jump" }
    private val ticksUntilJump by intValue("TicksUntilJump", 4, 0..20) { jumpCooldownMode == "Ticks" && mode == "Jump" }
    private val hitsUntilJump by intValue(
        "ReceivedHitsUntilJump",
        2,
        0..5
    ) { jumpCooldownMode == "ReceivedHits" && mode == "Jump" }

    // Ghost Block
    private val hurtTimeRange by intRange("HurtTime", 1..9, 1..10) { mode == "GhostBlock" }
    var polarHurtTime = Random.nextInt(8, 10)

    // Delay
    private val spoofDelay by intValue("SpoofDelay", 500, 0..5000) { mode == "Delay" }
    var delayMode = false

    // IntaveReduce
    private val reduceFactor by floatValue("Factor", 0.6f, 0.6f..1f) { mode == "IntaveReduce" || mode == "IntaveA" }
    private val hurtTime by intValue("HurtTime", 9, 1..10) { mode == "IntaveReduce" }

    // Explosion
    private val pauseOnExplosion by _boolean("PauseOnExplosion", true)
    private val ticksToPause by intValue("TicksToPause", 20, 1..50) { pauseOnExplosion }

    // Limits
    private val limitMaxMotionValue = _boolean("LimitMaxMotion", false) { mode == "Simple" }
    private val maxXZMotion by floatValue("MaxXZMotion", 0.4f, 0f..1.9f) { limitMaxMotionValue.isActive() }
    private val maxYMotion by floatValue("MaxYMotion", 0.36f, 0f..0.46f) { limitMaxMotionValue.isActive() }

    // Click
    private val clicks by intRange("Clicks", 3..5, 1..20) { mode == "Click" }
    private val hurtTimeToClick by intValue("HurtTimeToClick", 10, 0..10) { mode == "Click" }
    private val whenFacingEnemyOnly by _boolean("WhenFacingEnemyOnly", true) { mode == "Click" }
    private val ignoreBlocking by _boolean("IgnoreBlocking", false) { mode == "Click" }
    private val clickRange by floatValue("ClickRange", 3f, 1f..6f) { mode == "Click" }
    private val swingMode by choices("SwingMode", arrayOf("Off", "Normal", "Packet"), "Normal") { mode == "Click" }

    // GrimCombat
    private val grimrange by floatValue("Range", 3.5f, 0f..6f) { mode == "GrimCombat" }
    private val attackCountValue by intValue("Attack Counts", 5, 1..16) { mode == "GrimCombat" }
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
    private var bufferedPackets: MutableList<Packet<*>> = mutableListOf()
    private var kbYawDeg: Float = 0.0F
    private var forceJumpOnce = false
    private var ticksSinceBufferStart: Int = 0

    // GrimCombat
    var velocityInput: Boolean = false
    private var attacked = false
    private var reduceXZ = 1.00000
    var velX = 0
    var velY = 0
    var velZ = 0

    override val tag
        get() = when (mode) {
            "Simple", "Legit" -> {
                val horizontalPercentage = (horizontal * 100).toInt()
                val verticalPercentage = (vertical * 100).toInt()
                "$horizontalPercentage% $verticalPercentage%"
            }

            else -> mode
        }

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
        reset()
    }

    override fun onEnable() {
        reset()
    }

    private fun reset() {
        pauseTicks = 0
        timerTicks = 0
        sendPacketsByOrder(true)
        packets.clear()
        velocityInput = false
        attacked = false
        // Reset prediction state
        buffering = false
        releasing = false
        bufferedPackets.clear()
        forceJumpOnce = false
        ticksSinceBufferStart = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInLiquid || thePlayer.isInWeb || thePlayer.isDead || !handleEvents()) return

        when (mode.lowercase()) {
            "glitch" -> {
                thePlayer.noClip = hasReceivedVelocity

                if (thePlayer.hurtTime == 7) thePlayer.motionY = 0.4

                hasReceivedVelocity = false
            }

            "reverse" -> {
                if (!hasReceivedVelocity) return

                val nearbyEntity = getNearestEntityInRange()
                if (nearbyEntity != null) {
                    if (!thePlayer.onGround) {
                        if (onLook && !EntityUtils.isLookingOnEntities(nearbyEntity, maxAngleDifference.toDouble())) {
                            return
                        }
                        speed *= reverseStrength
                    } else if (velocityTimer.hasTimePassed(80)) {
                        hasReceivedVelocity = false
                    }
                }
            }

            "smoothreverse" -> {
                if (!hasReceivedVelocity) return

                val nearbyEntity = getNearestEntityInRange()
                if (nearbyEntity == null || (onLook && !EntityUtils.isLookingOnEntities(
                        nearbyEntity,
                        maxAngleDifference.toDouble()
                    ))
                ) {
                    hasReceivedVelocity = false
                    thePlayer.speedInAir = 0.02F
                    reverseHurt = false
                    return
                }

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

            "aac" -> if (hasReceivedVelocity && velocityTimer.hasTimePassed(80)) {
                thePlayer.motionX *= horizontal.toDouble()
                thePlayer.motionZ *= horizontal.toDouble()
                hasReceivedVelocity = false
            }

            "aacv4" -> if (thePlayer.hurtTime > 0 && !thePlayer.onGround) {
                val reduce = aacv4MotionReducer
                thePlayer.motionX *= reduce
                thePlayer.motionZ *= reduce
            }

            "aacpush" -> {
                if (jump) {
                    if (thePlayer.onGround) jump = false
                } else {
                    // Strafe
                    if (thePlayer.hurtTime > 0 && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0) thePlayer.onGround =
                        true

                    // Reduce Y
                    if (thePlayer.hurtResistantTime > 0 && aacPushYReducer) thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducer
                    thePlayer.motionX /= reduce
                    thePlayer.motionZ /= reduce
                }
            }

            "aaczero" -> if (thePlayer.hurtTime > 0) {
                if (!hasReceivedVelocity || thePlayer.onGround || thePlayer.fallDistance > 2F) return

                thePlayer.motionY -= 1.0
                thePlayer.isAirBorne = true
                thePlayer.onGround = true
            } else {
                hasReceivedVelocity = false
            }

            "legit" -> {
                if (legitDisableInAir && !isOnGround(0.5)) return

                if (mc.thePlayer.maxHurtResistantTime != mc.thePlayer.hurtResistantTime || mc.thePlayer.maxHurtResistantTime == 0) return

                if (nextInt(endExclusive = 100) < chance) {
                    thePlayer.motionX *= (horizontal / 100f).toDouble()
                    thePlayer.motionZ *= (horizontal / 100f).toDouble()
                    thePlayer.motionY *= (vertical / 100f).toDouble()
                }
            }

            "intavereduce", "intavea" -> {
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
                if (attacked && mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10)
                    mc.thePlayer.isSprinting = false
                }
            }

            "intaveb" -> {
                val target = getNearestEntityInRange()
                if (target != null && thePlayer.hurtTime > 0) {
                    thePlayer.isSprinting = false
                    if (thePlayer.hurtTime <= 6) {
                        val yaw = MathHelper.wrapAngleTo180_float(currentRotation?.yaw ?: thePlayer.rotationYaw)
                        thePlayer.motionX = -sin(yaw * (Math.PI / 180)) * 0.02
                        thePlayer.motionZ = cos(yaw * (Math.PI / 180)) * 0.02
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

    @EventTarget
    fun onGameTick(event: GameTickEvent) {
        val thePlayer = mc.thePlayer ?: return
        mc.theWorld ?: return


        // Prediction Mode Logic
        if (mode == "Prediction") {
            if (!buffering) {
                ticksSinceBufferStart = 0
                return
            }
            ticksSinceBufferStart++

            val playerYaw = wrapTo180(mc.thePlayer.rotationYaw.toDouble())
            val diff = abs(playerYaw - kbYawDeg)

            // Release buffered packets if player is facing the knockback direction, is on ground, or timeout
            if ((diff <= 20 || diff >= 340) || mc.thePlayer.onGround || ticksSinceBufferStart > 25) {
                releasing = true
                buffering = false
                for (packet in bufferedPackets) {
                    if (!isStatusPacket(packet)) {
                        @Suppress("UNCHECKED_CAST") (packet as Packet<NetHandlerPlayClient>).processPacket(mc.netHandler)
                    }
                }
                bufferedPackets.clear()
                releasing = false
                ticksSinceBufferStart = 0
            }
        }

        // Click Mode Logic
        if (mode == "Click") {
            if (thePlayer.hurtTime != hurtTimeToClick || (ignoreBlocking && (thePlayer.isBlocking || KillAura.blockStatus))) return

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
                } else {
                    entity = getNearestEntityInRange(clickRange)?.takeIf { EntityUtils.isSelected(it, true) }
                }
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
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val player = mc.thePlayer ?: return

        when (mode.lowercase()) {
            "intavea", "intavereduce" -> {
                // Shared logic for Intave modes
                if (player.hurtTime == hurtTime && System.currentTimeMillis() - lastAttackTime <= 8000) {
                    player.motionX *= reduceFactor
                    player.motionZ *= reduceFactor
                }
                lastAttackTime = System.currentTimeMillis()
            }

            "grimcombat" -> {
                if (attacked && event.targetEntity != null) {
                    mc.netHandler.networkManager.sendPacket(C0APacketAnimation())
                    mc.netHandler.networkManager.sendPacket(
                        C02PacketUseEntity(event.targetEntity, C02PacketUseEntity.Action.ATTACK)
                    )
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return
        if (mc.theWorld == null || mc.netHandler == null || event.isCancelled || !handleEvents()) return

        val packet = event.packet

        // Pause ticks override everything
        if (pauseTicks > 0) {
            pauseTicks--
            return
        }

        if (releasing) return

        // --- PREDICTION MODE BUFFERING ---
        if (mode == "Prediction" && buffering) {
            // Buffer incoming packets while waiting to counter knockback
            if (packet is S12PacketEntityVelocity || packet is S27PacketExplosion || packet is S08PacketPlayerPosLook || !excludedSpecial(
                    packet
                )
            ) {
                bufferedPackets.add(packet)
                event.cancelEvent()
                return
            }
        }

        // --- NON-VELOCITY PACKET HANDLING ---
        when (mode.lowercase()) {
            "vulcan" -> {
                if (Disabler.verusCombat && (!Disabler.onlyCombat || Disabler.isOnCombat)) return
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

            "s32packet" -> if (packet is S32PacketConfirmTransaction && hasReceivedVelocity) {
                event.cancelEvent()
                hasReceivedVelocity = false
            }

            "blocksmc" -> if (packet is C0BPacketEntityAction && hasReceivedVelocity) {
                hasReceivedVelocity = false
                event.cancelEvent()
            }
        }

        // --- S12 ENTITY VELOCITY HANDLING ---
        if (packet is S12PacketEntityVelocity && packet.entityID == thePlayer.entityId) {
            velocityTimer.reset()

            when (mode.lowercase()) {
                "simple", "advanced" -> handleVelocity(event)
                "glitch" -> {
                    if (thePlayer.onGround) {
                        hasReceivedVelocity = true
                        event.cancelEvent()
                    }
                }

                "aac", "reverse", "smoothreverse", "aaczero", "ghostblock", "intavereduce", "hypixel", "hypixelair", "s32packet" -> {
                    hasReceivedVelocity = true
                }

                "prediction" -> {
                    if (packet.motionY > 0 || thePlayer.fallDistance <= 14 || thePlayer.hurtTime <= 1) {
                        forceJumpOnce = true
                    }

                    if (!thePlayer.onGround && thePlayer.hurtTime > 4) {
                        buffering = true
                        ticksSinceBufferStart = 0
                        val kbX = packet.motionX / 8000.0
                        val kbZ = packet.motionZ / 8000.0
                        kbYawDeg = wrapTo180(atan2deg(kbZ, kbX)).toFloat()

                        bufferedPackets.add(packet)
                        event.cancelEvent()
                    }
                }

                "grimcombat" -> {
                    if (thePlayer.isDead || mc.currentScreen is GuiGameOver || mc.playerController.currentGameType === WorldSettings.GameType.SPECTATOR) return
                    if (thePlayer.isOnLadder || (thePlayer.isBurning && fireCheckValue) || (thePlayer.isInWater && waterCheckValue) || (thePlayer.fallDistance > 1.5 && fallCheckValue) || (thePlayer.isEating && consumecheck) || soulSandCheck()) return

                    val horizontalStrength = sqrt(packet.motionX.toDouble().pow(2) + packet.motionZ.toDouble().pow(2))
                    if (horizontalStrength <= 1000) return

                    velocityInput = true
                    var entity: Entity? = RaycastUtils.raycastEntity(grimrange.toDouble()) {
                        it is EntityLivingBase && EntityUtils.isSelected(
                            it,
                            true
                        )
                    }
                    if (entity == null && !raycastValue) {
                        entity = KillAura.target?.takeIf { thePlayer.getDistanceToEntityBox(it) <= grimrange }
                    }

                    if (entity != null) {
                        val wasSprinting = thePlayer.isSprinting
                        if (!wasSprinting) sendPacket(C0BPacketEntityAction(thePlayer, START_SPRINTING))
                        repeat(attackCountValue) {
                            mc.netHandler.networkManager.sendPacket(C0APacketAnimation())
                            mc.netHandler.networkManager.sendPacket(
                                C02PacketUseEntity(
                                    entity,
                                    C02PacketUseEntity.Action.ATTACK
                                )
                            )
                        }
                        if (!wasSprinting) sendPacket(C0BPacketEntityAction(thePlayer, STOP_SPRINTING))
                        velX = packet.motionX
                        velY = packet.motionY
                        velZ = packet.motionZ
                        attacked = true
                        event.cancelEvent()
                    }
                }

                "matrixreduce" -> {
                    val factor = if (thePlayer.onGround) 0.86 else 0.33
                    packet.motionX = (packet.motionX * factor).toInt()
                    packet.motionZ = (packet.motionZ * factor).toInt()
                }

                "blocksmc" -> {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                    sendPacket(C0BPacketEntityAction(thePlayer, START_SNEAKING))
                    sendPacket(C0BPacketEntityAction(thePlayer, STOP_SNEAKING))
                }

                "grimc03" -> if (thePlayer.isMoving) {
                    hasReceivedVelocity = true
                    event.cancelEvent()
                }

                "vulcan", "hypixel", "hypixelair" -> {
                    event.cancelEvent()
                    if (mode.lowercase() == "hypixel" && !thePlayer.onGround && !absorbedVelocity) {
                        absorbedVelocity = true
                    } else {
                        hasReceivedVelocity = true
                    }
                }

                "block" -> {
                    val x = packet.motionX / 8000.0
                    val y = packet.motionY / 8000.0
                    val z = packet.motionZ / 8000.0
                    val vec3 = Vec3(x, y, z)
                    val vec2 = RotationUtils.aimToPoint(thePlayer.getPositionEyes(mc.timer.renderPartialTicks), vec3)
                    thePlayer.rotationYaw = RotationUtils.shortestYaw(thePlayer.rotationYaw, vec2.x)
                }

                "jump" -> {
                    // Jump direction check
                    val packetDirection = atan2(packet.motionX.toDouble(), packet.motionZ.toDouble())
                    val degreePlayer = getDirection()
                    val degreePacket = Math.floorMod(packetDirection.toDegrees().toInt(), 360).toDouble()
                    val angle = abs(degreePacket + degreePlayer)
                    val threshold = 120.0
                    if (Math.floorMod(angle.toInt(), 360).toDouble() in (180 - threshold / 2)..(180 + threshold / 2)) {
                        hasReceivedVelocity = true
                    }
                }
            }
        }

        // --- S27 EXPLOSION HANDLING ---
        if (packet is S27PacketExplosion) {
            velocityTimer.reset()

            if (pauseOnExplosion) {
                pauseTicks = ticksToPause
                return
            }

            when (mode.lowercase()) {
                "simple", "advanced" -> handleVelocity(event)
                "grimreduce" -> event.cancelEvent()
                // Other modes generally ignore explosions to prevent false triggers
            }
        }
    }


    /**
     * Tick Event for time-based actions
     */
    @EventTarget
    fun onTick(event: GameTickEvent) {
        val thePlayer = mc.thePlayer ?: return

        when (mode.lowercase()) {
            "block" -> if (mc.thePlayer.hurtTime > 5) {
                InventoryUtils.findBlockInHotbar()?.let { blockSlot ->
                    SilentHotbar.selectSlotSilently(
                        this,
                        blockSlot,
                        immediate = true,
                        render = renderSlot,
                        resetManually = true
                    )
                }
                for (i in 60 downTo 45 step 5) {
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
                }
            } else {
                RotationUtils.resetRotation()
            }

            "grimc03" -> {
                // Timer Abuse
                if (timerTicks > 0 && mc.timer.timerSpeed <= 1) {
                    mc.timer.timerSpeed = (0.8f + (0.2f * (20 - timerTicks) / 20)).coerceAtMost(1f)
                    --timerTicks
                } else if (mc.timer.timerSpeed <= 1) {
                    mc.timer.timerSpeed = 1f
                }

                if (hasReceivedVelocity) {
                    val pos = BlockPos(thePlayer.posX, thePlayer.posY, thePlayer.posZ)
                    if (checkAir(pos)) hasReceivedVelocity = false
                }
            }
        }
    }

    /**
     * Delay Mode Packet Handling
     */
    @EventTarget
    fun onDelayPacket(event: PacketEvent) {
        if (mode != "Delay" || event.isCancelled) return

        val packet = event.packet
        if (packet is S32PacketConfirmTransaction || packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            synchronized(packets) {
                packets[packet] = System.currentTimeMillis()
            }
        }
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        if (mode == "Delay") sendPacketsByOrder(false)
    }

    private fun sendPacketsByOrder(force: Boolean) {
        synchronized(packets) {
            packets.entries.removeAll { (packet, timestamp) ->
                if (force || timestamp <= System.currentTimeMillis() - spoofDelay) {
                    PacketUtils.schedulePacketProcess(packet)
                    true
                } else false
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val thePlayer = mc.thePlayer ?: return
        if (thePlayer.isInLiquid || thePlayer.isInWeb) return

        when (mode.lowercase()) {
            "aacpush" -> {
                jump = true
                if (!thePlayer.isCollidedVertically) event.cancelEvent()
            }

            "aaczero" -> if (thePlayer.hurtTime > 0) event.cancelEvent()
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
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
    fun onBlockBB(event: BlockBBEvent) {
        val player = mc.thePlayer ?: return

        if (mode == "GhostBlock" && hasReceivedVelocity) {
            if (player.hurtTime in hurtTimeRange) {
                // Check if there is air exactly 1 level above the player's Y position
                if (event.block is BlockAir && event.y == player.posY.toInt() + 1) {
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

    @EventTarget
    fun onMovementInput(event: MovementInputEvent) {
        // Only trigger jump for Prediction mode
        if (mode == "Prediction") {
            if (forceJumpOnce) {
                event.originalInput.jump = true
                forceJumpOnce = false
            }
            if (buffering || releasing) {
                event.originalInput.jump = false // Prevent jumping while buffering
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
        if (mode == "GrimReduce") lastAttackTime = 0L
    }

    private fun handleVelocity(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            event.cancelEvent()
            when (mode) {
                "Simple" -> {
                    if (horizontal == 0f && vertical == 0f) return

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
                        thePlayer.motionX = motionX * horizontal
                        thePlayer.motionZ = motionZ * horizontal
                    }

                    if (vertical != 0f) {
                        var motionY = packet.realMotionY
                        if (limitMaxMotionValue.get()) {
                            motionY = motionY.coerceAtMost(maxYMotion + 0.00075)
                        }
                        thePlayer.motionY = motionY * vertical
                    }
                }

                "Advanced" -> {
                    if (thePlayer.onGround) {
                        if (horizontalGround == 0 && verticalGround == 0) return
                        if (horizontalGround != 0) {
                            thePlayer.motionX = packet.realMotionX * (horizontalGround / 100F)
                            thePlayer.motionZ = packet.realMotionZ * (horizontalGround / 100F)
                        }
                        if (verticalGround != 0) {
                            thePlayer.motionY = packet.realMotionY * (verticalGround / 100F)
                        }
                    } else {
                        if (horizontalInAir == 0 && verticalInAir == 0) return
                        if (horizontalInAir != 0) {
                            thePlayer.motionX = packet.realMotionX * (horizontalInAir / 100F)
                            thePlayer.motionZ = packet.realMotionZ * (horizontalInAir / 100F)
                        }
                        if (verticalInAir != 0) {
                            thePlayer.motionY = packet.realMotionY * (verticalInAir / 100F)
                        }
                    }
                }
            }
        } else if (packet is S27PacketExplosion) {
            when (mode) {
                "Simple" -> {
                    if (horizontal == 0f && vertical == 0f) {
                        packet.field_149152_f = 0f
                        packet.field_149153_g = 0f
                        packet.field_149159_h = 0f
                        return
                    }

                    packet.field_149152_f *= horizontal // motionX
                    packet.field_149153_g *= vertical // motionY
                    packet.field_149159_h *= horizontal // motionZ

                    if (limitMaxMotionValue.get()) {
                        val distXZ =
                            sqrt(packet.field_149152_f * packet.field_149152_f + packet.field_149159_h * packet.field_149159_h)
                        val distY = packet.field_149153_g
                        val effectiveMaxYMotion = maxYMotion + 0.00075f

                        if (distXZ > maxXZMotion) {
                            val ratioXZ = maxXZMotion / distXZ
                            packet.field_149152_f *= ratioXZ
                            packet.field_149159_h *= ratioXZ
                        }
                        if (distY > effectiveMaxYMotion) {
                            packet.field_149153_g *= effectiveMaxYMotion / distY
                        }
                    }
                }
                // Advanced mode does not handle explosions in this implementation
                "Advanced" -> return
            }
        }
    }

    // --- UTILITY FUNCTIONS ---
    private fun getNearestEntityInRange(range: Float = this.range): Entity? {
        val thePlayer = mc.thePlayer ?: return null
        val theWorld = mc.theWorld ?: return null
        return theWorld.loadedEntityList.filter {
            EntityUtils.isSelected(it, true) && thePlayer.getDistanceToEntityBox(it) <= range
        }.minByOrNull { thePlayer.getDistanceToEntityBox(it) }
    }

    private fun checkAir(blockPos: BlockPos): Boolean {
        val world = mc.theWorld ?: return false
        if (!world.isAirBlock(blockPos)) return false
        timerTicks = 20
        sendPackets(C03PacketPlayer(true), C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, DOWN))
        world.setBlockToAir(blockPos)
        return true
    }

    private fun getDirection(): Double {
        val player = mc.thePlayer ?: return 0.0
        var moveYaw = player.rotationYaw
        when {
            player.moveForward > 0f && player.moveStrafing == 0f -> moveYaw += 0
            player.moveForward < 0f && player.moveStrafing == 0f -> moveYaw += 180
            player.moveForward > 0f && player.moveStrafing > 0f -> moveYaw -= 45
            player.moveForward > 0f && player.moveStrafing < 0f -> moveYaw += 45
            player.moveForward < 0f && player.moveStrafing > 0f -> moveYaw -= 135
            player.moveForward < 0f && player.moveStrafing < 0f -> moveYaw += 135
            player.moveForward == 0f && player.moveStrafing > 0f -> moveYaw -= 90
            player.moveForward == 0f && player.moveStrafing < 0f -> moveYaw += 90
        }
        return Math.floorMod(moveYaw.toInt(), 360).toDouble()
    }

    private fun shouldJump() = when (jumpCooldownMode.lowercase()) {
        "ticks" -> limitUntilJump >= ticksUntilJump
        "receivedhits" -> limitUntilJump >= hitsUntilJump
        else -> false
    }

    fun soulSandCheck(): Boolean {
        val player = mc.thePlayer ?: return false
        val bb = player.entityBoundingBox.contract(0.001, 0.001, 0.001)
        val minX = MathHelper.floor_double(bb.minX)
        val maxX = MathHelper.floor_double(bb.maxX + 1.0)
        val minY = MathHelper.floor_double(bb.minY)
        val maxY = MathHelper.floor_double(bb.maxY + 1.0)
        val minZ = MathHelper.floor_double(bb.minZ)
        val maxZ = MathHelper.floor_double(bb.maxZ + 1.0)

        for (x in minX until maxX) {
            for (y in minY until maxY) {
                for (z in minZ until maxZ) {
                    val block = mc.theWorld?.getBlockState(BlockPos(x, y, z))?.block
                    if (block is BlockSoulSand) {
                        return true
                    }
                }
            }
        }
        return false
    }
}

// Global utility functions
private fun atan2deg(y: Double, x: Double): Double {
    return Math.toDegrees(atan2(y, x))
}

private fun wrapTo180(angle: Double): Double {
    var a = angle % 360.0
    if (a >= 180.0) a -= 360.0
    if (a < -180.0) a += 360.0
    return a
}

private fun excludedSpecial(packet: Packet<*>): Boolean {
    // Exclude packets that don't affect player state to avoid unnecessary buffering
    return packet !is S3BPacketScoreboardObjective && packet !is S3CPacketUpdateScore && packet !is S3DPacketDisplayScoreboard
}

private fun isStatusPacket(packet: Packet<*>): Boolean {
    return packet.javaClass.name.startsWith("net.minecraft.network.status.server.")
}
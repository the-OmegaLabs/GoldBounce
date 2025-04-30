
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.extensions.floorInt
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.world.WorldSettings

object AntiVoid : Module(name = "AntiVoid", category = Category.PLAYER) {
    private val modeValue = ListValue("Mode", arrayOf("Blink", "TPBack", "MotionFlag", "PacketFlag", "GroundSpoof",
        "OldHypixel", "Jartex", "OldCubecraft", "Packet", "Watchdog", "Vulcan", "Freeze", "NCPBounce", "MatrixGlide",
        "VerusTeleport", "AACFlag", "VelocityCancel", "SearchPearl"), "Blink")
    private val maxFallDistValue = FloatValue("MaxFallDistance", 20F, 1F..10F)
    private val checkDepthValue = IntegerValue("CheckDepth", 5, 3..10) { !modeValue.equals("Freeze") }
    private val glideSpeedValue = FloatValue("GlideSpeed", 0.2f, 0.05f..1f) { modeValue.equals("MatrixGlide") }
    private val teleportHeightValue = FloatValue("TeleportHeight", 1.5f, 0f..5f) { modeValue.equals("VerusTeleport") }
    private val resetMotionValue = BoolValue("ResetMotion", false) { modeValue.equals("Blink") }
    private val startFallDistValue = FloatValue("BlinkStartFallDistance", 5F,0F..2F ) { modeValue.equals("Blink") }
    private val autoScaffoldValue = BoolValue("BlinkAutoScaffold", true) { modeValue.equals("Blink") }
    private val motionflagValue = FloatValue("MotionFlag-MotionY",5.0F, 0.0F..1.0F ) { modeValue.equals("MotionFlag") }
    private val voidOnlyValue = BoolValue("OnlyVoid", true)

    private val packetCache = ArrayList<C03PacketPlayer>()
    private var blink = false
    private var canBlink = false
    private var canCancel = false
    private var canSpoof = false
    private var tried = false
    private var flagged = false
    private var freeze = false
    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var lastRecY = 0.0
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0

    override fun onEnable() {
        canCancel = false
        blink = false
        canBlink = false
        canSpoof = false
        if(mc.thePlayer != null) {
            lastRecY = mc.thePlayer.posY
        } else {
            lastRecY = 0.0
        }
        tried = false
        flagged = false
        if (modeValue.equals("Freeze")) {
            if (mc.thePlayer == null) {
                return
            }

            x = mc.thePlayer.posX
            y = mc.thePlayer.posY
            z = mc.thePlayer.posZ
            motionX = mc.thePlayer.motionX
            motionY = mc.thePlayer.motionY
            motionZ = mc.thePlayer.motionZ
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (lastRecY == 0.0) {
            lastRecY = mc.thePlayer?.posY ?: 0.0
        }
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            tried = false
            flagged = false
        }

        when (modeValue.get().lowercase()) {
            "freeze" -> {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

            }
            "groundspoof" -> {
                if (!voidOnlyValue.get() || checkVoid()) {
                    canSpoof = mc.thePlayer.fallDistance > maxFallDistValue.get()
                }
            }
            "searchpearl" -> {
                if (mc.currentScreen != null || mc.playerController.currentGameType == WorldSettings.GameType.SPECTATOR
                    || mc.playerController.currentGameType == WorldSettings.GameType.CREATIVE) return
                val entity = getClosestEntity()
                val distance = if (entity != null) mc.thePlayer.getDistanceToEntity(entity) else 0F
                freeze = distance > 4 || entity == null

                val pearl = InventoryUtils.findItem(36, 45, Items.ender_pearl)
                if (mc.thePlayer.fallDistance > maxFallDistValue.get() && pearl?.let { (it - 36) > -1 } == true) {
                    if (!voidOnlyValue.get() || checkVoid()) {
                        pearl.let { mc.thePlayer.inventory.currentItem = it - 36 }
                    }
                }
            }
            "vulcan" -> {
                if (mc.thePlayer.onGround && BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) !is BlockAir) {
                    posX = mc.thePlayer.prevPosX
                    posY = mc.thePlayer.prevPosY
                    posZ = mc.thePlayer.prevPosZ
                }
                if (!voidOnlyValue.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ)
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                        mc.thePlayer.setPosition(posX, posY, posZ)
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                        mc.thePlayer.fallDistance = 0F
                        MovementUtils.resetMotion(true)
                        tried = true
                    }
                }
            }

            "motionflag" -> {
                if (!voidOnlyValue.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.thePlayer.motionY += motionflagValue.get()
                        mc.thePlayer.fallDistance = 0.0F
                        tried = true
                    }
                }
            }

            "packetflag" -> {
                if (!voidOnlyValue.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 1, false))
                        tried = true
                    }
                }
            }

            "tpback" -> {
                if (mc.thePlayer.onGround && BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) !is BlockAir) {
                    posX = mc.thePlayer.prevPosX
                    posY = mc.thePlayer.prevPosY
                    posZ = mc.thePlayer.prevPosZ
                }
                if (!voidOnlyValue.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                        mc.thePlayer.fallDistance = 0F
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        tried = true
                    }
                }
            }

            "jartex" -> {
                canSpoof = false
                if (!voidOnlyValue.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance> maxFallDistValue.get() && mc.thePlayer.posY < lastRecY + 0.01 && mc.thePlayer.motionY <= 0 && !mc.thePlayer.onGround && !flagged) {
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ *= 0.838
                        mc.thePlayer.motionX *= 0.838
                        canSpoof = true
                    }
                }
                lastRecY = mc.thePlayer.posY
            }

            "oldcubecraft" -> {
                canSpoof = false
                if (!voidOnlyValue.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance> maxFallDistValue.get() && mc.thePlayer.posY < lastRecY + 0.01 && mc.thePlayer.motionY <= 0 && !mc.thePlayer.onGround && !flagged) {
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.jumpMovementFactor = 0.00f
                        canSpoof = true
                        if (!tried) {
                            tried = true
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, (32000.0).toDouble(), mc.thePlayer.posZ, false))
                        }
                    }
                }
                lastRecY = mc.thePlayer.posY
            }

            "packet" -> {
                if (checkVoid()) {
                    canCancel = true
                }

                if (canCancel) {
                    if (mc.thePlayer.onGround) {
                        for (packet in packetCache) {
                            mc.netHandler.addToSendQueue(packet)
                        }
                        packetCache.clear()
                    }
                    canCancel = false
                }
            }
            "ncpbounce" -> {
                if (checkVoid() && mc.thePlayer.fallDistance > maxFallDistValue.get()) {
                    mc.thePlayer.motionY = 0.42 + (0.1 * mc.thePlayer.fallDistance.coerceAtMost(5f))
                    mc.thePlayer.fallDistance = 0f
                }
            }
            "matrixglide" -> {
                if (checkVoid()) {
                    mc.thePlayer.motionY = -glideSpeedValue.get().toDouble()
                    mc.thePlayer.jumpMovementFactor = glideSpeedValue.get() * 0.6f
                }
            }
            "verusteleport" -> {
                if (checkVoid() && mc.thePlayer.fallDistance > maxFallDistValue.get()) {
                    mc.thePlayer.setPositionAndUpdate(
                        mc.thePlayer.posX, 
                        mc.thePlayer.posY + teleportHeightValue.get(), 
                        mc.thePlayer.posZ
                    )
                    mc.thePlayer.fallDistance = 0f
                }
            }
            "aacflag" -> {
                if (checkVoid() && mc.thePlayer.ticksExisted % 3 == 0) {
                    PacketUtils.sendPacket(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1e-6,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                }
            }
            "velocitycancel" -> {
                if (checkVoid() && mc.thePlayer.fallDistance > maxFallDistValue.get()) {
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY.floorInt().toDouble(), mc.thePlayer.posZ)
                }
            }
            "blink" -> {
                if (!blink) {
                    checkSafeLanding()
                    val collide = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, 0.0, 0.0, 0.0, 0F, 0F, 0F).findCollision(60)
                    if (canBlink && (collide == null || (mc.thePlayer.posY - collide.pos.y)> startFallDistValue.get())) {
                        posX = mc.thePlayer.posX
                        posY = mc.thePlayer.posY
                        posZ = mc.thePlayer.posZ
                        motionX = mc.thePlayer.motionX
                        motionY = mc.thePlayer.motionY
                        motionZ = mc.thePlayer.motionZ

                        packetCache.clear()
                        blink = true
                    }

                    if (mc.thePlayer.onGround) {
                        canBlink = true
                    }
                } else {
                    if (mc.thePlayer.fallDistance> maxFallDistValue.get()) {
                        mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                        if (resetMotionValue.get()) {
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionY = 0.0
                            mc.thePlayer.motionZ = 0.0
                            mc.thePlayer.jumpMovementFactor = 0.00f
                        } else {
                            mc.thePlayer.motionX = motionX
                            mc.thePlayer.motionY = motionY
                            mc.thePlayer.motionZ = motionZ
                            mc.thePlayer.jumpMovementFactor = 0.00f
                        }

                        if (autoScaffoldValue.get()) {
                            LiquidBounce.moduleManager[Scaffold::class.java].state = true
                        }

                        packetCache.clear()
                        blink = false
                        canBlink = false
                    } else if (mc.thePlayer.onGround) {
                        blink = false

                        for (packet in packetCache) {
                            mc.netHandler.addToSendQueue(packet)
                        }
                    }
                }
            }
        }
    }
    private fun getClosestEntity(): Entity? {
        val filteredEntities = mutableListOf<Entity>()
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityPlayer && entity !== mc.thePlayer) {
                filteredEntities.add(entity)
            }
        }
        filteredEntities.sortWith(
            compareBy(
                { mc.thePlayer.getDistanceToEntity(it) },
                { mc.thePlayer.getDistanceToEntity(it) })
        )
        return filteredEntities.lastOrNull()
    }
    private fun checkVoid(): Boolean {
        var i = (-(mc.thePlayer.posY-1.4857625)).toInt()
        var dangerous = true
        while (i <= 0) {
            dangerous = mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(mc.thePlayer.motionX * 0.5, i.toDouble(), mc.thePlayer.motionZ * 0.5)).isEmpty()
            i++
            if (!dangerous) break
        }
        return dangerous
    }
    private fun checkSafeLanding(): Boolean {
        val fallingPlayer = FallingPlayer(
            mc.thePlayer.posX,
            mc.thePlayer.posY,
            mc.thePlayer.posZ,
            mc.thePlayer.motionX,
            mc.thePlayer.motionY,
            mc.thePlayer.motionZ,
            mc.thePlayer.rotationYaw,
            mc.thePlayer.moveStrafing,
            mc.thePlayer.moveForward
        )
        return fallingPlayer.findCollision(20) != null
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (modeValue.get().lowercase()) {
            "watchdog" -> {
                if (packet is C03PacketPlayer) {
                    if (mc.thePlayer.onGround) {
                        posX = mc.thePlayer.posX
                        posY = mc.thePlayer.posY
                        posZ = mc.thePlayer.posZ
                        for (packet in packetCache) {
                            mc.netHandler.addToSendQueue(packet)
                        }
                        packetCache.clear()
                    } else {
                        event.cancelEvent()
                        packetCache.add(packet)
                        if (mc.thePlayer.fallDistance > maxFallDistValue.get()) {
                            PacketUtils.sendPacket(C03PacketPlayer.C04PacketPlayerPosition(posX, posY + 0.1, posZ, false))
                        }
                    }
                }
            }
            "blink" -> {
                if (blink && (packet is C03PacketPlayer)) {
                    packetCache.add(packet)
                    event.cancelEvent()
                }
            }

            "packet" -> {
                if (canCancel && (packet is C03PacketPlayer)) {
                    packetCache.add(packet)
                    event.cancelEvent()
                }

                if (packet is S08PacketPlayerPosLook) {
                    packetCache.clear()
                    canCancel = false
                }
            }

            "groundspoof" -> {
                if (canSpoof && (packet is C03PacketPlayer)) {
                    packet.onGround = true
                }
            }

            "jartex" -> {
                if (canSpoof && (packet is C03PacketPlayer)) {
                    packet.onGround = true
                }
                if (canSpoof && (packet is S08PacketPlayerPosLook)) {
                    flagged = true
                }
            }

            "oldcubecraft" -> {
                if (canSpoof && (packet is C03PacketPlayer)) {
                    if (packet.y < 1145.141919810) event.cancelEvent()
                }
                if (canSpoof && (packet is S08PacketPlayerPosLook)) {
                    flagged = true
                }
            }

            "oldhypixel" -> {
                if (packet is S08PacketPlayerPosLook && mc.thePlayer.fallDistance> 3.125) mc.thePlayer.fallDistance = 3.125f

                if (packet is C03PacketPlayer) {
                    if (voidOnlyValue.get() && mc.thePlayer.fallDistance >= maxFallDistValue.get() && mc.thePlayer.motionY <= 0 && checkVoid()) {
                        packet.y += 11.0
                    }
                    if (!voidOnlyValue.get() && mc.thePlayer.fallDistance >= maxFallDistValue.get()) packet.y += 11.0
                }
            }
            "freeze" -> {
                if (event.packet is C03PacketPlayer) {
                    event.cancelEvent()
                }
                if (event.packet is S08PacketPlayerPosLook) {
                    x = event.packet.x
                    y = event.packet.y
                    z = event.packet.z
                    motionX = 0.0
                    motionY = 0.0
                    motionZ = 0.0
                }
            }
        }
    }
    override fun onDisable() {
        if (modeValue.equals("Freeze")) {
            mc.thePlayer.motionX = motionX
            mc.thePlayer.motionY = motionY
            mc.thePlayer.motionZ = motionZ
            mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        }
    }

    override val tag: String
        get() = modeValue.get()
}

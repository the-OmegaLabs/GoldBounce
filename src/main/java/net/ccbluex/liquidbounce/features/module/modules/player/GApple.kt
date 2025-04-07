package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.ReflectionUtil
import net.ccbluex.liquidbounce.utils.StuckUtils
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.packet.BlinkUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Items
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import kotlin.math.min

object GApple : Module("GApple", Category.PLAYER) {
    // Values
    private val heal by IntegerValue("Health", 15, 0..20)
    private val noMove by BoolValue("StopMove", true)
    private val autoValue by BoolValue("Auto", true)
    private val stuckValue by BoolValue("Stuck", false)
    private val notValue by BoolValue("Notify", false)
    private val hudValue by BoolValue("HUD", false)
    private val sendDelay by IntegerValue("SendDelay", 3, 1..10)
    private val noCancelC02 by BoolValue("NoCancelC02", false)
    private val noC02 by BoolValue("NoC02", false)

    // State
    var eating = false
    private var movingPackets = 0
    private var slot = -1
    private val packets = LinkedList<Packet<*>>()
    private var needSkip = false
    private val stopWatch = MSTimer()
    private var target: EntityLivingBase? = null

    init {
        arrayOf(heal, noMove, autoValue, stuckValue, notValue, hudValue, sendDelay, noCancelC02, noC02)
    }

    override fun onEnable() {
        packets.clear()
        slot = -1
        needSkip = false
        movingPackets = 0
        eating = false
        stopWatch.reset()
    }

    override fun onDisable() {
        eating = false
        releasePackets()
        BlinkUtils.stopBlink()
        if (stuckValue) StuckUtils.stopStuck()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        eating = false
        releasePackets()
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive) {
            eating = false
            packets.clear()
            return
        }

        slot = InventoryUtils.findItem(36, 45, Items.golden_apple)?.minus(36) ?: -1

        if (slot == -1 || mc.thePlayer.health >= heal) {
            if (eating) {
                eating = false
                releasePackets()
                if (autoValue) toggle()
            }
            return
        }

        if (!eating) {
            eating = true
            if (notValue) Notification("Started eating!")
        }

        handleEatingLogic()
    }

    private fun handleEatingLogic() {
        target = KillAura.target

        if (movingPackets >= 32) {
            sendPackets(
                C09PacketHeldItemChange(slot),
                C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(slot + 36).stack),
            )
            releasePackets()
            sendPackets(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            stopWatch.reset()
            if (autoValue) slot = InventoryUtils.findItem(36, 45, Items.golden_apple)?.minus(36) ?: -1
        } else if (mc.thePlayer.ticksExisted % sendDelay == 0) {
            processPacketQueue()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when {
            packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId -> needSkip = true
            eating && !isWhitelistedPacket(packet) -> {
                event.cancelEvent()
                packets.add(packet)
            }
        }
    }

    private fun isWhitelistedPacket(packet: Packet<*>) = packet is C09PacketHeldItemChange ||
            packet is C0EPacketClickWindow ||
            packet is C16PacketClientStatus ||
            packet is C0DPacketCloseWindow

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        if (eating) {
            event.forward = 0.2f
            event.strafe = 0.2f
        }
    }

    @EventTarget
    fun onMoveInput(event: MoveEvent) {
        if (eating && noMove) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (eating && hudValue) {
            val progress = min(movingPackets.toFloat() / 32, 1f)
            chat("§7Eating: §f${(progress * 100).toInt()}%")
        }
    }

    private fun sendPackets(vararg packets: Packet<*>) {
        packets.forEach { mc.netHandler.addToSendQueue(it) }
    }

    private fun processPacketQueue() {
        while (packets.isNotEmpty()) {
            val packet = packets.poll()
            if (packet is C01PacketChatMessage) break
            if (packet is C03PacketPlayer) movingPackets--
            mc.netHandler.addToSendQueue(packet)
        }
    }

    private fun releasePackets() {
        packets.removeAll { it is C01PacketChatMessage || it is C08PacketPlayerBlockPlacement }
        packets.forEach { mc.netHandler.addToSendQueue(it) }
        packets.clear()
        movingPackets = 0
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        when (event.eventState) {
            EventState.PRE -> handlePreMotion()
            EventState.POST -> if (eating) movingPackets++
        }
    }

    private fun handlePreMotion() {
        val positionUpdateTicks: Int = ReflectionUtil.getFieldValue(mc.thePlayer, "positionUpdateTicks")

        if (eating && stuckValue && positionUpdateTicks < 20 && !needSkip) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        } else {
            needSkip = false
        }
    }
}
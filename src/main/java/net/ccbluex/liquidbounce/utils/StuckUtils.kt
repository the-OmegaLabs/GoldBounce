package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import org.lwjgl.input.Keyboard
import javax.vecmath.Vector2f

object StuckUtils : MinecraftInstance(), Listenable {
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var onGround = false
    private var rotation = Vector2f()

    // 控制标记位
    private var enabledByModule = false
    private var enabledByThirdParty = false

    fun enable() {
        if (!enabledByModule) {
            enabledByModule = true
            activate()
        }
    }

    fun disable() {
        if (enabledByModule) {
            enabledByModule = false
            if (!enabledByThirdParty) deactivate()
        }
    }

    fun enableByThirdParty() {
        if (!enabledByThirdParty) {
            enabledByThirdParty = true
            if (!enabledByModule) activate()
        }
    }

    fun disableByThirdParty() {
        if (enabledByThirdParty) {
            enabledByThirdParty = false
            if (!enabledByModule) deactivate()
        }
    }

    private fun activate() {
        val player = mc.thePlayer ?: return
        x = player.posX
        y = player.posY
        z = player.posZ
        motionX = player.motionX
        motionY = player.motionY
        motionZ = player.motionZ
        onGround = player.onGround
        rotation = Vector2f(player.rotationYaw, player.rotationPitch)

        val gcd = getGCD()
        rotation.x -= rotation.x % gcd
        rotation.y -= rotation.y % gcd

        LiquidBounce.eventManager.registerListener(this)
    }

    private fun deactivate() {
        LiquidBounce.eventManager.unregisterListener(this)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        player.motionX = 0.0
        player.motionY = 0.0
        player.motionZ = 0.0
        player.setPosition(x, y, z)
        mc.gameSettings.keyBindForward.pressed = false
        mc.gameSettings.keyBindLeft.pressed = false
        mc.gameSettings.keyBindBack.pressed = false
        mc.gameSettings.keyBindRight.pressed = false
        mc.gameSettings.keyBindJump.pressed = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C08PacketPlayerBlockPlacement) {
            val currentRot = Vector2f(player.rotationYaw, player.rotationPitch)
            val gcd = getGCD()
            currentRot.x -= currentRot.x % gcd
            currentRot.y -= currentRot.y % gcd

            if (!rotation.equals(currentRot)) {
                event.cancelEvent()
                sendPacket(C03PacketPlayer.C05PacketPlayerLook(currentRot.x, currentRot.y, onGround))
                sendPacket(C08PacketPlayerBlockPlacement(player.heldItem))
                rotation = currentRot
            }
        }

        if (packet is C03PacketPlayer) {
            event.cancelEvent()
        }
    }

    private fun getGCD(): Float {
        val sens = mc.gameSettings.mouseSensitivity
        val f = sens * 0.6f + 0.2f
        return f * f * f * 1.2f
    }

}

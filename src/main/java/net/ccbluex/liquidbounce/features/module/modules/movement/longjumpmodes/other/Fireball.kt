package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump.offGroundTicks
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.chat
import net.minecraft.item.ItemFireball
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation

object Fireball : LongJumpMode("Fireball"){
    var fart = false
    var send = false
    var tic = -1
    var lastSlot = -1
    override fun onEnable() {
        send = false
        fart = false
        tic = 0
        lastSlot = -1
        if (getBall() == -1) {
            chat("Couldnt find Fireball")
            LongJump.state = false
        }
        super.onEnable()
    }
    override fun onPreMotion(event : MotionEvent) {
        val ballSlot: Int = getBall()
        if (ballSlot != -1) {
            tic++
            if (tic <= 3) {
                lastSlot = mc.thePlayer.inventory.currentItem
                mc.thePlayer.inventory.currentItem = ballSlot
                mc.thePlayer.rotationPitch = 90F
                if (tic == 3) {
                    mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()))
                    send = true
                }
            } else if (tic == 5) {
                mc.thePlayer.rotationPitch = 90F
                mc.thePlayer.inventory.currentItem = lastSlot
                MovementUtils.strafe(1.3F)
            }
            if (tic >= 4 && !mc.thePlayer.onGround) {
                if (offGroundTicks < 20) {
                    //mc.thePlayer.motionY = 0.30;
                } else if (offGroundTicks > 20) {
                    LongJump.state = false
                }
            }
        }
    }
    private fun getBall(): Int {
        for (i in 0..8) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.getItem() is ItemFireball) {
                return i
            }
        }
        return -1
    }
}
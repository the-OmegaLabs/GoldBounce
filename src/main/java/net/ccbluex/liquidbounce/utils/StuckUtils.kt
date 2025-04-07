package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

object StuckUtils : Listenable, MinecraftInstance() {
    var moveTicks = 0
    var stuck = false
    var c03s = 0

    fun stuck() {
        stuck = true
    }

    fun stopStuck(){
        stuck = false
    }
    @EventTarget
    fun onPacket(event:PacketEvent) {
        if (event.packet is C03PacketPlayer && !event.packet.isMoving) {
            c03s++
            if (c03s >= 19) {
                c03s = 0
                if (stuck) moveTicks++
            }
        }

        if (event.packet is S12PacketEntityVelocity && stuck && event.packet.entityID == mc.thePlayer!!.entityId) {
            moveTicks++
        }
    }

    override fun handleEvents(): Boolean {
        return true
    }
}
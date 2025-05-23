package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.bmcDamageBoost
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.bmcLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.damageLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.fullStrafe
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.safeY
import net.ccbluex.liquidbounce.utils.MovementUtils.airTicks
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.skid.crosssine.MovementUtils.strafe
import net.minecraft.client.Minecraft
import net.minecraft.potion.Potion
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class JumpUtils {
    private var shouldJump = false

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END && shouldJump) {
            val player = Minecraft.getMinecraft().thePlayer
            if (player != null && player.onGround) {
                player.jump()
                shouldJump = false
            }
        }
    }

    fun jump() {

        shouldJump = true
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
}
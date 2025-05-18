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
            if (Speed.mode.get() == "BlocksMCHop" && !player.onGround) {
                if (fullStrafe) {
                    strafe(speed - 0.004F)
                } else {
                    if (airTicks >= 6) {
                        strafe()
                    }
                }

                if ((player.getActivePotionEffect(Potion.moveSpeed)?.amplifier ?: 0) > 0 && airTicks == 3) {
                    player.motionX *= 1.12
                    player.motionZ *= 1.12
                }

                if (bmcLowHop.get() && airTicks == 4) {
                    if (safeY) {
                        if (player.posY % 1.0 == 0.16610926093821377) {
                            player.motionY = -0.09800000190734863
                        }
                    } else {
                        player.motionY = -0.09800000190734863
                    }
                }

                if (player.hurtTime == 9 && bmcDamageBoost) {
                    strafe(speed.coerceAtLeast(0.7F))
                }

                if (damageLowHop && player.hurtTime >= 1) {
                    if (player.motionY > 0) {
                        player.motionY -= 0.15
                    }
                }
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
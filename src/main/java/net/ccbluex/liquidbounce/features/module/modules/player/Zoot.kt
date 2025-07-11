/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.value._boolean
import net.minecraft.network.play.client.C03PacketPlayer

object Zoot : Module("Zoot", Category.PLAYER) {

    private val badEffects by _boolean("BadEffects", true)
    private val fire by _boolean("Fire", true)
    private val noAir by _boolean("NoAir", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (noAir && !serverOnGround)
            return

        if (badEffects) {

            val effect = thePlayer.activePotionEffects
                .filter { it.potionID in NEGATIVE_EFFECT_IDS }
                .maxByOrNull { it.duration }

            if (effect != null) {
                repeat(effect.duration / 20) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }
            }
        }


        if (fire && mc.playerController.gameIsSurvivalOrAdventure() && thePlayer.isBurning) {
            repeat(9) {
                sendPacket(C03PacketPlayer(serverOnGround))
            }
        }
    }
}
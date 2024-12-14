/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.client

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

open class MinecraftInstance {
    companion object {
        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()
    }
}

fun Minecraft.playSound(
    resourceLocation: ResourceLocation,
    pitch: Float = 1.0f,
) = synchronized(this.soundHandler) {
    this.soundHandler.playSound(PositionedSoundRecord.create(resourceLocation, pitch))
}

fun String.asResourceLocation() = ResourceLocation(this)

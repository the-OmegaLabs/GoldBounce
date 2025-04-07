package net.ccbluex.liquidbounce.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

class MCGO {

    private val mc: Minecraft = Minecraft.getMinecraft()

    // Method to simulate right-click
    fun rightClick() {
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.keyCode)
    }

    // Method to switch to a specific inventory slot
    fun switchToInventorySlot(slot: Int) {
        if (slot in 0..8) {
            val hotbarKey = Keyboard.KEY_1 + slot
            KeyBinding.setKeyBindState(hotbarKey, true)
            KeyBinding.onTick(hotbarKey)
            KeyBinding.setKeyBindState(hotbarKey, false)
        }
    }

    // Shoot method
    fun shoot() {
        rightClick()
    }

}

package net.ccbluex.liquidbounce.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

public class MCGO {

    private val mc: Minecraft = Minecraft.getMinecraft()

    // Method to simulate right-click
    private fun rightClick() {
        val rightClickKey = mc.gameSettings.keyBindUseItem
        KeyBinding.setKeyBindState(rightClickKey.keyCode, true)
        mc.clickMouse()
        KeyBinding.setKeyBindState(rightClickKey.keyCode, false)
    }

    // Method to switch to a specific inventory slot
    private fun switchToInventorySlot(slot: Int) {
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

    // ShootNoSpread method
    fun shootNoSpread() {
        switchToInventorySlot(1) // Switch to slot 2 (index starts from 0)
        switchToInventorySlot(0) // Switch back to slot 1
        rightClick()
    }
}

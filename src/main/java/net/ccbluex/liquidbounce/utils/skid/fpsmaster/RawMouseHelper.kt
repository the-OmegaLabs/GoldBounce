package net.ccbluex.liquidbounce.utils.skid.fpsmaster

import net.minecraft.util.MouseHelper

class RawMouseHelper : MouseHelper() {
    override fun mouseXYChange() {
        deltaX = RawInputMod.dx
        RawInputMod.dx = 0
        deltaY = -RawInputMod.dy
        RawInputMod.dy = 0
    }
}
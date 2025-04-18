package net.ccbluex.liquidbounce.bzym

import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc

class global {
    var clientWalkStatus = false

    fun toggleWalk() {
        clientWalkStatus = !clientWalkStatus  // 切换状态

        // 设置前进键状态
        mc.gameSettings.keyBindForward.pressed = clientWalkStatus

    }
}

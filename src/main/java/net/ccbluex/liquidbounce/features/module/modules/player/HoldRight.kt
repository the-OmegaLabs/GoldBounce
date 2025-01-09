package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object HoldRight : Module("HoldRight", Category.PLAYER, hideModule = false) {

    override fun onEnable() {
        super.onEnable()
        // 模拟右键按下
        mc.gameSettings.keyBindUseItem.pressed = true
    }

    override fun onDisable() {
        super.onDisable()
        // 释放右键
        mc.gameSettings.keyBindUseItem.pressed = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // 确保右键一直被按下
        mc.gameSettings.keyBindUseItem.pressed = true
    }
}

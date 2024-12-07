package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.MCGO
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoSpreadShoot : Module("NoSpreadShoot", Category.COMBAT, hideModule = false) {
    private val mc = Minecraft.getMinecraft()
    private val MCGOObject = MCGO()
    private val delay = IntegerValue("Delay", 0, 0..500)
    private var lastSwitchTime = 0L

    override fun onEnable() {
        super.onEnable()
        lastSwitchTime = System.currentTimeMillis()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSwitchTime >= delay.get()) {
            // 切换到下一个槽位
            val currentSlot = mc.thePlayer.inventory.currentItem
            val nextSlot = if (currentSlot == 0) 1 else 0
            MCGOObject.switchToInventorySlot(nextSlot)
            lastSwitchTime = currentTime
        }
    }

}

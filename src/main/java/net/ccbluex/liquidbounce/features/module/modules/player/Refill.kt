/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.CLICK_TIMER
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.hasItemAgePassed
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.intValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow

object Refill : Module("Refill", Category.PLAYER, hideModule = false) {
    private val delay by intValue("Delay", 400, 10..1000)

    private val minItemAge by intValue("MinItemAge", 400, 0..1000)

    private val mode by choices("Mode", arrayOf("Swap", "Merge"), "Swap")

    private val invOpen by _boolean("InvOpen", false)
    private val simulateInventory by _boolean("SimulateInventory", false) { !invOpen }

    private val noMove by InventoryManager.noMoveValue
    private val noMoveAir by InventoryManager.noMoveAirValue
    private val noMoveGround by InventoryManager.noMoveGroundValue

    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (!CLICK_TIMER.hasTimePassed(delay))
            return

        if (invOpen && mc.currentScreen !is GuiInventory)
            return

        if (!canClickInventory())
            return

        for (slot in 36..44) {
            val stack = mc.thePlayer.inventorySlot(slot).stack ?: continue
            if (stack.stackSize == stack.maxStackSize || !stack.hasItemAgePassed(minItemAge)) continue

            when (mode) {
                "Swap" -> {
                    val bestOption = mc.thePlayer.inventoryContainer.inventory.withIndex()
                        .filter { (index, searchStack) ->
                            index < 36 && searchStack != null && searchStack.stackSize > stack.stackSize
                                    && (ItemStack.areItemsEqual(stack, searchStack)
                                    || searchStack.item.javaClass.isAssignableFrom(stack.item.javaClass)
                                    || stack.item.javaClass.isAssignableFrom(searchStack.item.javaClass))
                        }.maxByOrNull { it.value.stackSize }

                    if (bestOption != null) {
                        val (index, betterStack) = bestOption

                        click(index, slot - 36, 2, betterStack)
                        break
                    }
                }

                "Merge" -> {
                    val bestOption = mc.thePlayer.inventoryContainer.inventory.withIndex()
                        .filter { (index, searchStack) ->
                            index < 36 && searchStack != null && ItemStack.areItemsEqual(stack, searchStack)
                        }.minByOrNull { it.value.stackSize }

                    if (bestOption != null) {
                        val (otherSlot, otherStack) = bestOption

                        click(otherSlot, 0, 0, otherStack)
                        click(slot, 0, 0, stack)

                        // Return items that couldn't fit into hotbar slot
                        if (stack.stackSize + otherStack.stackSize > stack.maxStackSize)
                            click(otherSlot, 0, 0, otherStack)

                        break
                    }
                }
            }
        }

        if (simulateInventory && serverOpenInventory && mc.currentScreen !is GuiInventory)
            serverOpenInventory = false
    }

    fun click(slot: Int, button: Int, mode: Int, stack: ItemStack) {
        if (simulateInventory) serverOpenInventory = true

        sendPacket(
            C0EPacketClickWindow(
                mc.thePlayer.openContainer.windowId, slot, button, mode, stack,
                mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)
            )
        )
    }
}
/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.isFirstInventoryClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.floatValue
import net.ccbluex.liquidbounce.value.intValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object AutoSoup : Module("AutoSoup", Category.COMBAT, hideModule = false) {

    private val health by floatValue("Health", 15f, 0f..20f)
    private val delay by intValue("Delay", 150, 0..500)

    private val openInventory by _boolean("OpenInv", true)
    private val startDelay by intValue("StartDelay", 100, 0..1000) { openInventory }
    private val autoClose by _boolean("AutoClose", false) { openInventory }
    private val autoCloseNoSoup by _boolean("AutoCloseNoSoup", true) { autoClose }
    private val autoCloseDelay by intValue("CloseDelay", 500, 0..1000) { openInventory && autoClose }

    private val simulateInventory by _boolean("SimulateInventory", false) { !openInventory }

    private val bowl by choices("Bowl", arrayOf("Drop", "Move", "Stay"), "Drop")

    private val timer = MSTimer()
    private val startTimer = MSTimer()
    private val closeTimer = MSTimer()

    private var canCloseInventory = false

    override val tag
        get() = health.toString()

    @EventTarget(priority = -1)
    fun onGameTick(event: GameTickEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (!timer.hasTimePassed(delay))
            return

        val soupInHotbar = InventoryUtils.findItem(36, 44, Items.mushroom_stew)

        if (thePlayer.health <= health && soupInHotbar != null) {
            SilentHotbar.selectSlotSilently(this, soupInHotbar, 1, true)

            thePlayer.sendUseItem(thePlayer.inventory.mainInventory[SilentHotbar.currentSlot])

            // Schedule slot switch the next tick as we violate vanilla logic if we do it now.
            TickScheduler += {
                if (bowl == "Drop") {
                    if (!SilentHotbar.isSlotModified(this)) {
                        SilentHotbar.selectSlotSilently(this, soupInHotbar, 0, true)
                    }

                    sendPacket(C07PacketPlayerDigging(DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                }

                SilentHotbar.resetSlot(this)
            }

            timer.reset()
            return
        }

        val bowlInHotbar = InventoryUtils.findItem(36, 44, Items.bowl)

        if (bowl == "Move" && bowlInHotbar != null) {
            if (openInventory && mc.currentScreen !is GuiInventory)
                return

            var bowlMovable = false

            for (i in 9..36) {
                val itemStack = thePlayer.inventory.getStackInSlot(i)

                if (itemStack == null || (itemStack.item == Items.bowl && itemStack.stackSize < 64)) {
                    bowlMovable = true
                    break
                }
            }

            if (bowlMovable) {
                if (simulateInventory)
                    serverOpenInventory = true

                mc.playerController.windowClick(0, bowlInHotbar, 0, 1, thePlayer)
            }
        }

        val soupInInventory = InventoryUtils.findItem(9, 35, Items.mushroom_stew)

        if (soupInInventory != null && InventoryUtils.hasSpaceInHotbar()) {
            if (isFirstInventoryClick && !startTimer.hasTimePassed(startDelay)) {
                // GuiInventory checks, have to be put separately due to problem with reseting timer.
                if (mc.currentScreen is GuiInventory)
                    return
            } else {
                // GuiInventory checks, have to be put separately due to problem with reseting timer.
                if (mc.currentScreen is GuiInventory)
                    isFirstInventoryClick = false

                startTimer.reset()
            }

            if (openInventory && mc.currentScreen !is GuiInventory)
                return

            canCloseInventory = false

            if (simulateInventory)
                serverOpenInventory = true

            mc.playerController.windowClick(0, soupInInventory, 0, 1, thePlayer)

            if (simulateInventory && mc.currentScreen !is GuiInventory)
                serverOpenInventory = false

            timer.reset()
            closeTimer.reset()
        } else {
            canCloseInventory = true
        }

        if (autoClose && canCloseInventory && closeTimer.hasTimePassed(autoCloseDelay)) {
            if (!autoCloseNoSoup && soupInInventory == null) return

            if (mc.currentScreen is GuiInventory) {
                mc.thePlayer?.closeScreen()
            }

            closeTimer.reset()
            canCloseInventory = false
        }
    }
}
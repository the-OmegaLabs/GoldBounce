/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
@file:Suppress("unused")

package net.ccbluex.liquidbounce.features.module.modules.world

import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner.canBeSortedTo
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner.isStackUseful
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.chestStealerCurrentSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.chestStealerLastSlot
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.countSpaceInInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.hasSpaceInInventory
import net.ccbluex.liquidbounce.utils.kotlin.CoroutineUtils.waitUntil
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.EntityLiving.getArmorPosition
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S30PacketWindowItems
import java.awt.Color
import kotlin.math.sqrt

object ChestStealer : Module("ChestStealer", Category.WORLD, hideModule = false) {

    private val smartDelay by boolean("SmartDelay", false)
    private val multiplier by int("DelayMultiplier", 120, 0..500) { smartDelay }
    private val smartOrder by boolean("SmartOrder", true) { smartDelay }

    private val simulateShortStop by boolean("SimulateShortStop", false)

    private val maxDelay: Int by object : IntegerValue("MaxDelay", 50, 0..500) {
        override fun isSupported() = !smartDelay
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay)
    }
    private val minDelay by object : IntegerValue("MinDelay", 50, 0..500) {
        override fun isSupported() = maxDelay > 0 && !smartDelay
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay)
    }

    private val startDelay by int("StartDelay", 50, 0..500)
    private val closeDelay by int("CloseDelay", 50, 0..500)

    private val noMove by InventoryManager.noMoveValue
    private val noMoveAir by InventoryManager.noMoveAirValue
    private val noMoveGround by InventoryManager.noMoveGroundValue

    private val chestTitle by boolean("ChestTitle", true)

    private val randomSlot by boolean("RandomSlot", true)

    private val progressBar by boolean("ProgressBar", true, subjective = true)

    val silentGUI by boolean("SilentGUI", false, subjective = true)

    val highlightSlot by boolean("Highlight-Slot", false, subjective = true) { !silentGUI }

    val backgroundRed by int("Background-R", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val backgroundGreen by int("Background-G", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val backgroundBlue by int("Background-B", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val backgroundAlpha by int(
        "Background-Alpha",
        255,
        0..255,
        subjective = true
    ) { highlightSlot && !silentGUI }

    val borderStrength by int("Border-Strength", 3, 1..5, subjective = true) { highlightSlot && !silentGUI }
    val borderRed by int("Border-R", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val borderGreen by int("Border-G", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val borderBlue by int("Border-B", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val borderAlpha by int("Border-Alpha", 255, 0..255, subjective = true) { highlightSlot && !silentGUI }

    private val chestDebug by choices("Chest-Debug", arrayOf("Off", "Text", "Notification"), "Off", subjective = true)
    private val itemStolenDebug by boolean("ItemStolen-Debug", false, subjective = true) { chestDebug != "Off" }

    private var progress: Float? = null
        set(value) {
            field = value?.coerceIn(0f, 1f)

            if (field == null)
                easingProgress = 0f
        }

    private var easingProgress = 0f

    private var receivedId: Int? = null

    private var stacks = emptyList<ItemStack?>()

    private suspend fun shouldOperate(): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
                return false

            if (mc.currentScreen !is GuiChest)
                return false

            if (mc.thePlayer?.openContainer?.windowId != receivedId)
                return false

            // Wait till NoMove check isn't violated
            if (canClickInventory())
                return true

            // If NoMove is violated, wait a tick and check again
            // If there is no delay, very weird things happen: https://www.guilded.gg/CCBlueX/groups/1dgpg8Jz/channels/034be45e-1b72-4d5a-bee7-d6ba52ba1657/chat?messageId=94d314cd-6dc4-41c7-84a7-212c8ea1cc2a
            delay(50)
        }
    }

    suspend fun stealFromChest() {
        if (!handleEvents()) return

        val thePlayer = mc.thePlayer ?: return
        mc.currentScreen as? GuiChest ?: return

        if (!shouldOperate()) return
        delay(startDelay.toLong())

        debug("Stealing items...")
        while (shouldOperate() && hasSpaceInInventory()) {
            var hasTaken = false

            val itemsToSteal = getItemsToSteal()

            itemsToSteal.forEachIndexed { index, (slot, stack, sortableTo) ->
                if (!shouldOperate() || !hasSpaceInInventory()) return@forEachIndexed

                hasTaken = true
                chestStealerCurrentSlot = slot
                val stealingDelay = if (smartDelay && index + 1 < itemsToSteal.size) {
                    val dist = squaredDistanceOfSlots(slot, itemsToSteal[index + 1].index)
                    val trueDelay = sqrt(dist.toDouble()) * multiplier
                    randomDelay(trueDelay.toInt(), trueDelay.toInt() + 20)
                } else {
                    randomDelay(minDelay, maxDelay)
                }
                if (itemStolenDebug) debug("Item: ${stack.displayName.lowercase()} | Slot: $slot | Delay: ${stealingDelay}ms")
                TickScheduler.scheduleClick(slot, sortableTo ?: 0, if (sortableTo != null) 2 else 1) {
                    progress = (index + 1) / itemsToSteal.size.toFloat()

                    if (!AutoArmor.canEquipFromChest()) return@scheduleClick

                    val item = stack.item
                    if (item is ItemArmor && thePlayer.inventory.armorInventory[getArmorPosition(stack) - 1] == null) {
                        TickScheduler += {
                            val hotbarStacks = thePlayer.inventory.mainInventory.take(9)
                            val newIndex = hotbarStacks.indexOfFirst { it?.getIsItemStackEqual(stack) == true }

                            if (newIndex != -1) AutoArmor.equipFromHotbarInChest(newIndex, stack)
                        }
                    }
                }

                delay(stealingDelay.toLong())
            }

            if (!hasTaken) {
                progress = 1f
                delay(closeDelay.toLong())
                TickScheduler += { SilentHotbar.resetSlot() }
                break
            }
            waitUntil(TickScheduler::isEmpty)
            stacks = thePlayer.openContainer.inventory  // 更新库存
        }
        TickScheduler.scheduleAndSuspend ({
            chestStealerCurrentSlot = -1
            chestStealerLastSlot = -1
            thePlayer.closeScreen()
            progress = null
            debug("Chest closed")
        })
    }


    private fun squaredDistanceOfSlots(from: Int, to: Int): Int {
        fun getCoords(slot: Int): IntArray {
            val x = slot % 9
            val y = slot / 9
            return intArrayOf(x, y)
        }

        val (x1, y1) = getCoords(from)
        val (x2, y2) = getCoords(to)
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
    }

    private data class ItemTakeRecord(
        val index: Int,
        val stack: ItemStack,
        val sortableToSlot: Int?
    )

    private fun getItemsToSteal(): MutableList<ItemTakeRecord> {
        val sortBlacklist = BooleanArray(9)
        var spaceInInventory = countSpaceInInventory()
        val itemsToSteal = stacks.dropLast(36) // 只处理箱子的前几个槽位（不包括玩家背包）
            .mapIndexedNotNullTo(ArrayList(32)) { index, stack ->
                stack ?: return@mapIndexedNotNullTo null

                if (index in TickScheduler) return@mapIndexedNotNullTo null  // 跳过已调度的槽位

                val mergeableCount = mc.thePlayer.inventory.mainInventory.sumOf { otherStack ->
                    otherStack?.takeIf { it.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, otherStack) }
                        ?.let { it.maxStackSize - it.stackSize } ?: 0
                }

                if (mergeableCount == 0 && spaceInInventory <= 0) return@mapIndexedNotNullTo null
                if (handleEvents() && !isStackUseful(stack, stacks, noLimits = mergeableCount >= stack.stackSize)) return@mapIndexedNotNullTo null
                var sortableTo: Int? = null
                if (handleEvents() && mergeableCount<=0) {
                    for (hotbarIndex in 0..8) {
                        if (!canBeSortedTo(hotbarIndex, stack.item)) continue
                        val hotbarStack = stacks.getOrNull(stacks.size - 9 + hotbarIndex)
                        if (hotbarStack == null || canBeSortedTo(hotbarIndex, hotbarStack.item)) {
                            sortableTo = hotbarIndex
                            sortBlacklist[hotbarIndex] = true
                            break
                        }
                    }
                }
                if (mergeableCount < stack.stackSize) spaceInInventory--

                ItemTakeRecord(index, stack, sortableTo)
            }.also { list ->
                if (randomSlot) list.shuffle() // 随机化物品顺序
                list.sortByDescending { it.stack.item is ItemArmor }
                if (AutoArmor.canEquipFromChest()) {
                    list.sortByDescending { it.stack.item is ItemArmor }
                }
                list.sortByDescending { it.stack.item == Items.slime_ball && it.stack.hasTagCompound() && it.stack.tagCompound.hasKey("ench") }
                if (smartOrder) {
                    sortBasedOnOptimumPath(list)
                }
            }

        return itemsToSteal
    }



    private fun sortBasedOnOptimumPath(itemsToSteal: MutableList<ItemTakeRecord>) {
        for (i in itemsToSteal.indices) {
            var nextIndex = i
            var minDistance = Int.MAX_VALUE
            var next: ItemTakeRecord? = null
            for (j in i + 1 until itemsToSteal.size) {
                val distance = squaredDistanceOfSlots(itemsToSteal[i].index, itemsToSteal[j].index)
                if (distance < minDistance) {
                    minDistance = distance
                    next = itemsToSteal[j]
                    nextIndex = j
                }
            }
            if (next != null) {
                itemsToSteal[nextIndex] = itemsToSteal[i + 1]
                itemsToSteal[i + 1] = next
            }
        }
    }

    // Progress bar
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val scaledResolution = ScaledResolution(mc)
        if (silentGUI && progress != null) {
            RenderUtils.drawLoadingCircle(
                scaledResolution.scaledWidth / 2f,
                scaledResolution.scaledHeight / 4f
            )
            return
        }

        // 原有进度条逻辑（当silentGUI关闭时）
        if (!progressBar || mc.currentScreen !is GuiChest || progress == null) return

        val (scaledWidth, scaledHeight) = ScaledResolution(mc)
        val minX = scaledWidth * 0.3f
        val maxX = scaledWidth * 0.7f
        val minY = scaledHeight * 0.75f
        val maxY = minY + 10f

        easingProgress += (progress!! - easingProgress) / 6f * event.partialTicks

        // 只在进度变化时渲染
        drawRect(minX - 2, minY - 2, maxX + 2, maxY + 2, Color(200, 200, 200).rgb)
        drawRect(minX, minY, maxX, maxY, Color(50, 50, 50).rgb)
        drawRect(minX, minY, minX + (maxX - minX) * easingProgress, maxY, Color.HSBtoRGB(easingProgress / 5, 1f, 1f) or 0xFF0000)
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is C0DPacketCloseWindow, is S2DPacketOpenWindow, is S2EPacketCloseWindow -> {
                receivedId = null
                progress = null
            }
            is S30PacketWindowItems -> {
                if (packet.func_148911_c() == 0) return

                if (receivedId != packet.func_148911_c()) {
                    debug("Chest opened with ${stacks.size} items")
                }

                receivedId = packet.func_148911_c()
                stacks = packet.itemStacks.toList()  // 更新物品列表
            }
        }
    }


    private fun debug(message: String) {
        if (chestDebug == "Off") return

        when (chestDebug.lowercase()) {
            "text" -> chat(message)
            "notification" -> hud.addNotification(Notification(message, 500F))
        }
    }
}

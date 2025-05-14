/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.player.Gapple
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.hasMotion
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.ReflectionUtil
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.packet.sendOffHandUseItem
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.float
import net.ccbluex.liquidbounce.value.int
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.*
import net.minecraft.network.PacketBuffer
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse


object NoSlow : Module("NoSlow", Category.MOVEMENT, gameDetecting = false) {

    private val swordMode by choices(
        "SwordMode",
        arrayOf(
            "None",
            "NCP",
            "UpdatedNCP",
            "AAC5",
            "SwitchItem",
            "InvalidC08",
            "Blink",
            "PostPlace",
            "GrimAC",
            "BlocksMC",
            "HYTBW32",
            "Hypixel"
        ),
        "None"
    )
    private val bmcTicks by int("BMC Ticks", 1, 1..20) { swordMode == "BlocksMC" }
    private val bmcOldOffset by boolean("BMC OldOffset", false) { swordMode == "BlocksMC" }
    private val reblinkTicks by int("ReblinkTicks", 10, 1..20) { swordMode == "Blink" }

    private val blockForwardMultiplier by float("BlockForwardMultiplier", 1f, 0.2F..1f)
    private val blockStrafeMultiplier by float("BlockStrafeMultiplier", 1f, 0.2F..1f)

    private val consumeMode by choices(
        "ConsumeMode",
        arrayOf("None", "UpdatedNCP", "AAC5", "SwitchItem", "InvalidC08", "Intave", "Drop", "HYTSW", "HYTBW32", "Hypixel"),
        "None"
    )

    private val consumeForwardMultiplier by float("ConsumeForwardMultiplier", 1f, 0.2F..1f)
    private val consumeStrafeMultiplier by float("ConsumeStrafeMultiplier", 1f, 0.2F..1f)
    private val consumeFoodOnly by boolean(
        "ConsumeFood",
        true
    ) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }
    private val consumeDrinkOnly by boolean(
        "ConsumeDrink",
        true
    ) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }

    private val bowPacket by choices(
        "BowMode",
        arrayOf("None", "UpdatedNCP", "AAC5", "SwitchItem", "InvalidC08"),
        "None"
    )

    private val bowForwardMultiplier by float("BowForwardMultiplier", 1f, 0.2F..1f)
    private val bowStrafeMultiplier by float("BowStrafeMultiplier", 1f, 0.2F..1f)

    // Blocks
    val soulSand by boolean("SoulSand", true)
    val liquidPush by boolean("LiquidPush", true)

    private var shouldSwap = false
    private var shouldBlink = true
    private var shouldNoSlow = false

    private var hasDropped = false
    private val BlinkTimer = TickTimer()
    private var slow = false
    private var randomFactor = 0f
    private var sent = false
    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem ?: return
        val isUsingItem = usingItemFunc()

        if (!hasMotion && !shouldSwap)
            return

        if (hyp()){
            if (player.isUsingItem && player.onGround) {
                event.y += 1E-14
            }
            if (shouldSwap && player.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, false)
                player.jump()
                shouldSwap = false
            }
        }


        if (isUsingItem || shouldSwap) {
            if (heldItem.item !is ItemSword && heldItem.item !is ItemBow && (consumeFoodOnly && heldItem.item is ItemFood ||
                        consumeDrinkOnly && (heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk))
            ) {
                val stack = mc.thePlayer.getHeldItem()
                when (consumeMode.lowercase()) {
                    "aac5" ->
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))

                    "switchitem" ->
                        if (event.eventState == EventState.PRE) {
                            updateSlot()
                        }

                    "updatedncp" ->
                        if (event.eventState == EventState.PRE && shouldSwap) {
                            updateSlot()
                            sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                            shouldSwap = false
                        }

                    "invalidc08" -> {
                        if (event.eventState == EventState.PRE) {
                            if (InventoryUtils.hasSpaceInInventory()) {
                                if (player.ticksExisted % 3 == 0)
                                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                            }
                        }
                    }

                    "intave" -> {
                        if (event.eventState == EventState.PRE) {
                            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP))
                        }
                    }

                    "hytbw32" -> {
                        if (event.eventState.stateName == "PRE") {
                            if (stack.getItem() is ItemFood) {
                                mc.getNetHandler().addToSendQueue(
                                    C07PacketPlayerDigging(
                                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                        BlockPos(mc.thePlayer.getPosition().up()),
                                        EnumFacing.UP
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (heldItem.item is ItemBow && (isUsingItem || shouldSwap)) {
            val stack = mc.thePlayer.getHeldItem()
            when (bowPacket.lowercase()) {
                "aac5" ->
                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        updateSlot()
                    }

                "updatedncp" ->
                    if (event.eventState == EventState.PRE && shouldSwap) {
                        updateSlot()
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                        shouldSwap = false
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }
            }
        }

        if (heldItem.item is ItemSword && isUsingItem) {
            val stack = mc.thePlayer.getHeldItem()
            when (swordMode.lowercase()) {
                "postplace" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                    } else {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))
                        sendOffHandUseItem.sendOffHandUseItem()
                    }

                "grimac" ->
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C17PacketCustomPayload("许锦良", PacketBuffer(Unpooled.buffer())))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    } else {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))
                        sendOffHandUseItem.sendOffHandUseItem()
                    }


                "ncp" ->
                    when (event.eventState) {
                        EventState.PRE -> sendPacket(
                            C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
                        )

                        EventState.POST -> sendPacket(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f
                            )
                        )

                        else -> return
                    }

                "updatedncp" ->
                    if (event.eventState == EventState.POST) {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                    }

                "aac5" ->
                    if (event.eventState == EventState.POST) {
                        sendPacket(
                            C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f)
                        )
                    }

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        updateSlot()
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }

                "BlocksMC" -> {
                    if (!sent) {
                        sent = true
                        if (player.onGround) {
                            player.jump()
                        }
                    }

                    val slot = player.inventory.currentItem
                    val item = player.inventory.getStackInSlot(slot)

                    if (item != null && (item.unlocalizedName.contains(
                            "apple",
                            true
                        ) || item.unlocalizedName.contains("bow", true) || item.unlocalizedName.contains(
                            "potion",
                            true
                        ))
                    ) {
                        randomFactor = if (bmcOldOffset) {
                            0.5f + (Math.random() * 0.44).toFloat()
                        } else {
                            (Math.random() * 0.96).toFloat()
                        }

                        val playerPosition = player.position
                        val adjustedY = if (playerPosition.y > 0) playerPosition.y - 255 else playerPosition.y + 255
                        val inter = Vec3(playerPosition.x.toDouble(), adjustedY.toDouble(), playerPosition.z.toDouble())

                        sendPacket(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(
                                    inter.xCoord.toInt(),
                                    inter.yCoord.toInt(),
                                    inter.zCoord.toInt()
                                ), 0, item, 0f, randomFactor, 0f
                            )
                        )

                        if (player.ticksExisted % bmcTicks == 0) {
                            // Additional logic if needed
                        }
                    }
                    return
                }

                "hytbw32" -> {
                    if (event.eventState.stateName == "PRE") {
                        if (stack.getItem() is ItemSword || stack.getItem() is ItemBow) {
                            mc.getNetHandler()
                                .addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem + 1))
                            mc.getNetHandler()
                                .addToSendQueue(C17PacketCustomPayload("sbhyt", PacketBuffer(Unpooled.buffer())))
                            mc.getNetHandler()
                                .addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        }
                    }
                    if (event.eventState.stateName == "POST") {
                        if (stack.getItem() is ItemSword || stack.getItem() is ItemBow) {
                            mc.getNetHandler()
                                .addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()))
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val player = mc.thePlayer ?: return

        if (event.isCancelled || shouldSwap)
            return
        if (hyp()){
            if (packet is C08PacketPlayerBlockPlacement) {
                if (player.heldItem?.item is ItemFood ||
                    player.heldItem?.item is ItemPotion ||
                    player.heldItem?.item is ItemBucketMilk) {

                    event.cancelEvent()
                    if (player.onGround) {
                        player.jump()
                    }
                    shouldSwap = true
                }
            }

            if (packet is S2FPacketSetSlot) {
                if (ReflectionUtil.getFieldValue<Int>(packet, "slot") == player.inventory.currentItem + 36) {
                    event.cancelEvent()
                    player.inventory.mainInventory[SilentHotbar.currentSlot] = packet.func_149174_e()
                }
            }
        }
        // Credit: @ManInMyVan
        // TODO: Not sure how to fix random grim simulation flag. (Seem to only happen in Loyisa).
        if (consumeMode == "Drop") {
            if (player.heldItem?.item !is ItemFood || !player.isMoving) {
                shouldNoSlow = false
                return
            }

            val isUsingItem = packet is C08PacketPlayerBlockPlacement && packet.placedBlockDirection == 255

            if (!player.isUsingItem) {
                shouldNoSlow = false
                hasDropped = false
            }

            if (isUsingItem && !hasDropped) {
                sendPacket(C07PacketPlayerDigging(DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                shouldNoSlow = false
                hasDropped = true
            } else if (packet is S2FPacketSetSlot && player.isUsingItem) {
                if (packet.func_149175_c() != 0 || packet.func_149173_d() != SilentHotbar.currentSlot + 36) return

                event.cancelEvent()
                shouldNoSlow = true

                player.itemInUse = packet.func_149174_e()
                if (!player.isUsingItem) player.itemInUseCount = 0
                player.inventory.mainInventory[SilentHotbar.currentSlot] = packet.func_149174_e()
            }
        }
        if (consumeMode == "HYTSW") {
            val heldItem = player.heldItem?.item

            // 仅处理食物类物品
            if (heldItem is ItemFood || heldItem is ItemPotion || heldItem is ItemBucketMilk) {
                when (packet) {
                    // 拦截放置数据包
                    is C08PacketPlayerBlockPlacement -> {
                        if (packet.stack == player.heldItem && !slow) {
                            sendPacket(C07PacketPlayerDigging(DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                            slow = true
                            event.cancelEvent() // 取消原始数据包
                        }
                    }

                    // 处理服务端物品更新
                    is S2FPacketSetSlot -> {
                        if (slow && ReflectionUtil.getFieldValue<Int>(packet,"slot") == player.inventory.currentItem + 36) {
                            event.cancelEvent()
                            slow = false
                            player.inventory.mainInventory[SilentHotbar.currentSlot] = packet.func_149174_e()
                        }
                    }
                }
            }
        }
        if (swordMode == "Blink") {
            when (packet) {
                is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return

                is C07PacketPlayerDigging, is C02PacketUseEntity, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                    BlinkTimer.update()
                    if (shouldBlink && BlinkTimer.hasTimePassed(reblinkTicks) && (BlinkUtils.packetsReceived.isNotEmpty() || BlinkUtils.packets.isNotEmpty())) {
                        BlinkUtils.unblink()
                        BlinkTimer.reset()
                        shouldBlink = false
                    } else if (!BlinkTimer.hasTimePassed(reblinkTicks)) {
                        shouldBlink = true
                    }
                    return
                }

                // Flush on kb
                is S12PacketEntityVelocity -> {
                    if (mc.thePlayer.entityId == packet.entityID) {
                        BlinkUtils.unblink()
                        return
                    }
                }

                // Flush on explosion
                is S27PacketExplosion -> {
                    if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                        BlinkUtils.unblink()
                        return
                    }
                }

                is C03PacketPlayer -> {
                    if (swordMode == "Blink") {
                        if (player.isMoving) {
                            if (player.heldItem?.item is ItemSword && usingItemFunc()) {
                                if (shouldBlink)
                                    BlinkUtils.blink(packet, event)
                            } else {
                                shouldBlink = true
                                BlinkUtils.unblink()
                            }
                        }
                    }
                }
            }
        }

        when (packet) {
            is C08PacketPlayerBlockPlacement -> {
                if (packet.stack?.item != null && player.heldItem?.item != null && packet.stack.item == mc.thePlayer.heldItem?.item) {
                    if ((consumeMode == "UpdatedNCP" && (
                                packet.stack.item is ItemFood ||
                                        packet.stack.item is ItemPotion ||
                                        packet.stack.item is ItemBucketMilk)) ||
                        (bowPacket == "UpdatedNCP" && packet.stack.item is ItemBow)
                    ) {
                        shouldSwap = true
                    }
                }
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        if (hyp()) {
            if (player.isUsingItem && player.ticksExisted % 3 == 0) {
                sendPacket(C08PacketPlayerBlockPlacement(
                    BlockPos(-1, -1, -1),
                    255,
                    player.heldItem,
                    0f, 0f, 0f
                ))
            }
        }
        if (!mc.gameSettings.keyBindUseItem.isKeyDown) {
            slow = false
        }
    }
    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        if (heldItem !is ItemSword) {
            if (!consumeFoodOnly && heldItem is ItemFood ||
                !consumeDrinkOnly && (heldItem is ItemPotion || heldItem is ItemBucketMilk)
            ) {
                return
            }

            if (consumeMode == "Drop" && !shouldNoSlow)
                return
            if (hyp()) {
                event.forward = 1f
                event.strafe = 1f
            }
        }

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }
    private fun hyp(): Boolean {
        return (consumeMode == "Hypixel" || swordMode == "Hypixel")
    }
    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        if (consumeMode == "HYTSW" && slow) return 1.0f

        return when (item) {
            is ItemFood, is ItemPotion, is ItemBucketMilk ->
                if (isForward) consumeForwardMultiplier else consumeStrafeMultiplier
            is ItemSword ->
                if (isForward) blockForwardMultiplier else blockStrafeMultiplier
            is ItemBow ->
                if (isForward) bowForwardMultiplier else bowStrafeMultiplier
            else ->
                0.2f
        }
    }


    fun isUNCPBlocking() =
        swordMode == "UpdatedNCP" && mc.gameSettings.keyBindUseItem.isKeyDown && (mc.thePlayer.heldItem?.item is ItemSword)

    fun usingItemFunc() =
        mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && KillAura.blockStatus) || isUNCPBlocking())

    private fun updateSlot() {
        SilentHotbar.selectSlotSilently(this, (SilentHotbar.currentSlot + 1) % 9, immediate = true)
        SilentHotbar.resetSlot(this, true)
    }

    override val tag
        get() = "$swordMode $consumeMode $bowPacket"
}


package net.ccbluex.liquidbounce.features.module.modules.player

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import de.florianmichael.vialoadingbase.ViaLoadingBase
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.autoBlock
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.slotChangeAutoBlock
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.StuckUtils
import net.ccbluex.liquidbounce.utils.packet.BlinkUtils
import net.ccbluex.liquidbounce.utils.packet.sendOffHandUseItem.sendOffHandUseItem
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Items
import net.minecraft.network.play.client.*
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.gameevent.TickEvent

object GApple : Module("GApple",Category.PLAYER) {
    private val heal by int("health", 20, 0..40)
    private val sendDelay by int("SendDelay",3,1..10)
    private val sendOnceTicks = 1;
    private val stuck by boolean("Stuck",false)
    private val stopMove by boolean("StopMove",false)

    var noCancelC02 by boolean("NoCancelC02", false)
    var noC02 by boolean("NoC02", false) //这俩玩意本来是备着花雨庭更新grim搞得，结果就是他一直不更新，然后目前lastest Grim也绕不过。

    private val autoGapple by boolean("AutoGapple", false)

    private var slot = -1
    private var c03s = 0
    private var c02s = 0
    private var canStart = false

    var eating: Boolean = false //强制减速了。
    var pulsing: Boolean = false
    var target: EntityLivingBase? = null


    override fun onEnable() {

        c03s = 0

        slot = InventoryUtils.findItem(36, 45, Items.golden_apple)!!

        if (slot != -1) {
            slot = slot - 36
        }
    }


    override fun onDisable() {
        eating = false

        if (canStart) {
            pulsing = false
            eating = false
            BlinkUtils.stopBlink()
        }

        if (stuck) {
            StuckUtils.stopStuck()
        }

    }
    @EventTarget
    fun onTick(event:TickEvent){
        if(mc.thePlayer.health < heal) {
            if (!eating) {
                target = KillAura.target

                c03s = 0

                slot = InventoryUtils.findItem(36, 45, Items.golden_apple)!!

                if (slot != -1) {
                    slot -= 36
                }
            }

            if (MinecraftInstance.mc.thePlayer == null || MinecraftInstance.mc.thePlayer.isDead) {
                BlinkUtils.stopBlink()
                state = false
                return
            }
            if (slot == -1) {
                state = false
                return
            }
            if (eating) {
                if (stuck) {
                    StuckUtils.stuck()
                }
                if (!BlinkUtils.blinking) {
                    BlinkUtils.blink(
                        C09PacketHeldItemChange::class.java,
                        C0EPacketClickWindow::class.java,
                        C0DPacketCloseWindow::class.java
                    )
                    BlinkUtils.setCancelReturnPredicate(C07PacketPlayerDigging::class.java) { it -> (it as C07PacketPlayerDigging).status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM }
                    BlinkUtils.setCancelReturnPredicate(C08PacketPlayerBlockPlacement::class.java) { it ->
                        ((it as C08PacketPlayerBlockPlacement).position.y == -1)
                    }
                    BlinkUtils.setCancelReturnPredicate(C02PacketUseEntity::class.java) { it -> noCancelC02 }
                    BlinkUtils.setCancelReturnPredicate(C0APacketAnimation::class.java) { it -> noCancelC02 }
                    BlinkUtils.setCancelAction(C03PacketPlayer::class.java) { packet -> c03s++ }
                    BlinkUtils.setReleaseAction(C03PacketPlayer::class.java) { packet -> c03s-- }
                    BlinkUtils.setReleaseReturnPredicateMap(C02PacketUseEntity::class.java) { packet -> !eating && noC02 }
                    BlinkUtils.setCancelAction(C02PacketUseEntity::class.java) { packet -> c02s++ }
                    BlinkUtils.setReleaseAction(C02PacketUseEntity::class.java) { packet -> c02s-- }
                    canStart = true
                }
            } else {
                eating = true
//                chat("开始吃")
            }
            if (c03s >= 32) {
                eating = false
                pulsing = true
                BlinkUtils.resetBlackList()
                sendPacket(C09PacketHeldItemChange(slot), false)
                println("Start!")
                sendPacket(
                    C08PacketPlayerBlockPlacement(
                        MinecraftInstance.mc.thePlayer.inventoryContainer.getSlot(
                            slot + 36
                        ).stack
                    ), false
                )
                if (ViaLoadingBase.getInstance().targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_12_2)) {
                    sendPacket(
                        C08PacketPlayerBlockPlacement(BlockPos(-1, -2, -1), 255, null, 0.0f, 0.0f, 0.0f), false
                    )
                }
                BlinkUtils.stopBlink()
                println("Stop!")
//                chat("吃完了")
                sendPacket(C09PacketHeldItemChange(MinecraftInstance.mc.thePlayer.inventory.currentItem), false)
                pulsing = false
                if (autoGapple) {
                    c03s = 0

                    slot = InventoryUtils.findItem(36, 45, Items.golden_apple) ?: -1;

                    if (slot != -1) {
                        slot = slot - 36
                    }
                } else {
                    state = false
                }
//                if (autoBlock != "Off" && (!blinked || !net.ccbluex.liquidbounce.utils.client.BlinkUtils.isBlinking) && target != null) {
//                    if (autoBlock == "QuickMarco") {
//                        sendOffHandUseItem()
//                    } else if (autoBlock == "Packet") {
//                        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
//                    }
////                    chat("发送防砍包")
//                }
                return
            }

            if ((MinecraftInstance.mc.thePlayer.ticksExisted % sendDelay) == 0) {
                for (i in 0..sendOnceTicks) {
                    BlinkUtils.releasePacket(true)
                }
            }
        }else{
            eating = false

            if (canStart) {
                pulsing = false
                eating = false
                BlinkUtils.stopBlink()
            }

            if (stuck) {
                StuckUtils.stopStuck()
            }
        }
    }
    @EventTarget
    fun onMovementInput(event: MovementInputEvent){
        if(eating && stopMove){
            event.originalInput.moveStrafe = 0F
            event.originalInput.moveForward = 0F
        }
    }
}
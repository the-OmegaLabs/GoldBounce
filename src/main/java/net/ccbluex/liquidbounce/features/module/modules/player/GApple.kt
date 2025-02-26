
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.chat
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import java.util.*

object GApple : Module(name = "GApple",  category = Category.PLAYER) {

    private val modeValue = ListValue("Mode", arrayOf("Auto", "LegitAuto", "Legit", "Head"), "Auto")
    private val percent = FloatValue("HealthPercent", 1.0f,75.0f..100.0f,  )
    private val min = IntegerValue("MinDelay", 5000,1..75)
    private val max = IntegerValue("MaxDelay", 5000,1..125)
    private val regenSec = FloatValue("MinRegenSec",10.0f, 0.0f..4.6f)
    private val groundCheck = BoolValue("OnlyOnGround", false)
    private val waitRegen = BoolValue("WaitRegen", true)
    private val invCheck = BoolValue("InvCheck", false)
    private val absorpCheck = BoolValue("NoAbsorption", true)
    private val AlertValue = BoolValue("Alert", false)
    val timer = MSTimer()
    private var eating = -1
    var delay = 0
    var isDisable = false
    var tryHeal = false
    var prevSlot = -1
    var switchBack = false
    override fun onEnable() {
        eating = -1
	prevSlot = -1
	switchBack = false
        timer.reset()
        isDisable = false
        tryHeal = false
        delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        isDisable = true
        tryHeal = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (eating != -1 && packet is C03PacketPlayer) {
            eating++
        } else if (packet is S09PacketHeldItemChange || packet is C09PacketHeldItemChange) {
            eating = -1
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (tryHeal) {
            when (modeValue.get().lowercase()) {
                "auto" -> {
                    val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                    if (gappleInHotbar != -1) {
                        if (gappleInHotbar != null) {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(gappleInHotbar - 36))
                        }
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        repeat(35) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                        }
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        if (AlertValue.get()) chat("Gapple eaten")
                        tryHeal = false
                        timer.reset()
                        delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
                    }else {
                        tryHeal = false
                    }
                }
                "legitauto" -> {
                    if (eating == -1) {
                        val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                        if(gappleInHotbar == -1) {
                            tryHeal = false
                            return
                        }
                        if (gappleInHotbar != null) {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(gappleInHotbar - 36))
                        }
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        eating = 0
                    } else if (eating > 35) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
                    }
                }
                "legit" -> {
                    if (eating == -1) {
                        val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
                        if(gappleInHotbar == -1) {
                            tryHeal = false
                            return
                        }
                        if (prevSlot == -1)
                            prevSlot = mc.thePlayer.inventory.currentItem

                        if (gappleInHotbar != null) {
                            mc.thePlayer.inventory.currentItem = gappleInHotbar - 36
                        }
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        eating = 0
                    } else if (eating > 35) {
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
                    }
                }
                "head" -> {
                    val headInHotbar = InventoryUtils.findItem(36, 45, Items.skull)
                    if (headInHotbar != -1) {
                        if (headInHotbar != null) {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(headInHotbar - 36))
                        }
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        timer.reset()
                        tryHeal = false
                        delay = MathHelper.getRandomIntegerInRange(Random(), min.get(), max.get())
                    }else {
                        tryHeal = false
                    }
                }
            }
        }
        if (mc.thePlayer.ticksExisted <= 10 && isDisable) {
            isDisable = false
        }
        val absorp = MathHelper.ceiling_double_int(mc.thePlayer.absorptionAmount.toDouble())


        if (!tryHeal && prevSlot != -1) {
            if (!switchBack) {
                switchBack = true
                return
            }
            mc.thePlayer.inventory.currentItem = prevSlot
            prevSlot = -1;
            switchBack = false
        }

        if ((groundCheck.get() && !mc.thePlayer.onGround) || (invCheck.get() && mc.currentScreen is GuiContainer) || (absorp > 0 && absorpCheck.get()))
            return
        if (waitRegen.get() && mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).duration > regenSec.get() * 20.0f)
            return
        if (!isDisable && (mc.thePlayer.health <= (percent.get() / 100.0f) * mc.thePlayer.maxHealth) && timer.hasTimePassed(delay.toLong())) {
            if (tryHeal)
                return
            tryHeal = true
        }
    }

    override val tag: String
        get() = modeValue.get()
}

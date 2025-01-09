package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Eagle
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.init.Blocks.air
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper

object LegitScaffold : Module("LegitScaffold", Category.WORLD) {
    private val sneakDelay by int("SneakDelay", 0, 0..100)
    private val onlyWhenLookingDown by boolean("OnlyWhenLookingDown", false)
    private val lookDownThreshold by float("LookDownThreshold", 45f, 0f..90f) { onlyWhenLookingDown }
    val speed by int("Speed", 0, 0..4)
    val onlyBlocks by boolean("OnlyBlocks", true)
    val facingBlocks by boolean("OnlyWhenFacingBlocks", true)
    private val sneakTimer = MSTimer()
    private var sneakOn = false
    override fun onEnable() {
        super.onEnable()
        // 模拟右键按下
        mc.gameSettings.keyBindUseItem.pressed = true
    }

    override fun onDisable() {
        super.onDisable()
        // 释放右键
        mc.gameSettings.keyBindUseItem.pressed = false
        if (mc.thePlayer == null)
            return

        LegitScaffold.sneakOn = false

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // 确保右键一直被按下
        mc.gameSettings.keyBindUseItem.pressed = true
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.onGround && getBlock(BlockPos(thePlayer).down()) == air) {
            val shouldSneak = !LegitScaffold.onlyWhenLookingDown || thePlayer.rotationPitch >= LegitScaffold.lookDownThreshold

            if (shouldSneak && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
                if (LegitScaffold.sneakTimer.hasTimePassed(LegitScaffold.sneakDelay)) {
                    mc.gameSettings.keyBindSneak.pressed = true
                    LegitScaffold.sneakTimer.reset()
                    LegitScaffold.sneakOn = false
                }
            } else {
                mc.gameSettings.keyBindSneak.pressed = false
            }

            LegitScaffold.sneakOn = true
        } else {
            if (LegitScaffold.sneakOn) {
                mc.gameSettings.keyBindSneak.pressed = false
                LegitScaffold.sneakOn = false
            }
        }

        if (!LegitScaffold.sneakOn && mc.currentScreen !is Gui) mc.gameSettings.keyBindSneak.pressed =
            GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)

    }
}

package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.render.NLCrosshair
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.playWavSound
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Items
import net.minecraft.item.ItemSword
import org.lwjgl.input.Keyboard

object AuraZoom : Module("AuraZoom", Category.FUN) {

    var zoomPhase = 0
    var wasKeyDown = false

    val autoThirdPerson = BoolValue("Auto ThirdPerson", true)
    val autoFirstPerson = BoolValue("Auto FirstPerson", true)
    val onlySword       = BoolValue("Only on Sword", true)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val useKeyDown = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)

        // detect rising edge of right‑click
        if (useKeyDown && !wasKeyDown) {
            cycleZoom()
        }
        wasKeyDown = useKeyDown
    }

    private fun cycleZoom() {
        // if onlySword is on, ensure held item is a sword
        if (onlySword.get()) {
            val held = mc.thePlayer?.heldItem?.item
            if (held !is ItemSword && held != Items.diamond_sword &&
                held != Items.iron_sword && held != Items.golden_sword &&
                held != Items.stone_sword&& held != Items.wooden_sword) {
                return
            }
        }

        // advance phase
        zoomPhase = (zoomPhase + 1) % 3

        // first, turn everything off
        setAll(false)

        when (zoomPhase) {
            1 -> {
                // stage 1: Crosshair + KillAura
                setModule(KillAura, true)
                setModule(NLCrosshair, true)
                applyPerspective()
            }
            2 -> {
                // stage 2: stage 1 + Speed
                setModule(KillAura, true)
                setModule(NLCrosshair, true)
                setModule(Speed, true)
                applyPerspective()
            }
            0 -> {
                applyPerspective()
            }
        }

        // play zoom sound
        playWavSound("/assets/minecraft/liquidbounce/sounds/zoom.wav")
    }

    private fun applyPerspective() {
        // switch to third‑person if enabled
        if (autoThirdPerson.get()) {
            mc.gameSettings.thirdPersonView = 1
        }
        // switch back to first‑person if enabled (for stage reset or if user wants)
        if (autoFirstPerson.get() && zoomPhase == 0) {
            mc.gameSettings.thirdPersonView = 0
        }
    }

    private fun setModule(module: Module, state: Boolean) {
        LiquidBounce.moduleManager.getModule(module.name)?.state = state
    }

    private fun setAll(state: Boolean) {
        setModule(KillAura, state)
        setModule(NLCrosshair, state)
        setModule(Speed, state)
    }
}

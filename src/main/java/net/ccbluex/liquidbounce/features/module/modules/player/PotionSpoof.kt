/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value._boolean
import net.minecraft.potion.Potion.*
import net.minecraft.potion.PotionEffect

object PotionSpoof : Module("PotionSpoof", Category.PLAYER, hideModule = false) {

    private val level by object : IntegerValue("PotionLevel", 2, 1..5) {
        override fun onChanged(oldValue: Int, newValue: Int) = onDisable()
    }

    private val speedValue = _boolean("Speed", false)
    private val moveSlowDownValue = _boolean("Slowness", false)
    private val hasteValue = _boolean("Haste", false)
    private val digSlowDownValue = _boolean("MiningFatigue", false)
    private val blindnessValue = _boolean("Blindness", false)
    private val strengthValue = _boolean("Strength", false)
    private val jumpBoostValue = _boolean("JumpBoost", false)
    private val weaknessValue = _boolean("Weakness", false)
    private val regenerationValue = _boolean("Regeneration", false)
    private val witherValue = _boolean("Wither", false)
    private val resistanceValue = _boolean("Resistance", false)
    private val fireResistanceValue = _boolean("FireResistance", false)
    private val absorptionValue = _boolean("Absorption", false)
    private val healthBoostValue = _boolean("HealthBoost", false)
    private val poisonValue = _boolean("Poison", false)
    private val saturationValue = _boolean("Saturation", false)
    private val waterBreathingValue = _boolean("WaterBreathing", false)

    private val potionMap = mapOf(
        moveSpeed.id to speedValue,
        moveSlowdown.id to moveSlowDownValue,
        digSpeed.id to hasteValue,
        digSlowdown.id to digSlowDownValue,
        blindness.id to blindnessValue,
        damageBoost.id to strengthValue,
        jump.id to jumpBoostValue,
        weakness.id to weaknessValue,
        regeneration.id to regenerationValue,
        wither.id to witherValue,
        resistance.id to resistanceValue,
        fireResistance.id to fireResistanceValue,
        absorption.id to absorptionValue,
        healthBoost.id to healthBoostValue,
        poison.id to poisonValue,
        saturation.id to saturationValue,
        waterBreathing.id to waterBreathingValue
    )

    override fun onDisable() {
        mc.thePlayer ?: return

        mc.thePlayer.activePotionEffects
            .filter { it.duration == 0 && potionMap[it.potionID]?.get() == true }
            .forEach { mc.thePlayer.removePotionEffect(it.potionID) }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) =
        potionMap.forEach { (potionId, value) ->
            if (value.get())
                mc.thePlayer.addPotionEffect(PotionEffect(potionId, 0, level - 1, false, false))
            else if (mc.thePlayer.activePotionEffects.any { it.duration == 0 && it.potionID == potionId })
                mc.thePlayer.removePotionEffect(potionId)
        }
}
package net.ccbluex.liquidbounce.features.module.modules.hud

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils.drawGlow
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils水影加加
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.cos
import kotlin.math.sin

object PotionEffect : Module("PotionEffect", Category.HUD) {

    // --- User-configurable values ---
    private val backgroundAlpha = IntegerValue("BackgroundAlpha", 120, 0..255)
    private val leftRectRadius = FloatValue("LeftRectValue", 0f,0f..10f)
    private val rightRectRadius = FloatValue("RightRectValue", 0f, 0f..10f)
    private val animationSpeed = FloatValue("AnimationSpeed", 0.15f, 0.01f..0.5f)
    private val barWidth = FloatValue("BarWidth", 5f, 1f..10f)
    private val xOffset = FloatValue("X-Offset", 5f, 0f..50f)
    private val yOffset = FloatValue("Y-Offset", 0f, -50f..50f)
    private val spacing = FloatValue("Spacing", 5f, 0f..10f)

    // --- Internal state ---
    private val activePotions = CopyOnWriteArrayList<AnimatedPotion>()
    private val inventoryTexture = ResourceLocation("textures/gui/container/inventory.png")

    // --- Main Rendering Logic ---
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.thePlayer == null) return

        val sr = ScaledResolution(mc)

        // 1. Synchronize our list with the player's active effects
        updatePotionList()

        if (activePotions.isEmpty()) return

        // 2. Update animations and remove old effects
        activePotions.forEach { it.update(animationSpeed.get()) }
        activePotions.removeIf { it.isReadyToRemove() }

        // Sort by remaining duration to keep the list stable
        activePotions.sortBy { it.effect.duration }

        // 3. Calculate positioning for vertical centering
        val totalHeight = activePotions.sumByDouble { (it.boxHeight + spacing.get()) * it.animationY.toDouble() }.toFloat() - spacing.get()
        var currentY = (sr.scaledHeight / 2f) - (totalHeight / 2f) + yOffset.get()

        // 4. Draw each potion effect
        for (potion in activePotions) {
            potion.draw(currentY, xOffset.get())
            currentY += (potion.boxHeight + spacing.get()) * potion.animationY
        }
    }

    /**
     *  Checks for new potions to add and old ones to remove.
     */
    private fun updatePotionList() {
        val playerEffects = mc.thePlayer.activePotionEffects.filter { Potion.potionTypes[it.potionID] != null }

        // Mark potions that are no longer active on the player for removal
        activePotions.forEach { animatedPotion ->
            if (playerEffects.none { it.potionID == animatedPotion.effect.potionID }) {
                animatedPotion.isMarkedForRemoval = true
            }
        }

        // Add new potions that are not yet in our list
        playerEffects.forEach { playerEffect ->
            if (activePotions.none { it.effect.potionID == playerEffect.potionID }) {
                activePotions.add(AnimatedPotion(playerEffect))
            } else {
                // Update existing effect instances in case they are reapplied
                activePotions.find { it.effect.potionID == playerEffect.potionID }?.effect = playerEffect
            }
        }
    }

    /**
     * A helper class to manage the state and animation of a single potion effect.
     */
    private class AnimatedPotion(var effect: PotionEffect) {
        val boxWidth = 120f
        val boxHeight = 32f

        var isMarkedForRemoval = false
        private var animationX = -boxWidth - 10f
        var animationY = 0f // For fade-in/out effect on height

        // Get potion data (color, etc.) from our map
        private val potion: Potion = Potion.potionTypes[effect.potionID]
        private val dataColor = potionColorMap[effect.potionID] ?: Color.GRAY

        fun update(speed: Float) {
            val targetX = if (isMarkedForRemoval) -boxWidth - 10f else 0f
            val targetY = if (isMarkedForRemoval) 0f else 1f
            animationX = AnimationUtils水影加加.animate(targetX, animationX, speed)
            animationY = AnimationUtils水影加加.animate(targetY, animationY, speed)
        }

        fun isReadyToRemove(): Boolean {
            return isMarkedForRemoval && animationX <= -boxWidth
        }

        fun draw(y: Float, xOffset: Float) {
            val startX = xOffset + animationX
            val startY = y
            val endX = startX + boxWidth
            val endY = startY + boxHeight

            // Don't draw if fully off-screen
            if (startX > boxWidth) return

            val animatedHeight = boxHeight * animationY
            if (animatedHeight < 1) return

            val bgColor = Color(40, 40, 40, (backgroundAlpha.get() * animationY).toInt())

            // Draw the main container with left-rounded corners
            drawRoundedRect(startX, startY, endX, startY + animatedHeight, bgColor.rgb, leftRectRadius.get())
            drawGlow(startX,startY,endX-startX,endY-startY,8,bgColor)
            // Draw the colored side-bar
            drawRoundedRect(startX, startY, startX + barWidth.get(), startY + animatedHeight, dataColor.rgb, rightRectRadius.get())

            // Prepare for text and icon drawing
            glPushMatrix()
            // Clip content to prevent it from drawing outside the animated box
            RenderUtils.makeScissorBox(startX, startY, endX, startY + animatedHeight)
            glEnable(GL_SCISSOR_TEST)

            // Draw Potion Icon
            if (potion.hasStatusIcon()) {
                mc.textureManager.bindTexture(inventoryTexture)
                GlStateManager.color(1f, 1f, 1f, animationY)
                val iconX = potion.statusIconIndex % 8 * 18
                val iconY = 198 + potion.statusIconIndex / 8 * 18
                RenderUtils.drawTexturedModalRect((startX + 8f).toInt(), (startY + (animatedHeight - 18) / 2).toInt(), iconX, iconY, 18, 18, 0.0F)
            }

            // Draw Potion Name
            val textX = startX + 30
            val textY = startY + (animatedHeight / 2) - 8
            val nameColor = Color(dataColor.red, dataColor.green, dataColor.blue, (255 * animationY).toInt()).rgb
            // *** CORRECTED NAME LOGIC ***
            val potionName = I18n.format(potion.name)
            val displayName = potionName + if (effect.amplifier > 0) " ${effect.amplifier + 1}" else ""
            Fonts.font40.drawString(displayName, textX, textY, nameColor)

            // Draw Duration
            val duration = effect.duration / 20 // Ticks to seconds
            val durationText = String.format("%02d:%02d", duration / 60, duration % 60)
            val durationColor = if (duration <= 10) {
                Color(255, 80, 80, (255 * animationY).toInt()).rgb
            } else {
                Color(255, 255, 255, (200 * animationY).toInt()).rgb
            }
            Fonts.font35.drawString(durationText, textX, textY + 11, durationColor)

            // Cleanup
            glDisable(GL_SCISSOR_TEST)
            glPopMatrix()
        }
    }


    /**
     * Maps Minecraft potion IDs to specific colors for the UI.
     */
    private val potionColorMap = mapOf(
        // Positive Effects
        Potion.moveSpeed.id to Color(124, 175, 198),       // Speed
        Potion.digSpeed.id to Color(217, 192, 67),        // Haste
        Potion.damageBoost.id to Color(204, 91, 89),       // Strength
        Potion.jump.id to Color(34, 255, 76),           // Jump Boost
        Potion.regeneration.id to Color(221, 122, 146),     // Regeneration
        Potion.resistance.id to Color(153, 69, 59),        // Resistance
        Potion.fireResistance.id to Color(228, 154, 58),   // Fire Resistance
        Potion.waterBreathing.id to Color(46, 82, 153),    // Water Breathing
        Potion.invisibility.id to Color(127, 131, 146),    // Invisibility
        Potion.nightVision.id to Color(31, 31, 165),       // Night Vision
        Potion.healthBoost.id to Color(248, 125, 35),      // Health Boost
        Potion.absorption.id to Color(36, 147, 147),       // Absorption
        Potion.saturation.id to Color(248, 36, 35),        // Saturation

        // Negative Effects
        Potion.moveSlowdown.id to Color(90, 108, 127),      // Slowness
        Potion.digSlowdown.id to Color(74, 66, 23),       // Mining Fatigue
        Potion.weakness.id to Color(72, 77, 77),           // Weakness
        Potion.poison.id to Color(78, 157, 48),            // Poison
        Potion.wither.id to Color(53, 42, 39),             // Wither
        Potion.hunger.id to Color(88, 83, 22),             // Hunger
        Potion.confusion.id to Color(85, 29, 74),          // Nausea
        Potion.blindness.id to Color(31, 31, 36),          // Blindness
    )
}
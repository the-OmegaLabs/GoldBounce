/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.hotBarSlot
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.init.Items.egg
import net.minecraft.init.Items.snowball

object AutoProjectile : Module("AutoProjectile", Category.COMBAT, hideModule = false) {
    private val facingEnemy by _boolean("FacingEnemy", true)

    private val mode by choices("Mode", arrayOf("Normal", "Smart"), "Normal")
    private val range by floatValue("Range", 8F, 1F..20F)
    private val disableRange by floatValue("DisableRange", 3F, 0F..20F)
    private val throwDelay by intValue("ThrowDelay", 1000, 50..2000) { mode != "Smart" }

    private val minThrowDelay: IntegerValue = object : IntegerValue("MinThrowDelay", 1000, 50..2000) {
        override fun isSupported() = mode == "Smart"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxThrowDelay.get())
    }

    private val maxThrowDelay: IntegerValue = object : IntegerValue("MaxThrowDelay", 1500, 50..2000) {
        override fun isSupported() = mode == "Smart"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minThrowDelay.get())
    }

    private val switchBackDelay by intValue("SwitchBackDelay", 500, 50..2000)

    private val throwTimer = MSTimer()
    private val projectilePullTimer = MSTimer()

    private var projectileInUse = false
    private var switchBack = -1

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        val usingProjectile =
            (player.isUsingItem && (player.heldItem?.item == snowball || player.heldItem?.item == egg)) || projectileInUse

        if (usingProjectile) {
            if (projectilePullTimer.hasTimePassed(switchBackDelay)) {
                if (switchBack != -1 && player.inventory.currentItem != switchBack) {
                    player.inventory.currentItem = switchBack

                    mc.playerController.syncCurrentPlayItem()
                } else {
                    player.stopUsingItem()
                }

                switchBack = -1
                projectileInUse = false

                throwTimer.reset()
            }
        } else {
            var throwProjectile = false

            if (facingEnemy) {
                var facingEntity = mc.objectMouseOver?.entityHit
                var facingEntity2: Any? = null
                if(disableRange != 0F) {
                    facingEntity2 = mc.objectMouseOver?.entityHit
                }
                if (facingEntity == null) {
                    facingEntity = raycastEntity(range.toDouble()) { isSelected(it, true) }
                    if(disableRange != 0F) {
                        facingEntity2 = raycastEntity(disableRange.toDouble()) { isSelected(it, true) }
                    }
                }

                if (isSelected(facingEntity, true) && (facingEntity != facingEntity2 || disableRange==0F)) {
                    throwProjectile = true
                }
            } else {
                throwProjectile = true
            }

            if (throwProjectile) {
                if (mode == "Normal" && throwTimer.hasTimePassed(throwDelay)) {
                    if (player.heldItem?.item != snowball && player.heldItem?.item != egg) {
                        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

                        switchBack = player.inventory.currentItem

                        player.inventory.currentItem = projectile
                        mc.playerController.syncCurrentPlayItem()
                    }

                    throwProjectile()
                }

                val randomThrowDelay = RandomUtils.nextInt(minThrowDelay.get(), maxThrowDelay.get())
                if (mode == "Smart" && throwTimer.hasTimePassed(randomThrowDelay)) {
                    if (player.heldItem?.item != snowball && player.heldItem?.item != egg) {
                        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

                        switchBack = player.inventory.currentItem

                        player.inventory.currentItem = projectile
                        mc.playerController.syncCurrentPlayItem()
                    }

                    throwProjectile()
                }
            }
        }
    }

    /**
     * Throw projectile (snowball/egg)
     */
    private fun throwProjectile() {
        val player = mc.thePlayer ?: return
        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

        player.inventory.currentItem = projectile

        mc.playerController.sendUseItem(player, mc.theWorld, player.hotBarSlot(projectile).stack)

        projectileInUse = true
        projectilePullTimer.reset()
    }

    /**
     * Reset everything when disabled
     */
    override fun onDisable() {
        throwTimer.reset()
        projectilePullTimer.reset()
        projectileInUse = false
        switchBack = -1
    }

    /**
     * HUD Tag
     */
    override val tag
        get() = mode
}

package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications
import net.ccbluex.liquidbounce.value.int

object HalfLifeWarn : Module("HalfLifeWarn", Category.MISC, gameDetecting = true, hideModule = false) {
    private val healthValue by int("Health", 7, 1.. 20)

    private var canWarn = true

    override fun onEnable() {
        canWarn = true
    }

    override fun onDisable() {
        canWarn = true
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if (mc.thePlayer.health <= healthValue) {
            if (canWarn) {
                addNotification(Notification("主播快回血，你要死了。",3000F, "没血了", Notifications.SeverityType.WARNING))

                canWarn = false
            }
        } else {
            canWarn = true
        }
    }
    @EventTarget
    fun onAttack(event: AttackEvent){
        if (mc.thePlayer.health <= healthValue) {
            if (canWarn) {
                addNotification(Notification("你都他妈就这么点血了你打你妈呢",3000F, "别他妈打人了", Notifications.SeverityType.WARNING))

                canWarn = false
            }
        } else {
            canWarn = true
        }
    }
}
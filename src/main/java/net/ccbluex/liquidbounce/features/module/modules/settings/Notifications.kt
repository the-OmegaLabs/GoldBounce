package net.ccbluex.liquidbounce.features.module.modules.settings

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.ListValue

object Notifications : Module("Notifications", Category.SETTINGS, gameDetecting = false, hideModule = false) {
    val notiMode = ListValue("Mode", arrayOf("Normao", "Watermark", "Noti3"), "Noti")
}
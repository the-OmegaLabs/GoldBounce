/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.CameraPositionEvent
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value._boolean
import net.ccbluex.liquidbounce.value.floatValue

object CameraView : Module("CameraView", Category.RENDER, gameDetecting = false, hideModule = false) {

    private val customY by floatValue("CustomY", 0f, -10f..10f)
    private val saveLastGroundY by _boolean("SaveLastGroundY", true)
    private val onScaffold by _boolean("OnScaffold", true)
    private val onF5 by _boolean("OnF5", true)

    private var launchY: Double ?= null

    override fun onEnable() {
        mc.thePlayer?.run {
            launchY = posY
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.POST) return

        mc.thePlayer?.run {
            if (!saveLastGroundY || (onGround || ticksExisted == 1)) {
                launchY = posY
            }
        }
    }

    @EventTarget
    fun onCameraUpdate(event: CameraPositionEvent) {
        mc.thePlayer?.run {
            val currentLaunchY = launchY ?: return
            if (onScaffold && !handleEvents()) return
            if (onF5 && mc.gameSettings.thirdPersonView == 0) return

            event.withY(currentLaunchY + customY)
        }
    }
}
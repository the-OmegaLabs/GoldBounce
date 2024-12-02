/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float

object CameraView : Module("CameraView", Category.RENDER, gameDetecting = false, hideModule = false) {

    private val customY by float("CustomY", 0f, -10f..10f)
    private val saveLastGroundY by boolean("SaveLastGroundY", true)
    private val onScaffold by boolean("OnScaffold", true)
    private val onF5 by boolean("OnF5", true)

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
            if (onScaffold && !Scaffold.handleEvents()) return
            if (onF5 && mc.gameSettings.thirdPersonView == 0) return

            event.withY(currentLaunchY + customY)
        }
    }
}
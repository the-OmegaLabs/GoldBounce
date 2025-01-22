package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.util.MathHelper
import kotlin.math.sqrt

object BetterRotation : Module("BetterRotation", Category.RENDER, gameDetecting = false, hideModule = false) {
    var lastRYO = 0;
    var lastRYO2 = 0;
    var lastRYO3 = 0;
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val renderYaw = MathHelper.wrapAngleTo180_float(RotationUtils.serverRotation.yaw);
        val motionX = mc.thePlayer.motionX;
        val motionZ = mc.thePlayer.motionZ;
        val speed = sqrt(motionX * motionX + motionZ * motionZ);
        val setYaw = MathHelper.wrapAngleTo180_float(lastRYO2.toFloat());
        var newYaw = setYaw;
        if (speed > 0.03) newYaw = MathHelper.wrapAngleTo180_float((Math.atan2(motionZ, motionX) * (180 / Math.PI) - 90).toFloat());
        val yawDifference = MathHelper.wrapAngleTo180_float(newYaw - renderYaw);
        var increment = 0;
        increment = if (yawDifference < -50) {
            -50;
        } else if (yawDifference > 50) {
            50;
        } else {
            yawDifference.toInt();
        }
        val smoothIncrement = lastRYO3 + (increment - lastRYO3) * 0.03;
        lastRYO3 = smoothIncrement.toInt();
        lastRYO = MathHelper.wrapAngleTo180_float((renderYaw + smoothIncrement).toFloat()).toInt();
        mc.thePlayer.rotationYawHead = renderYaw;
        mc.thePlayer.renderYawOffset = lastRYO.toFloat();
        lastRYO2 = mc.thePlayer.renderYawOffset.toInt();
    }
}
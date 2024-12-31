package net.ccbluex.liquidbounce.utils.skid.moonlight.fireflies

import net.ccbluex.liquidbounce.utils.skid.moonlight.math.MathUtils
import net.minecraft.util.MathHelper

class FireFilesUtils(var anim: Float, var to: Float, var speed: Float) {
    var mc: Long

    init {
        this.mc = System.currentTimeMillis()
    }

    fun get_Anim(): Float {
        val count = ((System.currentTimeMillis() - this.mc) / 5L).toInt()
        if (count > 0) {
            this.mc = System.currentTimeMillis()
        }
        for (i in 0 until count) {
            this.anim = MathUtils.lerp(this.anim, this.to, this.speed)
        }
        return this.anim
    }

    val angleAnim: Float
        get() {
            val count = ((System.currentTimeMillis() - this.mc) / 5L).toInt()
            if (count > 0) {
                this.mc = System.currentTimeMillis()
            }
            for (i in 0 until count) {
                this.anim =
                    lerpAngle(this.anim, this.to, this.speed).toFloat()
            }
            return MathHelper.wrapAngleTo180_float(this.anim)
        }

    fun set_Anim(anim: Float) {
        this.anim = anim
        this.mc = System.currentTimeMillis()
    }

    fun lerpAngle(start: Float, end: Float, amount: Float): Double {
        val minAngle = (end - start + 180.0f) % 360.0f - 180.0f
        return (minAngle * amount + start).toDouble()
    }
}
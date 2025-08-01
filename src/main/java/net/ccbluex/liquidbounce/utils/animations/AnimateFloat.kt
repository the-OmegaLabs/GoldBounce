package net.ccbluex.liquidbounce.utils.animations

class AnimateFloat(var output: Float = 0f) {
    fun animate(target: Float, speed: Int) {
        output += (target - output) / speed
    }
}

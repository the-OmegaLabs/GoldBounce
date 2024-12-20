package net.ccbluex.liquidbounce.features.special

import org.lwjgl.Sys


class SmoothSneakingState {
    private var lastState = false
    private var isAnimationDone = false
    private var lastOperationTime = 0f
    private var lastX = 0f

    fun getSneakingHeightOffset(isSneaking: Boolean): Float {
        if (this.lastState == isSneaking) {
            if (this.isAnimationDone) {
                return if (isSneaking) -0.08f else 0.0f
            }
        } else {
            this.lastState = isSneaking
            this.isAnimationDone = false
        }
        val now = ((Sys.getTime() shl 3).toFloat()) / (Sys.getTimerResolution().toFloat())
        var timeDiff = now - this.lastOperationTime
        if (this.lastOperationTime == 0.0f) {
            timeDiff = 0.0f
        }
        this.lastOperationTime = now
        if (isSneaking) {
            if (this.lastX < 1.0f) {
                this.lastX += timeDiff
                if (this.lastX > 1.0f) {
                    this.lastX = 1.0f
                }
                return getDownY(this.lastX)
            }
            this.lastX = 1.0f
            this.isAnimationDone = true
            this.lastOperationTime = 0.0f
            return -0.08f
        }
        if (this.lastX > 0.0f) {
            this.lastX -= timeDiff
            if (this.lastX < 0.0f) {
                this.lastX = 0.0f
            }
            return getUpY(this.lastX)
        }
        this.lastX = 0.0f
        this.isAnimationDone = true
        this.lastOperationTime = 0.0f
        return 0.0f
    }

    companion object {
        private fun getUpY(x: Float): Float {
            return (-0.08f) * x * x
        }

        private fun getDownY(x: Float): Float {
            val x2 = x - 1.0f
            return ((0.08f * x2) * x2) - 0.08f
        }
    }
}
package net.ccbluex.liquidbounce.utils.movement

import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.PlayerStorage.PlayerInput
import net.minecraft.util.MovementInput

data class DirectionalInput(
    val forwards: Boolean,
    val backwards: Boolean,
    val left: Boolean,
    val right: Boolean,
) {

    constructor(input: MovementInput) : this(
        input.moveForward > 0.0,
        input.moveForward < 0.0,
        input.moveStrafe > 0.0,
        input.moveStrafe < 0.0
    )

    constructor(movementForward: Float, movementSideways: Float) : this(
        forwards = movementForward > 0.0,
        backwards = movementForward < 0.0,
        left = movementSideways > 0.0,
        right = movementSideways < 0.0
    )

    override fun equals(other: Any?): Boolean {
        return other is DirectionalInput &&
            forwards == other.forwards &&
            backwards == other.backwards &&
            left == other.left &&
            right == other.right
    }

    override fun hashCode(): Int {
        var result = forwards.hashCode()
        result = 30 * result + backwards.hashCode()
        result = 30 * result + left.hashCode()
        result = 30 * result + right.hashCode()
        return result
    }

    val isMoving: Boolean
        get() = forwards || backwards || left || right

    companion object {
        val NONE = DirectionalInput(forwards = false, backwards = false, left = false, right = false)
        val FORWARDS = DirectionalInput(forwards = true, backwards = false, left = false, right = false)
        val BACKWARDS = DirectionalInput(forwards = false, backwards = true, left = false, right = false)
        val LEFT = DirectionalInput(forwards = false, backwards = false, left = true, right = false)
        val RIGHT = DirectionalInput(forwards = false, backwards = false, left = false, right = true)
    }
}

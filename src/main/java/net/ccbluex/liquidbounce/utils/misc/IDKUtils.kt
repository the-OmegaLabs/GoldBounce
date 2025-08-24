package net.ccbluex.liquidbounce.utils.misc

fun Float.roundToInt(): Int {
    if (this.isNaN()) throw IllegalArgumentException("Cannot round NaN to Int")
    val maxFloat = Int.MAX_VALUE.toFloat()
    val minFloat = Int.MIN_VALUE.toFloat()
    if (this >= maxFloat) return Int.MAX_VALUE
    if (this <= minFloat) return Int.MIN_VALUE

    return if (this >= 0f) {
        (this + 0.5f).toInt()
    } else {
        (this - 0.5f).toInt()
    }
}

fun Double.roundToInt(): Int {
    if (this.isNaN()) throw IllegalArgumentException("Cannot round NaN to Int")
    val maxDouble = Int.MAX_VALUE.toDouble()
    val minDouble = Int.MIN_VALUE.toDouble()
    if (this >= maxDouble) return Int.MAX_VALUE
    if (this <= minDouble) return Int.MIN_VALUE

    return if (this >= 0.0) {
        (this + 0.5).toInt()
    } else {
        (this - 0.5).toInt()
    }
}
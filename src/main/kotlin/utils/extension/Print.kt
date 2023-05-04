package utils.extension

import kotlin.math.roundToInt


fun printProgress(value: Float) {
    print("\r${"◼".repeat(value.roundToInt()) + "◻".repeat(100 - value.roundToInt())} ${String.format("%2.2f", value)}%")
}

fun printProgress(position: Float, unit: Int = 100) {
    val newValue = position / 100f * unit
    print("\r${"◼".repeat(newValue.roundToInt()) + "◻".repeat(unit - newValue.roundToInt())} ${String.format("%2.2f", position)}%")
}

fun printProgress(position: Float, duration: Float, unit: Int = 100) {
    val newValue = position / duration * unit
    print("\r${"◼".repeat(newValue.roundToInt()) + "◻".repeat(unit - newValue.roundToInt())} ${String.format("%2.2f", position / duration * 100)}%")
}
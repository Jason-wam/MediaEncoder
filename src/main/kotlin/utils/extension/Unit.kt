package utils.extension

fun String.formatTime(): Long {
    val h = this.substring(0, 2).toLong()
    val s = this.substring(3, 5).toLong()
    val m = this.substring(6, 8).toLong()
    return h * 60 * 60 + s * 60 + m
}
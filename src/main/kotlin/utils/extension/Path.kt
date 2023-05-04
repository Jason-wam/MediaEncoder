package utils.extension

import java.io.File

fun String.formatPath(): String {
    return replace("\\", "\\\\")
}

fun File.symbolicPath(dir:File): String {
    return createSymbolicLink(dir,absolutePath.toMd5String() + ".$extension").absolutePath.formatPath()
}

fun File.symbolicPath(): String {
    val cacheDir = File(System.getProperty("user.dir"),"cache")
    return createSymbolicLink(cacheDir,absolutePath.toMd5String() + ".$extension").absolutePath.formatPath()
}

fun File.symbolicFile(): File {
    val cacheDir = File(System.getProperty("user.dir"),"cache")
    return createSymbolicLink(cacheDir,absolutePath.toMd5String() + ".$extension")
}
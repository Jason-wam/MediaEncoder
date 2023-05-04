package utils.extension

import utils.FileUtil
import java.io.File
import java.nio.file.Files

inline val File.files: List<File>
    get() {
        return FileUtil.getAllFiles(this).filter { it.isFile }
    }

inline val File.folders: List<File>
    get() {
        return FileUtil.getAllFiles(this).filter { it.isDirectory }
    }

inline val List<File>.folderSizes: List<Pair<File, Long>>
    get() {
        return ArrayList<Pair<File, Long>>().apply {
            this@folderSizes.forEach {
                add(Pair(it, it.size))
            }
        }
    }

inline val File.children: List<File>
    get() {
        return FileUtil.getAllFiles(this@children)
    }

inline val File.size: Long
    get() {
        return if (isDirectory) {
            files.sumOf { it.length() }
        } else {
            length()
        }
    }

fun File.createSymbolicLink(toDir: File, overwrite: Boolean = false): File {
    if (toDir.exists().not()) {
        toDir.mkdirs()
    }

    val link = File(toDir, name)
    if (Files.exists(link.toPath()) && !overwrite) {
        return link
    }
    Files.deleteIfExists(link.toPath())
    Files.createSymbolicLink(link.toPath(), this.toPath())
    return link
}

fun File.createSymbolicLink(toDir: File, fileName: String, overwrite: Boolean = false): File {
    if (toDir.exists().not()) {
        toDir.mkdirs()
    }

    val link = File(toDir, fileName)
    if (Files.exists(link.toPath()) && !overwrite) {
        return link
    }

    Files.deleteIfExists(link.toPath())
    Files.createSymbolicLink(link.toPath(), this.toPath())
    return link
}
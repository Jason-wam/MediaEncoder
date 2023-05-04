package utils.extension

import java.io.File
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

fun File.convertZipToCbz(cbzDir: File) {
    try {
        val outFile = File(cbzDir, "${name.substringBeforeLast(".")}.cbz")
        if (outFile.exists().not()) {
            outFile.parentFile.mkdirs()
            outFile.createNewFile()
        } else {
            return
        }

        println("正在解压：$absolutePath..")
        val outDir = File(parent, name.substringBeforeLast("."))

        ZipFile(this, Charset.forName(System.getProperty("sun.jnu.encoding"))).use { zip ->
            zip.entries().asIterator().forEach { entry ->
                print("正在解压：$absolutePath >> ${entry.name}\r")
                if (entry.isDirectory) {
                    File(outDir.absolutePath + File.separator + entry.name).mkdirs()
                } else {
                    File(outDir.absolutePath + File.separator + entry.name).also { file ->
                        file.parentFile.mkdirs()
                        file.createNewFile()
                        file.outputStream().buffered().use { out ->
                            zip.getInputStream(entry).use { input ->
                                input.copyTo(out)
                            }
                        }
                    }
                }
            }
        }

        println()
        println("开始压缩：${outFile.absolutePath}..")
        ZipOutputStream(outFile.outputStream()).use { outZipStream ->
            outDir.files.forEach {
                print("正在压缩：${outFile.absolutePath} >> ${it.name}\r")
                outZipStream.putNextEntry(ZipEntry(it.name))
                outZipStream.write(it.readBytes())
            }
        }

        println()
        println("压缩完毕：${outFile.absolutePath}")

        println("清理缓存：${outDir.absolutePath}")
        outDir.children.forEach {
            print("正在清理：${outDir.absolutePath} >> ${it.name}\r")
            it.delete()
        }

        println()
        println("清理完毕：${outDir.absolutePath}")
    } catch (e: Exception) {
        println()
        println("转换错误：${absolutePath}")
        e.printStackTrace()
    }
}
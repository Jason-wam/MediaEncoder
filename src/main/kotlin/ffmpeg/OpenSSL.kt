package ffmpeg

import utils.extension.formatPath
import java.io.File

class OpenSSL {
    private var ssl = ""

    fun setSSL(path: String): OpenSSL {
        ssl = path.formatPath()
        return this
    }

    fun setSSL(file: File): OpenSSL {
        ssl = file.absolutePath.formatPath()
        return this
    }

    fun randHex16(): String {
        val command = arrayOf("cmd", "/C", "$ssl rand -hex 16")
        val process = Runtime.getRuntime().exec(command)
        return if (process.waitFor() != 0) { //0表示正常结束，1：非正常结束
            System.err.println(process.errorReader().readText())
            ""
        } else {
            process.inputReader().readText()
        }
    }

    fun rand16ToFile(file: File): Boolean {
        val command = arrayOf("cmd", "/C", "$ssl rand 16 > ${file.absolutePath.formatPath()}")
        val process = Runtime.getRuntime().exec(command)
        return if (process.waitFor() != 0) { //0表示正常结束，1：非正常结束
            System.err.println(process.errorReader().readText())
            false
        } else {
            true
        }
    }
}
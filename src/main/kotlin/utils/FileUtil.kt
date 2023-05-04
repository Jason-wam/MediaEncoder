package utils

import java.io.File

object FileUtil {
    fun getAllFiles(file: File): List<File> {
        val list: ArrayList<File> = arrayListOf()
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isFile) {
                    list.add(it)
                } else {
                    list.addAll(getAllFiles(it))
                }
            }
            list.add(file)
        } else {
            list.add(file)
        }
        return list
    }
}
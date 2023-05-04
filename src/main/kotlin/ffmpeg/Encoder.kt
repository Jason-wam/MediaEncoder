package ffmpeg

import utils.extension.*
import java.io.File

class Encoder(ffmpeg: String = "ffmpeg") {
    private val params = ArrayList<String>()
    private val userDir = System.getProperty("user.dir")
    private var cacheDir = File(userDir, "cache").also {
        it.mkdirs()
    }

    private var duration: Float = 0f
    private var openssl: String = ""

    init {
        params.add(ffmpeg)
    }

    /**
     * 指定OpenSSL路径，用于M3U8加密
     * @param bin OpenSSL路径 such as:"E:\FFmpeg\ssl\openssl.exe"
     */
    fun openssl(bin: String): Encoder {
        this.openssl = bin
        return this
    }

    fun cacheDir(dir: File): Encoder {
        this.cacheDir = dir
        this.cacheDir.mkdirs()
        return this
    }

    fun hwaccel(value: String): Encoder {
        params.add("-hwaccel $value")
        return this
    }

    fun useCuda(): Encoder {
        params.add("-hwaccel cuda")
        return this
    }

    fun crf(crf: Int = 23): Encoder {
        params.add("-crf $crf")
        return this
    }

    /**
     * such as: -ss 00:10:00
     */
    fun start(start: String): Encoder {
        params.add("-ss $start")
        return this
    }

    fun param(key: String, value: String): Encoder {
        params.add("$key $value")
        return this
    }

    fun param(param: String): Encoder {
        params.add(param)
        return this
    }

    /**
     * such as: -ss 10.5f
     */
    fun start(seconds: Float): Encoder {
        params.add("-ss $seconds")
        return this
    }

    /**
     * such as: -to 00:10:00
     */
    fun to(to: String): Encoder {
        params.add("-to $to")
        return this
    }

    /**
     * such as: -to 10.5f
     */
    fun to(to: Float): Encoder {
        params.add("-to $to")
        return this
    }

    /**
     * -t 持续时间
     * @param t 秒
     */
    fun t(t: Float): Encoder {
        duration = t
        params.add("-t $t")
        return this
    }

    /**
     * 指定视频&&音频播放速度
     */
    fun rate(rate: Double): Encoder {
        val videoRate = 1.0 / rate
        params.add("-filter_complex \"[0:v]setpts=$videoRate*PTS[v];[0:a]atempo=$rate[a]\" -map \"[v]\" -map \"[a]\"") //倍速
        return this
    }

    /**
     * 视频倍速 0.25 .. 4.0
     */
    fun videoRate(rate: Double): Encoder {
        val videoRate = 1.0 / rate
        params.add("-filter:v \"setpts=$videoRate*PTS\"") //倍速
        return this
    }

    /**
     * 音频倍速 0.5 .. 2.0
     */
    fun audioRate(rate: Double): Encoder {
        params.add("-filter:a \"atempo=$rate,atempo=$rate\"") //倍速
        return this
    }

    fun input(file: File): Encoder {
        params.add("-i \"${file.symbolicPath(cacheDir)}\"")
        if (duration == 0f) {
            duration = MediaInfo.create(file).format.duration.toFloat()
        }
        return this
    }

    fun input(vararg files: File): Encoder {
        input(files.toList())
        return this
    }

    fun input(files: List<File>): Encoder {
        val concatList = File(cacheDir, "concatList.txt").also {
            it.delete()
            it.createNewFile()
            it.outputStream().writer().use { writer ->
                files.forEachIndexed { index, file ->
                    val newName = "$index.${file.extension}"
                    print("\r正在准备合成数据 $index/${files.size}: $newName ...")
                    writer.appendLine("file '${file.symbolicPath(cacheDir)}'")
                    if (ignoreProgressFormat.not()) {
                        duration += MediaInfo.create(file.symbolicPath(cacheDir)).format.duration.toFloat()
                    }
                }
            }
        }
        println()
        params.add("-safe 0")
        params.add("-f concat")
        params.add("-i \"${concatList.absolutePath.formatPath()}\"")
        return this
    }

    /**
     * 软字幕它也叫内挂字幕、封装字幕、内封字幕，字幕流等..
     * 并非所有的容器都支持字幕流，先进的 MKV 是支持的。
     */
    fun softSubtitle(file: File): Encoder {
        params.add("-i \"${file.absolutePath.formatPath()}\"")
        return this
    }

    /**
     * 硬字幕,也被称之为嵌入式字幕、内嵌字幕、内置字幕等等，通常字幕的文字是已经嵌入电影中的字幕。这种字幕的文字已经不再是文字了，而是图像。
     */
    fun hardSubtitles(file: File): Encoder {
        params.add("-vf subtitles=\"${file.absolutePath.formatPath()}\"")
        return this
    }

    fun title(title: String): Encoder {
        params.add("-title $title")
        return this
    }

    fun album(album: String): Encoder {
        params.add("-album $album")
        return this
    }

    fun author(author: String): Encoder {
        params.add("-author $author")
        return this
    }

    fun cover(cover: File): Encoder {
        params.add("-i \"${cover.symbolicPath(cacheDir)}\"")
        params.add("-map 0:0")
        params.add("-map 1:0")
        params.add("-c copy")
        params.add("-id3v2_version 3")
        return this
    }

    fun copyright(copyright: String): Encoder {
        params.add("-copyright $copyright")
        return this
    }

    fun comment(comment: String): Encoder {
        params.add("-comment $comment")
        return this
    }


    /**
     * 指定视频帧率/s
     */
    fun fps(fps: Int): Encoder {
        params.add("-r $fps")
        return this
    }

    /**
     * 指定视频比特率(k bits/s)
     */
    fun videoBitrate(kBitrate: Int): Encoder {
        params.add("-vb ${kBitrate}k")
        return this
    }

    /**
     * 指定音频比特率(k bits/s)
     */
    fun audioBitrate(kBitrate: Int): Encoder {
        params.add("-ab ${kBitrate}k")
        return this
    }

    /**
     * 静音
     */
    fun mute(): Encoder {
        params.add("-an")
        return this
    }

    /**
     * 移动视频信息到开头，达成快速起播的目的
     */
    fun fastStart(): Encoder {
        params.add("-movflags faststart")
        return this
    }

    /**
     * 指定输出编码
     */
    fun format(format: String): Encoder {
        params.add("-f $format")
        return this
    }

    /**
     * 重新定义视频尺寸，其中一个参数为-1时自动等比例缩放
     */
    fun resize(w: Int = -1, h: Int = -1): Encoder {
        params.add("-vf scale=$w:$h")
        return this
    }

    /**
     * 复制视频流和音频流
     */
    fun copy(): Encoder {
        params.add("-c copy")
        return this
    }

    /**
     * 复制音频流
     */
    fun copyAudio(): Encoder {
        params.add("-c:a copy")
        return this
    }

    /**
     * 复制视频流
     */
    fun copyVideo(): Encoder {
        params.add("-c:v copy")
        return this
    }

    fun copySubtitle(): Encoder {
        params.add("-c:s copy")
        return this
    }

    /**
     * 指定音频编码器
     */
    fun audioCodec(codec: String): Encoder {
        params.add("-c:a $codec")
        return this
    }

    /**
     * 指定视频编码器
     */
    fun videoCodec(codec: String): Encoder {
        params.add("-c:v $codec")
        return this
    }

    /**
     * NVIDIA显卡硬件H264编码器
     */
    fun h264(): Encoder {
        params.add("-c:v h264_nvenc")
        return this
    }

    fun h264Cuvid(): Encoder {
        params.add("-c:v h264_cuvid")
        return this
    }

    /**
     * NVIDIA显卡硬件H265编码器
     */
    fun h265(): Encoder {
        params.add("-c:v hevc_nvenc")
        return this
    }

    /**
     * Intel核显QuickSync H264编码器
     */
    fun h264Qsv(): Encoder {
        params.add("-c:v h264_qsv")
        return this
    }

    /**
     * 基本等同于x264编码器，纯CPU编码
     */
    fun libx264(): Encoder {
        params.add("-c:v libx264")
        return this
    }

    /**
     * 开源的H.264编解码器 ,OpenH264是一个编解码器,支持H.264编码和解码。
     */
    fun libOpenH264(): Encoder {
        params.add("-c:v libopenh264")
        return this
    }

    /**
     * AMD显卡硬件H264编码器
     */
    fun h264MF(): Encoder {
        params.add("-c:v h264_mf")
        return this
    }

    /**
     * AMD显卡硬件H264编码器
     */
    fun h264AMF(): Encoder {
        params.add("-c:v h264_amf")
        return this
    }

    fun loop(loop: Int): Encoder {
        params.add("-loop_output $loop")
        return this
    }

    fun threads(threads: Int): Encoder {
        params.add("-threads $threads")
        return this
    }

    private var ignoreProgressFormat = false

    fun ignoreProgressFormat(): Encoder {
        ignoreProgressFormat = true
        return this
    }

    /**
     * 用于避免opus in MP4 support is experimental这个问题
     */
    fun strict(strict: Int): Encoder {
        params.add("-strict $strict")
        return this
    }

    fun removeCover(): Encoder {
        params.add("-metadata:s:v:0 \"handler=0\"")
        return this
    }

    private var overwrite = false

    fun overwrite(overwrite: Boolean): Encoder {
        this.overwrite = overwrite
        return this
    }

    private var startAtHalfDuration = false

    fun startAtHalfDuration(startAtHalfDuration: Boolean): Encoder {
        this.startAtHalfDuration = startAtHalfDuration
        return this
    }

    fun executeToPics(dir: File, extension: String = "png"): Boolean {
        if (dir.exists() && overwrite.not()) {
            return true
        } else if (dir.exists()) {
            dir.deleteRecursively()
        }

        dir.mkdirs()

        if (startAtHalfDuration) {
            params.indexOfFirst {
                it.startsWith("-i")
            }.also {
                if (it != -1) {
                    params.add(it - 1, "-ss ${duration / 2f}")
                }
            }
        }

        params.add("-y")
        params.add("\"${File(dir, "%8d.$extension").absolutePath.formatPath()}\"")

        execute(params.joinToString(" ")).also {
            cacheDir.deleteRecursively()
            return if (it.first) {
                println()
                println("转码成功: >> ${dir.absolutePath}")
                true
            } else {
                println()
                System.err.println("转码失败: ${dir.absolutePath} >> ${it.second}")
                dir.deleteRecursively()
                false
            }
        }
    }

    fun toFile(path: String): Boolean {
        return execute(File(path))
    }

    fun execute(output: File): Boolean {
        if (output.exists() && overwrite.not()) {
            return true
        } else if (output.exists()) {
            output.delete()
        }

        if (startAtHalfDuration) {
            params.indexOfFirst {
                it.startsWith("-i")
            }.also {
                if (it != -1) {
                    params.add(it, "-ss ${duration / 2f}")
                }
            }
        }

        val cache = File(output.parent, output.absolutePath.toMd5String() + ".${output.extension}")
        params.add("-y")
        params.add("\"${cache.absolutePath.formatPath()}\"")

        execute(params.joinToString(" ")).also {
            cacheDir.deleteRecursively()
            return if (it.first) {
                println()
                println("转码成功: >> ${output.name}")
                cache.renameTo(output)
                true
            } else {
                println()
                System.err.println("转码失败: ${output.name} >> ${it.second}")
                cache.delete()
                false
            }
        }
    }

    private fun execute(command: String): Pair<Boolean, String> {
        tryToBuildDurationWithStartAndTo()

        println("开始执行: $command")
        var line: String?
        val error = StringBuilder()
        val process = Runtime.getRuntime().exec(command)
        val reader = process.errorStream.bufferedReader()
        while (reader.readLine().also { line = it } != null) {
            error.appendLine(line)
            Regex("time=(\\d\\d:\\d\\d:\\d\\d)").find(line.orEmpty())?.let {
                if (ignoreProgressFormat) {
                    println(line)
                } else {
                    if (it.groupValues.size > 1) {
                        val time = it.groupValues[1].formatTime()
                        printProgress(time.toFloat(), duration, 20)
                    }
                }
            }
        }

        if (ignoreProgressFormat) {
            println("Process finished..")
        } else {
            printProgress(duration, duration, 20)
        }

        return if (process.waitFor() != 0) { //0表示正常结束，1：非正常结束
            process.destroy()
            Pair(false, error.toString())
        } else {
            process.destroy()
            Pair(true, error.toString())
        }
    }

    fun executeToM3U8(outDir: File, seconds: Float, encrypt: Boolean = false): Boolean {
        if (outDir.exists() && overwrite.not()) {
            return true
        } else if (outDir.exists()) {
            outDir.deleteRecursively()
        }

        outDir.mkdirs()

        if (startAtHalfDuration) {
            params.indexOfFirst {
                it.startsWith("-i")
            }.also {
                if (it != -1) {
                    params.add(it - 1, "-ss ${duration / 2f}")
                }
            }
        }

        params.add("-f hls")
        params.add("-hls_time $seconds")
        params.add("-hls_list_size 0")
        params.add("-hls_segment_filename \"${File(outDir, "%05d.ts").absolutePath.formatPath()}\"")

        if (encrypt) {
            if (openssl.isBlank()) {
                throw Exception("openssl 路径未指定！！")
            }
            val ssl = OpenSSL().setSSL(openssl)
            val key = ssl.randHex16()
            if (key.isBlank()) {
                throw Exception("创建加密Key失败！")
            } else {
                val keyFile = File(outDir, "encrypt.key")
                if (ssl.rand16ToFile(keyFile).not()) {
                    throw Exception("创建加密Key文件失败！")
                } else {
                    File(outDir, "index.keyinfo").also {
                        it.delete()
                        it.createNewFile()
                        it.outputStream().writer().use { writer ->
                            writer.appendLine(keyFile.name)
                            writer.appendLine(keyFile.absolutePath.formatPath())
                            writer.appendLine(key)
                        }
                    }.also {
                        params.add("-hls_key_info_file \"${it.absolutePath.formatPath()}\"")
                    }
                }
            }
        }

        params.add("-y")
        params.add("\"${File(outDir, "index.m3u8").absolutePath.formatPath()}\"")

        execute(params.joinToString(" ")).also {
            File(outDir, "index.keyinfo").delete()
            cacheDir.deleteRecursively()
            return if (it.first) {
                println()
                println("转码成功: >> ${outDir.absolutePath}")
                true
            } else {
                println()
                System.err.println("转码失败: ${outDir.absolutePath} >> ${it.second}")
                outDir.deleteRecursively()
                false
            }
        }
    }

    private fun tryToBuildDurationWithStartAndTo() {
        if (ignoreProgressFormat.not()) {
            try {
                val start = params.find { it.startsWith("-ss") }?.removePrefix("-ss")?.trim()
                val to = params.find { it.startsWith("-to") }?.removePrefix("-to")?.trim()
                when {
                    start != null && to == null -> {
                        duration -= if (start.contains(":")) {
                            start.formatTime().toFloat()
                        } else {
                            start.toFloat()
                        }
                    }

                    start == null && to != null -> {
                        duration = if (to.contains(":")) {
                            to.formatTime().toFloat()
                        } else {
                            to.toFloat()
                        }
                    }

                    start != null && to != null -> {
                        val startTime = if (start.contains(":")) {
                            start.formatTime().toFloat()
                        } else {
                            start.toFloat()
                        }
                        val toTime = if (to.contains(":")) {
                            to.formatTime().toFloat()
                        } else {
                            to.toFloat()
                        }
                        duration = toTime - startTime
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
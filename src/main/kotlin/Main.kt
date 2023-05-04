import ffmpeg.Encoder
import utils.extension.convertZipToCbz
import utils.extension.createSymbolicLink
import utils.extension.files
import utils.extension.toMd5String
import java.io.File


fun main(args: Array<String>) {
    convert()
}


fun removeCover() {
    File("F:\\XLDownload\\eu0407").files.forEach {
        if (it.name.endsWith(".mp4")) {
            val outFilename = File(it.parent, it.name.replace(it.extension, "format.mp4"))
            if (Encoder().useCuda().input(it).copy().param("-metadata:s:v:0 \"handler=0\"").fastStart().overwrite(true).execute(outFilename)) {
                it.delete()
            }
        }
    }
}

fun minimize() {
    val input = File("H:\\Video\\1\\MXGS-697.mp4")
    val output = File("H:\\Video\\1\\MXGS-697.1080.mp4")

    Encoder().useCuda().input(input).h264().resize(1920).overwrite(true).execute(output)
}

fun convert() {
    File("C:\\Users\\Administrator\\Documents\\Tencent Files\\1\\FileRecv\\【歌手】邓紫棋").files.filter {
        it.extension == "mp3"
    }.forEach {
        val cache = File(it.parent, "cache")
        val name = it.name.replace(it.extension, "wav")
        Encoder().useCuda().cacheDir(cache).input(it).threads(20).overwrite(true).execute(File(it.parent, name))
    }
}

fun createHls(input: File) {
    val cacheDir = File(input.parent, "cache")
    val outDir = File(input.parent, input.absolutePath.toMd5String())
    val encoder = Encoder()
    encoder.cacheDir(cacheDir).useCuda().input(input).copy().overwrite(true).threads(6)
    encoder.openssl("E:\\FFmpeg\\ssl\\openssl.exe")
    encoder.executeToM3U8(outDir, 10f, true)
}

fun createPreview() {
    val outDir = File("H:\\Video\\Preview").also { it.mkdirs() }
    File("H:\\Video\\").files.forEach {
        val outFileName = File(outDir, it.name.replace(it.extension, "gif"))
        if (Encoder().useCuda().input(it).fps(10).t(20f).resize(720).threads(3).startAtHalfDuration(true).overwrite(false).execute(outFileName)) {
            it.createSymbolicLink(outDir)
        }
    }
}

fun concatFiles() {
    File("G:\\媒体文件\\有声书\\剑仙在此").files.forEach { file ->
        val image = File("C:\\Users\\Administrator\\Desktop\\20221216111807867311.jpg")
        val cacheDir = File("G:\\媒体文件\\有声书\\cache")
        val outDir = File(file.parent, "covered").also { it.mkdirs() }
        val outFileName = File(outDir, file.name)
        Encoder().cacheDir(cacheDir).useCuda().input(file).cover(image).execute(outFileName)
    }
}

fun cutVideo() {
    val input = File("G:\\媒体文件\\电影\\狼溪2.Wolf.Creek.2.2014.BD1080P.英语中字.BTDX8\\狼溪2.Wolf.Creek.2.2014.BD1080P.英语中字.BTDX8.mp4")
    val output = File("G:\\媒体文件\\电影\\狼溪2.Wolf.Creek.2.2014.BD1080P.英语中字.BTDX8\\狼溪2.cut.mp4")

    Encoder().useCuda().start("01:20:30").to("01:41:30").input(input).copy().overwrite(true).execute(output)
}

fun convertCbz() {
    File("C:\\Users\\Administrator\\Desktop\\[韩漫]冰岛小店.zip").convertZipToCbz(File("F:\\XLDownload\\Test"))
}


fun getDecoders() {
    val process = Runtime.getRuntime().exec("E:\\ffmpeg-4.3.2\\bin\\ffmpeg.exe –decoders")
    val reader = process.errorStream.bufferedReader()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        println(line)
    }
}

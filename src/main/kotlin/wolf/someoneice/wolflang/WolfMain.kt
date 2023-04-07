package wolf.someoneice.wolflang

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.floor

object WolfMain {
    private val gson: Gson = Gson()
    @Throws(NotWolfFileException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("C:\\Users\\snowl\\Desktop\\voodoo\\Test.wolf")
        wolfDecode(file)
    }

    @Throws(NotWolfFileException::class, IOException::class)
    fun wolfDecode(file: File) {
        val type = file.name.substring(file.name.indexOf("."))
        if (type != ".wolf") throw NotWolfFileException()

        val input = FileInputStream(file)
        val buffered = ByteArray(input.available())
        input.read(buffered)
        input.close()

        val text = String(buffered)
        val line = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size
        val output = FileOutputStream(file.canonicalFile.toString().replace(file.name, file.name.replace(type, getFileType(text)!!)))
        output.write(decode(getText(text), line).toByteArray())
        output.close()
    }

    @Throws(IOException::class)
    fun wolfEncode(file: File) {
        val type = file.name.substring(file.name.indexOf("."))

        val input = FileInputStream(file)
        val buffered = ByteArray(input.available())
        input.read(buffered)
        input.close()

        var text = String(buffered)
        val line = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size
        text = addCode(encode(text, line), type)
        val output = FileOutputStream(file.canonicalFile.toString().replace(file.name, file.name.replace(type, ".wolf")))
        output.write(text.toByteArray())
        output.close()
    }

    private fun decode(input: String, line: Int): String {
        var str = input
        val buffer = StringBuilder()
        while (str.isNotEmpty()) {
            if (str.indexOf('\n') == 0) {
                str = str.replaceFirst("\n".toRegex(), "")
                buffer.append('\n')
                continue
            } else if (str.indexOf("au") == 0) {
                str = str.replaceFirst("au".toRegex(), "")
                continue
            } else {
                val index = if (str.indexOf("au") != -1) str.indexOf("au") else str.length
                val s = str.substring(0, index)
                var f =0
                var w = 0
                for (j in s) {
                    if (j == 'w') w += 1
                    else if (j == '5') f += 1
                }
                if (f * 5 + w == line) continue
                buffer.append(getCharByIndex(f * 5 + w - line))
                str = str.replaceFirst(s.toRegex(), "")
            }
        }

        /*
        for (i in str) {
            if (i == '\n') {
                str = str.replaceFirst("\n".toRegex(), "")
                buffer.append("\n")
                continue
            } else if (i == 'a') {
                str = str.replaceFirst("au".toRegex(), "")
                continue
            } else {
                val s = str.substring(0, str.indexOf("au"))
                var f =0
                var w = 0
                for (j in s) {
                    if (j == 'w') w += 1
                    else if (j == '5') f += 1
                }
                if (f * 5 + w == line) continue
                buffer.append(getCharByIndex(f * 5 + w - line))
                str = str.replaceFirst(s.toRegex(), "")
            }
        }
        */

        return buffer.toString()
    }

    private fun encode(input: String, line: Int): String {
        var str = input
        val buffer = StringBuilder()
        for (i in str) {
            if (i == '\n') {
                str = str.replaceFirst("\n".toRegex(), "")
                buffer.append("\n")
                continue
            }
            var charIndex = getCharIndex(i) + line
            if (charIndex == line) continue

            val fiveG = if (charIndex > 5) floor((charIndex / 5).toDouble()).toInt() else 0
            charIndex -= fiveG * 5
            buffer.append("au")
            buffer.append("5".repeat(0.coerceAtLeast(fiveG)))
            buffer.append("w".repeat(0.coerceAtLeast(charIndex)))
        }
        return buffer.toString()
    }

    private fun getText(str: String): String {
        return str.substring(0, str.lastIndexOf("wolfcode:", str.lastIndexOf("wolfcode:")))
    }

    private fun getFileType(str: String): String? {
        val info = str.substring(str.lastIndexOf("wolfcode:", str.lastIndexOf("wolfcode:")) + 9)
        return (gson.fromJson(info, object : TypeToken<Map<String?, String?>?>() {}.type) as Map<String, String>)["info"]
    }

    private fun addCode(str: String, typ: String): String {
        return str + "wolfcode:" + gson.toJson(info(typ))
    }

    const val box: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890{}[]()*&^%$#@!~:<>;/+-= "
    private fun getCharIndex(c: Char): Int {
        return box.indexOf(c) + 1
    }

    private fun getCharByIndex(i: Int): Char {
        return box[i - 1]
    }
}

fun main(args: Array<String>) {

}

data class info(
    val info: String
)
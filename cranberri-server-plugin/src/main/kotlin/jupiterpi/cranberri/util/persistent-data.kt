package jupiterpi.cranberri.util

import java.io.*

const val DATA_ROOT = "world/cranberri_data"

class TextFile(val lines: List<String>) {
    val file = lines.joinToString("\n")

    constructor(file: String) : this(file.split(Regex("(\\r\\n|\\r|\\n)")))

    companion object {

        // read

        fun readFile(path: String)
        = File(path).let { if (it.exists()) readFile(it) else TextFile(listOf()) }

        fun readCsvFile(path: String)
        = readFile(path).lines.map { it.split(Regex(";")) }

        fun readResourceFile(path: String)
        = readFile(TextFile::class.java.getResourceAsStream(
            if (path.startsWith("/")) path else "/$path"
        )!!)

        fun readFile(file: File)
        = TextFile(BufferedReader(FileReader(file)).lines().toList())
        private fun readFile(inputStream: InputStream)
        = TextFile(BufferedReader(InputStreamReader(inputStream)).lines().toList())

        // (write)

        fun csv(data: List<List<String>>) = TextFile(data.map { it.joinToString(";") })

        fun createPath(path: String) = File(path).mkdirs()

    }

    // write

    fun writeFile(path: String)
    = writeFile(File(path).also { if (!it.exists()) it.createNewFile() })

    fun writeFile(file: File)
    = BufferedWriter(FileWriter(file)).let {
        it.write(this.file)
        it.close()
    }
}
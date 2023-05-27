package util

import java.io.*

const val DATA_ROOT = "world/cranberri_data"

class TextFile(val lines: List<String>) {
    val file = lines.joinToString("\n")

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

        // write

        fun writeFile(path: String, lines: List<String>) {
            writeFile(File(path).also { if (!it.exists()) it.createNewFile() }, lines)
        }

        fun writeCsvFile(path: String, data: List<List<String>>) {
            writeFile(path, data.map { it.joinToString(";") })
        }

        fun writeFile(file: File, lines: List<String>) {
            BufferedWriter(FileWriter(file)).let {
                it.write(lines.joinToString("\n"))
                it.close()
            }
        }

        fun createPath(path: String) {
            File(path).mkdirs()
        }

    }
}
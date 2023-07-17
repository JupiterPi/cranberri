package jupiterpi.cranberri.runtime.compilation

private val standardImports = """
    import jupiterpi.cranberri.runtime.api.Script
    import jupiterpi.cranberri.runtime.api.Setup
    import jupiterpi.cranberri.runtime.api.Tick
    import jupiterpi.cranberri.runtime.api.IO
    import jupiterpi.cranberri.runtime.api.IO.disableDebug
    import jupiterpi.cranberri.runtime.api.IO.log
    import jupiterpi.cranberri.runtime.api.IO.writePin
    import jupiterpi.cranberri.runtime.api.IO.readPin
    import jupiterpi.cranberri.runtime.api.IO.PinValue
    import jupiterpi.cranberri.runtime.api.IO.PinValue.*
    
    import kotlin.math.*
""".trimIndent()

object SimpleProjectCompiler : SpecificProjectCompiler {
    override fun compileProject(sourceFiles: List<SourceFile>, packageName: String): List<SourceFile> {
        sourceFiles.forEach { file ->
            if (file.isScript) {

                if (file.source.contains("fun setup()")) file.source = file.source.replace("fun setup()", "@Setup fun setup()")
                if (file.source.contains("fun tick()")) file.source = file.source.replace("fun tick()", "@Tick fun tick()")

                val extractedImportLines = extractImportLines(file.source)
                file.source = """
                    package $packageName
                    
                    $standardImports
                    
                    ${extractedImportLines.importLines.joinToString("\n")}
                    
                    @Script
                    class ${file.nameWithoutExtension} {
                        
                    ${extractedImportLines.source}
                    
                    }
                """.trimIndent()

            } else {

                val extractedImportLines = extractImportLines(file.source)
                file.source = """
                    package $packageName.lib
                    
                    $standardImports
                    
                    ${extractedImportLines.importLines.joinToString("\n")}
                    
                    ${extractedImportLines.source}
                """.trimIndent()

            }
        }

        return sourceFiles
    }

    private data class ExtractedImportLines(val source: String, val importLines: List<String>)
    private fun extractImportLines(source: String): ExtractedImportLines {
        lateinit var importLines: List<String>
        val source = source.lines().toMutableList().also { lines ->
            importLines = lines.filter { it.startsWith("import") }
            lines.removeAll(importLines)
        }.joinToString("\n")
        return ExtractedImportLines(source, importLines)
    }
}
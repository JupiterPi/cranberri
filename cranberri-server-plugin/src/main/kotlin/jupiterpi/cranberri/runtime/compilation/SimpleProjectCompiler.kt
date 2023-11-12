package jupiterpi.cranberri.runtime.compilation

object SimpleProjectCompiler : SpecificProjectCompiler {
    override fun compileProject(sourceFiles: List<SourceFile>, packageName: String, language: ProjectManifest.ProjectLanguage, arduinoMode: Boolean): List<SourceFile> {
        /**
         * optional semicolon
         */
        val s = if (language == ProjectManifest.ProjectLanguage.JAVA) ";" else ""

        val setupDefinition = if (language == ProjectManifest.ProjectLanguage.KOTLIN) "fun setup()" else "void setup()"
        val tickDefinition = if (language == ProjectManifest.ProjectLanguage.KOTLIN) "fun tick()" else "void tick()"
        val loopDefinition = if (language == ProjectManifest.ProjectLanguage.KOTLIN) "fun loop()" else "void loop()"
        val optionalImportStatic = if (language == ProjectManifest.ProjectLanguage.JAVA) " static" else ""
        val standardImports = """
            import jupiterpi.cranberri.runtime.api.Script$s
            import jupiterpi.cranberri.runtime.api.Setup$s
            import jupiterpi.cranberri.runtime.api.Tick$s
            import jupiterpi.cranberri.runtime.api.IO$s
            import$optionalImportStatic jupiterpi.cranberri.runtime.api.IO.disableDebug$s
            import$optionalImportStatic jupiterpi.cranberri.runtime.api.IO.log$s
            import$optionalImportStatic jupiterpi.cranberri.runtime.api.IO.writePin$s
            import$optionalImportStatic jupiterpi.cranberri.runtime.api.IO.readPin$s
            import jupiterpi.cranberri.runtime.api.IO.PinValue$s
            import$optionalImportStatic jupiterpi.cranberri.runtime.api.IO.PinValue.*$s
            ${if (arduinoMode) """
                import jupiterpi.cranberri.runtime.api.Loop$s
                import $optionalImportStatic jupiterpi.cranberri.runtime.api.Arduino.pinMode$s
                import $optionalImportStatic jupiterpi.cranberri.runtime.api.Arduino.PinMode.*$s
            """ else ""}
        """.trimIndent()

        val sourceFiles = sourceFiles.toMutableList()

        var insertInScript = ""
        var extraSourceFiles = mutableListOf<SourceFile>()
        if (language == ProjectManifest.ProjectLanguage.JAVA) {
            val libClasses = sourceFiles.filter { !it.isScript }.map { it.nameWithoutExtension }
            val libInstancesFile = "package $packageName;\n\npublic class LibInstances {\n\n${libClasses.joinToString("\n") { "public static $it $it = new $it();" }}\n\n}"
            extraSourceFiles += SourceFile("LibInstances", "java", libInstancesFile, false)
            insertInScript = libClasses.joinToString("\n") { "$it $it = LibInstances.$it;" }
        }

        sourceFiles.forEach { file ->
            if (file.isScript) {

                file.source = file.source.replace(setupDefinition, "@Setup $setupDefinition")
                file.source = file.source.replace(tickDefinition, "@Tick $tickDefinition")
                file.source = file.source.replace(loopDefinition, "@Loop $loopDefinition")

                file.source = file.source.replace(Regex("delay\\(([0-9]*)\\)")) { "new jupiterpi.cranberri.runtime.api.Arduino.Delay(${it.groupValues[1]})" }

                val extractedImportLines = extractImportLines(file.source)
                file.source = """
                    package $packageName$s
                    
                    $standardImports
                    
                    ${extractedImportLines.importLines.joinToString("\n")}
                    
                    @Script
                    public class ${file.nameWithoutExtension} {
                    
                    $insertInScript
                        
                    ${extractedImportLines.source}
                    
                    }
                """.trimIndent()

            } else {

                var source = file.source
                if (language == ProjectManifest.ProjectLanguage.JAVA) {
                    source = """
                        public class ${file.nameWithoutExtension} {
                        $source
                        }
                    """.trimIndent()
                }

                file.source = """
                    package $packageName$s
                    
                    $standardImports
                    
                    $source
                """.trimIndent()

            }
        }

        return sourceFiles.apply { addAll(extraSourceFiles) }
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
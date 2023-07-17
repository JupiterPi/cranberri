package jupiterpi.cranberri.runtime

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import jupiterpi.cranberri.util.TextFile
import java.io.File

const val PROJECTS_ROOT = "cranberri_projects"
const val PROJECTS_OUT_ROOT = "cranberri_projects-out"

private const val API_JAR = "plugins/cranberri-server-plugin-1.0-SNAPSHOT-all.jar"

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

object ProjectCompiler {
    fun compileProject(projectName: String, instanceId: String) {
        if (projectName.contains("-")) throw Exception("Invalid project name: Mustn't include '-'")

        val files = mutableListOf<String>()

        TextFile.createPath(PROJECTS_OUT_ROOT)
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId")
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId/scripts")
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId/lib")

        val packageName = "cranberri_project_$projectName.instance_$instanceId"

        val manifest = ProjectManifest.read(File("$PROJECTS_ROOT/$projectName/project.yaml"))
        if (manifest.language != ProjectManifest.ProjectLanguage.KOTLIN) throw Exception("project language ${manifest.language} not yet supported!")
        val processSimpleProject = manifest.projectType == ProjectManifest.ProjectType.SIMPLE

        File("$PROJECTS_ROOT/$projectName/scripts").listFiles()?.forEach { file ->
            files += "scripts/${file.nameWithoutExtension}.kt"

            var source = TextFile.readFile(file).file
            if (processSimpleProject) {
                if (source.contains("fun setup()")) source = source.replace("fun setup()", "@Setup fun setup()")
                if (source.contains("fun tick()")) source = source.replace("fun tick()", "@Tick fun tick()")

                val extractedImportLines = extractImportLines(source)
                source = """
                    package $packageName
                    
                    $standardImports
                    
                    ${extractedImportLines.importLines.joinToString("\n")}
                    
                    @Script
                    class ${file.nameWithoutExtension} {
                        
                    ${extractedImportLines.source}
                    
                    }
                """.trimIndent()
            } else {
                if (source.contains("package ")) source = source.replace("package ", "package $packageName.")
                else source = "package $packageName\n\n"
            }

            TextFile(source).writeFile("$PROJECTS_OUT_ROOT/$projectName-$instanceId/scripts/${file.nameWithoutExtension}.kt")
        }

        File("$projectName/lib").listFiles()?.forEach { file ->
            files += "lib/${file.nameWithoutExtension}.kt"

            var source = TextFile.readFile(file).file
            if (processSimpleProject) {
                val extractedImportLines = extractImportLines(source)
                source = """
                    package $packageName.lib
                    
                    $standardImports
                    
                    ${extractedImportLines.importLines.joinToString("\n")}
                    
                    ${extractedImportLines.source}
                """.trimIndent()
            } else {
                if (source.contains("package ")) source = source.replace("package ", "package $packageName.")
                else source = "package $packageName.lib\n\n"
            }

            TextFile(source).writeFile("$PROJECTS_OUT_ROOT/$projectName-$instanceId/lib/${file.nameWithoutExtension}.kt")
        }

        val filesStr = files.joinToString(" ") { "$PROJECTS_OUT_ROOT/$projectName-$instanceId/$it" }
        val cmd = "kotlinc -include-runtime -d $PROJECTS_OUT_ROOT/$projectName-$instanceId.jar -cp \"$API_JAR\" $filesStr"

        Runtime.getRuntime().exec("cmd.exe /c $cmd").let {
            it.inputStream.transferTo(System.out)
            it.errorStream.transferTo(System.err)
        }
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

private class ProjectManifest(
    val projectType: ProjectType,
    val language: ProjectLanguage,
) {
    enum class ProjectType {
        SIMPLE, FULL;

        @JsonValue
        fun getCode() = toString().lowercase()
        companion object {
            @JsonCreator
            fun decode(value: String) = valueOf(value.uppercase())
        }
    }

    enum class ProjectLanguage {
        KOTLIN, JAVA;

        @JsonValue
        fun getCode() = toString().lowercase()
        companion object {
            @JsonCreator
            fun decode(value: String) = ProjectType.valueOf(value.uppercase())
        }
    }

    companion object {
        private val mapper = ObjectMapper(YAMLFactory()).also { it.findAndRegisterModules() }
        fun read(file: File) = mapper.readValue(file, ProjectManifest::class.java)
    }
}
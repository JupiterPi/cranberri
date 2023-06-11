package jupiterpi.cranberri.runtime

import jupiterpi.cranberri.util.TextFile
import java.io.File

private const val PROJECTS_ROOT = "cranberri_projects"
private const val PROJECTS_OUT_ROOT = "cranberri_projects-out"

private const val API_JAR = "plugins/cranberri-server-plugin-1.0-SNAPSHOT-all.jar"

object ProjectCompiler {
    fun compileProject(projectName: String, instanceId: String) {
        if (projectName.contains("-")) throw Exception("Invalid project name: Mustn't include '-'")

        val files = mutableListOf<String>()

        TextFile.createPath(PROJECTS_OUT_ROOT)
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId")
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId/scripts")
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId/lib")

        val fileHeader = """
            package cranberri_project_$projectName.instance_$instanceId
            
            import jupiterpi.cranberri.runtime.api.Script
            import jupiterpi.cranberri.runtime.api.Setup
            import jupiterpi.cranberri.runtime.api.Tick
            import jupiterpi.cranberri.runtime.api.IO
            import jupiterpi.cranberri.runtime.api.IO.pinMode
            import jupiterpi.cranberri.runtime.api.IO.writePin
            import jupiterpi.cranberri.runtime.api.IO.readPin
            import jupiterpi.cranberri.runtime.api.IO.log
            import jupiterpi.cranberri.runtime.api.IO.test
            import jupiterpi.cranberri.runtime.api.IO.PinMode.*
            import jupiterpi.cranberri.runtime.api.IO.PinValue.*
        """.trimIndent()

        File("$PROJECTS_ROOT/$projectName/scripts").listFiles()?.forEach { file ->
            files += "scripts/${file.nameWithoutExtension}.kt"

            var source = TextFile.readFile(file).file
            if (source.contains("fun setup()")) source = source.replace("fun setup()", "@Setup fun setup()")
            if (source.contains("fun tick()")) source = source.replace("fun tick()", "@Tick fun tick()")

            TextFile("""
                $fileHeader
                
                @Script("${file.nameWithoutExtension}")
                    class script_${file.nameWithoutExtension} {
                    
                $source
                
                }
            """.trimIndent()).writeFile("$PROJECTS_OUT_ROOT/$projectName-$instanceId/scripts/${file.nameWithoutExtension}.kt")
        }

        File("$projectName/lib").listFiles()?.forEach { file ->
            files += "lib/${file.nameWithoutExtension}.kt"

            TextFile("""
                $fileHeader
                
                ${TextFile.readFile(file)}
            """.trimIndent()).writeFile("$PROJECTS_OUT_ROOT/$projectName-$instanceId/lib/${file.nameWithoutExtension}.kt")
        }

        val filesStr = files.joinToString(" ") { "$PROJECTS_OUT_ROOT/$projectName-$instanceId/$it" }
        val cmd = "kotlinc -include-runtime -d $PROJECTS_OUT_ROOT/$projectName-$instanceId.jar -cp \"$API_JAR\" $filesStr"

        Runtime.getRuntime().exec("cmd.exe /c $cmd").let {
            it.inputStream.transferTo(System.out)
            it.errorStream.transferTo(System.err)
        }
    }
}
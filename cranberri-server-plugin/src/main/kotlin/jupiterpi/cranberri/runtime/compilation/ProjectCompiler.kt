package jupiterpi.cranberri.runtime.compilation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import jupiterpi.cranberri.CranberriPlugin
import jupiterpi.cranberri.util.TextFile
import java.io.File

const val PROJECTS_ROOT = "cranberri_projects"
const val PROJECTS_OUT_ROOT = "cranberri_projects-out"

private val API_JAR = CranberriPlugin::class.java.protectionDomain.codeSource.location.path.let { if (it.matches(Regex("/.:/.*"))) it.substring(1) else it }

object ProjectCompiler {
    fun clearOutputCache() {
        File(PROJECTS_OUT_ROOT).listFiles()?.forEach { it.deleteRecursively() }
    }

    fun compileProject(projectName: String, instanceId: String) {
        if (projectName.contains("-")) throw Exception("Invalid project name: Mustn't include '-'")

        TextFile.createPath(PROJECTS_OUT_ROOT)
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId")
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId/scripts")
        TextFile.createPath("$PROJECTS_OUT_ROOT/$projectName-$instanceId/lib")

        var files = listOfNotNull(
            File("$PROJECTS_ROOT/$projectName/scripts").listFiles()
                ?.map { SourceFile(it.nameWithoutExtension, it.extension, TextFile.readFile(it).file, true) },
            File("$PROJECTS_ROOT/$projectName/lib").listFiles()
                ?.map { SourceFile(it.nameWithoutExtension, it.extension, TextFile.readFile(it).file, false) }
        ).flatten()

        val manifest = ProjectManifest.read(File("$PROJECTS_ROOT/$projectName/project.yaml"))

        val compiler = when (manifest.projectType) {
            ProjectManifest.ProjectType.SIMPLE -> SimpleProjectCompiler
            ProjectManifest.ProjectType.FULL -> FullProjectCompiler
        }
        files = compiler.compileProject(files, "cranberri_project_$projectName.instance_$instanceId", manifest.language)

        files.forEach {
            TextFile(it.source).writeFile("$PROJECTS_OUT_ROOT/$projectName-$instanceId/${it.path}")
        }

        val filesStr = files.joinToString(" ") { "$PROJECTS_OUT_ROOT/$projectName-$instanceId/${it.path}" }
        val cmd = when (manifest.language) {
            ProjectManifest.ProjectLanguage.KOTLIN -> "kotlinc -include-runtime -d $PROJECTS_OUT_ROOT/$projectName-$instanceId.jar -cp \"$API_JAR\" $filesStr"
            ProjectManifest.ProjectLanguage.JAVA -> "javac -d $PROJECTS_OUT_ROOT/$projectName-$instanceId.jar -cp \"$API_JAR\" $filesStr"
        }
        Runtime.getRuntime().exec("cmd.exe /c $cmd").let {
            it.inputStream.transferTo(System.out)
            it.errorStream.transferTo(System.err)
        }
    }
}

class ProjectManifest(
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
        // see https://gist.github.com/Arham4/429ee1216e0d7224cb1522ac5a2a0fe2
        // see https://github.com/Arham4/silentbot/blob/334cddcbaf05a2d764d599cafd72f5bae9138b58/src/main/kotlin/com/gmail/arhamjsiddiqui/silentbot/data/YAMLUtil.kt
        private val mapper = ObjectMapper(YAMLFactory())
            .also { it.registerModule(KotlinModule.Builder().build()) }
            .also { it.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true) }
        fun read(file: File) = mapper.readValue<ProjectManifest>(file)
    }
}

class SourceFile(
    val nameWithoutExtension: String,
    val extension: String,
    var source: String,
    val isScript: Boolean,
) {
    val path: String get() {
        return "${if (isScript) "scripts" else "lib"}/$nameWithoutExtension.$extension"
    }
}

interface SpecificProjectCompiler {
    fun compileProject(sourceFiles: List<SourceFile>, packageName: String, language: ProjectManifest.ProjectLanguage): List<SourceFile>
}
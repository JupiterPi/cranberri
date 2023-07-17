package jupiterpi.cranberri.runtime.compilation

object FullProjectCompiler : SpecificProjectCompiler {
    override fun compileProject(sourceFiles: List<SourceFile>, packageName: String): List<SourceFile> {
        val modifiedPackageNames = mutableSetOf<String>()
        sourceFiles.forEach { file ->
            if (file.source.contains("package ")) {
                modifiedPackageNames += Regex("package (.+)\n").find(file.source)!!.groupValues[1]
                file.source = file.source.replace("package ", "package $packageName.")
            } else {
                val specificPackageName = if (file.isScript) packageName else "$packageName.lib"
                file.source = "package $specificPackageName\n\n${file.source}"
            }
        }
        sourceFiles.forEach { file ->
            file.source = file.source
                .lines()
                .map { line ->
                    var line = line
                    if (line.startsWith("import ")) {
                        modifiedPackageNames.forEach { line = line.replace(it, "$packageName.$it") }
                    }
                    return@map line
                }
                .joinToString("\n")
        }
        return sourceFiles
    }
}
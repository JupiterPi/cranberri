package jupiterpi.cranberri.runtime.compilation

object FullProjectCompiler : SpecificProjectCompiler {
    override fun compileProject(sourceFiles: List<SourceFile>, packageName: String): List<SourceFile> {
        sourceFiles.forEach { file ->
            if (file.source.contains("package ")) {
                file.source = file.source.replace("package ", "package $packageName.")
            } else {
                val specificPackageName = if (file.isScript) packageName else "$packageName.lib"
                file.source = "package $specificPackageName\n\n${file.source}"
            }
        }
        return sourceFiles
    }
}
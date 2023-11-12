package jupiterpi.cranberri.runtime.compilation

object FullProjectCompiler : SpecificProjectCompiler {
    override fun compileProject(sourceFiles: List<SourceFile>, packageName: String, language: ProjectManifest.ProjectLanguage, arduinoMode: Boolean): List<SourceFile> {
        /**
         * optional semicolon
         */
        val s = if (language == ProjectManifest.ProjectLanguage.JAVA) ";" else ""

        var modifiedPackageNames = mutableSetOf<String>()
        sourceFiles.forEach { file ->
            if (file.source.contains("package ")) {
                modifiedPackageNames += Regex("package (.+)\n").find(file.source)!!.groupValues[1]
                file.source = file.source.replace("package ", "package $packageName.")
            } else {
                file.source = "package $packageName$s\n\n${file.source}"
            }
        }

        if (language == ProjectManifest.ProjectLanguage.JAVA) modifiedPackageNames = modifiedPackageNames.map { it.substring(0, it.length - 1) }.toMutableSet()

        sourceFiles.forEach { file ->
            file.source = file.source
                .lines()
                .map { line ->
                    var line = line
                    if (line.startsWith("import ")) {
                        for (modifiedPackageName in modifiedPackageNames.sortedByDescending { it.length }) {
                            if (line.contains(modifiedPackageName)) {
                                line = line.replace(modifiedPackageName, "$packageName.$modifiedPackageName")
                                break
                            }
                        }
                    }
                    return@map line
                }
                .joinToString("\n")
        }
        return sourceFiles
    }
}
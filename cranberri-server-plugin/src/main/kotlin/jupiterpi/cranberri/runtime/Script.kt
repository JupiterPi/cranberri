package jupiterpi.cranberri.runtime

import jupiterpi.cranberri.runtime.api.Setup
import jupiterpi.cranberri.runtime.api.Tick
import jupiterpi.cranberri.runtime.compilation.PROJECTS_OUT_ROOT
import jupiterpi.cranberri.runtime.compilation.ProjectCompiler
import org.xeustechnologies.jcl.JarClassLoader
import org.xeustechnologies.jcl.JclObjectFactory
import java.lang.reflect.Method
import java.util.UUID

class Script private constructor(
    file: String,
    val projectName: String,
    val instanceId: String,
    val scriptName: String,
) {
    companion object {
        fun compile(projectName: String, script: String): Script {
            val instanceId = UUID.randomUUID().toString().replace("-", "")
            ProjectCompiler.compileProject(projectName, instanceId)
            return Script("$projectName-$instanceId.jar", projectName, instanceId, script)
        }
    }

    private val scriptInstance: Any
    private val setup: Method?
    private val tick: Method?

    val scriptClassName get() = "cranberri_project_$projectName.instance_$instanceId.$scriptName"

    init {
        val jcl = JarClassLoader().also { it.add("$PROJECTS_OUT_ROOT/$file") }
        val scriptClass = JclObjectFactory.getInstance().create(jcl, scriptClassName).javaClass

        if (scriptClass.isAnnotationPresent(jupiterpi.cranberri.runtime.api.Script::class.java)) {
            scriptInstance = scriptClass.getConstructor().newInstance()

            setup = scriptClass.declaredMethods.singleOrNull { it.isAnnotationPresent(Setup::class.java) }.also { it?.isAccessible = true }
            tick = scriptClass.declaredMethods.singleOrNull { it.isAnnotationPresent(Tick::class.java) }.also { it?.isAccessible = true }
        } else throw Exception("Invalid script: Not annotated @Script")
    }

    fun invokeSetup() { setup?.invoke(scriptInstance) }
    fun invokeTick() { tick?.invoke(scriptInstance) }
}
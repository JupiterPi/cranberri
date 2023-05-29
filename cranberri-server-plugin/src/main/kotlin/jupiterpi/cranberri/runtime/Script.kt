package jupiterpi.cranberri.runtime

import jupiterpi.cranberri.runtime.api.Setup
import jupiterpi.cranberri.runtime.api.Tick
import org.xeustechnologies.jcl.JarClassLoader
import org.xeustechnologies.jcl.JclObjectFactory
import java.lang.reflect.Method
import java.util.UUID

val scripts = listOf<Script>()

class Script private constructor(
    file: String,
    private val projectName: String,
    private val instanceId: String,
    private val script: String,
) {
    companion object {
        fun compile(projectName: String, script: String): Script {
            val instanceId = UUID.randomUUID().toString()
            ProjectCompiler.compileProject(projectName, instanceId)
            return Script("$projectName-$instanceId.jar", projectName, instanceId, script)
        }
    }

    private val scriptInstance: Any
    private val setup: Method?
    private val tick: Method?

    val scriptClassName get() = "cranberri_project_$projectName.instance_$instanceId.script_$script"

    init {
        val jcl = JarClassLoader().also { it.add(file) }
        val scriptClass = JclObjectFactory.getInstance().create(jcl, scriptClassName).javaClass

        if (scriptClass.isAnnotationPresent(jupiterpi.cranberri.runtime.api.Script::class.java)) {
            val scriptName = scriptClass.getAnnotation(jupiterpi.cranberri.runtime.api.Script::class.java).name
            scriptInstance = scriptClass.getConstructor().newInstance()

            setup = scriptClass.methods.singleOrNull { it.isAnnotationPresent(Setup::class.java) }
            tick = scriptClass.methods.singleOrNull { it.isAnnotationPresent(Tick::class.java) }
        } else throw Exception("Invalid script: Not annotated @Script")
    }

    fun invokeSetup() { setup?.invoke(scriptInstance) }
    fun invokeTick() { tick?.invoke(scriptInstance) }
}
package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.Script
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

class RunningScript(computer: Computer, val script: Script) {
    companion object {
        fun compile(computer: Computer, projectName: String, scriptName: String)
        = RunningScript(computer, Script.compile(projectName, scriptName))
    }

    val pins = computer.loadPins()

    val loggers = mutableListOf<Logger>()
    var doDebug = true
    fun disableDebug() { doDebug = false }
    val logger = object : Logger() {
        override fun printDebug(msg: String) { if (doDebug) loggers.forEach { it.printDebug(msg) } }
        override fun printLog  (msg: String) {              loggers.forEach { it.printLog  (msg) } }
        override fun printError(msg: String) {              loggers.forEach { it.printError(msg) } }
        override fun sendMessage(message: Component) {}
    }
    init {
        loggers += ConsoleLogger(script)
    }

    fun start() {
        logger.printDebug("starting")
        script.invokeSetup()
        Bukkit.getScheduler().runTaskTimer(plugin, { task ->
            if (shutdown) task.cancel()
            else script.invokeTick()
        }, 0, 2)
    }

    private var shutdown = false
    fun deactivate() {
        shutdown = true
        logger.printDebug("shutting down")
    }
}
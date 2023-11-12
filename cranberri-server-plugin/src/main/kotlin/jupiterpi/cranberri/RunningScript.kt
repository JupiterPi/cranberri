package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.Script
import jupiterpi.cranberri.runtime.api.IO
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

open class RunningScript(private val computer: Computer, val script: Script) {
    companion object {
        fun compile(computer: Computer, projectName: String, scriptName: String): RunningScript {
            val script = Script.compile(projectName, scriptName)
            return if (!script.arduinoMode) RunningScript(computer, script) else ArduinoModeRunningScript(computer, script)
        }
    }

    val pins = computer.loadPins()

    val loggers = mutableListOf<Logger>(ConsoleLogger(script))
    var doDebug = true
    fun disableDebug() { doDebug = false }
    val logger = object : Logger() {
        override fun printSystem(msg: String) {              loggers.forEach { it.printSystem(msg) } }
        override fun printDebug (msg: String) { if (doDebug) loggers.forEach { it.printDebug (msg) } }
        override fun printLog   (msg: String) {              loggers.forEach { it.printLog   (msg) } }
        override fun printError (msg: String) {              loggers.forEach { it.printError (msg) } }
        override fun sendMessage(message: Component) {}
    }

    fun start() {
        logger.printSystem("Starting")

        fun handleError(e: Exception) {
            computer.status = Computer.Status.ERROR
            e.cause?.printStackTrace() ?: e.printStackTrace()
            computer.runningScript?.logger?.printError(e.cause.toString())
        }

        try { script.invokeSetup() }
        catch (e: Exception) { handleError(e) }

        startScript {
            try { script.invokeTickOrLoop() }
            catch (e: Exception) { handleError(e) }
        }
    }

    protected open fun startScript(invokeTickOrLoop: () -> Unit) {
        Bukkit.getScheduler().runTaskTimer(plugin, { task ->
            if (shutdown) {
                pins.filterIsInstance<OutputPin>().forEach {
                    it.writeValue(IO.PinValue.LOW)
                    it.fulfillValue()
                }
                task.cancel()
            } else {
                invokeTickOrLoop()
            }
        }, 0, 2)
    }

    var shutdown = false
    fun deactivate() {
        shutdown = true
        logger.printSystem("Shutting down")
    }
}
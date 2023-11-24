package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.Script
import jupiterpi.cranberri.runtime.api.Arduino
import jupiterpi.cranberri.runtime.api.IO
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import kotlin.concurrent.thread

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
            computer.runningScript?.logger?.printError(if (e.cause is CranberriRuntimeError) e.cause!!.message!! else e.cause.toString())
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
                pins.filterIsInstance<OutputPin>().forEach { it.writeValue(IO.PinValue.LOW) }
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

class ArduinoModeRunningScript(computer: Computer, script: Script) : RunningScript(computer, script) {
    val pinModesSet = mutableSetOf<Int>()

    fun setPinMode(pin: Int, mode: Arduino.PinMode) {
        if ((mode == Arduino.PinMode.INPUT && pins[pin-1] !is InputPin) || (mode == Arduino.PinMode.OUTPUT && pins[pin-1] !is OutputPin)) {
            throw CranberriRuntimeError("Tried to set to wrong pin mode!")
        }
        pinModesSet += pin
    }

    var delayed = false
    private var loopsWithoutDelay = 0

    fun delayScript(ticks: Int) {
        delayed = true
        loopsWithoutDelay = 0
        Bukkit.getScheduler().runTaskLater(plugin, { _ -> delayed = false }, ticks.toLong())
    }

    override fun startScript(invokeTickOrLoop: () -> Unit) {
        thread(name = "Arduino_${script.instanceId}") {
            while (true) {
                if (shutdown) break

                invokeTickOrLoop()
                loopsWithoutDelay++

                if (loopsWithoutDelay >= 2) {
                    delayScript(1)
                    while (delayed) {
                        if (shutdown) break
                        Thread.sleep(10)
                    }
                }
            }
        }
    }
}

class CranberriRuntimeError(msg: String): Exception(msg)
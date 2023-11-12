package jupiterpi.cranberri.runtime.api

import jupiterpi.cranberri.*

@Suppress("unused")
object IO {
    // logging

    @JvmStatic fun disableDebug() {
        getComputer()?.runningScript?.disableDebug()
    }

    @JvmStatic fun log(msg: Any) {
        getComputer()?.runningScript?.logger?.printLog(msg.toString())
    }

    // pins io

    enum class PinValue {
        HIGH, LOW;

        fun toBoolean() = this == HIGH

        companion object {
            @JvmStatic fun fromBoolean(value: Boolean) = if (value) HIGH else LOW
        }
    }

    @JvmStatic fun writePin(pin: Int, value: PinValue) {
        val runningScript = getComputer()?.runningScript ?: return
        assertModeSet(pin)

        runningScript.pins[pin-1].let {
            if (it is OutputPin) it.writeValue(value) else throw Exception("Tried to write to input pin!")
        }

        val valueStr = when (value) {
            PinValue.HIGH -> 1
            PinValue.LOW -> 0
        }
        runningScript.logger.printDebug("out $pin $valueStr")
    }

    @JvmStatic fun readPin(pin: Int): PinValue {
        val runningScript = getComputer()?.runningScript ?: return PinValue.LOW
        assertModeSet(pin)

        runningScript.logger.printDebug("in $pin")
        runningScript.pins[pin-1].let {
            if (it is InputPin) return it.readValue() else throw Exception("Tried to write to input pin!")
        }
    }

    private fun assertModeSet(pin: Int) {
        val runningScript = getComputer()?.runningScript ?: return
        if (runningScript is ArduinoModeRunningScript && !runningScript.pinModesSet.contains(pin)) throw Exception("Tried to access pin without mode set!")
    }
}

@Suppress("unused")
object Arduino {
    enum class PinMode {
        INPUT, OUTPUT
    }

    @JvmStatic fun pinMode(pin: Int, mode: PinMode) {
        if (getScriptContext() != "setup") throw Exception("You can only call pinMode() from within setup()!")

        val runningScript = (getComputer()?.runningScript as ArduinoModeRunningScript?) ?: return
        runningScript.setPinMode(pin, mode)
    }

    class Delay(ticks: Int) {
        init {
            if (getScriptContext() != "loop") throw Exception("You can only call delay() from within loop()!")

            val runningScript = getComputer()?.runningScript as ArduinoModeRunningScript?
            if (runningScript != null) {
                runningScript.delayScript(ticks)
                while (runningScript.delayed) {
                    if (runningScript.shutdown) break
                    Thread.sleep(10)
                }
            }
        }
    }

    private fun getScriptContext() = Thread.currentThread().stackTrace.last { it.className.startsWith("cranberri_project_") }.methodName
}

private fun getComputer(): Computer? {
    val className = Thread.currentThread().stackTrace.last { it.className.startsWith("cranberri_project_") }.className
    return Computers.computers.firstOrNull { it.runningScript != null && className.startsWith(it.runningScript!!.script.scriptClassName) }
}
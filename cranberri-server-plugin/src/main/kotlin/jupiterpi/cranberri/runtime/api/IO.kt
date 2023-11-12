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
        runningScript.logger.printDebug("in $pin")
        runningScript.pins[pin-1].let {
            if (it is InputPin) return it.readValue() else throw Exception("Tried to write to input pin!")
        }
    }
}

@Suppress("unused")
object Arduino {
    enum class PinMode {
        INPUT, OUTPUT
    }

    //TODO check if called from right context (Arduino + setup/loop)

    @JvmStatic fun pinMode(pin: Int, pinMode: PinMode) {
        println("setting pin mode: $pin to $pinMode")
        //TODO implement
    }

    class Delay(ticks: Int) {
        init {
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
}

private fun getComputer(): Computer? {
    val className = Thread.currentThread().stackTrace.last { it.className.startsWith("cranberri_project_") }.className
    return Computers.computers.firstOrNull { it.runningScript != null && className.startsWith(it.runningScript!!.script.scriptClassName) }
}
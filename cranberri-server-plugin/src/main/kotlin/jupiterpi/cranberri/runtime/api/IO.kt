package jupiterpi.cranberri.runtime.api

import jupiterpi.cranberri.Computer
import jupiterpi.cranberri.Computers
import jupiterpi.cranberri.InputPin
import jupiterpi.cranberri.OutputPin

@Suppress("unused")
object IO {
    // logging

    fun disableDebug() {
        getComputer().runningScript!!.disableDebug()
    }

    fun log(msg: Any) {
        getComputer().runningScript!!.logger.printLog(msg.toString())
    }

    // pins io

    enum class PinValue {
        HIGH, LOW;

        fun toBoolean() = this == HIGH

        companion object {
            fun fromBoolean(value: Boolean) = if (value) HIGH else LOW
        }
    }

    fun writePin(pin: Int, value: PinValue) {
        val runningScript = getComputer().runningScript!!
        runningScript.pins[pin-1].let {
            if (it is OutputPin) it.writeValue(value) else throw Exception("Tried to write to input pin!")
        }

        val valueStr = when (value) {
            PinValue.HIGH -> 1
            PinValue.LOW -> 0
        }
        runningScript.logger.printDebug("out $pin $valueStr")
    }

    fun readPin(pin: Int): PinValue {
        val runningScript = getComputer().runningScript!!
        getComputer().runningScript!!.logger.printDebug("in $pin")
        runningScript.pins[pin-1].let {
            if (it is InputPin) return it.readValue() else throw Exception("Tried to write to input pin!")
        }
    }

    // ...

    private fun getComputer(): Computer {
        val className = Thread.currentThread().stackTrace.first { it.className.startsWith("cranberri_project_") }.className
        return Computers.computers.single { it.runningScript != null && className.startsWith(it.runningScript!!.script.scriptClassName) }
    }
}
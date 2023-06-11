package jupiterpi.cranberri.runtime.api

import jupiterpi.cranberri.Computer
import jupiterpi.cranberri.Computers
import jupiterpi.cranberri.tools.loggingSessions

@Suppress("unused")
object IO {
    private const val LOGGER_PREFIX = "[LOG]"
    private const val PINS_PREFIX = "[IO] "

    private fun out(prefix: String, str: String) {
        val msg = "$prefix $str"

        println(msg)

        val computer = getComputer()
        loggingSessions.filterValues { it == computer.runningScript!! }.keys.forEach { it.sendMessage(msg) }
    }

    // logging

    fun log(msg: String) {
        out(LOGGER_PREFIX, msg)
    }
    fun log(i: Int) {
        log(i.toString() + " [${getComputer()}]")
    }

    // pins io

    enum class PinMode { INPUT, OUTPUT }

    fun pinMode(pin: Int, mode: PinMode) {
        val modeStr = when (mode) {
            PinMode.INPUT -> "in"
            PinMode.OUTPUT -> "out"
        }
        out(PINS_PREFIX, "mode $pin $modeStr")
    }

    enum class PinValue {
        HIGH, LOW;

        fun toBoolean() = this == HIGH
    }

    fun writePin(pin: Int, value: PinValue) {
        val valueStr = when (value) {
            PinValue.HIGH -> 1
            PinValue.LOW -> 0
        }
        out(PINS_PREFIX, "out $pin $valueStr")
    }

    fun readPin(pin: Int): PinValue {
        out(PINS_PREFIX, "in $pin")
        return PinValue.HIGH  //TODO implement
    }

    // ...

    private fun getComputer(): Computer {
        val className = Thread.currentThread().stackTrace.single { it.className.startsWith("cranberri_project_") }.className
        return Computers.computers.single { it.runningScript != null && className.startsWith(it.runningScript!!.scriptClassName) }
    }
}
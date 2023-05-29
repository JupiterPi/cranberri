package jupiterpi.cranberri.runtime.api

import jupiterpi.cranberri.runtime.Script
import jupiterpi.cranberri.runtime.scripts

@Suppress("unused")
object IO {
    private const val LOGGER_PREFIX = "[LOG]"
    private const val PINS_PREFIX = "[IO] "

    private fun out(prefix: String, str: String) {
        println("$prefix $str")
    }

    // logging

    fun log(msg: String) {
        out(LOGGER_PREFIX, msg)
    }
    fun log(i: Int) {
        log(i.toString() + " [${getScript()}]")
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

    // test

    fun test() {
        out(LOGGER_PREFIX, getScript().scriptClassName)
    }

    private fun getScript(): Script {
        val className = Thread.currentThread().stackTrace.single { it.className.startsWith("cranberri_project_") }.className
        return scripts.single { className.startsWith(it.scriptClassName) }
    }
}
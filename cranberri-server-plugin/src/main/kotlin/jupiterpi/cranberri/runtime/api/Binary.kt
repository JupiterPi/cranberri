package jupiterpi.cranberri.runtime.api

import kotlin.math.pow

@Suppress("unused")
object Binary {

    @JvmStatic fun readBinary(startPin: Int, length: Int): List<Boolean> {
        return (startPin until startPin + length).map { IO.readPin(it).toBoolean() }
    }

    @JvmStatic fun writeBinary(startPin: Int, binary: List<Boolean>) {
        binary.forEachIndexed { i, bit -> IO.writePin(startPin + i, IO.PinValue.fromBoolean(bit)) }
    }

    @JvmStatic fun List<Boolean>.binaryToInt(): Int {
        var int = 0
        reversed().forEachIndexed { i, bit -> int += (2.0.pow(i) * (if (bit) 1 else 0)).toInt() }
        return int
    }

    @JvmStatic fun Int.toBinary(length: Int): List<Boolean> {
        var str = Integer.toBinaryString(this)
        while (str.length < length) { str = "0${str}" }
        return str.split("").subList(1, 1+length).map { it == "1" }
    }

}
package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.api.IO
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.Repeater
import org.bukkit.util.Vector

val PIN_BASE_MATERIAL = Material.GLASS
val PIN_MATERIAL = Material.REPEATER

open class Pin(
    val location: Location,
)

class OutputPin(location: Location) : Pin(location) {
    private var value = IO.PinValue.LOW

    fun writeValue(value: IO.PinValue) {
        this.value = value
    }

    fun fulfillValue() {
        location.block.blockData = (location.block.blockData as Repeater).also {
            it.isPowered = value.toBoolean()
        }
    }
}

class InputPin(location: Location) : Pin(location) {
    fun readValue(): IO.PinValue {
        return IO.PinValue.fromBoolean((location.block.blockData as Repeater).isPowered)
    }
}

@Throws(Exception::class)
fun Computer.loadPins(): List<Pin> {
    val direction = listOf(
        Vector(1, 0, 0),
        Vector(0, 0, -1),
        Vector(-1, 0, 0),
        Vector(1, 0, 1),
    ).singleOrNull { location.clone().add(it).block.type == PIN_BASE_MATERIAL } ?: throw Exception("No or too many adjacent glass blocks found!")
    val pinDirection = direction.clone().rotateAroundY(-0.5*Math.PI /*-90°*/)

    val pins = mutableListOf<Pin>()
    val location = location.clone()
    while (location.add(direction)/*has side effect*/.block.type == PIN_BASE_MATERIAL) {
        val pinLocation = location.clone().add(pinDirection)
        if (pinLocation.block.type != PIN_MATERIAL) continue
        pins += when ((pinLocation.block.blockData as Repeater).facing.direction) {
            pinDirection -> InputPin(pinLocation)
            pinDirection.clone().rotateAroundY(Math.PI /*180°*/) -> OutputPin(pinLocation)
            else -> throw Exception("Repeater is rotated the wrong way!")
        }
    }
    return pins
}
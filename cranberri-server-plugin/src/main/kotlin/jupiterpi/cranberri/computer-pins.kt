package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.api.IO
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.Repeater
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.util.Vector

val PIN_BASE_MATERIAL = Material.GLASS
val PIN_MATERIAL = Material.REPEATER

open class Pin(
    val location: Location,
)

class OutputPin(location: Location) : Pin(location) {
    var value = IO.PinValue.LOW

    fun writeValue(value: IO.PinValue) {
        this.value = value
        location.block.blockData = (location.block.blockData as Repeater).also {
            it.isPowered = value.toBoolean()
        }
    }
}

val outputPinListener = object : Listener {
    @EventHandler
    @Suppress("unused")
    fun onRedstone(event: BlockRedstoneEvent) {
        if (event.block.type == Material.REPEATER) {
            for (computer in Computers.computers) {
                if (computer.runningScript == null) continue
                val pin = computer.runningScript!!.pins.filterIsInstance<OutputPin>().singleOrNull { it.location == event.block.location } ?: continue
                event.newCurrent = if (pin.value == IO.PinValue.HIGH) 15 else 0
                break
            }
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
        Vector(0, 0, 1),
    )
        .filter { location.clone().add(it).block.type == PIN_BASE_MATERIAL }
        .let {
            if (it.isEmpty()) return listOf()
            if (it.size > 1) throw Exception("Too many adjacent glass blocks found!")
            it.single()
        }
    val pinDirections = listOf(1, -1).map { direction.clone().rotateAroundY(-0.5*it*Math.PI /* +/- 90° */) }

    val pins = mutableListOf<Pin>()
    val location = location.clone()
    while (location.add(direction)/*has side effect*/.block.type == PIN_BASE_MATERIAL) {
        val pinDirection = pinDirections.singleOrNull { location.clone().add(it).block.type == PIN_MATERIAL } ?: continue
        val pinLocation = location.clone().add(pinDirection)
        pins += when ((pinLocation.block.blockData as Repeater).facing.direction) {
            pinDirection -> InputPin(pinLocation)
            pinDirection.clone().rotateAroundY(Math.PI /*180°*/) -> OutputPin(pinLocation)
            else -> throw Exception("Repeater is rotated the wrong way!")
        }
    }
    return pins
}
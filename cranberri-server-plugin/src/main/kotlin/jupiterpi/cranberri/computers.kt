package jupiterpi.cranberri

import jupiterpi.cranberri.tools.isLoggerToolItem
import jupiterpi.cranberri.util.DATA_ROOT
import jupiterpi.cranberri.util.TextFile
import jupiterpi.cranberri.util.deserializeLocationFromString
import jupiterpi.cranberri.util.serializeToString
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

val COMPUTER_MATERIAL = Material.TARGET

class Computer(
    val location: Location,
    var activate: Boolean = false,
    var script: String? = null,
) {
    val status get() = if (activate) Status.ON else Status.OFF

    enum class Status(
        val color: Color,
        val material: Material,
        val displayName: String,
    ) {
        NOT_CONFIGURED(Color.GRAY, Material.GRAY_WOOL, "Not configured"),
        PINS_ERROR(Color.BLACK, Material.BLACK_WOOL, "Error with pins"),
        OFF(Color.WHITE, Material.WHITE_WOOL, "Off"),
        ON(Color.LIME, Material.LIME_WOOL, "On"),
        ERROR(Color.RED, Material.RED_WOOL, "Error!")
    }
}

object Computers {
    val computers = mutableListOf<Computer>()

    fun createComputer(block: Block): Computer {
        block.type = COMPUTER_MATERIAL
        return Computer(block.location).also { computers += it }
    }

    // persistence

    private const val PERSISTENCE_FILE = "$DATA_ROOT/computers.csv"

    fun load() {
        TextFile.readCsvFile(PERSISTENCE_FILE).forEach {
            computers += Computer(
                deserializeLocationFromString(it[0]),
                it[1].toBoolean(),
                if (it[2] != "null") it[2] else null,
            )
        }
    }

    fun save() {
        TextFile.csv(computers.map { listOf(
            it.location.serializeToString(),
            it.activate.toString(),
            it.script.toString(),
        ) }).writeFile(PERSISTENCE_FILE)
    }
}

fun getComputerBlock(block: Block?): Computer? {
    if (block?.type != COMPUTER_MATERIAL) return null
    return Computers.computers.singleOrNull { it.location == block.location }
}

val computersListener = object : Listener {
    @EventHandler
    @Suppress("unused")
    fun deleteComputerOnBreak(event: BlockBreakEvent) {
        val brokenComputer = Computers.computers.filter { it.location == event.block.location }
        Computers.computers.removeAll(brokenComputer)
    }

    @EventHandler
    @Suppress("unused")
    fun rightClickToOpenConfigurationGui(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (isLoggerToolItem(event.item)) return
        getComputerBlock(event.clickedBlock)?.openConfigurationGui(event.player) ?: return
        event.isCancelled = true
    }
}

val computersStatusParticleSpawner = {
    Computers.computers.forEach {
        it.location.world.spawnParticle(
            Particle.REDSTONE,
            it.location.clone().add(Vector(0.5, 1.1, 0.5)), 20,
            DustOptions(it.status.color, 1f)
        )
    }
}
package jupiterpi.cranberri

import jupiterpi.cranberri.tools.isLoggerToolItem
import jupiterpi.cranberri.util.DATA_ROOT
import jupiterpi.cranberri.util.TextFile
import jupiterpi.cranberri.util.deserializeLocationFromString
import jupiterpi.cranberri.util.serializeToString
import org.bukkit.*
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
    activate: Boolean = false,
    var script: String? = null,
) {
    var status = Status.OFF
        set(status) {
            field = status
            ComputerConfigurationGuis.refreshOpenedGuis(this)
        }
    var isCompiling = false

    enum class Status(
        val color: Color,
        val material: Material,
        val displayName: String,
        val wasActivated: Boolean,
    ) {
        NOT_CONFIGURED(Color.GRAY, Material.GRAY_WOOL, "Not configured", false),
        PINS_ERROR(Color.BLACK, Material.BLACK_WOOL, "Error with pins", false),
        OFF(Color.WHITE, Material.WHITE_WOOL, "Off", false),
        ON(Color.LIME, Material.LIME_WOOL, "On", true),
        COMPILATION_ERROR(Color.RED, Material.RED_WOOL, "Compilation Error!", true),
        ERROR(Color.RED, Material.RED_WOOL, "Runtime Error!", true)
    }

    var runningScript: RunningScript? = null

    init {
        if (activate) activate()
    }
    fun activate(onComplete: (() -> Unit)? = null) {
        if (script != null) {
            status = Status.ON

            val projectName = script!!.split(":")[0]
            val scriptName = script!!.split(":")[1]
            Bukkit.getScheduler().runTaskAsynchronously(plugin) { _ ->
                try {
                    isCompiling = true
                    runningScript = RunningScript.compile(this, projectName, scriptName)
                    onComplete?.invoke()
                } catch (e: Exception) {
                    status = Status.COMPILATION_ERROR
                    e.printStackTrace()
                } finally {
                    isCompiling = false
                }
            }

            var invocations = 0
            Bukkit.getScheduler().runTaskTimer(plugin, { task ->
                invocations++
                if (invocations > 40) task.cancel()

                if (runningScript != null) {
                    task.cancel()
                    runningScript!!.start()
                }
            }, 0, 5)
        }
    }
    fun deactivate() {
        status = Status.OFF

        runningScript?.deactivate()
        runningScript = null
    }
}

object Computers {
    val computers = mutableListOf<Computer>()

    fun createComputer(block: Block): Computer {
        block.type = COMPUTER_MATERIAL
        return Computer(block.location).also { computers += it }
    }

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, { _ ->
            computers.forEach { it.runningScript?.pins?.forEach { if (it is OutputPin) it.fulfillValue() } }
        }, 0, 1)
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
            (it.status == Computer.Status.ON).toString(),
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
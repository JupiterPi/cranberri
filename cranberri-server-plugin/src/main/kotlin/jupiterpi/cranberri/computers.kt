package jupiterpi.cranberri

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class Computer(
    val location: Location,
    var status: ComputerStatus = ComputerStatus.NOT_CONFIGURED,
) {
    fun openConfigurationGui(player: Player) {
        fun statusItem(gui: ChestGui, pane: OutlinePane): GuiItem
        = GuiItem(
            ItemStack(status.material).also { item ->
                item.itemMeta = item.itemMeta.also { it.displayName(Component.text(status.displayName, Style.style(TextColor.color(status.color.asRGB())))) }
            }
        ) {
            status = if (status == ComputerStatus.ON) ComputerStatus.OFF else ComputerStatus.ON

            pane.removeItem(pane.items[0])
            pane.addItem(statusItem(gui, pane))
            gui.show(player)
        }

        ChestGui(3, "Computer Configuration").run {
            setOnGlobalClick { it.isCancelled = true }
            addPane(OutlinePane(3, 1, 3, 1).also { it.addItem(statusItem(this, it)) })
            show(player)
        }
    }
}

enum class ComputerStatus(
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

object Computers {
    val computers = mutableListOf<Computer>()
}

val computersListener = object : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val brokenComputer = Computers.computers.filter { it.location == event.block.location }
        Computers.computers.removeAll(brokenComputer)
    }

    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.clickedBlock?.type != Material.TARGET) return
        Computers.computers.singleOrNull { it.location == event.clickedBlock!!.location }?.openConfigurationGui(event.player)
        event.isCancelled = true
    }
}

val computersStatusProvider = {
    Computers.computers.forEach {
        it.location.world.spawnParticle(
            Particle.REDSTONE,
            it.location.clone().add(Vector(0.5, 1.1, 0.5)), 20,
            DustOptions(it.status.color, 1f)
        )
    }
}
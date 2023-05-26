package jupiterpi.cranberri

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Material
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

val cranberriToolItem get() = ItemStack(Material.SWEET_BERRIES).also { item ->
    item.itemMeta = item.itemMeta.also {
        it.displayName(Component.join(JoinConfiguration.noSeparators(),
            cranberriLettering,
            Component.text(" Tool")
        ))
        it.lore(listOf(
            Component.text("Right click to place computer.")
        ))
    }
}

val cranberriToolCommand = CommandExecutor { sender, _, _, _ ->
    if (sender !is Player) return@CommandExecutor false
    val player = sender as Player

    player.inventory.addItem(cranberriToolItem)

    return@CommandExecutor true
}

val cranberriToolListener = object : Listener {
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.itemInHand.displayName() != cranberriToolItem.displayName()) return

        event.blockPlaced.type = Material.TARGET

        val computer = Computer(event.blockPlaced.location)
        Computers.computers += computer
        computer.openConfigurationGui(event.player)
    }
}
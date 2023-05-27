package jupiterpi.cranberri.tools

import jupiterpi.cranberri.cranberriLettering
import jupiterpi.cranberri.getComputerBlock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

val loggerToolItem get() = ItemStack(Material.SPYGLASS).also { item ->
    item.itemMeta = item.itemMeta.also {
        it.displayName(Component.join(JoinConfiguration.noSeparators(),
            cranberriLettering,
            Component.text(" Logger Tool")
        ))
        it.lore(listOf(
            Component.text("Right click on computer for logs."),
            Component.text("Right click for options.")
        ))
    }
}

fun isLoggerToolItem(item: ItemStack?)
= item?.displayName() == loggerToolItem.displayName()

val loggerToolListener = object : Listener {
    @EventHandler
    @Suppress("unused")
    fun onRightClick(event: PlayerInteractEvent) {
        if (!isLoggerToolItem(event.item)) return
        if (!(event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR)) return

        val computer = getComputerBlock(event.clickedBlock)
        if (computer != null) {
            event.player.sendMessage("Showing logs for computer")
        } else {
            event.player.sendMessage("Closing logs")
        }

        event.isCancelled = true
    }
}
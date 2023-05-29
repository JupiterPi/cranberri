package jupiterpi.cranberri.tools

import jupiterpi.cranberri.Computers
import jupiterpi.cranberri.cranberriLettering
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

val computerToolItem get() = ItemStack(Material.SWEET_BERRIES).also { item ->
    item.itemMeta = item.itemMeta.also {
        it.displayName(Component.join(JoinConfiguration.noSeparators(),
            cranberriLettering,
            Component.text(" Computer Tool", Style.style(TextColor.color(255, 255, 255)))
        ))
        it.lore(listOf(
            Component.text("Right click to place computer.")
        ))
        it.addEnchant(Enchantment.LUCK, 1, false)
        it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }
}
fun isComputerToolItem(item: ItemStack?)
= item?.displayName() == computerToolItem.displayName()

val computerToolListener = object : Listener {
    @EventHandler
    @Suppress("unused")
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!isComputerToolItem(event.itemInHand)) return
        Computers.createComputer(event.blockPlaced).openConfigurationGui(event.player)
    }
}
package jupiterpi.cranberri

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object ComputerConfigurationGuis {
    private val openedGuis = mutableMapOf<Player, Computer>()
    private val lastOpened = mutableMapOf<Player, Long>()

    fun openForPlayer(player: Player, computer: Computer) {
        openedGuis[player] = computer
        lastOpened[player] = player.world.gameTime
    }

    fun closeForPlayer(player: Player) {
        if (player.world.gameTime - (lastOpened[player] ?: return) > 3) openedGuis.remove(player)
    }

    fun refreshOpenedGuis(computer: Computer) {
        Bukkit.getScheduler().runTask(plugin) { _ ->
            openedGuis.filterValues { it == computer }.keys.forEach { player -> computer.openConfigurationGui(player) }
        }
    }
}

fun Computer.openConfigurationGui(player: Player) {
    ComputerConfigurationGuis.openForPlayer(player, this)

    val statusItem = GuiItem(
        ItemStack(status.material).also { item ->
            item.itemMeta = item.itemMeta.also { it.displayName(Component.text(status.displayName, Style.style(TextColor.color(status.color.asRGB())))) }
            if (status == Computer.Status.ERROR || status == Computer.Status.COMPILATION_ERROR) {
                item.lore(listOf(Component.text("Check the server console for error details.")))
            }
        }
    ) {
        if (!isCompiling) {
            if (!status.wasActivated) {
                activate(onComplete = {
                    runningScript!!.loggers += PlayerLogger(player, runningScript!!.script)
                })
            } else {
                deactivate()
            }
        }
    }

    val scriptItem = GuiItem(
        ItemStack(Material.BOOK).also { item ->
            item.itemMeta = item.itemMeta.also {
                it.displayName(Component.text(if (script == null) "Script..." else "Script: $script"))
                if (script != null) it.addEnchant(Enchantment.LUCK, 1, false)
                it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
        }
    ) {
        AnvilGui("Configure Script...").run {
            val firstItem = GuiItem(
                ItemStack(Material.BOOK).also { item ->
                    item.itemMeta = item.itemMeta.also {
                        it.displayName(Component.text(if (script != null) script!! else "<none>"))
                    }
                }
            )
            val tooltipItem = GuiItem(
                ItemStack(Material.LIGHT).also { item ->
                    item.itemMeta = item.itemMeta.also {
                        it.displayName(Component.text("Info: You can change the name to one space \" \" to clear the script."))
                    }
                }
            )
            val resultItem = GuiItem(
                ItemStack(Material.BOOK).also { item ->
                    item.itemMeta = item.itemMeta.also {
                        it.displayName(Component.text("CONFIRM", Style.style(TextColor.color(255, 255, 255), TextDecoration.BOLD)))
                    }
                }
            ) {
                if (renameText.isBlank()) script = null
                else script = (if (renameText == "<none>") null else renameText)
                openConfigurationGui(player)
            }

            setOnGlobalClick { it.isCancelled = true }
            firstItemComponent.addPane(OutlinePane(0, 0, 1, 1).also { it.addItem(firstItem) })
            secondItemComponent.addPane(OutlinePane(0, 0, 1, 1).also { it.addItem(tooltipItem) })
            resultComponent.addPane(OutlinePane(0, 0, 1, 1).also { it.addItem(resultItem) })
            show(player)
        }
    }

    ChestGui(3, "Configure Computer...").run {
        setOnGlobalClick { it.isCancelled = true }
        addPane(OutlinePane(3, 1, 1, 1).also { it.addItem(statusItem) })
        addPane(OutlinePane(5, 1, 1, 1).also { it.addItem(scriptItem) })
        setOnClose { ComputerConfigurationGuis.closeForPlayer(player) }
        show(player)
    }
}
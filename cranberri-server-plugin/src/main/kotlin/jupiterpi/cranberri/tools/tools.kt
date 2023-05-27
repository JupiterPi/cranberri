package jupiterpi.cranberri.tools

import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player

val toolsCommand = CommandExecutor { sender, _, _, argsArray ->
    if (sender !is Player) return@CommandExecutor false
    val player: Player = sender

    val args = argsArray.toMutableList()
    if (args.size == 0) args += "tool"

    player.inventory.addItem(when (args[0]) {
        "tool" -> computerToolItem
        "logger" -> loggerToolItem
        else -> return@CommandExecutor false
    })

    return@CommandExecutor true
}
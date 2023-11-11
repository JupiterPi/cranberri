package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.Script
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Player

abstract class Logger {
    open fun printSystem(msg: String) {
        print(Component.text("[SYS]", TextColor.color(Color.GRAY.asRGB())), msg)
    }

    open fun printDebug(msg: String) {
        print(Component.text("[IO] ", TextColor.color(Color.BLUE.asRGB())), msg)
    }

    open fun printLog(msg: String) {
        print(Component.text("[LOG]", TextColor.color(Color.GREEN.asRGB())), msg)
    }

    open fun printError(msg: String) {
        print(Component.text("[ERR]", TextColor.color(Color.RED.asRGB())), msg)
    }

    // print

    protected open fun print(prefix: Component, msg: String) {
        val message = Component.join(JoinConfiguration.noSeparators(),
            prefix,
            Component.text(" "),
            Component.text(msg),
        )
        sendMessage(message)
    }

    protected abstract fun sendMessage(message: Component)
}

class PlayerLogger(val player: Player, private val script: Script) : Logger() {
    override fun sendMessage(message: Component) {
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            //Component.text("${script.projectName}/${script.scriptName}/${script.instanceId.substring(0, 4)} ", TextColor.color(Color.GRAY.asRGB())),
            Component.text("${script.shortInstanceId}: ", TextColor.color(Color.GRAY.asRGB())),
            message
        ))
    }
}

class ConsoleLogger(private val script: Script) : Logger() {
    override fun sendMessage(message: Component) {
        Bukkit.getConsoleSender().sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("${script.scriptClassName}: ", TextColor.color(Color.GRAY.asRGB())),
            message
        ))
    }
}
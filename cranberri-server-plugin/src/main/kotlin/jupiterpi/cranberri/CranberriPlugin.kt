package jupiterpi.cranberri

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class CranberriPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        plugin = this

        Bukkit.getPluginManager().registerEvents(this, this)

        server.consoleSender.sendMessage(
            Component.join(JoinConfiguration.noSeparators(),
                Component.text("Cranberri", Style.style(TextColor.color(Color.WHITE.asRGB()), TextDecoration.BOLD)),
                Component.text(".", Style.style(TextColor.color(Color.RED.asRGB()), TextDecoration.BOLD)),
                Component.text(" enabled")
            )
        )
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage(Component.text("Hello, ${event.player.name}, welcome to Cranberri."))
    }

}

lateinit var plugin: CranberriPlugin
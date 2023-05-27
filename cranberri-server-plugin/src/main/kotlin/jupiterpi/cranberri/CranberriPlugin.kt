package jupiterpi.cranberri

import jupiterpi.cranberri.tools.computerToolListener
import jupiterpi.cranberri.tools.loggerToolListener
import jupiterpi.cranberri.tools.toolsCommand
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
import jupiterpi.cranberri.util.DATA_ROOT
import jupiterpi.cranberri.util.TextFile

val cranberriLettering = Component.join(JoinConfiguration.noSeparators(),
    Component.text("Cranberri", Style.style(TextColor.color(Color.WHITE.asRGB()), TextDecoration.BOLD)),
    Component.text(".", Style.style(TextColor.color(Color.RED.asRGB()), TextDecoration.BOLD)),
)

class CranberriPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        plugin = this

        TextFile.createPath(DATA_ROOT)

        Bukkit.getPluginManager().registerEvents(this, this)
        server.consoleSender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            cranberriLettering,
            Component.text(" enabled")
        ))

        // tools

        getCommand("cranberri")!!.setExecutor(toolsCommand)
        Bukkit.getPluginManager().registerEvents(loggerToolListener, this)
        Bukkit.getPluginManager().registerEvents(computerToolListener, this)

        // computers

        Computers.load()

        Bukkit.getScheduler().runTaskTimer(this, computersStatusParticleSpawner, 0, 10)
        Bukkit.getPluginManager().registerEvents(computersListener, this)
    }

    override fun onDisable() {
        Computers.save()
    }

    @EventHandler
    @Suppress("unused")
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("Hello ${event.player.name}, welcome to "),
            cranberriLettering
        ))
    }

}

lateinit var plugin: CranberriPlugin
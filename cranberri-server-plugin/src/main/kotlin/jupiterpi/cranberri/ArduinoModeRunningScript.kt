package jupiterpi.cranberri

import jupiterpi.cranberri.runtime.Script
import org.bukkit.Bukkit
import kotlin.concurrent.thread

class ArduinoModeRunningScript(computer: Computer, script: Script) : RunningScript(computer, script) {
    var delayed = false
    private var loopsWithoutDelay = 0

    fun delayScript(ticks: Int) {
        delayed = true
        loopsWithoutDelay = 0
        Bukkit.getScheduler().runTaskLater(plugin, { _ -> delayed = false }, ticks.toLong())
    }

    override fun startScript(invokeTickOrLoop: () -> Unit) {
        thread(name = "Arduino_${script.instanceId}") {
            while (true) {
                if (shutdown) break

                invokeTickOrLoop()
                loopsWithoutDelay++
                if (loopsWithoutDelay > 2) delayScript(1)
            }
        }
    }
}
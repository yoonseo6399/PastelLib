package io.github.yoonseo.pastellib

import io.github.yoonseo.pastellib.utils.Selector
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.plugin.java.JavaPlugin

class PastelLib : JavaPlugin() {
    companion object {
        lateinit var instance: PastelLib
    }
    override fun onEnable() {
        instance = this
        Bukkit.getPlayer("command_juho")?.let {
            syncRepeating {
                val e = Selector.entity<LivingEntity>(it.eyeLocation,it.location.direction,0.2,20.0) { e -> e != it }
                if(e != null) {
                    e.debug()
                    cancel()
                }
            }
        }
    }
    override fun onDisable() {
        // Plugin shutdown logic
    }
}

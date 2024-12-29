package io.github.yoonseo.pastellib

import io.github.yoonseo.pastellib.activities.Sit
import io.github.yoonseo.pastellib.activities.isSitting
import io.github.yoonseo.pastellib.guns.Gun
import io.github.yoonseo.pastellib.guns.GunCommand
import io.github.yoonseo.pastellib.guns.GunCommandTabCompleter
import io.github.yoonseo.pastellib.utils.TaskCommand
import org.bukkit.plugin.java.JavaPlugin

class PastelLib : JavaPlugin() {
    companion object {
        lateinit var instance: PastelLib
    }
    override fun onEnable() {
        instance = this
        Sit.initialize()
        Gun.initalize()
        getCommand("gun")?.also { it.tabCompleter = GunCommandTabCompleter() }?.setExecutor(GunCommand())
        getCommand("task")?.setExecutor(TaskCommand())
    }
    override fun onDisable() {
        // Plugin shutdown logic
        Sit.map.keys.forEach { it.isSitting = false }
    }
}

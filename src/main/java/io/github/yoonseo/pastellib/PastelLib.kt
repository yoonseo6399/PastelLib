package io.github.yoonseo.pastellib

import org.bukkit.plugin.java.JavaPlugin

class PastelLib : JavaPlugin() {
    companion object {
        lateinit var instance: PastelLib
    }
    override fun onEnable() {
        instance = this
    }
    override fun onDisable() {
        // Plugin shutdown logic
    }
}

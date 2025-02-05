package io.github.yoonseo.pastellib.activities

import com.destroystokyo.paper.MaterialTags
import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.debug
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent

object Sit {
    val map :HashMap<LivingEntity, BlockDisplay> = HashMap()
    fun sit(entity : LivingEntity) {
        entity.isSitting = true
    }
    fun initialize() {
        PastelLib.instance.getCommand("sit")?.setExecutor(OnSitCommand())
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun onUnMount(event: EntityDismountEvent) {
                val e = event.entity
                if(e is LivingEntity) {
                    e.isSitting = false
                }
            }
        }, PastelLib.instance)
    }
}
class OnSitCommand() : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if(sender !is LivingEntity) return false
        sender.isSitting = true
        return true
    }
}

var LivingEntity.isSitting : Boolean
    get() = Sit.map[this]!=null
    set(value) {
        if(value && !isSitting) {
            val a = this.location.world.spawn(location,BlockDisplay::class.java) {
                it.block = Material.AIR.createBlockData()
            }
            a.addPassenger(this@isSitting)
        }
        else {
            Sit.map[this]?.remove()
            Sit.map.remove(this)
        }
    }
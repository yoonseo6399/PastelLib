package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.PastelLib
import kotlinx.serialization.builtins.PairSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.system.measureTimeMillis

class TaskCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.size == 2 && args[0] == "cancel") Bukkit.getScheduler().cancelTask(args[0].toInt())
        if(args.size == 2 && args[0] == "run") {
            log("starting performace test")
            //val time = measureTimeMillis    a {
            //    val item = ItemStack(Material.STICK,1)
            //    item.dataContainer.addValue("TEST")
            //}
        }
        return true
    }
}
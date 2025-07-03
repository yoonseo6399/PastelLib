package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.celestia.Celestia
import io.github.yoonseo.pastellib.celestia.models.EnergyRing
import io.github.yoonseo.pastellib.celestia.models.Lighting
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import io.github.yoonseo.pastellib.utils.entity.model.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.joml.AxisAngle4f
import org.joml.Quaternionf

class TaskCommand : CommandExecutor {
    var testModel : Model<BlockDisplay>? by nullIf { it?.isDead }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.size == 2 && args[0] == "cancel") Bukkit.getScheduler().cancelTask(args[0].toInt())
        if(args.size == 2 && args[0] == "run") {
            log("starting performace test")
            //val time = measureTimeMillis    a {
            //    val item = ItemStack(Material.STICK,1)
            //    item.dataContainer.addValue("TEST")
            //}
        }

        if(args.size == 1 && args[0] == "init"){
            debug {

                commandJuho.sendMessage("initalizing")
            }
        }

        if(args.size == 1 && args[0] == "celestia" && sender is Player) Celestia(sender)
        if(args.size == 1 && args[0] == "lighting" && sender is Player) Lighting(sender,7,sender.location.direction).renderer.load(sender.eyeLocation)
        return true
    }
}


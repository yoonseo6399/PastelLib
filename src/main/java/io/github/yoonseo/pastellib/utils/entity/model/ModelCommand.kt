package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.DebugScope.commandJuho
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player

class ModelCommand : CommandExecutor{
    
    
    
    
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if(p3.isEmpty()) return false
        when(p3[0]){
            "register" -> {
                if(p3.size != 2) return false
                if(sender !is Player){
                    sender.sendMessage("only players can register models")
                    return true
                }
                sender.sendMessage("searching for target")
                val target = sender.location.world.getNearbyEntities(sender.boundingBox).firstOrNull { it is BlockDisplay && it.passengers.isNotEmpty()} as? BlockDisplay
                sender.sendMessage("target : ${target?.name ?: "not found"}")
                sender.sendMessage("or found no passenger, single model cannot be registered")
                if(target == null) return true
                val passengers = target.passengers.mapNotNull { it as? BlockDisplay }
                val datas = mutableListOf<DisplayData>()
                passengers.forEach {
                    val data = DisplayData.Block(it.transformation,it.block,it.interpolationDuration,it.teleportDuration) as DisplayData
                    datas.add(data)
                }
                PastelLib.modelFileManager.saveModel(p3[1],datas)
                sender.sendMessage("saved")
            }
            "load" -> {
                if(p3.size != 2) return false
                if(sender !is Player){
                    sender.sendMessage("only players can register models")
                    return true
                }
                val model = ModelRenderer<BlockDisplay>().load(sender.location,p3[1])
            }
        }
        return true
    }

}
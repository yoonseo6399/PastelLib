package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.DebugScope.commandJuho
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
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
                val target = sender.location.world.getNearbyEntities(sender.boundingBox).firstOrNull { it is BlockDisplay && it.passengers.isNotEmpty() && it.block.material == Material.AIR} as? BlockDisplay
                sender.sendMessage("target : ${target?.name ?: "not found or found no passenger, single model cannot be registered"}")
                if(target == null) return true
                val passengers = recursivePassengers(target).mapNotNull { it as? BlockDisplay }
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
                DefaultModel<Display>(p3[1]).renderer.load(sender.location)
                sender.sendMessage("Successfully loaded model ${p3[1]}")
            }
            "debug" -> {
                if(sender !is Player){
                    sender.sendMessage("only players can register models")
                    return true
                }
                sender.sendMessage("searching for target")
                val target = sender.location.world.getNearbyEntities(sender.boundingBox).firstOrNull { it is BlockDisplay && it.passengers.isNotEmpty() && it.block.material == Material.AIR} as? BlockDisplay
                sender.sendMessage("target : ${target?.name ?: "not found or found no passenger, single model cannot be registered"}")
                if(target == null) return true
                val passengers = recursivePassengers(target).mapNotNull { it as? BlockDisplay }
                var count = 0
                syncRepeating {
                    count++
                    passengers.forEach { AdvancedBlockDisplay.getBy(it).debug() }
                    if(count >= 20*10) cancel()
                }
            }
        }
        return true
    }

    fun recursivePassengers(entity: Entity): List<Entity> {
        val passengers = mutableListOf<Entity>()
        for (passenger in entity.passengers) {
            if(passenger.passengers.isEmpty()){
                passengers.add(passenger)
            } else {
                passengers.add(passenger)
                passengers.addAll(recursivePassengers(passenger))
            }
        }
        return passengers
    }
}
package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.entity.model.DisplayData
import io.github.yoonseo.pastellib.utils.entity.model.LASER
import io.github.yoonseo.pastellib.utils.entity.model.LaserSizeModule
import io.github.yoonseo.pastellib.utils.entity.model.ModelRenderer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.BlockDisplay

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
        if(args.size == 1 && args[0] == "init"){
            debug {
                val sizeModule = LaserSizeModule()
                executeOnItem(Material.STICK){
                    ModelRenderer<BlockDisplay>().render(commandJuho.location, LASER).also {
                        it.attachModule(sizeModule)
                    }
                }
                executeOnItem(Material.ANDESITE_STAIRS) {
                    commandJuho.sendMessage("affw")
                    sizeModule.size(1f,1f,10f)
                }
                executeOnItem(Material.RED_TERRACOTTA){
                    sizeModule.size(1f,1f,5f)
                }
                executeOnItem(Material.AMETHYST_BLOCK){
                    commandJuho.sendMessage("interaccccct searching for target")
                    val target = commandJuho.location.world.getNearbyEntities(commandJuho.boundingBox).firstOrNull { it is BlockDisplay } as? BlockDisplay
                    commandJuho.sendMessage("target : ${target?.name ?: "not found"}")
                    if(target == null) return@executeOnItem
                    if(target.passengers.size == 0) {
                        commandJuho.sendMessage("found no passenger, single model cannot be registered")
                    }
                    val passengers = target.passengers.mapNotNull { it as? BlockDisplay }
                    val datas = mutableListOf<DisplayData>()
                    passengers.forEach {
                        val data = DisplayData.Block(it.transformation,it.block,it.interpolationDuration,it.teleportDuration) as DisplayData
                        datas.add(data)
                    }
                    PastelLib.modelFileManager.saveModel("Laser",datas)
                    commandJuho.sendMessage("saved")
                }
                executeOnItem(Material.AMETHYST_SHARD){
                    val model = ModelRenderer<BlockDisplay>().load(commandJuho.location,"Laser").also {
                        it.attachModule(sizeModule)
                    }
                    commandJuho.sendMessage("loaded")

                }

                commandJuho.sendMessage("initalizing")
            }
        }
        return true
    }
}
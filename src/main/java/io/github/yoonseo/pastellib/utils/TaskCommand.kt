package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.PastelLib.Companion.json
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.DisplayData
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.LASER
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.LaserSizeModule
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.ModelRenderer
import kotlinx.serialization.builtins.PairSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.BlockDisplay
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
                    var target = commandJuho.location.world.getNearbyEntities(commandJuho.boundingBox).firstOrNull { it is BlockDisplay } as? BlockDisplay
                    commandJuho.sendMessage("target : ${target?.name ?: "not found"}")
                    if(target == null) return@executeOnItem
                    if(target.passengers.size != 0) target = target.passengers.first { it is BlockDisplay } as? BlockDisplay ?: return@executeOnItem
                    val data = DisplayData.Block(target.transformation,target.block,target.interpolationDuration,target.teleportDuration)
                    commandJuho.sendMessage("displayData : $data")
                    commandJuho.sendMessage("---------JSON----------")
                    val dataJson = json.encodeToString(data)
                    commandJuho.sendMessage(dataJson)
                    commandJuho.sendMessage("---------Deserializing-----------")
                    commandJuho.sendMessage("${json.decodeFromString<DisplayData>(dataJson)}")
                }
                commandJuho.sendMessage("initalizing")
            }
        }
        return true
    }
}
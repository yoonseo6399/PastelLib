package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import io.github.yoonseo.pastellib.utils.entity.model.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.BlockDisplay
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
                val e = EnergyRing(commandJuho).also { it.renderer.load(commandJuho.location) }
                executeOnItem(Material.GLASS){
                    if(e.energy<8) e.energy++
                }
                executeOnItem(Material.STONE){
                    if(e.energy>0) e.energy--
                }

                executeOnItem(Material.BREEZE_ROD){
                    testModel = testModel ?: TestModel().renderer.load(commandJuho.location)
                    if(commandJuho.isSneaking){
                        testModel!!.rotate(Quaternionf().set(AxisAngle4f((Math.PI/3f).toFloat(),1f,0f,0f)))
                    }else testModel!!.applyGlobalRotation(Quaternionf().set(AxisAngle4f((Math.PI/3f).toFloat(),1f,0f,0f)))
                }
                executeOnItem(Material.BLAZE_ROD){
                    testModel = testModel ?: TestModel().renderer.load(commandJuho.location)
                    if(commandJuho.isSneaking){
                        testModel!!.rotate(Quaternionf().set(AxisAngle4f((Math.PI/3f).toFloat(),0f,1f,0f)))
                    }else testModel!!.applyGlobalRotation(Quaternionf().set(AxisAngle4f((Math.PI/3f).toFloat(),0f,1f,0f)))
                }
                executeOnItem(Material.STICK){
                    testModel = testModel ?: TestModel().renderer.load(commandJuho.location)
                    if(commandJuho.isSneaking){
                        testModel!!.rotate(Quaternionf().set(AxisAngle4f((Math.PI/3f).toFloat(),0f,0f,1f)))
                    }else testModel!!.applyGlobalRotation(Quaternionf().set(AxisAngle4f((Math.PI/3f).toFloat(),0f,0f,1f)))
                }
                executeOnItem(Material.END_CRYSTAL){
                    testModel = testModel ?: TestModel().renderer.load(commandJuho.location)
                    if(commandJuho.isSneaking){
                        AdvancedBlockDisplay.getBy(testModel!!.displays[0]).debug()
                    }else testModel!!.applyGlobalRotation(Quaternionf())
                }
                commandJuho.sendMessage("initalizing")
            }
        }
        return true
    }
}


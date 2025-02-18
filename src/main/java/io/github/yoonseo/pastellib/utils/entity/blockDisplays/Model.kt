package io.github.yoonseo.pastellib.utils.entity.blockDisplays
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.github.yoonseo.pastellib.utils.tasks.toTicks
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.*
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.joml.*
import kotlin.time.Duration


interface ModelPart{
    fun rotate(quaternionf: Quaternionf)
    fun teleport(location: Location)
}
interface ModelModule{
    fun onAttach(model: Model)
    fun onDetach(model: Model)
}


class Model(val mainDisplay: BlockDisplay) {
    val isDead : Boolean
        get() = mainDisplay.isDead || mainDisplay.passengers.any { isDead }
    val parts : List<ModelPart>
    val modules = ArrayList<ModelModule>()

    fun attachModule(module : ModelModule){
        modules.add(module)
        module.onAttach(this)
    }
    fun detachModule(model: Model,module: ModelModule) : Boolean{
        module.onDetach(this)
        return modules.remove(module)
    }

    fun remove(){
        for (passenger in mainDisplay.passengers) {
            passenger.remove()
        }
        mainDisplay.remove()
    }

    fun teleport(location: Location){
        mainDisplay.teleport(location, TeleportFlag.EntityState.RETAIN_PASSENGERS)
        mainDisplay.passengers.forEach {
            it.teleport(location,TeleportFlag.EntityState.RETAIN_VEHICLE)
        }
    }

    fun validate() : Boolean {
        return mainDisplay.passengers.all { it is Display || it is ModelPart }
    }
}

class ValidationModule(val checkInterval : Int) : ModelModule {
    lateinit var task : Promise
    override fun onAttach(model: Model) {
        task = syncRepeating(interval = checkInterval.toLong()) {
            if(model.validate() || model.isDead) model.remove()
        }
    }

    override fun onDetach(model: Model) {
        task.cancel()
    }
}


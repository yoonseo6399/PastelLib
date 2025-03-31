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


abstract class ModelPart{
    var localTranslation : Vector3f = Vector3f()
    fun rotate(quaternionf: Quaternionf){

    }
    fun teleport(location: Location){
        //location.clone().add(localTranslation)
    }
}
interface ModelModule<T : Display>{
    fun onAttach(model: Model<T>)
    fun onDetach(model: Model<T>)
}
interface SizeModule <T : Display> : ModelModule<T>{
    var baseSize : MutableMap<T,Vector3f>
    fun size(x : Float, y : Float, z : Float)
    fun size(size : Vector3f){
        size(size.x, size.y, size.z)
    }
    fun size(size : Float) = size(size,size,size)

    fun proportionalSize(x : Float, y : Float, z : Float)
    fun proportionalSize(size : Vector3f){
        proportionalSize(size.x, size.y, size.z)
    }
    fun proportionalSize(size : Float) = proportionalSize(size,size,size)
}
typealias DisplayModel = Model<BlockDisplay>

class Model<T : Display>(val mainDisplay: BlockDisplay, val displayData: List<DisplayData>) {
    val isDead : Boolean
        get() = mainDisplay.isDead || mainDisplay.passengers.any { isDead }
    //val parts : List<ModelPart>
    val modules = mutableSetOf<ModelModule<T>>()
    val displays : MutableList<T>
        get() {
            require(validate()) { "Invalid Model structure" }
            return mainDisplay.passengers.mapNotNull { it as? T }.toMutableList()
        }
    fun attachModule(module : ModelModule<T>){
        modules.add(module)
        module.onAttach(this)
    }
    fun detachModule(module: ModelModule<T>) : Boolean{
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

class ValidationModule(val checkInterval : Int) : ModelModule<Display> {
    lateinit var task : Promise
    override fun onAttach(model: Model<Display>) {
        task = syncRepeating(interval = checkInterval.toLong()) {
            if(model.validate() || model.isDead) model.remove()
        }
    }

    override fun onDetach(model: Model<Display>) {
        task.cancel()
    }
}


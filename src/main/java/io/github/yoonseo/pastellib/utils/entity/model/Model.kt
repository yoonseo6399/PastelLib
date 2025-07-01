package io.github.yoonseo.pastellib.utils.entity.model
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.*
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.joml.*


abstract class ModelPart{
    var localTranslation : Vector3f = Vector3f()
    fun rotate(quaternionf: Quaternionf){

    }
    fun teleport(location: Location){
        //location.clone().add(localTranslation)
    }
}
typealias DisplayModel = Model<BlockDisplay>


class TestModel : Model<BlockDisplay>("test"){
    override val renderer : ModelRenderer<BlockDisplay> = modelRenderer(this){

    }
}


class DefaultModel<T : Display>(name : String) : Model<T>(name){
    override val renderer = ModelRenderer(this)
}




abstract class Model<T : Display>(val name: String){

    abstract val renderer : ModelRenderer<T>
    lateinit var mainDisplay: BlockDisplay
    lateinit var displayData: List<DisplayData>
    val location : Location
        get() = mainDisplay.location

    val isDead : Boolean
        get() = mainDisplay.isDead || mainDisplay.passengers.any { it.isDead }
    //val parts : List<ModelPart>
    val modules = mutableSetOf<ModelModule<T>>()
    val displays : MutableList<T>
        get() {
            require(validate()) { "Invalid Model structure" }
            @Suppress("UNCHECKED_CAST")
            return mainDisplay.passengers as MutableList<T>
        }

    fun attachModule(module : ModelModule<T>){
        modules.add(module)
        module.onAttach(this)
    }
    fun detachModule(module: ModelModule<T>) : Boolean{
        module.onDetach(this)
        return modules.remove(module)
    }

    open fun remove(){
        modules.forEach { it.onDetach(this) }
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
    fun rotate(quaternionf: Quaternionf){
        AdvancedBlockDisplay.getBy(mainDisplay).rotate(quaternionf) // text Display 지원 안함 TODO
        mainDisplay.passengers.forEach { AdvancedBlockDisplay.getBy(it as BlockDisplay).rotate(quaternionf) }
    }
    fun applyGlobalRotation(quaternionf: Quaternionf){
        AdvancedBlockDisplay.getBy(mainDisplay).globalRotation(quaternionf)
        displays.forEach {
            AdvancedBlockDisplay.getBy(it as BlockDisplay).globalRotation(quaternionf)
        }
    }
    fun interpolation(){
        displays.forEach {
            it.interpolationDuration = 1
            it.interpolationDelay = -1
        }
    }


    fun validate() : Boolean {
        return mainDisplay.passengers.all { it is Display || it is ModelPart }
    }
}

class ValidationModule(val checkInterval : Int) : ModelModule<Display>() {
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


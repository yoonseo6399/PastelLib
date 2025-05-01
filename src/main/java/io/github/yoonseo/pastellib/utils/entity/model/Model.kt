package io.github.yoonseo.pastellib.utils.entity.model
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import io.github.yoonseo.pastellib.utils.forceDamage
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.*
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.joml.*
import java.lang.Math.random
import java.util.function.Predicate


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
interface SizeModule <T : Display> : ModelModule<T> {
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

abstract class TickingModelModule<T : Display> : ModelModule<T> {
    lateinit var promise : Promise
    var model : Model<T>? = null
    override fun onAttach(model: Model<T>) {
        require(this.model == null) { "Model already attached" }
        this.model = model
        promise = syncRepeating { tickingTask() }
    }
    override fun onDetach(model: Model<T>) {
        this.model = null
        promise.cancel()
    }
    abstract fun tickingTask()
}
class SelfPropellingModule<T : Display>(val velocity : Double,var maxDistance : Double = -1.0) : TickingModelModule<T>() {
    var distance = 0.0
    override fun tickingTask() {
        val model = this.model ?: return
        if(distance >= maxDistance && maxDistance != -1.0) model.remove()
        else {
            model.teleport(model.mainDisplay.let { it.location.add(it.location.direction.multiply(velocity)) } )
            distance += velocity
        }

    }
}
class SimpleCollusionModule<T : Display> : TickingModelModule<T>(){
    override fun tickingTask() {
        val model = this.model ?: return
        val block = model.mainDisplay.location.block
        if(block.isCollidable) model.remove()
    }
}
/**
 * @param targetAmount if set to 0 -> return all targets
 *
 * */
class SimpleDamageModule<T : Display>(val damageAmount : Double,val targetAmount : Int = 0,val predicate : Predicate<LivingEntity>) : TickingModelModule<T>(){
    override fun tickingTask() {
        val model = this.model ?: return
        val detected = model.location.world.getNearbyEntities(model.location,1.0,1.0,1.0).mapNotNull { it as? LivingEntity }.filter { predicate.test(it) }
        if(targetAmount == 0) detected.forEach { it.forceDamage(damageAmount) }
        else detected.firstOrNull()?.forceDamage(damageAmount)
    }
}
class SingleDamageModule<T : Display>(
    val damageAmount : Double,
    val casuingEntity: Entity,
    val damageType: DamageType,
    val noDamageTick : Boolean = false,
    val predicate : Predicate<LivingEntity>
) : TickingModelModule<T>(){
    val damaged = mutableSetOf<LivingEntity>()
    override fun tickingTask() {
        val model = this.model ?: return // 함수 인자로 받을까...?
        val detected = model.location.world.getNearbyEntities(model.location,1.0,1.0,1.0).mapNotNull { it as? LivingEntity }.filter { !damaged.contains(it) && predicate.test(it) }
        detected.forEach {
            if(noDamageTick) it.noDamageTicks = 0
            it.damage(damageAmount,DamageSource.builder(damageType).withDirectEntity(it).withCausingEntity(casuingEntity).build())
            damaged.add(it)
        }
    }
}












class TestModel : Model<BlockDisplay>("test"){
    override val renderer : ModelRenderer<BlockDisplay> = modelRenderer(this){

    }
}
class SwordDemon(val owner : LivingEntity) : Model<BlockDisplay>("boss1-L"){

    override val renderer: ModelRenderer<BlockDisplay> = modelRenderer(this){
        teleport(owner.eyeLocation)
        attachModule(SelfPropellingModule(2.0,100.0))
        applyGlobalRotation(Quaternionf().fromAxisAngleDeg(Vector3f(0f,0f,1f), (random()*360).toFloat()))
        attachModule(SimpleCollusionModule())
        attachModule(SingleDamageModule(10.0,owner, DamageType.LIGHTNING_BOLT,true) { it != owner})
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


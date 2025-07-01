package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.utils.entity.blockDisplays.getRotation3x3
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.toMatrix4f
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.later
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.github.yoonseo.pastellib.utils.tasks.toTicks
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.time.Duration

abstract class ModelModule<T : Display>{
    var model : Model<T>? = null
    open fun onAttach(model: Model<T>) {
        require(this.model == null) { "Model already attached" }
        this.model = model
    }
    open fun onDetach(model: Model<T>) {
        this.model = null
    }
}

open class SizeModule<T : Display> : ModelModule<T>() {

    fun multiplyGlobally(x : Double,y : Double,z : Double){
        multiplyGlobally(Vector3f(x.toFloat(),y.toFloat(),z.toFloat()))
    }
    fun multiplyGlobally(vec : Vector3f){
        require(model != null) { "Module Is Not Attached" }
        for (display in model!!.displays) {
            val t = display.transformation

            val originalTranslation = Vector3f(t.translation)

            // 회전을 제거해서 "월드 기준 위치"로 환산
            val rotation = Quaternionf()
            t.toMatrix4f().getRotation3x3().getNormalizedRotation(rotation)
            val invRot = Quaternionf(rotation).invert()
            val worldPos = Vector3f(originalTranslation).rotate(invRot)

            // 원점 기준으로 고정 좌표계 스케일링
            worldPos.mul(vec)

            // 다시 회전을 적용해서 로컬 기준 translation으로 환산
            val newTranslation = worldPos.rotate(rotation)

            // 기존 회전과 스케일 유지
            val newTransform = Transformation(newTranslation, t.leftRotation, t.scale.mul(vec), t.rightRotation)

            display.transformation = newTransform
        }
    }
}
open class AnimationModule<T : Display> : ModelModule<T>(),Cloneable {
    private val animationQueue : MutableList<Pair<Duration,Model<T>.() -> Unit>> = mutableListOf()
    private var untilEndRepeating : (Model<T>.() -> Unit)? = null
    private var untilPromise : Promise? = null
    private var delayPromise : Promise? = null
    fun animate(index : Int = 0){
        require(model != null)
        if(untilEndRepeating != null) {
            untilPromise = syncRepeating { model?.let { untilEndRepeating!!.invoke(it) } }
        }
        //if(animationQueue.size == 0) return //TODO repeating Until End 의 뜻과 맞지 않음, 이러면 무한 반복이 됌
        val ani = animationQueue[index]
        ani.second(model!!)

        if(animationQueue.size-1 > index){
            delayPromise = later(ani.first.toTicks()) {animate(index+1) }
        }else later(ani.first.toTicks()) { if(untilPromise?.isCanceled == false) untilPromise?.cancel() }
    }

    //-1 은 에니메이션의 끝
    fun configureAnimation(animationBlock: AnimationModule<T>.() -> Unit){
        animationBlock()
    }
    fun then(animationPeriod: Duration = Duration.ZERO, animation: Model<T>.() -> Unit){
        animationQueue.add(animationPeriod to animation)
    }
    fun repeatUntilEnds(animation: Model<T>.() -> Unit) {
        untilEndRepeating = animation
    }

    override fun onDetach(model: Model<T>) {
        super.onDetach(model)
        untilPromise?.cancel()
        delayPromise?.cancel()
    }
}

abstract class TickingModelModule<T : Display> : ModelModule<T>() {
    lateinit var promise : Promise
    override fun onAttach(model: Model<T>) {
        super.onAttach(model)
        promise = syncRepeating { tickingTask() }
    }
    override fun onDetach(model: Model<T>) {
        super.onDetach(model)
        promise.cancel()
    }
    abstract fun tickingTask()
}

class SelfPropellingModule<T : Display>(val velocity : Double, var maxDistance : Double = -1.0) : TickingModelModule<T>() {
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
class SimpleDamageModule<T : Display>(
    val targetAmount : Int = 1,
    val damageAmount : Double,
    val casuingEntity: Entity,
    val damageType: DamageType,
    val noDamageTick : Boolean = false,
    val predicate : Predicate<LivingEntity>
) : TickingModelModule<T>(){
    override fun tickingTask() {
        val model = this.model ?: return
        val detected = model.location.world.getNearbyEntities(model.location,1.0,1.0,1.0).mapNotNull { it as? LivingEntity }.filter { predicate.test(it) }
        detected.take(targetAmount).forEach { it.damage(damageAmount,casuingEntity,damageType,noDamageTick) }
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
            it.damage(damageAmount,casuingEntity,damageType,noDamageTick)
            damaged.add(it)
        }
    }
}
class TaskModule<T : Display> private constructor(val period : Int = 0,var promise: Promise? = null,val repeatingBlock : Consumer<Model<T>>?) : ModelModule<T>(){
    constructor(promise: Promise) : this(0,promise,null)
    constructor(period: Int,block: Consumer<Model<T>>) : this(period,null,block)
    private var _period = 0


    override fun onAttach(model: Model<T>) {
        super.onAttach(model)
        //다른방법으로 task 등록 허용
        if(repeatingBlock != null) promise = syncRepeating {
            if(period == 0 || _period == period){
                repeatingBlock.accept(model)
                _period = 0
            }else _period ++
        }
    }

    override fun onDetach(model: Model<T>) {
        promise?.cancel()
        super.onDetach(model)
    }
}



fun LivingEntity.damage(damageAmount : Double, casuingEntity: Entity, damageType: DamageType, noDamageTick : Boolean = false){
    if(noDamageTick) noDamageTicks = 0
    damage(damageAmount, DamageSource.builder(damageType).withDirectEntity(this).withCausingEntity(casuingEntity).build())
}
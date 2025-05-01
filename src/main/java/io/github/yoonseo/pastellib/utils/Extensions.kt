package io.github.yoonseo.pastellib.utils

import com.google.common.base.Predicate
import io.github.yoonseo.pastellib.mainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed class Union<T1,T2>{
    data class Left<T1>(val value: T1) : Union<T1, Nothing>()
    data class Right<T2>(val value: T2) : Union<Nothing, T2>()

    fun then(block: (T1) -> Unit,block2: (T2) -> Unit): Unit = when (this) {
        is Left -> block(value)
        is Right -> block2(value)
    }
    fun whenLeft(block: (T1) -> Unit) {
        if(this is Left) block(value)
    }
    fun whenRight(block: (T2) -> Unit) {
        if(this is Right) block(value)
    }
}
sealed class ListOrSingle<T> {
    data class List<T>(val values: List<T>) : ListOrSingle<T>()
    data class Single<T>(val value: T) : ListOrSingle<T>()
}

suspend fun <T> runInMainThread(
    block: suspend CoroutineScope.() -> T
): T = withContext(mainThread,block)
val Int.ticks : Duration
    get() = (50*this).milliseconds
class WhenBlock<T : Comparable<T>>{
    val branches = hashMapOf<ClosedRange<T>,Runnable>()
    var lastSuccess = false
    val default : Runnable? = null
    fun execute(that : T){
        for(branch in branches){
            if(branch.key.contains(that)){
                branch.value.run()
                lastSuccess = true
            }
        }
        if (!lastSuccess) default?.run()
    }
    fun inRange(range : ClosedRange<T>,runnable: Runnable){
        branches[range] = runnable
    }
    fun default(runnable: Runnable){
        runnable.run()
    }
}
fun <T : Comparable<T>> whenR(that : T, whenBlock : WhenBlock<T>.() -> Unit){
    val block = WhenBlock<T>()
    block.whenBlock()
    block.execute(that)
}


infix fun Location.lookVector(location : Location) = location.toVector().subtract(this.toVector()).normalize()

fun Inventory.amountOf(predicate: Predicate<ItemStack>): Int =
    contents.foldRight(0) { item, count -> if(item != null && predicate.apply(item)) count + item.amount else count }
fun Inventory.removeItem(material : Material?, amount: Int,predicate: Predicate<ItemStack> = Predicate { true }): Boolean {
    var remaining = amount
    for ((i,e) in contents.asList().withIndex()) {
        if(e == null || !predicate.apply(e)) continue
        if(material != null && e.type != material) continue

        if(e.amount <= remaining) {
            remaining - e.amount
            clear(i)
        }else {
            e.amount -= remaining
            remaining = 0
            setItem(i,e)
        }
        if (remaining == 0) break
    }
    return remaining == 0
}

fun <T> List<T>.copyToArrayList() : ArrayList<T> = ArrayList(this)
inline fun <reified T : Any> String.isAssignable(): Boolean {
    return isAssignable(T::class)
}
fun <T : Any> String.isAssignable(clz : KClass<T>): Boolean {
    return when(clz) {
        Int::class -> this.toIntOrNull()!= null
        Long::class -> this.toLongOrNull()!= null
        Double::class -> this.toDoubleOrNull()!= null
        String::class -> true
        Boolean::class -> this.toBooleanStrictOrNull()!=null
        else -> false
    }
}
@Suppress("UNCHECKED_CAST")
fun <T : Any> String.toType(clz : KClass<T>): Any? {
    return when(clz) {
        Int::class -> this.toIntOrNull()
        Long::class -> this.toLongOrNull()
        Double::class -> this.toDoubleOrNull()
        String::class -> this
        Boolean::class -> this.toBooleanStrictOrNull()
        else -> null
    } as? T?
}

fun LivingEntity.forceDamage(damage: Double, damageSource: DamageSource = DamageSource.builder(DamageType.GENERIC).build(), causeEvent : Boolean = false) {
    if(health - damage <= 0) health = 0.0
    else health -= damage
    Bukkit.getPluginManager().callEvent(EntityDamageEvent(this,EntityDamageEvent.DamageCause.ENTITY_ATTACK,damageSource,damage))
    //playEffect(EntityEffect.HURT)
    hurtSound?.let { location.world.playSound(Sound.sound(it,Sound.Source.PLAYER,1f,1f)) }
}
fun Transformation.cloneSetTranslation(vector: Vector3f): Transformation = Transformation(vector,leftRotation,scale,rightRotation)
fun Transformation.cloneSetLeftRotation(quaternionf: Quaternionf): Transformation = Transformation(translation,quaternionf,scale,rightRotation)
fun Transformation.cloneSetRightRotation(quaternionf: Quaternionf): Transformation = Transformation(translation,leftRotation,scale,quaternionf)
fun Transformation.cloneSetScale(vector: Vector3f): Transformation = Transformation(translation,leftRotation,vector,rightRotation)

fun Transformation.mapScale(block : (Vector3f) -> Vector3f): Transformation = Transformation(translation, leftRotation, block(scale), rightRotation)
fun Transformation.mapTranslation(block : (Vector3f) -> Vector3f): Transformation = Transformation(block(translation), leftRotation, scale, rightRotation)
fun Transformation.mapLeftRotation(block : (Quaternionf) -> Quaternionf): Transformation = Transformation(translation, block(leftRotation), scale, rightRotation)
fun Transformation.mapRightRotation(block : (Quaternionf) -> Quaternionf): Transformation = Transformation(translation, leftRotation, scale, block(rightRotation))


fun <T : Collection<*>> T.takeIsNotEmpty() : T? = if(isEmpty()) null else this
package io.github.yoonseo.pastellib.utils

import com.google.common.base.Predicate
import net.kyori.adventure.sound.Sound
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.reflect.KClass

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

fun LivingEntity.forceDamage(damage: Double) {
    if(health - damage <= 0) health = 0.0
    else health -= damage
    playEffect(EntityEffect.HURT)
    hurtSound?.key?.let { location.world.playSound(Sound.sound(it,Sound.Source.PLAYER,1f,1f)) }
}
fun Transformation.cloneSetTranslation(vector: Vector3f): Transformation = Transformation(vector,leftRotation,scale,rightRotation)
fun Transformation.cloneSetLeftRotation(quaternionf: Quaternionf): Transformation = Transformation(translation,quaternionf,scale,rightRotation)
fun Transformation.cloneSetRightRotation(quaternionf: Quaternionf): Transformation = Transformation(translation,leftRotation,scale,quaternionf)
fun Transformation.cloneSetScale(vector: Vector3f): Transformation = Transformation(translation,leftRotation,vector,rightRotation)


fun <T : Collection<*>> T.takeIsNotEmpty() : T? = if(isEmpty()) null else this
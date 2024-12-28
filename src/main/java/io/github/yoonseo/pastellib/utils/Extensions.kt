package io.github.yoonseo.pastellib.utils

import com.google.common.base.Predicate
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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

fun Inventory.amountOf(material: org.bukkit.Material): Int =
    contents.foldRight(0) { item, count -> if(item?.type == material) count + item.amount else count }
fun Inventory.removeItem(material: org.bukkit.Material, amount: Int,predicate: Predicate<ItemStack> = Predicate { true }): Boolean {
    var remaining = amount
    for ((i,e) in contents.asList().withIndex()) {
        if(e?.type != material || !predicate.apply(e)) continue
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
fun <T : Any> String.toType(clz : KClass<T>): T? {
    return when(clz) {
        Int::class -> this.toIntOrNull()
        Long::class -> this.toLongOrNull()
        Double::class -> this.toDoubleOrNull()
        String::class -> this
        Boolean::class -> this.toBooleanStrictOrNull()
        else -> null
    } as? T
}
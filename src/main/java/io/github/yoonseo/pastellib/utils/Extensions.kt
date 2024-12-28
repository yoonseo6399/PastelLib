package io.github.yoonseo.pastellib.utils

import com.google.common.base.Predicate
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

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
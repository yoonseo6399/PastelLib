package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.PastelLib
import kotlinx.serialization.descriptors.PrimitiveKind
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.javaType

//getValue(Gun, KMutableProperty1<*, *>)
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> persistentDataTypeOf(): PersistentDataType<*,T> {
    return persistentDataTypeOf(T::class)
}
@Suppress("UNCHECKED_CAST")

fun <T : Any> persistentDataTypeOf(type : KClass<T>): PersistentDataType<*,T> {
    return when (type) {
        Int::class -> PersistentDataType.INTEGER
        Long::class -> PersistentDataType.LONG
        Double::class -> PersistentDataType.DOUBLE
        String::class -> PersistentDataType.STRING
        Boolean::class -> PersistentDataType.BOOLEAN
        else -> throw IllegalArgumentException("Unsupported type ${type.simpleName}")
    } as PersistentDataType<*, T>
}
annotation class UseKey(val key: String)
class ItemDataContainer(val item : ItemStack) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
        val type = persistentDataTypeOf<T>()
        val key = if(property.hasAnnotation<UseKey>()) NamespacedKey(PastelLib.instance, property.findAnnotation<UseKey>()!!.key) else NamespacedKey(PastelLib.instance, property.name)
        return item.itemMeta.persistentDataContainer.get(key,type) ?: throw IllegalArgumentException("Cannot find data named ${property.name}")
    }
    inline operator fun <reified T : Any> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = if(property.hasAnnotation<UseKey>()) NamespacedKey(PastelLib.instance, property.findAnnotation<UseKey>()!!.key) else NamespacedKey(PastelLib.instance, property.name)
        var meta = item.itemMeta
        meta.persistentDataContainer.set(key, persistentDataTypeOf<T>(), value)
        item.itemMeta = meta
    }

    inline fun <reified T : Any> addValue(key: String, value: T) {
        var meta = item.itemMeta
        meta.persistentDataContainer.set(NamespacedKey(PastelLib.instance, key),persistentDataTypeOf<T>(), value)
        item.itemMeta = meta
    }
    inline operator fun <reified T : Any> get(key: String,type : KClass<T>) : T{
        val type = persistentDataTypeOf<T>()

        val key = NamespacedKey(PastelLib.instance, key)
        return item.itemMeta.persistentDataContainer.get(key,type) ?: throw IllegalArgumentException("Cannot find data : $key")
    }
    inline fun <reified T : Any> getOrNull(key: String,type : KClass<T>) : T?{
        val type = persistentDataTypeOf<T>()
        val key = NamespacedKey(PastelLib.instance, key)
        return item.itemMeta.persistentDataContainer.get(key,type)
    }
    fun containsKey(key : String) : Boolean = item.itemMeta.persistentDataContainer.keys.contains(NamespacedKey(PastelLib.instance, key))

}

fun namespacedKey(key: String) = NamespacedKey(PastelLib.instance, key)
val ItemStack.dataContainer : ItemDataContainer
    get() {
    if(itemMeta == null) throw IllegalStateException("No itemMeta found in getting dataContainer")
    return ItemDataContainer(this)
}


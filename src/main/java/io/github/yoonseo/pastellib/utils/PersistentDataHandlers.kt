package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.PastelLib
import kotlinx.serialization.descriptors.PrimitiveKind
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.javaType

//getValue(Gun, KMutableProperty1<*, *>)
@Suppress("UNCHECKED_CAST")
inline fun <reified T> persistentDataTypeOf(): PersistentDataType<*,T> {
    return when (T::class) {
        Int::class -> PersistentDataType.INTEGER
        Long::class -> PersistentDataType.LONG
        Double::class -> PersistentDataType.DOUBLE
        String::class -> PersistentDataType.STRING
        Boolean::class -> PersistentDataType.BOOLEAN
        else -> throw IllegalArgumentException("Unsupported type ${T::class.simpleName}")
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
    }
}
class OptionalDataContainerExtension(val item : ItemStack ) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T? {
        val type = persistentDataTypeOf<T>()

        val key = if(property.hasAnnotation<UseKey>()) NamespacedKey(PastelLib.instance, property.findAnnotation<UseKey>()!!.key) else NamespacedKey(PastelLib.instance, property.name)
        return item.itemMeta.persistentDataContainer.get(key,type)
    }

    inline operator fun <reified T : Any> setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val key = if(property.hasAnnotation<UseKey>()) NamespacedKey(PastelLib.instance, property.findAnnotation<UseKey>()!!.key) else NamespacedKey(PastelLib.instance, property.name)
        var meta = item.itemMeta
        if(value != null) meta.persistentDataContainer.set(key, persistentDataTypeOf<T>(), value) else meta.persistentDataContainer.remove(key)
        item.itemMeta = meta
    }
}

fun ItemStack.dataContainer() = ItemDataContainer(this)
fun ItemStack.optionalDataContainer() = OptionalDataContainerExtension(this)


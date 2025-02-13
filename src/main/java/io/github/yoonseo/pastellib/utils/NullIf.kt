package io.github.yoonseo.pastellib.utils

import io.github.yoonseo.pastellib.utils.blockDisplays.Laser
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NullIf<V>(val predicate : (V?) -> Boolean?) : ReadWriteProperty<Any, V?> {
    var value : V? = null
    override fun getValue(thisRef: Any, property: KProperty<*>): V? {
        if(predicate(value) == true) value = null
        return null
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V?) {
        this.value = value
    }
}

fun <V> nullIf(predicate: (Laser?) -> Boolean?) = NullIf(predicate)
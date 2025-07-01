package io.github.yoonseo.pastellib.utils.skill

import com.google.common.util.concurrent.AtomicDouble
import io.github.yoonseo.pastellib.utils.runInMainThread

class EnergyPool(initial : Double,val onChange : (Double, Double) -> Unit = {_,_ -> }) {
    private val atomic = AtomicDouble(initial)

    var value: Double
        get() = atomic.get()
        set(v) {
            onChange(atomic.get(),v)
            atomic.set(v)
        }
    fun compareAndSet(oldValue : Double,newValue : Double) : Boolean {
        if(atomic.compareAndSet(oldValue,newValue)) {
            onChange(oldValue,newValue)
            return true
        }else return false
    }
}
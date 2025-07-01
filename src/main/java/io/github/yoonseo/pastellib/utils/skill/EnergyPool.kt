package io.github.yoonseo.pastellib.utils.skill

import com.google.common.util.concurrent.AtomicDouble

class EnergyPool(initial : Double) {
    private val atomic = AtomicDouble(initial)

    var value: Double
        get() = atomic.get()
        set(v) = atomic.set(v)
}
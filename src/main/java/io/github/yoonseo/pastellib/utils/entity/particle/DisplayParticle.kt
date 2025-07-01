package io.github.yoonseo.pastellib.utils.entity.particle

import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.util.Vector
import java.lang.Math.random

abstract class DisplayParticle<T : Display> : Cloneable {
    companion object {
        val particles = ArrayList<DisplayParticle<*>>()
    }
    init {
        particles.add(this)
    }
    open lateinit var display : T
    lateinit var task : Promise
    var time : Int = 0
    var velocity : Vector = Vector(0.0, 0.0, 0.0)
    open val expiresAt : Int = -1
    open fun spawn(location: Location) {
        task = syncRepeating {
            display.teleport(display.location.add(velocity))
            tickedBehavior()
            time ++
            if(expiresAt != -1 && time >= expiresAt) remove()
        }
    }
    open fun tickedBehavior(){

    }
    open fun remove() {
        task.cancel()
        display.remove()
    }
    public abstract override fun clone() : DisplayParticle<*>
}
fun randomNegativedInclude() : Double = if((0..1).random() == 0) random() else -random()
fun randomVector(): Vector {
    val theta = Math.random() * 2 * Math.PI
    val phi = Math.acos(2 * Math.random() - 1)
    val x = Math.sin(phi) * Math.cos(theta)
    val y = Math.sin(phi) * Math.sin(theta)
    val z = Math.cos(phi)
    return Vector(x, y, z)
}
//class BlockParticle : DisplayParticle() {}


fun Location.showParticle(displayParticle: DisplayParticle<*>, count: Int = 1) {
    repeat(count) {
        displayParticle.clone().spawn(this)
    }
}
package io.github.yoonseo.pastellib.utils.selectors

import io.github.yoonseo.pastellib.utils.lookVector
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.reflect.full.isSubclassOf


class Ray(
    val loc1 : Location,
    val loc2 : Location,
    var checkingInterval : Double = 0.2
) : Iterable<Location>{

    fun distance() = loc1.distance(loc2)
    val direction : Vector = loc1 lookVector loc2


    override fun iterator(): Iterator<Location> {
        return LineIterator(this, checkingInterval)
    }
    inline fun <reified T : Any> select(block : List<T>.(Location) -> Boolean){
        TODO()
        if(T::class.isSubclassOf(Entity::class)){

        }else if(T::class.isSubclassOf(Block::class)){

        }else throw IllegalArgumentException("${T::class.simpleName} is not a subclass of Entity or Block")
    }
}
class LineIterator(private val ray : Ray, private val interval : Double) : Iterator<Location> {
    private val current : Location = ray.loc1.clone()
    private var distance = ray.loc1.clone().distance(ray.loc2)
    private val dir = ray.direction.clone().multiply(interval)
    override fun hasNext(): Boolean {
        return ray.loc1.distance(current) < distance
    }

    override fun next(): Location {
        current.add(dir)
        return current
    }
}
infix fun Location.rayTo(location: Location) = Ray(this,location)

interface RayTraceable {

}


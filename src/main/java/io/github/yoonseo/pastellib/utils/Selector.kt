package io.github.yoonseo.pastellib.utils

import com.google.common.base.Predicate
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

object Selector {
    /**
     * Searches for a block of a specified type along a specified direction from a starting point.
     *
     * @param start The starting location for the search.
     * @param direction The direction vector in which to search.
     * @param interval The distance between each check point along the search path.
     * @param range The maximum distance from the start point to search.
     * @param predicate A predicate to filter the blocks.
     * @return The first block of type [R] that matches the predicate within the specified range, or null if no such block is found.
     */
    inline fun <reified R : Block> block(start: Location, direction : Vector, interval : Double, range : Double, predicate: Predicate<R>) : R?{
        var current = start.clone()
        val intervalVector = direction.clone().normalize().multiply(interval)
        while (current.distance(start) <= range) {
            val block = current.block
            if (block.type != Material.AIR && block is R &&predicate.apply(block)) {
                return block
            }
            current = current.add(intervalVector)
        }
        return null
    }


    /**
     * Searches for entities along a specified direction from a starting point.
     *
     * @param E The type of entity to search for.
     * @param start The starting location for the search.
     * @param direction The direction vector in which to search.
     * @param interval The distance between each check point along the search path.
     * @param range The maximum distance from the start point to search.
     * @param predicate A predicate to filter the entities.
     * @return A list of entities that match the predicate, or null if no entities are found.
     */
    fun <E : Entity> entities(start: Location,
                              direction : Vector,
                              interval : Double = 0.2,
                              range : Double,
                              predicate: Predicate<E> = Predicate { true }) : List<E>?{

        var current = start.clone()
        val intervalVector = direction.clone().normalize().multiply(interval)
        while (current.distance(start) <= range) {
            debug { current.showParticle(Particle.END_ROD) }
            @Suppress("UNCHECKED_CAST")
            val entities = current.world.getNearbyEntities(BoundingBox(current.x,current.y,current.z,current.x,current.y,current.z)).mapNotNull { it as? E }.filter { predicate.apply(it) }
            if(entities.isNotEmpty()) return entities
            current = current.add(intervalVector)
        }
        return null
    }
    /**
     * Searches for single entity along a specified direction from a starting point.
     *
     * @param E The type of entity to search for.
     * @param start The starting location for the search.
     * @param direction The direction vector in which to search.
     * @param interval The distance between each check point along the search path.
     * @param range The maximum distance from the start point to search.
     * @param predicate A predicate to filter the entities.
     * @return An entity that match the predicate, or null if no entities are found.
     */
    fun <E : Entity> entity(start: Location, direction : Vector, interval : Double, range : Double, predicate: Predicate<E> = Predicate { true }) : E? =
        entities(start, direction, interval, range, predicate)?.firstOrNull()
}

abstract class TickedSelector<R>(
    val start: Location,
    val direction : Vector,
    val interval : Double = 0.2,
    val range : Double,
    val predicate: Predicate<R> = Predicate { true }
){
    var current : Location = start.clone()
    val intervalVector = direction.clone().normalize().multiply(interval)

    internal abstract fun step() : R?
    fun isInRange() : Boolean = current.distance(start) <= range
}

class TickedEntitySelector<R : Entity>(
    start: Location,
    direction : Vector,
    interval : Double = 0.2,
    range : Double,
    predicate: Predicate<R> = Predicate { true }
) : TickedSelector<R>(start, direction,interval,range,predicate) {
    override fun step(): R? {

    }
}
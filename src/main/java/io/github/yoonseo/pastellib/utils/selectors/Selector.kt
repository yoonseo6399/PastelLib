package io.github.yoonseo.pastellib.utils.selectors

import com.google.common.base.Predicate
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.showParticle
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
        return TickedBlockSelector(start, direction, interval, range, predicate).tickAll()?.first()
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
                              predicate: Predicate<E> = Predicate { true }
    ) : List<E>?{
        return TickedEntitySelector(start,direction,interval,range,predicate).tickAll()
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
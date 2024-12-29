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
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BiPredicate

class TickedEntitySelector<R : Entity>(
    start: Location,
    direction : Vector,
    interval : Double = 0.2,
    range : Double,
    predicate: Predicate<R> = Predicate { true }
) : TickedSelector<R>(start, direction,interval,range,predicate) {
    override fun step(): List<R>? {
        debug { current.showParticle(Particle.END_ROD) }
        @Suppress("UNCHECKED_CAST")
        val entities = current.world.getNearbyEntities(
            BoundingBox(
                current.x,
                current.y,
                current.z,
                current.x,
                current.y,
                current.z
            )
        ).mapNotNull { it as? R }.filter { predicate.apply(it) }
        if(entities.isNotEmpty()) return entities
        return null
    }
}

class ComplexTickedSelector<E : Entity,B : Block,R>(
    start: Location,
    direction : Vector,
    interval : Double = 0.2,
    range : Double,
    val determiner : ComplexTickedSelector<E,B,R>.() -> List<R>?
) : TickedSelector<R>(start, direction, interval, range) {
    @Suppress("UNCHECKED_CAST")
    override fun step(): List<R>? {
        return determiner()
    }
    @Suppress("UNCHECKED_CAST")
    fun getEntities() : List<E>? =
        current.world.getNearbyEntities(
            BoundingBox(
                current.x,
                current.y,
                current.z,
                current.x,
                current.y,
                current.z
            )
        ).mapNotNull { it as? E }.takeIf { it.isNotEmpty() }

    @Suppress("UNCHECKED_CAST")
    fun getBlock() : B? = current.block.takeIf { it.type != Material.AIR } as? B
}


class TickedBlockSelector<R : Block>(
    start: Location,
    direction : Vector,
    interval : Double = 0.2,
    range : Double,
    predicate: Predicate<R> = Predicate { true }
) : TickedSelector<R>(start, direction,interval,range,predicate) {
    override fun step(): List<R>? {
        debug { current.showParticle(Particle.END_ROD) }
        val block = current.block
        @Suppress("UNCHECKED_CAST")
        if (block.type != Material.AIR && (block as? R) != null &&predicate.apply(block)) {
            return listOf(block)
        }
        return null
    }
}
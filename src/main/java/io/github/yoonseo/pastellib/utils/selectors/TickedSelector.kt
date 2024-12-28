package io.github.yoonseo.pastellib.utils.selectors

import com.google.common.base.Predicate
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.eventual
import org.bukkit.Location
import org.bukkit.util.Vector

/**
 * An abstract class representing a ticked selector for searching objects of type R.
 * Ticking automatically stops when the [step] returns NonNull.
 *
 * @param R The type of object being selected.
 * @property start The starting location for the search.
 * @property direction The direction vector in which to search.
 * @property interval The distance between each check point along the search path. Default is 0.2.
 * @property range The maximum distance from the start point to search.
 * @property predicate A predicate to filter the objects. Default accepts all objects.
 *
 * whatever a subclass needs to follow under rules
 * should override [step] and [isInRange] methods.
 * The [isFinished] property should be changed to determine when the search is finished.
 * step method should be implemented to perform a single step(length [interval]) in the search process.
 */
abstract class TickedSelector<R>(
    val start: Location,
    val direction : Vector,
    val interval : Double = 0.2,
    val range : Double,
    val predicate: Predicate<R> = Predicate { true }
){
    var current : Location = start.clone()
    val intervalVector = direction.clone().normalize().multiply(interval)
    var isFinished : Boolean = false
    var result : List<R>? = null

    /**
     * Executes all ticks until a result is found or the search is finished.
     *
     * @return The first non-null result from [step], or null if the search finishes without finding a result.
     */
    fun tickAll(): List<R>? {
        while (!isFinished) {
            val result = performTick()
            if(result != null) {
                return result
            }
        }
        return null
    }


    /**
     * Performs a single tick of the search process.
     *
     * This function executes one step of the search, checks if the search is still within range,
     * and updates the finished state accordingly.
     *
     * @throws IllegalStateException if the search is already finished.
     * @return R? The result of the current step, which is an object of type R if found in this step, or null otherwise.
     */
    fun performTick() : List<R>? {
        if(isFinished) throw IllegalStateException("TickedEntitySelector is already finished")
        current = current.add(intervalVector)
        val s = step()
        if(!isInRange() || s != null) isFinished = true
        if(s!= null) result = s
        return s
    }
    /**
     * Performs a single step in the search process.
     *
     * @return An object of type R if found in this step, or null otherwise.
     */
    internal abstract fun step() : List<R>?

    /**
     * Checks if the current location is within the specified range from the start location.
     *
     * @return true if the current location is within range, false otherwise.
     */
    fun isInRange() : Boolean = current.distance(start) <= range
}
/**
 * Runs the TickedSelector by performing a specified number of ticks and returns a Promise.
 *
 * This function executes the TickedSelector for a given number of steps, or until it finishes.
 * It returns a Promise that will resolve with the result of the selector's operation.
 *
 * @param stepSize The number of ticks to perform in each iteration. Default is 1.
 * @param onFinish A callback function that will be called with the final result when the selector finishes.
 * @return A Promise that resolves with the result of the selector's operation.
 */
fun <R> TickedSelector<R>.runByTick(stepSize: Int = 1, onFinish: (List<R>) -> Unit): Promise =
    eventual(onFinish) {
        var a: List<R>? = null
        for (i in 1..stepSize) {
            if (isFinished) cancel()
            a = performTick()
        }
        return@eventual a
    }
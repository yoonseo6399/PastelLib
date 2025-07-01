package io.github.yoonseo.pastellib.utils.tasks

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.log
import io.github.yoonseo.pastellib.utils.ticks
import org.bukkit.Bukkit
import kotlin.time.Duration
import kotlin.time.DurationUnit

enum class TaskType {
    RayCast, Continual, WaitForCondition, Undefined
}
//TODO: Implement WaitForCondition Task
open class Promise(val id : Int,val type : TaskType = TaskType.Undefined) {
    val creationTime : Int = Bukkit.getCurrentTick()
    val lifetime : Int
        get() = Bukkit.getCurrentTick() - creationTime
    var isCanceled = false
        internal set
    private val terminationMethods = mutableListOf<TerminationMethod>()

    fun cancel() {
        if(isCanceled) throw IllegalStateException("Task is already canceled")
        Bukkit.getScheduler().cancelTask(id)
        isCanceled = true
    }
    fun checkForTermination(){
        val termination = terminationMethods.any { it.check(this) }
        if(termination) cancel()
    }
    infix fun addTermination(terminationMethod: TerminationMethod){
        terminationMethods.add(terminationMethod)
    }
}


interface TerminationMethod {
    fun check(promise: Promise) : Boolean

    //run final *time* tick and terminate it
    class TimeOut(val time : Duration) : TerminationMethod {
        override fun check(promise: Promise): Boolean {
            return promise.lifetime >= time.toTicks()
        }
    }
    class NumCycleAfter(num : Int) : TerminationMethod by TimeOut(num.ticks)
}

class AsyncPromise<T>(id : Int) : Promise(id){
    var value : T? = null
    fun complete(returned : T) {
        super.isCanceled = true
        value = returned
    }
}

fun syncRepeating(interval: Long = 1,block : Promise.() -> Unit) : Promise {
    var promise : Promise? = null
    val id = Bukkit.getScheduler().scheduleSyncRepeatingTask(PastelLib.instance, {
        try {
            block(promise!!)
            promise!!.checkForTermination()
        } catch (e : Exception) {
            log("task#${promise?.id} generated exception")
            e.printStackTrace()
            promise?.cancel()
        } },0,interval)
    promise = Promise(id)
    return promise
}

fun <T> async(block : AsyncPromise<T>.() -> T) : AsyncPromise<T> {
    var promise : AsyncPromise<T>? = null
    val id = Bukkit.getScheduler().runTaskAsynchronously(PastelLib.instance, Runnable {
        try {
            promise!!.complete(block(promise!!))
        } catch (e : Exception) {
            log("task#${promise?.id} generated exception")
            e.printStackTrace()
            promise?.cancel()
        }

    }).taskId
    promise = AsyncPromise(id)
    return promise
}
fun later(delay : Long, block : Promise.() -> Unit) : Promise {
    var promise : Promise? = null
    val id = Bukkit.getScheduler().scheduleSyncDelayedTask(PastelLib.instance, {
        try {
            block(promise!!)
        } catch (e : Exception) {
            log("task#${promise?.id} generated exception")
            e.printStackTrace()
            promise?.cancel()
        } },delay)
    promise = Promise(id)
    return promise
}
fun later(delay : Duration, block : Promise.() -> Unit) : Promise = later(delay.toTicks(), block)
fun Duration.toTicks() : Long = this.toLong(DurationUnit.MILLISECONDS)/50



fun <R> eventual(callback: (R) -> Unit,block: Promise.() -> R?) : Promise = syncRepeating {
    TODO()
    val a = block(this)
    if(a != null) {
        cancel()
        callback(a)
    }
}

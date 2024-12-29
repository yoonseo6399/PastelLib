package io.github.yoonseo.pastellib.utils.tasks

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.log
import org.bukkit.Bukkit

enum class TaskType {
    RayCast, Continual, WaitForCondition, Undefined
}
//TODO: Implement WaitForCondition Task
open class Promise(val id : Int,val type : TaskType = TaskType.Undefined) {
    var isCanceled = false
        internal set
    fun cancel() {
        if(isCanceled) throw IllegalStateException("Task is already canceled")
        Bukkit.getScheduler().cancelTask(id)
        isCanceled = true
    }
}
class AsyncPromise<T>(id : Int) : Promise(id){
    var value : T? = null
    fun complete(returned : T) {
        super.isCanceled = true
        value = returned
    }
}

fun syncRepeating(block : Promise.() -> Unit) : Promise {
    var promise : Promise? = null
    val id = Bukkit.getScheduler().scheduleSyncRepeatingTask(PastelLib.instance, {
        try {
            block(promise!!)
        } catch (e : Exception) {
            log("task#${promise?.id} generated exception")
            e.printStackTrace()
            promise?.cancel()
        } },0,1)
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

fun <R> eventual(callback: (R) -> Unit,block: Promise.() -> R?) : Promise = syncRepeating { block(this)?.let { callback(it) } }

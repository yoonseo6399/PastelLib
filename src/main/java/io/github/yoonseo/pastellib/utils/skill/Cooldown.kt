package io.github.yoonseo.pastellib.utils.skill

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.github.yoonseo.pastellib.utils.tasks.toTicks
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener
import org.w3c.dom.events.EventListener
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

open class Skill(
    val cooldown: Duration? = null,
    val chargeTime : Duration? = null,
) {
    private var inCooldown : Int = 0
    private var inCharge : Int = 0
    private var inCasting : Int = 0
    var status : SkillStatus = SkillStatus.READY
    companion object {
        private val coroutineScope = CoroutineScope(Job() + Dispatchers.Default + CoroutineName("Skill"))
    }
    fun setActivationMethod(activationMethod: ActivationMethod) {
        activationMethod.skillInstance = this
        Bukkit.getPluginManager().registerEvents(activationMethod,PastelLib.instance)
    }
    fun initiate(caster : LivingEntity){
        if(status != SkillStatus.READY) return notReady(caster,status)
        coroutineScope.launch {
            if(chargeTime != null) {
                status = SkillStatus.CHARGING
                runInMainThread { charge(caster) }
                delay(chargeTime)
            }
            status = SkillStatus.CASTING
            cast(caster)
            if(cooldown != null){
                status = SkillStatus.COOLDOWN
                delay(cooldown)
                status = SkillStatus.READY
                runInMainThread { cooldownComplete(caster) }
            }
            status = SkillStatus.READY
        }
    }
    private fun cooldownClock(){
        if(cooldown == null) return
        inCooldown = cooldown.toTicks().toInt()
        syncRepeating { inCooldown-- }
    }
    fun getCooldown() : Double {
        cooldown!!.toTicks()
        return inCooldown*0.05
    }
    open fun charge(caster : LivingEntity){}
    open suspend fun cast(caster : LivingEntity){}
    open fun cooldownComplete(caster : LivingEntity){}
    open fun notReady(caster: LivingEntity,status: SkillStatus){}
}
abstract class ActivationMethod : Listener {
    lateinit var skillInstance : Skill
}
enum class SkillStatus {
    READY,
    COOLDOWN,
    CASTING,
    CHARGING
}
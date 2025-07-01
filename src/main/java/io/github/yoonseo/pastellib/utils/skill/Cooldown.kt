package io.github.yoonseo.pastellib.utils.skill

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.celestia.Celestia
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.tasks.toTicks
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import java.net.http.WebSocket
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.round
import kotlin.time.Duration

open class Skill(
    val skillID : String,
    var defaultCooldown: Duration = Duration.ZERO,
    val chargeTime : Duration? = null,
    val condition : (LivingEntity) -> Boolean = {true}
) {
    val mutex = Mutex()
    var activationMethod : ActivationMethod<*>? = null
    var status : SkillStatus = SkillStatus.READY
    var lastCast : Int = 0

    companion object {
        private val coroutineScope = CoroutineScope(Job() + Dispatchers.Default + CoroutineName("Skill"))
    }
    fun initiate(caster : LivingEntity){

        //READY or Condition 이 아니라면
        if(status != SkillStatus.READY || !condition.invoke(caster)) return notReady(caster,status)

        status = SkillStatus.CASTING //TODO 만약 ChargeTime 이 있다면 Cancel Charge 만들어야 동기화 오류 안남, 마나통같은거 쓸때 동기화 오류 가능
        coroutineScope.launch {
            mutex.withLock {

                if(runInMainThread { hasEnergyAndPay(caster) }) return@withLock
                if(chargeTime != null) {
                    status = SkillStatus.CHARGING
                    runInMainThread { charge(caster) }
                    delay(chargeTime)
                }

                status = SkillStatus.CASTING
                cast(caster)

                val cooldown = getCooldownFor(caster)
                if(cooldown != Duration.ZERO){
                    status = SkillStatus.COOLDOWN
                    lastCast = Bukkit.getCurrentTick()
                    delay(cooldown)
                    status = SkillStatus.READY
                    runInMainThread { cooldownComplete(caster) }
                }
                status = SkillStatus.READY
            }
        }
    }


    private fun hasEnergyAndPay(caster: LivingEntity) : Boolean{
        val pool = getEnergyPool(caster)
        val cost = getEnergyCostFor(caster)
        if(pool != null && cost != null) {
            while (true) {
                val current = pool.value
                if (current < cost) return false
                if (pool.compareAndSet(current, current - cost)) return true
            }
            return false
        }
        return true
    }
    open fun getEnergyCostFor(caster: LivingEntity) : Double? = null //suspend ?
    open fun getCooldownFor(caster : LivingEntity) = defaultCooldown
    fun getCooldown(caster: LivingEntity) : Double {
        defaultCooldown.toTicks()
        return round((getCooldownFor(caster).toTicks() - (Bukkit.getCurrentTick() - lastCast)) * 1000*0.05) / 1000
    }
    open fun getEnergyPool(caster: LivingEntity) : EnergyPool? = null
    open fun charge(caster : LivingEntity){}
    open suspend fun cast(caster : LivingEntity){}
    open fun cooldownComplete(caster : LivingEntity){}
    open fun notReady(caster: LivingEntity, status: SkillStatus) {
        val message = when (status) {
            SkillStatus.COOLDOWN -> "[ 쿨타임중입니다 ${getCooldown(caster)}s ]"
            SkillStatus.CASTING -> "[ 스킬 시전 중입니다 ]"
            SkillStatus.CHARGING -> "[ 차지 중입니다 ]"
            else -> "[ 에너지 준위가 낮습니다 ${Celestia.instance?.energyPool?.value} ]"
        }
        caster.sendActionBar(Component.text(message).color(NamedTextColor.RED))
    }}
class ActivationMethod<E : Event>(val casterProvider : (E) -> LivingEntity,val block : Skill.(E) -> Boolean) {
    companion object{
        fun leftClick(block : Skill.(PlayerInteractEvent) -> Boolean) = ActivationMethod<PlayerInteractEvent>({it.player}) { e -> if(e.action.isLeftClick) block(e) else false }
        fun rightClick(block : Skill.(PlayerInteractEvent) -> Boolean) = ActivationMethod<PlayerInteractEvent>({it.player}) { e -> if(e.action.isRightClick) block(e) else false }
        fun offhand(block : Skill.(PlayerSwapHandItemsEvent) -> Boolean) = ActivationMethod<PlayerSwapHandItemsEvent>({it.player}) { e -> block(e) }
    }
}
enum class SkillStatus {
    READY,
    COOLDOWN,
    CASTING,
    CHARGING
}
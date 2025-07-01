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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import kotlin.math.round
import kotlin.time.Duration

open class Skill(
    var defaultCooldown: Duration = Duration.ZERO,
    val chargeTime : Duration? = null,
    val condition : (LivingEntity) -> Boolean = {true}
) {
    val skillLocks = mutableMapOf<UUID, Mutex>() //✅ 더 좋은 구조: SkillSystem 에서 관리

    var status : SkillStatus = SkillStatus.READY
    var lastCast : Int = 0

    companion object {
        private val coroutineScope = CoroutineScope(Job() + Dispatchers.Default + CoroutineName("Skill"))
    }
    fun setActivationMethod(activationMethod: ActivationMethod) {
        activationMethod.skillInstance = this
        Bukkit.getPluginManager().registerEvents(activationMethod,PastelLib.instance)
    }
    fun initiate(caster : LivingEntity){

        //READY or Condition 이 아니라면
        if(status != SkillStatus.READY || !condition.invoke(caster)) return notReady(caster,status)

        status = SkillStatus.CASTING //TODO 만약 ChargeTime 이 있다면 Cancel Charge 만들어야 동기화 오류 안남, 마나통같은거 쓸때 동기화 오류 가능
        val mutex = getMutexFor(caster)
        coroutineScope.launch {
            mutex.withLock {
                if(!hasEnergyAndPay(caster)) return@withLock
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
    fun getMutexFor(caster: LivingEntity): Mutex {
        return skillLocks.getOrPut(caster.uniqueId) { Mutex() }
    }

    private fun hasEnergyAndPay(caster: LivingEntity) : Boolean{
        val pool = getEnergyPool(caster)
        val cost = getEnergyCostFor(caster)
        if(pool != null && cost != null) {
            if(pool.value - cost  >= 0){
                pool.value -= cost
                return true
            }
            return false
        }
        return true
    }
    open fun getEnergyCostFor(caster: LivingEntity) : Double? = null
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
            else -> "[ 에너지 준위가 낮습니다 ${Celestia.instance?.energyPool} ]"
        }
        caster.sendActionBar(Component.text(message).color(NamedTextColor.RED))
    }}
abstract class ActivationMethod : Listener {
    lateinit var skillInstance : Skill
    companion object {
        fun leftClickWith(material: Material, block: (Player) -> Unit): ActivationMethod {
            return object : ActivationMethod() {
                @EventHandler
                fun onInteract(e: PlayerInteractEvent) {
                    if (!e.action.isLeftClick || e.item?.type != material) return
                    block(e.player)
                }
            }
        }
        fun rightClickWith(material: Material, block: (Player) -> Unit): ActivationMethod {
            return object : ActivationMethod() {
                @EventHandler
                fun onInteract(e: PlayerInteractEvent) {
                    if (!e.action.isRightClick || e.item?.type != material) return
                    block(e.player)
                }
            }
        }
    }

}
enum class SkillStatus {
    READY,
    COOLDOWN,
    CASTING,
    CHARGING
}
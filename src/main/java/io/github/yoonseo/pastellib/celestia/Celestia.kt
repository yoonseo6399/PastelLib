package io.github.yoonseo.pastellib.celestia

import com.google.common.util.concurrent.AtomicDouble
import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.celestia.models.EnergyRing
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.skill.EnergyPool
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.time.Duration.Companion.seconds

class Celestia(val body : Player) {
    companion object {
        var instance : Celestia? = null
    }
    var phase = 1
        private set
    var eRing : EnergyRing? = null ; val regenTime = (1).seconds
    var energyPool = EnergyPool(100.0) { _ , new -> eRing?.energy = new.toInt()} // 1페라면 에너지 제한 없에기
        get() {
            if(phase == 1) return field
            else {
                field.value = eRing!!.energy.toDouble()
                return field
            }
        }
    val bossbar = BossBar.bossBar(Component.text("[ 별의 수호자 ]"),BossBar.MAX_PROGRESS,BossBar.Color.YELLOW,BossBar.Overlay.PROGRESS)

    init { // TODO celestia 가 두명이거나 부활할때 두번 등록할수있음
        Bukkit.getPluginManager().registerEvents(CelestiaEventHandler(this),PastelLib.instance)
        instance = this
        body.location.world.playSound(body.location, Sound.ENTITY_ENDER_DRAGON_GROWL,1f,1f)
        body.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 200.0
        body.health = 200.0
        body.location.world.getNearbyEntities(body.location,15.0,15.0,15.0) { it is Player }.toList().map { it as Player }
            .forEach { it.showBossBar(bossbar) }

    }
    fun nextPhase(){
        phase++
        if (phase == 2){
            body.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY,20,1,false,false))
            body.getAttribute(Attribute.MAX_HEALTH)?.apply {
                baseValue = 600.0
            }
            body.health = 600.0
            eRing = EnergyRing(body,regenTime).also { it.renderer.load(body.location) }

        }
    }
}
inline fun <R>celestiaCondition(caster : LivingEntity,block : (Celestia) -> R?) : R?{
    val celestia = Celestia.instance ?: return null
    if(celestia.body == caster) return block(celestia)
    return null
}
class CelestiaEventHandler(val c: Celestia) : Listener {
    @EventHandler
    fun onDamaged(e : EntityDamageEvent){

        if(e.entity != c.body) return
        val percent = c.body.health/c.body.getAttribute(Attribute.MAX_HEALTH)!!.baseValue
        c.bossbar.progress(percent.toFloat())
        if(c.phase == 1 && (e.entity as LivingEntity).health - e.damage <= 0){
            e.isCancelled = true
            c.nextPhase()
        }
    }
}
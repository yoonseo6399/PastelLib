package io.github.yoonseo.pastellib.utils.skill

import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.entity.particle.particles.FireParticle
import io.github.yoonseo.pastellib.utils.entity.particle.showParticle
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.showParticle
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.github.yoonseo.pastellib.utils.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class LightLaser : Skill((3).seconds) {
    init {
        val method = object : ActivationMethod() {
            @EventHandler
            fun onSwapItem(e : PlayerSwapHandItemsEvent){
                if(e.offHandItem.type != Material.GOLDEN_SWORD) return
                e.isCancelled = true
                skillInstance.initiate(e.player)
            }
        }
        setActivationMethod(method)
    }

    override suspend fun cast(caster : LivingEntity) {
        repeat(5){
            runInMainThread {
                magic(caster)
            }
            delay((80).ticks)
        }
    }
    fun magic(caster: LivingEntity){
        val targets = caster.location.world.getNearbyEntities(caster.location,10.0,10.0,10.0){ it != caster && it is LivingEntity}.map { it as LivingEntity }
        var expire = 40
        syncRepeating {
            for (target in targets) {
                target.location.showParticle(FireParticle(),1)
            }
            expire--
            if(expire <= 0) {
                targets.forEach { it.damage(5.0) }
                cancel()
            }
        }
    }
}

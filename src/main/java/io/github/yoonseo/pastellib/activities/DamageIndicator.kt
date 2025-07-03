package io.github.yoonseo.pastellib.activities

import io.github.yoonseo.pastellib.utils.entity.particle.particles.NumberDisplayParticle
import io.github.yoonseo.pastellib.utils.entity.particle.showParticle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DamageIndicator : Listener{
    @EventHandler
    fun hurtEvent(e : EntityDamageEvent){
        if(e.entity is LivingEntity) {
            (e.entity as LivingEntity).eyeLocation.showParticle(NumberDisplayParticle(e.finalDamage))
        }
    }
}
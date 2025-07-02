package io.github.yoonseo.pastellib.celestia.skills

import io.github.yoonseo.pastellib.celestia.Celestia
import io.github.yoonseo.pastellib.celestia.celestiaCondition
import io.github.yoonseo.pastellib.celestia.models.Lighting
import io.github.yoonseo.pastellib.guns.ParameterType
import io.github.yoonseo.pastellib.utils.entity.model.damage
import io.github.yoonseo.pastellib.utils.lookVector
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.selectors.rayTo
import io.github.yoonseo.pastellib.utils.skill.ActivationMethod
import io.github.yoonseo.pastellib.utils.skill.Skill
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.entity.Firework
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

import kotlin.time.Duration

class LightSakura : CelestiaSkill("LightSakura", Duration.ZERO, energyCost = 4.0){
    companion object {
        var activationMethod = ActivationMethod.rightClick { it.material == Material.GOLDEN_SWORD && celestiaCondition(it.player) { it.phase == 2 } == true }
    }

    override suspend fun cast(caster: LivingEntity) { require(caster is Player)
        val targets = caster.location.world.getNearbyEntities(caster.location,15.0,15.0,15.0) { it != caster && it is LivingEntity }.toList().map { it as LivingEntity }
        if(targets.isEmpty()) return
        targets.sortedBy { it.location.distance(caster.location) }
        hitParticle(caster.eyeLocation)

        for ((i,now) in targets.withIndex()) {
//            val before = if(i == 0) caster else targets[i-1]
//            val dir = before.eyeLocation lookVector e.eyeLocation
//            dir.multiply(-1) // 번개는 뒤로 뿌림
//            val ray = before.eyeLocation rayTo e.eyeLocation
//            ray.checkingInterval = 3.0
//            //LightingBeam(caster,dir.clone().multiply(e.eyeLocation.distance(before.eyeLocation))).renderer.load(e.eyeLocation)
//            ray.forEach {
//                location: Location ->
//                Lighting(caster,7,dir.clone(), size = 0.6).renderer.load(location)            }
            val hitCount = targets.size
            val before = if(i == 0) caster else targets[i-1]
            val ray = before.location rayTo now.location
            ray.forEach {
                it.add(0.0,0.5,0.0)
                rayParticle(it)
            }

            repeat(hitCount) { now.damage(25.0,caster, DamageType.EXPLOSION,true) }
            hitParticle(now.location.add(0.0,0.5,0.0))
        }
        caster.teleport(targets.last().location)
    }
    fun hitParticle(loc : Location) {
        loc.world.spawnParticle(Particle.FLASH,loc,1)
        loc.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION,loc,100,0.0,0.0,0.0,0.5)
        loc.world.spawn(loc,Firework::class.java).apply {
             fireworkMeta.addEffects(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withTrail().withColor(Color.YELLOW).withFade(
                Color.WHITE).withFlicker().build())
            detonate()
        }
    }
    fun rayParticle(loc : Location){
        loc.world.spawnParticle(Particle.WAX_OFF,loc,25,0.0,0.0,0.0,1.5)
        loc.world.spawnParticle(Particle.WAX_ON,loc,10,0.0,0.0,0.0,0.5)
    }
}


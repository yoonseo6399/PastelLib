package io.github.yoonseo.pastellib.celestia.skills

import io.github.yoonseo.pastellib.celestia.Celestia
import io.github.yoonseo.pastellib.celestia.models.Lighting
import io.github.yoonseo.pastellib.utils.lookVector
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.selectors.rayTo
import io.github.yoonseo.pastellib.utils.skill.ActivationMethod
import io.github.yoonseo.pastellib.utils.skill.Skill
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.time.Duration

class LightSakura : Skill(defaultCooldown = Duration.ZERO, null,getCelestiaCondition(4,2)){
    init {
        val method = object : ActivationMethod() {
            @EventHandler
            fun onInteract(e : PlayerInteractEvent){
                if(!e.action.isRightClick || e.item?.type != Material.GOLDEN_SWORD) return
                initiate(e.player)
            }
        }
        setActivationMethod(method)
    }

    override suspend fun cast(caster: LivingEntity) {
        require(caster is Player)
        runInMainThread {
            Celestia.instance!!.energyPool -= 4
            val targets = caster.location.world.getNearbyEntities(caster.location,15.0,15.0,15.0) { it != caster && it is LivingEntity }.toList().map { it as LivingEntity }
            targets.sortedBy { it.location.distance(caster.location) }
            for ((i,e) in targets.withIndex()) {
                val before = if(i == 0) caster else targets[i-1]
                val dir = before.eyeLocation lookVector e.eyeLocation
                dir.multiply(-1) // 번개는 뒤로 뿌림
                val ray = before.eyeLocation rayTo e.eyeLocation
                ray.checkingInterval = 3.0
                //LightingBeam(caster,dir.clone().multiply(e.eyeLocation.distance(before.eyeLocation))).renderer.load(e.eyeLocation)
                ray.forEach {
                        location: Location ->
                    Lighting(caster,7,dir.clone(), size = 0.6).renderer.load(location)            }
            }
        }
    }
}
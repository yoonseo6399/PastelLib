package io.github.yoonseo.pastellib.celestia.skills

import io.github.yoonseo.pastellib.celestia.celestiaCondition
import io.github.yoonseo.pastellib.celestia.isCelestiaItem
import io.github.yoonseo.pastellib.celestia.models.LightLaser
import io.github.yoonseo.pastellib.utils.entity.model.spawnModel
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.skill.ActivationMethod
import io.github.yoonseo.pastellib.utils.skill.Skill
import io.github.yoonseo.pastellib.utils.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

class LightLaserSkill : CelestiaSkill("LightLaser",(15).seconds, energyCost = null) {
    companion object {
        var activationMethod = ActivationMethod.offhand(true) { isCelestiaItem(it.offHandItem) && celestiaCondition(it.player) { it.phase == 2 } == true }
    }

    override suspend fun cast(caster : LivingEntity) {
        repeat(5){
            magic(caster)
            delay((80).ticks)
        }
    }
    fun magic(caster: LivingEntity){
        val targets = caster.location.world.getNearbyEntities(caster.location,30.0,30.0,30.0){ it != caster && it is LivingEntity}.map { it as LivingEntity }
        for (target in targets) {
            target.location.setDirection(Vector(0,0,1)).spawnModel(LightLaser::class,caster)
        }
    }
}

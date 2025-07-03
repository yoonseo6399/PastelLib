package io.github.yoonseo.pastellib.celestia.skills

import io.github.yoonseo.pastellib.celestia.Celestia
import io.github.yoonseo.pastellib.celestia.celestiaCondition
import io.github.yoonseo.pastellib.celestia.models.SwordDemon
import io.github.yoonseo.pastellib.utils.entity.model.Model
import io.github.yoonseo.pastellib.utils.entity.model.spawnModel
import io.github.yoonseo.pastellib.utils.skill.ActivationMethod
import io.github.yoonseo.pastellib.utils.ticks
import kotlinx.coroutines.delay
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

class SwordDemonSkill : CelestiaSkill("SwordDemon",(3).seconds, energyCost = 1.0){
    companion object {
        var activationMethod = ActivationMethod.leftClick { it.material == Material.GOLDEN_SWORD && celestiaCondition(it.player) { true } == true }
    }

    override suspend fun cast(caster: LivingEntity) {
        require(Celestia.instance != null)
        repeat(3){
            magic(caster)
            delay((1).ticks)
        }
    }

    override fun getCooldownFor(caster: LivingEntity): Duration {
        return celestiaCondition(caster) { if(it.phase==2) ZERO else null } ?: defaultCooldown
    }

    fun magic(caster: LivingEntity){
        //SwordDemon(caster).renderer.load(caster.location)
        caster.location.spawnModel(SwordDemon::class,caster)
        caster.playSound(Sound.sound(org.bukkit.Sound.ITEM_TRIDENT_THROW,Sound.Source.PLAYER,2f,0.9f))
    }
}
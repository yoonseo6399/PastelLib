package io.github.yoonseo.pastellib.celestia.skills

import io.github.yoonseo.pastellib.celestia.celestiaCondition
import io.github.yoonseo.pastellib.utils.skill.EnergyPool
import io.github.yoonseo.pastellib.utils.skill.Skill
import org.bukkit.entity.LivingEntity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

open class CelestiaSkill(
    skillID : String,
    defaultCooldown: Duration = ZERO,
    chargeTime : Duration? = null,
    condition : (LivingEntity) -> Boolean = {true},
    val energyCost : Double?
) : Skill(skillID,defaultCooldown,chargeTime,condition) {


    //override fun getCooldownFor(caster: LivingEntity): Duration {
    //    return celestiaCondition(caster) { if(it.phase==2) ZERO else null } ?: defaultCooldown
    //}

    override fun getEnergyPool(caster: LivingEntity): EnergyPool? {
        return celestiaCondition(caster) { it.energyPool }
    }

    override fun getEnergyCostFor(caster: LivingEntity): Double? {
        return celestiaCondition(caster) { energyCost }
    }
}
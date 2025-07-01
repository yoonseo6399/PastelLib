package io.github.yoonseo.pastellib.celestia.skills

import io.github.yoonseo.pastellib.celestia.Celestia
import io.github.yoonseo.pastellib.celestia.models.SwordDemon
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.skill.ActivationMethod
import io.github.yoonseo.pastellib.utils.skill.Skill
import io.github.yoonseo.pastellib.utils.skill.SkillStatus
import io.github.yoonseo.pastellib.utils.ticks
import kotlinx.coroutines.delay
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

class SwordDemonSkill : Skill(defaultCooldown = (3).seconds, condition = getCelestiaCondition(1)){
    init {
        val method = object : ActivationMethod() {
            @EventHandler
            fun onInteract(e : PlayerInteractEvent){
                if(!e.action.isLeftClick || e.item?.type != Material.GOLDEN_SWORD) return
                initiate(e.player)
            }
        }
        setActivationMethod(method)
    }

    override suspend fun cast(caster: LivingEntity) {
        require(Celestia.instance != null)
        if(Celestia.instance?.phase == 2) defaultCooldown = ZERO
        runInMainThread { Celestia.instance!!.energyPool -= 1 }
        repeat(3){
            runInMainThread { magic(caster) }
            delay((1).ticks)

        }
    }
    fun magic(caster: LivingEntity){
        SwordDemon(caster).renderer.load(caster.location)
        caster.playSound(Sound.sound(org.bukkit.Sound.ITEM_TRIDENT_THROW,Sound.Source.PLAYER,2f,0.9f))
    }

    override fun notReady(caster: LivingEntity,status: SkillStatus) {
        if(status == SkillStatus.COOLDOWN) caster.sendActionBar(Component.text("[ 쿨타임중입니다 ${getCooldown()}s ]").color(NamedTextColor.RED))
        else caster.sendActionBar(Component.text("[ 에너지 준위가 낮습니다 ${Celestia.instance?.energyPool} ]").color(NamedTextColor.RED))
    }
}

fun getCelestiaCondition(energyRequired : Int,phase : Int = 0) : (LivingEntity) -> Boolean =
    { Celestia.instance != null
            && if (phase == 0) true else phase == Celestia.instance!!.phase
            && Celestia.instance!!.body == it
            && Celestia.instance!!.energyPool >= energyRequired }
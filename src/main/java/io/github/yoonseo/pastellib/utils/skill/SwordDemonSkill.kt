package io.github.yoonseo.pastellib.utils.skill

import io.github.yoonseo.pastellib.utils.DebugScope.commandJuho
import io.github.yoonseo.pastellib.utils.entity.model.SwordDemon
import io.github.yoonseo.pastellib.utils.runInMainThread
import io.github.yoonseo.pastellib.utils.ticks
import kotlinx.coroutines.delay
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.time.Duration.Companion.seconds

class SwordDemonSkill : Skill(cooldown = (3).seconds){
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
    }
}
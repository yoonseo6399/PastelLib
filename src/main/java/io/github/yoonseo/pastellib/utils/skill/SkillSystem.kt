package io.github.yoonseo.pastellib.utils.skill

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.DebugScope
import io.github.yoonseo.pastellib.utils.debug
import kotlinx.coroutines.sync.Mutex
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

object SkillSystem {
    private val skillLocks = mutableMapOf<UUID,Mutex>()
    val skillFactories = mutableMapOf<String, () -> Skill>()

    inline fun <reified E : Event,S : Skill> registerSkill(skillId : String, activationMethod: ActivationMethod<E>, noinline skillFactory : () -> S) {
        skillFactories[skillId] = skillFactory
        val listener = object : Listener {
            @EventHandler
            fun onEvent(event : E){
                val caster = activationMethod.casterProvider.invoke(event)
                val skill = getSkillFor(caster, skillId) ?: return Bukkit.getLogger().warning("skill $skillId not registered but initiated")
                if(activationMethod.block(skill,event)) {
                    DebugScope.commandJuho.sendMessage("Debug : $skill is found and initiated")
                    skill.initiate(caster)
                }
            }
        }
        Bukkit.getPluginManager().registerEvents(listener, PastelLib.instance)
    }
    val skillMap = mutableMapOf<UUID,MutableMap<String,Skill>>()
    fun getSkillFor(caster : LivingEntity,skillId: String) : Skill? {
        if(!skillFactories.contains(skillId)) return null
        return skillMap.getOrPut(caster.uniqueId) { mutableMapOf() }
            .getOrPut(skillId) { skillFactories[skillId]!!() }
    }
}
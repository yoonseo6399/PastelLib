package io.github.yoonseo.pastellib.utils.skill

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.DebugScope
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import java.util.UUID

object SkillSystem {
    val skillFactories = mutableMapOf<String, () -> Skill>()

    inline fun <reified E : Event,S : Skill> registerSkill(skillId : String, activationMethod: ActivationMethod<E>, noinline skillFactory : () -> S) {
        skillFactories[skillId] = skillFactory
        val listener = getSpecificEventListener<E> {
            val caster = activationMethod.casterProvider.invoke(it)
            val skill = getSkillFor(caster, skillId) ?: return@getSpecificEventListener Bukkit.getLogger().warning("skill $skillId not registered but initiated")
            if(activationMethod.block(skill,it)) {
                DebugScope.commandJuho.sendMessage("Debug : $skill is found and initiated")
                skill.initiate(caster)
            }
        }
        Bukkit.getPluginManager().registerEvents(listener, PastelLib.instance)
    }
    val skillMap = mutableMapOf<UUID,MutableMap<String,Skill>>()
    inline fun <reified E : Event> getSpecificEventListener(crossinline block: (E) -> Unit): Listener { //TODO 여기가 아니라 익스텐션에 있어야하지 않을까
        return when (E::class) {
            PlayerInteractEvent::class -> object : Listener {
                @EventHandler
                fun onEvent(e: PlayerInteractEvent) {
                    @Suppress("UNCHECKED_CAST")
                    block(e as E)
                }
            }

            PlayerDropItemEvent::class -> object : Listener {
                @EventHandler
                fun onEvent(e: PlayerDropItemEvent) {
                    @Suppress("UNCHECKED_CAST")
                    block(e as E)
                }
            }

            PlayerSwapHandItemsEvent::class -> object : Listener {
                @EventHandler
                fun onEvent(e: PlayerSwapHandItemsEvent) {
                    @Suppress("UNCHECKED_CAST")
                    block(e as E)
                }
            }

            PlayerItemHeldEvent::class -> object : Listener {
                @EventHandler
                fun onEvent(e: PlayerItemHeldEvent) {
                    @Suppress("UNCHECKED_CAST")
                    block(e as E)
                }
            }

            else -> throw IllegalArgumentException("Unsupported event type: ${E::class}")
        }
    }
    fun getSkillFor(caster : LivingEntity,skillId: String) : Skill? {
        if(!skillFactories.contains(skillId)) return null
        return skillMap.getOrPut(caster.uniqueId) { mutableMapOf() }
            .getOrPut(skillId) { skillFactories[skillId]!!() }
    }
}
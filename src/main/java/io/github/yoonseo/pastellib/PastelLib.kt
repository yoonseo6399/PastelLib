package io.github.yoonseo.pastellib

import io.github.yoonseo.pastellib.activities.DamageIndicator
import io.github.yoonseo.pastellib.activities.Sit
import io.github.yoonseo.pastellib.activities.isSitting
import io.github.yoonseo.pastellib.guns.Gun
import io.github.yoonseo.pastellib.guns.GunCommand
import io.github.yoonseo.pastellib.guns.GunCommandTabCompleter
import io.github.yoonseo.pastellib.utils.*
import io.github.yoonseo.pastellib.utils.entity.model.*
import io.github.yoonseo.pastellib.utils.entity.particle.DisplayParticle
import io.github.yoonseo.pastellib.celestia.skills.LightLaserSkill
import io.github.yoonseo.pastellib.celestia.skills.SwordDemonSkill
import io.github.yoonseo.pastellib.celestia.skills.LightSakura
import io.github.yoonseo.pastellib.utils.skill.SkillSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.block.data.BlockData
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.coroutines.CoroutineContext


class PastelLib : JavaPlugin() {
    companion object {
        lateinit var instance: PastelLib

        val json : Json = Json {
            serializersModule = SerializersModule {
                contextual(Vector3f::class, Vector3fSerializer)
                contextual(Quaternionf::class, QuaternionfSerializer)
                contextual(Transformation::class, TransformationSerializer)
                contextual(BlockData::class, BlockDataSerializer)
                contextual(Color::class, ColorSerializer)
            }
        }
        lateinit var modelFileManager: ModelFileManager
    }
    override fun onEnable() {
        instance = this
        modelFileManager = ModelFileManager()
        Sit.initialize()
        Gun.initalize()
        SkillSystem.registerSkill("SwordDemon",SwordDemonSkill.activationMethod) { SwordDemonSkill() }
        SkillSystem.registerSkill("LightSakura",LightSakura.activationMethod) { LightSakura() }
        SkillSystem.registerSkill("LightLaser",LightLaserSkill.activationMethod) { LightLaserSkill() }





        getCommand("gun")?.also { it.tabCompleter = GunCommandTabCompleter() }?.setExecutor(GunCommand())
        getCommand("task")?.setExecutor(TaskCommand())
        getCommand("model")?.setExecutor(ModelCommand())
        Bukkit.getPluginManager().registerEvents(DamageIndicator(),this)
    }
    override fun onDisable() {
        // Plugin shutdown logic
        ModelManager.shutdown()
        Sit.map.keys.forEach { it.isSitting = false }
        DebugScope.resetScoreboard()
        DisplayParticle.particles.forEach { it.remove() }
    }
}

val mainThread = Bukkit.getScheduler().asCoroutineDispatcher(PastelLib.instance)
fun BukkitScheduler.asCoroutineDispatcher(plugin: Plugin): CoroutineDispatcher =
    object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            runTask(plugin, block)
        }
    }
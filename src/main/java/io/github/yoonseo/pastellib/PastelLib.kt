package io.github.yoonseo.pastellib

import io.github.yoonseo.pastellib.activities.Sit
import io.github.yoonseo.pastellib.activities.isSitting
import io.github.yoonseo.pastellib.celestia.models.Lighting
import io.github.yoonseo.pastellib.guns.Gun
import io.github.yoonseo.pastellib.guns.GunCommand
import io.github.yoonseo.pastellib.guns.GunCommandTabCompleter
import io.github.yoonseo.pastellib.utils.*
import io.github.yoonseo.pastellib.utils.entity.model.*
import io.github.yoonseo.pastellib.utils.entity.particle.particles.FireParticle
import io.github.yoonseo.pastellib.utils.entity.particle.particles.NumberDisplayParticle
import io.github.yoonseo.pastellib.utils.entity.particle.DisplayParticle
import io.github.yoonseo.pastellib.utils.entity.particle.showParticle
import io.github.yoonseo.pastellib.utils.selectors.TickedEntitySelector
import io.github.yoonseo.pastellib.celestia.skills.LightLaserSkill
import io.github.yoonseo.pastellib.celestia.skills.SwordDemonSkill
import io.github.yoonseo.pastellib.celestia.models.SwordDemon
import io.github.yoonseo.pastellib.celestia.skills.LightSakura
import io.github.yoonseo.pastellib.utils.skill.SkillSystem
import io.github.yoonseo.pastellib.utils.tasks.later
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
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
        Bukkit.getPluginManager().registerEvents(TEST(),this)
    }
    override fun onDisable() {
        // Plugin shutdown logic
        Sit.map.keys.forEach { it.isSitting = false }
        DebugScope.resetScoreboard()
        DisplayParticle.particles.forEach { it.remove() }
    }
}
class TEST : Listener{
    var blockDisplay by nullIf<Model<BlockDisplay>> { it?.isDead }
    var laser by nullIf<Laser> { it?.isDead }
    var cooldown = 0
    @EventHandler(priority = EventPriority.LOW)
    fun clickEvent(e: PlayerInteractEvent){
        if(e.action.isLeftClick) return

        when(e.item?.type){
            Material.AMETHYST_SHARD -> { //////////
                e.clickedBlock?.debug()
                val a = syncRepeating {
                    e.clickedBlock?.location?.showParticle(FireParticle(),5)
                }
                later(4) {
                    a.cancel()
                }
                e.isCancelled = true
            }
            Material.END_ROD -> {
                debug {
                    if(laser == null) laser = Laser(commandJuho.location,10f,0.5f,Material.WHITE_CONCRETE.createBlockData(),Material.WHITE_STAINED_GLASS.createBlockData(),
                        LaserOptions.RotateZ,
                        LaserOptions.FOLLOW,
                        LaserOptions.LIGHT_EMIT)
                    laser!!.teleport(commandJuho.location)
                }
            }
            else -> {}
        }
    }
    fun load(){
        if(blockDisplay == null){ /////////////
            debug {
                blockDisplay = DefaultModel<BlockDisplay>("boss1-L").renderer.load(commandJuho.location)
                SwordDemon(commandJuho).renderer.load(commandJuho.location)
//                syncRepeating {
//                    blockDisplay!!.debug()
//                }
                commandJuho.sendMessage("block display created")
            }
        }
    }

    @EventHandler
    fun hurtEvent(e : EntityDamageEvent){
        if(e.entity is LivingEntity) {
            (e.entity as LivingEntity).eyeLocation.showParticle(NumberDisplayParticle(e.damage))
        }
    }

    @EventHandler
    fun hotbarChange(e : PlayerItemHeldEvent){
        if(e.player.inventory.getItem(e.newSlot)?.type == Material.SPYGLASS){
            TickedEntitySelector<LivingEntity>(e.player.eyeLocation,e.player.location.direction,0.2, range = 100.0).tickAll()?.firstOrNull()?.debug()
        }
    }
}

val mainThread = Bukkit.getScheduler().asCoroutineDispatcher(PastelLib.instance)
fun BukkitScheduler.asCoroutineDispatcher(plugin: Plugin): CoroutineDispatcher =
    object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            runTask(plugin, block)
        }
    }
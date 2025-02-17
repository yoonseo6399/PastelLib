package io.github.yoonseo.pastellib

import io.github.yoonseo.pastellib.activities.Sit
import io.github.yoonseo.pastellib.activities.isSitting
import io.github.yoonseo.pastellib.guns.Gun
import io.github.yoonseo.pastellib.guns.GunCommand
import io.github.yoonseo.pastellib.guns.GunCommandTabCompleter
import io.github.yoonseo.pastellib.utils.*
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.*
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.particles.FireParticle
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.particles.FloorBloodParticle
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.particles.NumberDisplayParticle
import io.github.yoonseo.pastellib.utils.selectors.TickedEntitySelector
import io.github.yoonseo.pastellib.utils.tasks.later
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.plugin.java.JavaPlugin


class PastelLib : JavaPlugin() {
    companion object {
        lateinit var instance: PastelLib
    }
    override fun onEnable() {
        instance = this
        Sit.initialize()
        Gun.initalize()
        getCommand("gun")?.also { it.tabCompleter = GunCommandTabCompleter() }?.setExecutor(GunCommand())
        getCommand("task")?.setExecutor(TaskCommand())
        Bukkit.getPluginManager().registerEvents(TEST(),this)
        debug {
            executeOnItem(Material.STICK){

            }
        }
    }
    override fun onDisable() {
        // Plugin shutdown logic
        Sit.map.keys.forEach { it.isSitting = false }
        DebugScope.resetScoreboard()
        DisplayParticle.particles.forEach { it.remove() }
    }
}
class TEST : Listener{
    var blockDisplay : AdvancedBlockDisplay? = null
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
                    if(laser == null) laser = Laser(commandJuho.location,10f,0.5f,Material.WHITE_CONCRETE.createBlockData(),Material.WHITE_STAINED_GLASS.createBlockData(),LaserOptions.RotateZ,LaserOptions.FOLLOW,LaserOptions.LIGHT_EMIT)
                    laser!!.teleport(commandJuho.location)
                }
            }
            else -> {}
        }
    }


    @EventHandler
    fun hurtEvent(e : EntityDamageEvent){
        e.entity.location.showParticle(FloorBloodParticle(e.damage))
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
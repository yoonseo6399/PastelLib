package io.github.yoonseo.pastellib.guns

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.amountOf
import io.github.yoonseo.pastellib.utils.dataContainer
import io.github.yoonseo.pastellib.utils.removeItem
import io.github.yoonseo.pastellib.utils.selectors.ComplexTickedSelector
import io.github.yoonseo.pastellib.utils.selectors.runByTick
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.later
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class Gun(val item: ItemStack) {
    companion object{
        val eventAttached = ArrayList<UUID>()
        fun fromItem(itemStack: ItemStack) : Gun = Gun(itemStack)
    }

    val dataContainer = item.dataContainer()
    var ammo : Int             by dataContainer
    var magazineSize : Int     by dataContainer
    var isReloading : Boolean  by dataContainer
    val reloadTime : Int       by dataContainer
    val damage : Double        by dataContainer
    val bulletSpeed : Double   by dataContainer
    val recoil : Double        by dataContainer
    // how much tick is needed for next firing
    val fireRate : Int         by dataContainer
    val ammoType : String      by dataContainer
    var lastFired : Int = 0
    var reloadPromise : Promise? = null
    var fireTask : Promise? = null


    fun isReadyToFire() : Boolean {
        return Bukkit.getCurrentTick() - lastFired > fireRate && ammo > 0 && !isReloading
    }

    private fun createSelector(player : Player) : ComplexTickedSelector<LivingEntity,Block, LivingEntity> = ComplexTickedSelector<LivingEntity,Block, LivingEntity>(player.eyeLocation,player.location.direction, range = 100.0) {
        if(getBlock() != null) isFinished = true
        return@ComplexTickedSelector getEntities()
    }

    fun attachFireTask(player: Player) {
        var times = 0
        if(fireTask!= null) fireTask!!.cancel()
        fireTask = syncRepeating {
            fire(player)
            times++
            if(times >= 4) cancel()
        }
    }
    private fun fire(player: Player) : Boolean{
        if (!isReadyToFire()) {
            reload(player)
            return false
        }
        val damage = damage // caching
        val selector = createSelector(player)
        selector.runByTick(stepSize = (bulletSpeed / 0.2).toInt()) {
            it.forEach {
                it.health -= damage
                it.playEffect(EntityEffect.HURT)
                it.hurtSound?.let { s -> it.playSound(Sound.sound(s.key,Sound.Source.PLAYER,1f,1f)) }
            }
        }
        lastFired = Bukkit.getCurrentTick()
        ammo--
        return true
    }
    fun reload(player: Player) {
        if(ammo == magazineSize || isReloading) return
        reloadPromise = later(reloadTime.toLong()) {
            if(!isReloading) return@later
            val ammoAmount = player.inventory.amountOf(Material.valueOf(ammoType))
            player.inventory.removeItem(Material.valueOf(ammoType), magazineSize-ammo)
            if(ammoAmount >= magazineSize-ammo) ammo = magazineSize else ammo += ammoAmount
            isReloading = false
        }
    }
    override fun equals(other: Any?) : Boolean = (other is Gun && item == other.item) || other is ItemStack && item == other
    fun attachEvent(player: Player) {
        if(eventAttached.contains(player.uniqueId)) return
        eventAttached.add(player.uniqueId)
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun onChangeItem(e: PlayerItemHeldEvent) {
                if(e.player == player) {
                    isReloading = false
                    reloadPromise?.cancel()
                }
            }
            @EventHandler
            fun onInteraction(e: PlayerInteractEvent) {
                triggerGun(e.player)
            }
            @EventHandler
            fun onEntityInteraction(e: PlayerInteractEntityEvent) {
                triggerGun(e.player)
            }
            fun triggerGun(p: Player) {
                if(player != p) return
                if(fromItem(p.equipment.itemInMainHand) == this) attachFireTask(player)
            }
        }, PastelLib.instance)
    }

    override fun hashCode(): Int {
        var result = lastFired
        result = 31 * result + item.hashCode()
        result = 31 * result + dataContainer.hashCode()
        result = 31 * result + (reloadPromise?.hashCode() ?: 0)
        result = 31 * result + (fireTask?.hashCode() ?: 0)
        result = 31 * result + ammo
        result = 31 * result + magazineSize
        result = 31 * result + isReloading.hashCode()
        result = 31 * result + reloadTime
        result = 31 * result + damage.hashCode()
        result = 31 * result + bulletSpeed.hashCode()
        result = 31 * result + recoil.hashCode()
        result = 31 * result + fireRate
        result = 31 * result + ammoType.hashCode()
        return result
    }
}
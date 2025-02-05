package io.github.yoonseo.pastellib.guns

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.amountOf
import io.github.yoonseo.pastellib.utils.dataContainer
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.forceDamage
import io.github.yoonseo.pastellib.utils.log
import io.github.yoonseo.pastellib.utils.namespacedKey
import io.github.yoonseo.pastellib.utils.removeItem
import io.github.yoonseo.pastellib.utils.selectors.ComplexTickedSelector
import io.github.yoonseo.pastellib.utils.selectors.runByTick
import io.github.yoonseo.pastellib.utils.showParticle
import io.github.yoonseo.pastellib.utils.takeIsNotEmpty
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.later
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class Gun(val item: ItemStack) {
    companion object{
        fun ItemStack.asGunOrNull() : Gun? {
            if(itemMeta != null && this.dataContainer.containsKey("ammo") == true)
                return Gun(this)
            return null
        }
        fun initalize() {
            Bukkit.getPluginManager().registerEvents(object : Listener {
                @EventHandler
                fun onChangeItem(e: PlayerItemHeldEvent) {
                    e.player.equipment.itemInMainHand.asGunOrNull()?.cancelReload()
                }
                @EventHandler
                fun onInteraction(e: PlayerInteractEvent) {
                    //debug {
                    //    if(commandJuho == e.player) log(e.toString())
                    //}
                    triggerGun(e.player)
                }
                fun triggerGun(p: Player) {
                    //두번씩 실행되는 버그 TODO
                    p.equipment.itemInMainHand.asGunOrNull()?.attachFireTask(p)
                }
            }, PastelLib.instance)
        }
    }

    val dataContainer = item.dataContainer
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
        return Bukkit.getCurrentTick() - lastFired > fireRate && !isAmmoEmpty() && !isReloading
    }
    fun isAmmoEmpty() : Boolean {
        return ammo == 0
    }

    private fun createSelector(player : Player) : ComplexTickedSelector<LivingEntity,Block, LivingEntity> = ComplexTickedSelector<LivingEntity,Block, LivingEntity>(player.eyeLocation,player.location.direction, range = 100.0) {
        if(getBlock() != null) isFinished = true
        current.showParticle(Particle.END_ROD)
        return@ComplexTickedSelector getEntities()?.filter { it!= player }?.takeIsNotEmpty()
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
            player.sendActionBar(Component.text("Out Of Ammo").color(NamedTextColor.RED))
            reload(player)
            return false
        }
        val damage = damage // caching
        val selector = createSelector(player)
        selector.runByTick(stepSize = (bulletSpeed / 0.2).toInt()) {
            it.forEach {
                it.forceDamage(damage)
            }
        }
        lastFired = Bukkit.getCurrentTick()
        ammo--
        return true
    }
    fun reload(player: Player) {
        player.sendActionBar(Component.text("Reloading...").color(NamedTextColor.RED))
        if(ammo == magazineSize || isReloading) return
        isReloading = true

        reloadPromise = later(reloadTime.toLong()) {
            if(!isReloading) {
                cancelReload()
                return@later
            }

            debug {
                if(commandJuho != player) return@debug
                log("reload called")
                log("magazineSize : $magazineSize, ammo : $ammo")
                log("ammoAmount Cal")
            }
            val ammoAmount = player.inventory.amountOf {
                it.type == Material.ACACIA_BUTTON &&
                        it.dataContainer.getOrNull("ammoType", String::class) == ammoType
            }
            debug {
                if(commandJuho != player) return@debug
                log("ammoAmount $ammoAmount, type : $ammoType")
            }
            if(ammoAmount >= magazineSize-ammo) {
                player.inventory.removeItem(Material.ACACIA_BUTTON, magazineSize-ammo){
                    it.dataContainer.getOrNull("ammoType", String::class) == ammoType
                }
                ammo += (magazineSize - ammo)
            }else {
                player.inventory.removeItem(Material.ACACIA_BUTTON, ammoAmount){
                    it.dataContainer.getOrNull("ammoType", String::class) == ammoType
                }
                ammo += ammoAmount
            }
            debug {
                if(commandJuho != player) return@debug
                log("ammo : $ammo, magazineSize : $magazineSize")
                log("reload done")
            }
            isReloading = false
        }
    }
    fun cancelReload() {
        reloadPromise?.cancel()
        isReloading = false
    }
    override fun equals(other: Any?) : Boolean = (other is Gun && item == other.item) || other is ItemStack && item == other

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
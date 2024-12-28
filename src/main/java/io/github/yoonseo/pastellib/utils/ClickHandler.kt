package io.github.yoonseo.pastellib.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent

class ItemHandler : Listener {

    companion object{
        const val CLICK_INTERVAL = 4
        private val clickers = HashMap<Player,Int>()

        fun isKeepClicking(player: Player) : Boolean{
            return clickers[player]?.let { Bukkit.getServer().currentTick - it <= CLICK_INTERVAL } == true
        }
    }
    @EventHandler
    fun onChangeItem(e: PlayerItemHeldEvent){

    }
    //LeftClick only
    @EventHandler
    fun onInteraction(e: PlayerInteractEvent){
        if(e.action.isRightClick) return
        clickLeft(e.player)
    }
    @EventHandler
    fun onEnInteraction(e: PlayerInteractEntityEvent){
        clickLeft(e.player)
    }

    private fun clickLeft(player: Player){
        clickers[player] = Bukkit.getServer().currentTick
    }

}
val Player.isKeepClicking : Boolean
    get() = ItemHandler.isKeepClicking(this)
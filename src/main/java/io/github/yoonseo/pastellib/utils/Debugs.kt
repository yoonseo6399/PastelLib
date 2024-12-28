package io.github.yoonseo.pastellib.utils

import io.papermc.paper.entity.LookAnchor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID



fun log(message: Component) {
    val ju = Bukkit.getPlayer("command_juho")
    if(ju!=null) ju.sendMessage(message)
    else if(message is TextComponent) Bukkit.getLogger().info(message.content())
}
fun log(message : String) = log(Component.text(message))


fun Location.showParticle(particleType: Particle) { world.spawnParticle(particleType,this,1,0.0,0.0,0.0,0.0) }

fun LivingEntity.debug(){
    addPotionEffect(PotionEffect(PotionEffectType.GLOWING,20*10,1,false,false))
    log("${this.name} is Debugged")
    log(this.uniqueId.toComponent(true))
    log(this.location.toComponent(true))
}

fun Block.debug(){
    log(Component.text("Block, ${this.type.name} at ").append(location.toComponent(this,true)))
}


operator fun Component?.plus(other: Component) = this?.append(other) ?: other
internal fun Location.toComponent(data : Any? = null,prefix: Boolean = false) : Component{
    val component = Component.text(String.format("(%.2f, %.2f, %.2f)",this.x,this.y,this.z)).clickEvent(ClickEvent.callback { e ->
        if(e !is Player) return@callback
        e.lookAt(this, LookAnchor.FEET)
        //additional info
        if(data is LivingEntity) data.debug()
        else if(data is Block) data.debug()
    }).hoverEvent(Component.text("Click to look at this location"))
    return if(prefix) Component.text("Location : ") + component else component
}
internal fun UUID.toComponent(prefix : Boolean = false) : Component =
    Component.text("UUID : ").takeIf { prefix } + Component.text(this.toString()).clickEvent(ClickEvent.copyToClipboard(this.toString()))



fun Location.toComponent(prefix: Boolean = false) : Component = toComponent(null,prefix)


data class DebugScope(val commandJuho : Player?)
fun <R : Any?> debug(block : DebugScope.() -> R) {
    val scope = DebugScope(Bukkit.getPlayer("command_juho"))
    try {
        block(scope)
    } catch (e : Exception) {
        log("${e.message}")
        e.printStackTrace()
    }
}
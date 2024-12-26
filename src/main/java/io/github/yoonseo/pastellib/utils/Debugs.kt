package io.github.yoonseo.pastellib.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


fun log(message: String) {
    val ju = Bukkit.getPlayer("command_juho")
    if(ju!=null) ju.sendMessage(message)
    else Bukkit.getLogger().info(message)
}

fun Location.showParticle(particleType: Particle) { world.spawnParticle(particleType,this,1,0.0,0.0,0.0,0.0,0.0) }

fun LivingEntity.debug(){
    addPotionEffect(PotionEffect(PotionEffectType.GLOWING,20*10,1,false,false))
    log("${this.name} is Debugged")
    log("UUID : ${this.uniqueId}")
    log(String.format("Location : (%.2f, %.2f, %.2f)",this.location.x,this.location.y,this.location.z))
}
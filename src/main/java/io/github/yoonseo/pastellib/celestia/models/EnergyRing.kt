package io.github.yoonseo.pastellib.celestia.models

import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.entity.model.*
import io.github.yoonseo.pastellib.utils.tasks.toTicks
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.properties.Delegates
import kotlin.reflect.KProperty
import kotlin.time.Duration


//default Radius 1.5 block
class EnergyRing(val owner: LivingEntity,val energyRegen : Duration) : Model<BlockDisplay>("boss-energy-ring") {
    val RADIUS = 1.6f
    val orbs = mutableListOf<BlockDisplay>()
    var energy by Delegates.observable(0) { _: KProperty<*>, old: Int, new: Int ->
        whenEnergyChanges(old,new)
    }
    override val renderer: ModelRenderer<BlockDisplay>
        get() =  modelRenderer(this){

            val task = TaskModule<BlockDisplay>(0){
                teleport(
                    owner.location.also { it.pitch = 0f } // 위아래 무시
                    .add(owner.location.direction.setY(0).normalize().multiply(-0.7)).add(0.0,1.0,0.0) // 보정
                )
            }
            val regenTask = TaskModule<BlockDisplay>(energyRegen.toTicks().toInt()){
                if(energy in 0..<8) {
                    energy++
                }
            }
            attachModule(task)
            attachModule(regenTask)
            for (display in displays) {
                display.teleportDuration = 1
            }
        }
    fun whenEnergyChanges(old : Int,newValue : Int) {
        if(newValue == old) return
        if(!(0..8).contains(newValue)){
            energy = newValue.coerceIn(0..8)
            debug { commandJuho.sendMessage("energy has inappropriate number , $newValue, coerceIn 0..8 -> $energy") }
            return
        }
        //require((0..8).contains(newValue)) { "range 0..8 does not contain $newValue" }
        if(newValue > old){
            for (index in (old)..<newValue){
                val translation = Vector3f(0f,RADIUS,0f)
                    .rotateZ((index*PI/4).toFloat())
                    .add(-0.15f,-0.15f,-0.15f)

                location.world.spawn(location,BlockDisplay::class.java).apply {
                    block = Material.OCHRE_FROGLIGHT.createBlockData()
                    transformation = Transformation(
                        translation,Quaternionf().identity(),
                        Vector3f(0.3f,0.3f,0.3f),
                        Quaternionf().identity()
                    )
                    teleport(this@EnergyRing.location)
                    this.teleportDuration = 1

                    mainDisplay.addPassenger(this)
                    orbs.add(this)
                }
            }
        }else {
            val removes = orbs.takeLast(old-newValue)
            removes.forEach { mainDisplay.removePassenger(it) }
            orbs.removeAll(removes)
            removes.forEach { it.remove() }
        }
    }
}
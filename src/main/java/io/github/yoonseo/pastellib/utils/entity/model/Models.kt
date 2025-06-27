package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.damage.DamageType
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.properties.Delegates
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.seconds

class SwordDemon(val owner : LivingEntity) : Model<BlockDisplay>("boss1-L"){

    override val renderer: ModelRenderer<BlockDisplay> = modelRenderer(this) {
        teleport(owner.eyeLocation)
        attachModule(SelfPropellingModule(2.0, 100.0))
        val size = SizeModule<BlockDisplay>()
        attachModule(size)
        size.multiplyGlobally(vec = Vector3f(Math.random().toFloat() * 2 + 1, 1f, 1f))
        applyGlobalRotation(Quaternionf().fromAxisAngleDeg(Vector3f(0f, 0f, 1f), (Math.random() * 360).toFloat()))
        attachModule(SimpleCollusionModule())
        attachModule(SingleDamageModule(10.0, owner, DamageType.LIGHTNING_BOLT, true) { it != owner })
    }
}
class LightLaser(val owner: LivingEntity,val loc : Location) : Model<BlockDisplay>("boss-laser-target"){
    override val renderer: ModelRenderer<BlockDisplay> = modelRenderer(this) {
        teleport(loc)
        val sizeModule = SizeModule<BlockDisplay>()
        attachModule(sizeModule)
        val animationModule = AnimationModule<BlockDisplay>()
        animationModule.configureAnimation {
            repeatUntilEnds {  applyGlobalRotation(Quaternionf(AxisAngle4f(-0.1f,0f,1f,0f))) }
            then((2).seconds){
                syncRepeating {
                    sizeModule.multiplyGlobally(1.02,1.0,1.02)
                }.setDeathTime(20*2)
            }
            then((1).seconds){
                LaserBeam(owner,loc).renderer.load(loc)
            }
            then((2).seconds){
                syncRepeating {
                    sizeModule.multiplyGlobally(0.9,1.0,0.9)
                }.setDeathTime(20*2)
            }
            then {
                remove()
            }
        }
        attachModule(animationModule)
        animationModule.animate()
    }
    class LaserBeam(val owner: LivingEntity,val loc : Location) : Model<BlockDisplay>("boss-laser-beam"){

        override val renderer: ModelRenderer<BlockDisplay> = modelRenderer(this) {
            teleport(loc)
            val sizeModule = SizeModule<BlockDisplay>()
            attachModule(sizeModule)
            val animationModule = AnimationModule<BlockDisplay>()
            animationModule.configureAnimation {
                repeatUntilEnds {  applyGlobalRotation(Quaternionf(AxisAngle4f(0.1f,0f,1f,0f))) }
                then((1).seconds){
                    location.world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE,1f,0.9f)
                    location.world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1f,1.2f)
                    syncRepeating {
                        sizeModule.multiplyGlobally(1.1,1.0,1.1)
                    }.setDeathTime(20*1)
                }
                then((1.5).seconds){
                    syncRepeating {
                        sizeModule.multiplyGlobally(0.87,1.0,0.87)
                    }.setDeathTime(20*1)
                }
                then { remove() }
            }
            attachModule(animationModule)
            attachModule(SimpleDamageModule(100,50.0,owner, DamageType.LIGHTNING_BOLT,false) { it != owner })
            animationModule.animate()
        }

    }
}


//default Radius 1.5 block
class EnergyRing(val owner: LivingEntity) : Model<BlockDisplay>("boss-energy-ring") {
    val RADIUS = 1.6f
    val orbs = mutableListOf<BlockDisplay>()
    var energy by Delegates.observable(0) { _: KProperty<*>, old: Int, new: Int ->
        whenEnergyChanges(old,new)
    }
    override val renderer: ModelRenderer<BlockDisplay> get() =  modelRenderer(this){
        val animation = AnimationModule<BlockDisplay>()
        animation.configureAnimation {
            repeatUntilEnds { teleport(owner.location.also { it.pitch = 0f }.add(owner.location.direction.setY(0).normalize().multiply(-0.7)).add(0.0,1.0,0.0)) }
        }
        attachModule(animation)
        animation.animate()
    }
    fun whenEnergyChanges(old : Int,newValue : Int) {
        if(newValue == old) return
        require((0..8).contains(newValue))
        if(newValue > old){
            for (index in (old+1)..(newValue)){
                val translation = Vector3f(0f,RADIUS,0f)
                    .rotateZ((index*PI/4).toFloat())
                    .add(-0.15f,-0.15f,-0.15f)
                location.world.spawn(location,BlockDisplay::class.java).apply {
                    block = Material.OCHRE_FROGLIGHT.createBlockData()
                    transformation = Transformation(translation,Quaternionf().identity(),
                        Vector3f(0.3f,0.3f,0.3f),
                        Quaternionf().identity())
                    teleport(this@EnergyRing.location)
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
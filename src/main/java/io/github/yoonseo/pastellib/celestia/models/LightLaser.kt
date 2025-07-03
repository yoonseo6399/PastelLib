package io.github.yoonseo.pastellib.celestia.models

import io.github.yoonseo.pastellib.utils.entity.model.*
import io.github.yoonseo.pastellib.utils.tasks.TerminationMethod
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.damage.DamageType
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.LivingEntity
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.time.Duration.Companion.seconds

class LightLaser(val owner: LivingEntity) : Model<BlockDisplay>("boss-laser-target"){
    override val renderer: ModelRenderer<BlockDisplay> = ModelRenderer(this)
    override fun initialize(location: Location, renderResult: RenderResult<BlockDisplay>) {
        super.initialize(location, renderResult)

        val sizeModule = SizeModule<BlockDisplay>()
        attachModule(sizeModule)
        val animationModule = AnimationModule<BlockDisplay>()
        animationModule.configureAnimation {
            repeatUntilEnds { applyGlobalRotation(Quaternionf(AxisAngle4f(-0.1f, 0f, 1f, 0f))) }
            then((2).seconds) {
                syncRepeating {
                    sizeModule.multiplyGlobally(1.02, 1.0, 1.02)
                } addTermination TerminationMethod.TimeOut((2).seconds)
            }
            then((1).seconds) {
                location.spawnModel(LaserBeam::class,owner)
            }
            then((2).seconds) {
                syncRepeating {
                    sizeModule.multiplyGlobally(0.9, 1.0, 0.9)
                } addTermination TerminationMethod.TimeOut((2).seconds)
            }
            then {
                remove()
            }
        }
        attachModule(animationModule)
        animationModule.animate()
    }

    class LaserBeam(val owner: LivingEntity) : Model<BlockDisplay>("boss-laser-beam"){

        override val renderer: ModelRenderer<BlockDisplay> = ModelRenderer(this)
        override fun initialize(location: Location, renderResult: RenderResult<BlockDisplay>) {
            super.initialize(location, renderResult)


            val sizeModule = SizeModule<BlockDisplay>()
            attachModule(sizeModule)
            val animationModule = AnimationModule<BlockDisplay>()
            animationModule.configureAnimation {
                repeatUntilEnds { applyGlobalRotation(Quaternionf(AxisAngle4f(0.1f, 0f, 1f, 0f))) }
                then((1).seconds) {
                    location.world.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.9f)
                    location.world.playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 1.2f)
                    syncRepeating {
                        sizeModule.multiplyGlobally(1.1, 1.0, 1.1)
                    } addTermination TerminationMethod.TimeOut((1).seconds)
                }
                then((1.5).seconds) {
                    syncRepeating {
                        sizeModule.multiplyGlobally(0.87, 1.0, 0.87)
                    } addTermination TerminationMethod.TimeOut((1.5).seconds)
                }
                then { remove() }
            }
            attachModule(animationModule)
            attachModule(SimpleDamageModule(100, 100.0, owner, DamageType.LIGHTNING_BOLT, false) { it != owner })
            animationModule.animate()
        }

    }
}
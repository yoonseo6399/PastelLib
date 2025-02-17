package io.github.yoonseo.pastellib.utils.entity.blockDisplays.particles

import io.github.yoonseo.pastellib.utils.entity.blockDisplays.DisplayParticle
import io.github.yoonseo.pastellib.utils.cloneSetScale
import io.github.yoonseo.pastellib.utils.cloneSetTranslation
import io.github.yoonseo.pastellib.utils.debug
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.log2

class FloorBloodParticle(private var bloodAmount: Double) : DisplayParticle<BlockDisplay>() {
    val radius : Float
        get() = (log2(bloodAmount.toFloat()+1).takeIf { it >= 0 } ?: 0.5f)/2
    override fun spawn(location: Location) {
        display = location.world.spawn(location.setDirection(Vector(0,0,1)),BlockDisplay::class.java).apply {
            interpolationDuration = 10
            teleportDuration = 10
            block = Material.RED_CONCRETE.createBlockData()
            transformation = Transformation(
                Vector3f(-radius,0f,-radius),
                Quaternionf(),
                Vector3f(radius*2, 0.01F,radius*2),
                Quaternionf()
            )
        }
        super.spawn(location)
    }

    override fun tickedBehavior() {
        debug {
            scoreboard("blood amount",bloodAmount)
        }
        if(time >= bloodAmount*10) {
            bloodAmount-=0.1
            if(bloodAmount <= 0) return remove()
            display.transformation = display.transformation
                .cloneSetScale(Vector3f(radius*2, 0.01F,radius*2))
                .cloneSetTranslation(Vector3f(-radius,0f,-radius))
        }
    }

    override fun clone(): DisplayParticle<BlockDisplay> = FloorBloodParticle(bloodAmount)
}
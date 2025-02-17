package io.github.yoonseo.pastellib.utils.entity.blockDisplays

import io.github.yoonseo.pastellib.utils.cloneSetScale
import io.github.yoonseo.pastellib.utils.cloneSetTranslation
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.particles.LightParticle
import io.github.yoonseo.pastellib.utils.lookVector
import io.github.yoonseo.pastellib.utils.selectors.Ray
import io.github.yoonseo.pastellib.utils.selectors.rayTo
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.util.Vector
import org.joml.Math
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.joml.Vector3f

class Laser(spawnLocation : Location, length : Float, size : Float, inner : BlockData, outer : BlockData, vararg options: LaserOptions) {

    private val inner : AdvancedBlockDisplay
    private val outer : AdvancedBlockDisplay
    private lateinit var selfTickingTask : Promise
    val isDead : Boolean
        get() = inner.isDead
    val ray : Ray
        get() = inner.location.rayTo(inner.location.add(direction.multiply(length)))
    val direction : Vector
        get() = this.inner.location.direction.normalize()
    var length : Float = length
        set(value) {
            field = value
            updateTransformation()
        }
    var size : Float = size
        set(value) {
            field = value
            updateTransformation()
        }
    //Z 축을 레이져 길이로 설정
    init {
        this.inner = AdvancedBlockDisplay.spawn(spawnLocation).apply {
            block = inner
            val size2 = (size * 0.75).toFloat()
            transformation = TransformationBuilder().scale(size2,size2,length).translate(-(size2/2),-(size2/2),0f).build()
            teleportDuration = 5
            interpolationDuration = 1
        }
        this.outer = AdvancedBlockDisplay.spawn(spawnLocation).apply {
            block = outer
            transformation = TransformationBuilder().scale(size,size,length).translate(-size/2,-size/2,0f).build()
            teleportDuration = 5
            interpolationDuration = 1
        }
        selfTickingTask = syncRepeating {
            if (options.isNotEmpty()) for (option in options) when (option) {
                LaserOptions.RotateZ -> rotate(Quaternionf().fromAxisAngleDeg(0f, 0f, 1f, 10f))
                LaserOptions.FOLLOW -> debug {
                    this@Laser.setDirection(this@Laser.inner.location lookVector commandJuho.eyeLocation)
                }

                LaserOptions.LENGTH_FOLLOW -> debug {
                    this@Laser.length = commandJuho.eyeLocation.distance(this@Laser.inner.location).toFloat()
                }

                LaserOptions.LIGHT_EMIT -> {
                    for (loc in ray) {
                        if (Math.random() <= 0.1) loc.add(randomVector().multiply(0.2)).showParticle(LightParticle())
                    }
                }
            }
            if (this@Laser.inner.isDead) remove()
        }
    }

    fun teleport(location : Location){
        inner.teleport(location)
        outer.teleport(location)
    }
    private fun updateTransformation(){

        val size2 = (size * 0.75).toFloat()
        val matrix = inner.transformation.toMatrix4f()
        val rotationMatrix = Matrix3f()
        matrix.get3x3(rotationMatrix)
        val innerT = rotationMatrix.transform(Vector3f(-(size2 / 2), -(size2 / 2), 0f))
        val outerT = rotationMatrix.transform(Vector3f(-size / 2, -size / 2, 0f))

        inner.transformation = inner.transformation.cloneSetScale(Vector3f(size2, size2, length)).cloneSetTranslation(innerT)
        outer.transformation = outer.transformation.cloneSetScale(Vector3f(size, size, length)).cloneSetTranslation(outerT)
    }
    fun setDirection(direction : Vector){
        teleport(inner.location.setDirection(direction))
    }
    private fun rotate(quaternionf: Quaternionf){
        inner.rotate(quaternionf)
        outer.rotate(quaternionf)
    }
    fun remove(){
        inner.remove()
        outer.remove()
        selfTickingTask.cancel()
    }

    fun rayTrace() {

    }

}

enum class LaserOptions{
    RotateZ,FOLLOW,LENGTH_FOLLOW,LIGHT_EMIT
}
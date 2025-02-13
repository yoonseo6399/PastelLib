package io.github.yoonseo.pastellib.utils.blockDisplays

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.reflect.KClass

open class AdvancedBlockDisplay(location: Location, initializer: BlockDisplay.() -> Unit = {}) : BlockDisplay by location.world.spawn(location,
    BlockDisplay::class.java) {
    init {
        this.initializer()
    }
    fun rotate(quaternionf: Quaternionf){
        var translation = transformation.translation // 원하는 회전 중심 좌표
        val matrix = transformation.toMatrix4f()
        val rotation = Matrix3f()
        Matrix4f(matrix).invert().get3x3(rotation)
        translation = rotation.transform(translation)

        matrix.rotate(quaternionf)
        val rotationMatrix = Matrix3f()
        matrix.get3x3(rotationMatrix)
        matrix.setTranslation(rotationMatrix.transform(translation))
        setTransformationMatrix(matrix)
    }
    fun debug(){
        val x = Vector3f(1f, 0f, 0f)
        val y = Vector3f(0f, 1f, 0f)
        val z = Vector3f(0f, 0f, 1f)
        val trans = transformation.translation
        val transformedX = x.rotate(transformation.leftRotation)
        val transformedY = y.rotate(transformation.leftRotation)
        val transformedZ = z.rotate(transformation.leftRotation)
        val X = location.add(transformedX.x.toDouble(), transformedX.y.toDouble(), transformedX.z.toDouble())
        val Y = location.add(transformedY.x.toDouble(), transformedY.y.toDouble(), transformedY.z.toDouble())
        val Z = location.add(transformedZ.x.toDouble(), transformedZ.y.toDouble(), transformedZ.z.toDouble())
        io.github.yoonseo.pastellib.utils.debug {
            X.world.spawnParticle(Particle.DUST, X, 1, 0.0, 0.0, 0.0, Particle.DustOptions(Color.RED, 1f))
            Y.world.spawnParticle(Particle.DUST, Y, 1, 0.0, 0.0, 0.0, Particle.DustOptions(Color.GREEN, 1f))
            Z.world.spawnParticle(Particle.DUST, Z, 1, 0.0, 0.0, 0.0, Particle.DustOptions(Color.BLUE, 1f))
            X.world.spawnParticle(
                Particle.DUST,
                X.set(
                    trans.x.toDouble() + location.x,
                    trans.y.toDouble() + location.y,
                    trans.z.toDouble() + location.z
                ),
                1,
                0.0,
                0.0,
                0.0,
                Particle.DustOptions(Color.YELLOW, 1f)
            )
        }
    }
}

fun World.spawn(location : Location, clazz : KClass<AdvancedBlockDisplay>) = AdvancedBlockDisplay(location)
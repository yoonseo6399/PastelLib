package io.github.yoonseo.pastellib.utils.blockDisplays
import io.github.yoonseo.pastellib.utils.cloneSetScale
import io.github.yoonseo.pastellib.utils.cloneSetTranslation
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.lookVector
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.Vector
import org.joml.*
import kotlin.reflect.KClass




enum class LaserOptions{
    RotateZ,FOLLOW,LENGTH_FOLLOW
}
class Laser(spawnLocation : Location, length : Float, size : Float, inner : BlockData, outer : BlockData, vararg options: LaserOptions) {

    private val inner : AdvancedBlockDisplay
    private val outer : AdvancedBlockDisplay

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
        this.inner = spawnLocation.world.spawn(spawnLocation,AdvancedBlockDisplay::class).apply {
            block = inner
            val size2 = (size * 0.75).toFloat()
            transformation = TransformationBuilder().scale(size2,size2,length).translate(-(size2/2),-(size2/2),0f).build()
            teleportDuration = 5
            interpolationDuration = 1
        }
        this.outer = spawnLocation.world.spawn(spawnLocation,AdvancedBlockDisplay::class).apply {
            block = outer
            transformation = TransformationBuilder().scale(size,size,length).translate(-size/2,-size/2,0f).build()
            teleportDuration = 5
            interpolationDuration = 1
        }
        if(options.isNotEmpty()) syncRepeating {
            for(option in options) when(option){
                LaserOptions.RotateZ -> rotate(Quaternionf().fromAxisAngleDeg(0f, 0f, 1f, 10f))
                LaserOptions.FOLLOW -> debug {
                    this@Laser.setDirection(commandJuho.eyeLocation lookVector this@Laser.inner.location)
                }
                LaserOptions.LENGTH_FOLLOW -> debug {
                    this@Laser.length = commandJuho.eyeLocation.distance(this@Laser.inner.location).toFloat()
                }
            }
        }
    }

    fun teleport(location : Location){
        inner.teleport(location)
        outer.teleport(location)
    }
    fun updateTransformation(){

        val size2 = (size * 0.75).toFloat()
        val matrix = inner.transformation.toMatrix4f()
        val rotationMatrix = Matrix3f()
        matrix.get3x3(rotationMatrix)
        val innerT = rotationMatrix.transform(Vector3f(-(size2/2),-(size2/2),0f))
        val outerT = rotationMatrix.transform(Vector3f(-size/2,-size/2,0f))

        inner.transformation = inner.transformation.cloneSetScale(Vector3f(size2,size2,length)).cloneSetTranslation(innerT)
        outer.transformation = outer.transformation.cloneSetScale(Vector3f(size,size,length)).cloneSetTranslation(outerT)
    }
    fun setDirection(direction : Vector){
        teleport(inner.location.setDirection(direction.multiply(-1)))
    }
    private fun rotate(quaternionf: Quaternionf){
        inner.rotate(quaternionf)
        outer.rotate(quaternionf)
    }


    fun rayTrace() {

    }
}



open class AdvancedBlockDisplay(location: Location,initializer: BlockDisplay.() -> Unit = {}) : BlockDisplay by location.world.spawn(location,BlockDisplay::class.java) {
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
        val x = Vector3f(1f,0f,0f)
        val y = Vector3f(0f,1f,0f)
        val z = Vector3f(0f,0f,1f)
        val trans = transformation.translation
        val transformedX = x.rotate(transformation.leftRotation)
        val transformedY = y.rotate(transformation.leftRotation)
        val transformedZ = z.rotate(transformation.leftRotation)
        val X = location.add(transformedX.x.toDouble(), transformedX.y.toDouble(), transformedX.z.toDouble())
        val Y = location.add(transformedY.x.toDouble(), transformedY.y.toDouble(), transformedY.z.toDouble())
        val Z = location.add(transformedZ.x.toDouble(), transformedZ.y.toDouble(), transformedZ.z.toDouble())
        io.github.yoonseo.pastellib.utils.debug {
            X.world.spawnParticle(Particle.DUST,X,1,0.0,0.0,0.0,DustOptions(Color.RED,  1f))
            Y.world.spawnParticle(Particle.DUST,Y,1,0.0,0.0,0.0,DustOptions(Color.GREEN,1f))
            Z.world.spawnParticle(Particle.DUST,Z,1,0.0,0.0,0.0,DustOptions(Color.BLUE, 1f))
            X.world.spawnParticle(Particle.DUST,X.set(trans.x.toDouble()+location.x,trans.y.toDouble()+location.y,trans.z.toDouble()+location.z),1,0.0,0.0,0.0,DustOptions(Color.YELLOW,1f))
        }
    }
}
fun World.spawn(location : Location, clazz : KClass<AdvancedBlockDisplay>) = AdvancedBlockDisplay(location)


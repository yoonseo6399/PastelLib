package io.github.yoonseo.pastellib.model

import io.github.yoonseo.pastellib.Updatable
import io.github.yoonseo.pastellib.serializer.GlobalSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.util.Transformation
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.io.Closeable

@Serializable
abstract class Model<E : Display> : Closeable,Updatable{
    companion object {

        //의미 없음으로 판정
//        fun fromModelEntities(entity : Display) {
//            val main = entity.vehicle ?: entity
//
//            var target = if(main.passengers.isEmpty()) listOf( main ) else main.passengers
//            target.forEach {
//                if(it is BlockDisplay){
//                    val matrix = Matrix4f().identity()
//                    it.transformation.apply {
//                        matrix.translate(translation).rotate(leftRotation).scale(scale).rotate(rightRotation)
//                    }
//                    BlockModelPart
//                    BlockModelPart.serializer().serialize()
//                }
//            }
//        }
    }

    var parts = HashMap<String,ModelPart<E>>()
    abstract val location : Location

    override fun close() {
        parts.forEach { (_, part) -> part.close() }
    }

    override fun update() {
        parts.forEach { (_, part) ->
            part.entity.teleport(location)
            part.update()
        }
    }

    fun add(id : String,modelPart: ModelPart<E>){
        parts += id to modelPart
    }
    fun add(modelPart: ModelPart<E>){
        parts += parts.size.toString() to modelPart
    }
    operator fun plusAssign(pair: Pair<String,ModelPart<E>>){
        add(pair.first,pair.second)
    }
    operator fun plusAssign(part: ModelPart<E>){
        add(part)
    }
}
@Serializable
abstract class ModelPart<out E : Display> : Closeable,Updatable{
    @Contextual
    //update required
    var matrix : Matrix4f = Matrix4f()
    abstract val entity : E
    val transformation : Transformation
        get() = transformFromMatrix(matrix)
    var translation : Vector3f
        get() = Vector3f().also { matrix.getTranslation(it) }
        set(value) { matrix.setTranslation(value) }

    override fun close() {
        entity.remove()
    }
    fun addTranslation(x : Number, y : Number, z : Number){
        translation =  translation.add(x.toFloat(),y.toFloat(),z.toFloat())
    }

}
@Serializable
abstract class BlockModelPart : ModelPart<BlockDisplay>(){

}

fun transformFromMatrix(matrix: Matrix4f): Transformation {
    val translation = matrix.getTranslation(Vector3f())
    val rotation = matrix.getUnnormalizedRotation(Quaternionf())
    val scale = matrix.getScale(Vector3f())

    return Transformation(translation, rotation, scale, Quaternionf())
}
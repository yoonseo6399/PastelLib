package io.github.yoonseo.pastellib.model

import io.github.yoonseo.pastellib.Updatable
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.Transformation
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.io.Closeable

abstract class Model<E : Entity> : Closeable,Updatable{
    var parts = HashMap<Any,ModelPart<E>>()
    val location : Location
        get() = parts.values.first().entity.location

    override fun close() {
        parts.forEach { (_, part) -> part.close() }
    }

    override fun update() {
        parts.forEach { (_, part) -> part.update() }
    }
    //abstract fun







    fun add(id : Any,modelPart: ModelPart<E>){
        parts += id to modelPart
    }
    fun add(modelPart: ModelPart<E>){
        parts += parts.size to modelPart
    }
    operator fun plusAssign(pair: Pair<Any,ModelPart<E>>){
        add(pair.first,pair.second)
    }
    operator fun plusAssign(part: ModelPart<E>){
        add(part)
    }
}
abstract class ModelPart<out E : Entity> : Closeable,Updatable{
    var matrix : Matrix4f = Matrix4f()
    abstract val entity : E
    val transformation : Transformation
        get() = transformFromMatrix(matrix)


    override fun close() {
        entity.remove()
    }
}
fun transformFromMatrix(matrix: Matrix4f): Transformation {
    val translation = matrix.getTranslation(Vector3f())
    val rotation = matrix.getUnnormalizedRotation(Quaternionf())
    val scale = matrix.getScale(Vector3f())

    return Transformation(translation, rotation, scale, Quaternionf())
}
package io.github.yoonseo.pastellib.utils.entity.blockDisplays

import org.bukkit.util.Transformation
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

fun Transformation.toMatrix4f(): Matrix4f = Matrix4f()
    .translate(translation)  // T(P)
    .rotate(leftRotation)  // R
    .scale(scale)
    .rotate(rightRotation)

class TransformationBuilder(){
    var translation : Vector3f = Vector3f()
    var leftRotation : Quaternionf = Quaternionf()
    var scale : Vector3f = Vector3f(1f, 1f, 1f)
    var rightRotation : Quaternionf = Quaternionf()

    fun translate(x: Float, y: Float, z: Float) = apply { translation = Vector3f(x, y, z) }
    fun translate(vector: Vector3f) = apply { translation = vector }
    fun leftRotate(x: Float, y: Float, z: Float, angle: Float) = apply { leftRotation = leftRotation.fromAxisAngleDeg(x, y, z, angle) }
    fun leftRotate(quaternionf: Quaternionf) = apply { leftRotation = quaternionf }
    fun scale(x: Float, y: Float, z: Float) = apply { scale = Vector3f(x, y, z) }
    fun scale(vector: Vector3f) = apply { scale = vector }
    fun rightRotate(x: Float, y: Float, z: Float, angle: Float) = apply { rightRotation = rightRotation.fromAxisAngleDeg(x, y, z, angle) }
    fun rightRotate(quaternionf: Quaternionf) = apply { rightRotation = quaternionf }


    fun build() : Transformation = Transformation(translation, leftRotation, scale, rightRotation)
}

fun Matrix4f.getRotation3x3() : Matrix3f {
    val rotationMatrix = Matrix3f()
    get3x3(rotationMatrix)
    return rotationMatrix
}


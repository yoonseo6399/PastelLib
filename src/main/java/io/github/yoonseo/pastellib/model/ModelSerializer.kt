package io.github.yoonseo.pastellib.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Matrix4f
import java.nio.FloatBuffer

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Matrix4f::class)
object Matrix4fSerializer : KSerializer<Matrix4f>{
    override fun serialize(encoder: Encoder, value: Matrix4f) {
        FloatArray(16).also { value[it] }.forEach {
            encoder.encodeFloat(it)
        }
    }

    override fun deserialize(decoder: Decoder): Matrix4f {
        val floatbff = FloatBuffer.allocate(16)
        repeat(16){
            floatbff.put(decoder.decodeFloat())
        }
        return Matrix4f(floatbff)
    }
}
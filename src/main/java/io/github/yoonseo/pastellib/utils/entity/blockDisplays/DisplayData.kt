package io.github.yoonseo.pastellib.utils.entity.blockDisplays

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

@Serializable
sealed class DisplayData(@Transient open val transformation : Transformation = TransformationBuilder().build()){
    @Serializable
    data class Text(
        @Contextual override val transformation : Transformation = TransformationBuilder().build(),
        val text : Component,
        @Contextual val backgroundColor : Color?
    ) : DisplayData(transformation)
    @Serializable
    data class Block(
        @Contextual override val transformation : Transformation = TransformationBuilder().build(),
        val blockData: BlockData,
        val interpolationDuration : Int = 0,
        val teleportDuration : Int = 0
    ) : DisplayData(transformation)

}
fun BlockDisplay.extractData() : DisplayData{
    return DisplayData.Block(transformation,block,interpolationDuration,teleportDuration)
}
fun TextDisplay.extractData() : DisplayData{
    return DisplayData.Text(transformation, text(), backgroundColor)
}
val LASER : List<DisplayData> = listOf(
    DisplayData.Block(
        TransformationBuilder().scale(1f, 1f, 1f).translate(-0.5f,-0.5f,-0.5f).build(),
        Material.WHITE_STAINED_GLASS.createBlockData(),
        1,
        10
    ),
    DisplayData.Block(
        TransformationBuilder().scale(0.5f, 0.5f, 0.5f).translate(-0.25f,-0.25f,-0.25f).build(),
        Material.WHITE_CONCRETE.createBlockData(),
        1,
        10
    )
)


object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Color") {
        element<Int>("r")
        element<Int>("g")
        element<Int>("b")
        element<Int>("a")
    }

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.red)
            encodeIntElement(descriptor, 1, value.green)
            encodeIntElement(descriptor, 2, value.blue)
            encodeIntElement(descriptor, 3, value.alpha)
        }
    }

    override fun deserialize(decoder: Decoder): Color {
        return decoder.decodeStructure(descriptor) {
            var r = 0
            var g = 0
            var b = 0
            var a = 255  // 기본값 (불투명)

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> r = decodeIntElement(descriptor, index)
                    1 -> g = decodeIntElement(descriptor, index)
                    2 -> b = decodeIntElement(descriptor, index)
                    3 -> a = decodeIntElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            Color.fromARGB(a, r, g, b)
        }
    }
}

object BlockDataSerializer : KSerializer<BlockData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BlockData") {
        element<Material>("material")
        element<Byte>("data")
    }

    override fun serialize(encoder: Encoder, value: BlockData) {
        encoder.encodeString(value.getAsString(true))
    }

    override fun deserialize(decoder: Decoder): BlockData {
        return Bukkit.getServer().createBlockData(decoder.decodeString())
    }
}
object TransformationSerializer : KSerializer<Transformation> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Transformation") {
        element<Vector3f>("translation")
        element<Quaternionf>("leftRotation")
        element<Vector3f>("Vector3f")
        element<Quaternionf>("rightRotation")
    }

    override fun serialize(encoder: Encoder, value: Transformation) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, Vector3fSerializer, value.translation)
            encodeSerializableElement(descriptor, 1, QuaternionfSerializer, value.leftRotation)
            encodeSerializableElement(descriptor, 2, Vector3fSerializer, value.scale)
            encodeSerializableElement(descriptor, 3, QuaternionfSerializer, value.rightRotation)
        }
    }
    override fun deserialize(decoder: Decoder): Transformation {
        return decoder.decodeStructure(descriptor) {
            var translation = Vector3f()
            var leftRotation = Quaternionf()
            var scale = Vector3f()
            var rightRotation = Quaternionf()

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> translation = decodeSerializableElement(descriptor, index, Vector3fSerializer)
                    1 -> leftRotation = decodeSerializableElement(descriptor, index, QuaternionfSerializer)
                    2 -> scale = decodeSerializableElement(descriptor, index, Vector3fSerializer)
                    3 -> rightRotation = decodeSerializableElement(descriptor, index, QuaternionfSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            // Transformation 객체를 생성 (이미 존재하는 API를 사용)
            Transformation(translation, leftRotation, scale, rightRotation)
        }
    }
}

object QuaternionfSerializer : KSerializer<Quaternionf> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Quaternionf") {
            element<Float>("x")
            element<Float>("y")
            element<Float>("z")
            element<Float>("w")
        }

    override fun serialize(encoder: Encoder, value: Quaternionf) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
            encodeFloatElement(descriptor, 2, value.z)
            encodeFloatElement(descriptor, 3, value.w)
        }
    }

    override fun deserialize(decoder: Decoder): Quaternionf {
        return decoder.decodeStructure(descriptor) {
            var x = 0f
            var y = 0f
            var z = 0f
            var w = 1f

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x = decodeFloatElement(descriptor, index)
                    1 -> y = decodeFloatElement(descriptor, index)
                    2 -> z = decodeFloatElement(descriptor, index)
                    3 -> w = decodeFloatElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            Quaternionf(x, y, z, w)
        }
    }
}

object Vector3fSerializer : KSerializer<Vector3f> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Vector3f") {
            element<Float>("x")
            element<Float>("y")
            element<Float>("z")
        }

    override fun serialize(encoder: Encoder, value: Vector3f) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
            encodeFloatElement(descriptor, 2, value.z)
        }
    }

    override fun deserialize(decoder: Decoder): Vector3f {
        return decoder.decodeStructure(descriptor) {
            var x = 0f
            var y = 0f
            var z = 0f

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x = decodeFloatElement(descriptor, index)
                    1 -> y = decodeFloatElement(descriptor, index)
                    2 -> z = decodeFloatElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            Vector3f(x, y, z)
        }
    }
}

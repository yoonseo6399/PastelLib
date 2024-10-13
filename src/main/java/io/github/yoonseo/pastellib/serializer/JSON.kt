package io.github.yoonseo.pastellib.serializer

import io.github.yoonseo.pastellib.model.Matrix4fSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object GlobalSerializer {
    val json = Json {
        serializersModule = SerializersModule {
            contextual(Matrix4fSerializer)
        }
    }
}
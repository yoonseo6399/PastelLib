package io.github.yoonseo.pastellib.utils.entity.blockDisplays

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.data.BlockData
import org.bukkit.util.Transformation

sealed class DisplayData(open val transformation : Transformation = TransformationBuilder().build()){
    data class Text(
        override val transformation : Transformation = TransformationBuilder().build(),
        val text : Component,
        val backgroundColor : Color
    ) : DisplayData(transformation)

    data class Block(
        override val transformation : Transformation = TransformationBuilder().build(),
        val blockData: BlockData,
        val interpolationDuration : Int = 0,
        val teleportDuration : Int = 0
    ) : DisplayData(transformation)
}
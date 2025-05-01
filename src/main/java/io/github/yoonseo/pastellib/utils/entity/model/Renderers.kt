package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Vector

interface Renderer<T,R> {
    fun render(location: Location, data : T) : R
}

class ModelRenderer<T : Display>(val model : Model<T>) : Renderer<List<DisplayData>, Model<T>> {
    var customRendering : ((Model<T>) -> Unit)? = null
    fun load(location: Location) : Model<T> {
        val data = PastelLib.modelFileManager.loadModelData(model.name)
        require(data.isNotEmpty()) { "that name of model is not exist" }
        return render(location,data)
    }
    override fun render(location: Location, data: List<DisplayData>): Model<T> {
        val main = location.world.spawn(location.clone().setDirection(Vector(0,0,1)), BlockDisplay::class.java)
        for (displayData in data) {
            main.addPassenger(DisplayRenderer().render(location.clone().setDirection(Vector(0,0,1)),displayData))
        }
        model.mainDisplay = main
        model.displayData = data
        customRendering?.invoke(model)
        return model
    }
}
fun <T : Display> modelRenderer(model : Model<T>,whenRendering : Model<T>.() -> Unit): ModelRenderer<T> {
    return ModelRenderer(model).also { it.customRendering = whenRendering }
}

class DisplayRenderer : Renderer<DisplayData, Display> {
    override fun render(location: Location, data: DisplayData): Display {
        return when(data){
            is DisplayData.Text -> TextDisplayRenderer.render(location, data)
            is DisplayData.Block -> BlockDisplayRenderer.render(location, data)
        }
    }
}

object BlockDisplayRenderer : Renderer<DisplayData.Block, BlockDisplay> {
    override fun render(location: Location, data: DisplayData.Block): BlockDisplay {
        return location.world.spawn(location,BlockDisplay::class.java).apply {
            block = data.blockData
            transformation = data.transformation
            teleportDuration = data.teleportDuration
            interpolationDuration = data.interpolationDuration
        }
    }
}

object TextDisplayRenderer : Renderer<DisplayData.Text, TextDisplay> {
    override fun render(location: Location, data: DisplayData.Text): TextDisplay {
        return location.world.spawn(location, TextDisplay::class.java).apply {
            text(data.text)
            backgroundColor = data.backgroundColor
            //brightness = Display.Brightness(10, 10)
            //textOpacity = 0
            //interpolationDuration = 5
        }
    }
}
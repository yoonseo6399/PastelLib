package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.PastelLib
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

interface Renderer<T,R> {
    fun render(location: Location, data : T) : R
}

class ModelRenderer<T : Display> : Renderer<List<DisplayData>, Model<T>> {

    fun load(location: Location, name : String) : Model<T> {
        val data = PastelLib.modelFileManager.loadModelData(name)
        return render(location,data)
    }
    override fun render(location: Location, data: List<DisplayData>): Model<T> {
        val main = location.world.spawn(location, BlockDisplay::class.java)
        for (displayData in data) {
            main.addPassenger(DisplayRenderer().render(location,displayData))
        }
        return Model(main, data)
    }
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
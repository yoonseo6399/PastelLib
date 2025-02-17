package io.github.yoonseo.pastellib.utils.entity.blockDisplays

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

abstract class ColorableParticle(protected var color: Color) : DisplayParticle<TextDisplay>() {

    override fun spawn(location: Location) {
        super.spawn(location)
        display = location.world.spawn(location, TextDisplay::class.java).apply {
            text(Component.text("  "))
            backgroundColor = color
            brightness = Display.Brightness(10, 10)
            textOpacity = 0
            interpolationDuration = 5
            billboard = Display.Billboard.CENTER
            teleportDuration = 5
        }
    }
    protected fun updateColor(color : Color) {
        this.color = color
        display.backgroundColor = color
    }
    override fun remove(){
        task.cancel()
        display.remove()
    }
}
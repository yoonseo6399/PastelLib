package io.github.yoonseo.pastellib.utils.blockDisplays.particles

import io.github.yoonseo.pastellib.utils.blockDisplays.DisplayParticle
import io.github.yoonseo.pastellib.utils.blockDisplays.randomVector
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Vector
import org.w3c.dom.Text

class NumberDisplayParticle(var number: Double) : DisplayParticle<TextDisplay>() {
    override val expiresAt: Int = 30
    override fun spawn(location: Location) {
        super.spawn(location)
        val text = Component.text(String.format("%.1f", number)).color(NamedTextColor.YELLOW).let { if (number>=10) it.color(NamedTextColor.RED) else it }
        display = location.world.spawn(location,TextDisplay::class.java).apply {
            text(text)
            teleportDuration = 3
            billboard = Display.Billboard.CENTER
            backgroundColor = Color.BLACK.setAlpha(0)
            textOpacity = (-1).toByte()
        }
        velocity = randomVector().multiply(0.2)
    }

    override fun tickedBehavior() {
        if(time >= 5) velocity = Vector()
        if(time >= 10) display.textOpacity = (display.textOpacity - 10).toByte()
    }
    override fun clone() = NumberDisplayParticle(number)
}
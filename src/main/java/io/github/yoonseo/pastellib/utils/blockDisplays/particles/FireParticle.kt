package io.github.yoonseo.pastellib.utils.blockDisplays.particles

import io.github.yoonseo.pastellib.utils.blockDisplays.DisplayParticle
import io.github.yoonseo.pastellib.utils.blockDisplays.randomNegativedInclude
import io.github.yoonseo.pastellib.utils.debug
import io.github.yoonseo.pastellib.utils.interpolate
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

open class FireParticle : DisplayParticle<TextDisplay>(){
    lateinit var startLoc : Location

    override fun spawn(location: Location) {
        startLoc = location
        display = location.world.spawn(location, TextDisplay::class.java).apply {
            text(Component.text("  "))
            backgroundColor = Color.WHITE
            brightness = Display.Brightness(10, 10)
            textOpacity = 0
            interpolationDuration = 5
            billboard = Display.Billboard.CENTER
            teleportDuration = 5
            transformation = Transformation(
                Vector3f(0f, 0.25f, 0f),
                Quaternionf().fromAxisAngleDeg(0f, 0f, 1f, (Math.random() * 360).toFloat()),
                Vector3f(0.5f),
                Quaternionf()
            )
        }
        velocity = Vector(randomNegativedInclude() / 40, randomNegativedInclude() / 40, randomNegativedInclude() / 40)
        task = syncRepeating {
            display.teleport(display.location.clone().add(velocity))
            tickedBehavior()
            time++
        }
    }
    override fun tickedBehavior(){
        if(time>=40) return remove()
        display.textOpacity = (display.textOpacity.toInt() + 5).toByte()
        if(display.backgroundColor != null){
            if(time in 0..10) display.backgroundColor = display.backgroundColor!!.interpolate(Color.YELLOW.setAlpha(150),0.2)
            if(time in 10..25) display.backgroundColor = display.backgroundColor!!.interpolate(Color.RED.setAlpha(150),0.15)
            if(time in 25..40) display.backgroundColor = display.backgroundColor!!.interpolate(Color.BLACK.setAlpha(50),0.1)
            debug {
                scoreboard("Particle RED", display.backgroundColor!!.red)
                //commandJuho?.sendMessage(c.alpha.toString())
            }
        }
        display.transformation = Transformation(
            Vector3f(0f, 0.25f, 0f),
            Quaternionf().fromAxisAngleDeg(0f, 0f, 1f, (Math.random() * 360).toFloat()),
            Vector3f(0.5f),
            Quaternionf()
        )
        //velocity.add(Vector(randomNegativedInclude()/2000,0.0008+ randomNegativedInclude()/2000, randomNegativedInclude()/2000))
        if(time>=5){
            velocity.add(display.location.toVector().subtract(startLoc.clone().add(0.0,2.0,0.0).toVector()).normalize().multiply(-0.02).setY(0.005))
        }

    }
    override fun remove(){
        task.cancel()
        display.remove()
    }

    override fun clone() = FireParticle()
}
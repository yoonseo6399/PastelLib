package io.github.yoonseo.pastellib.utils.entity.blockDisplays.particles

import io.github.yoonseo.pastellib.utils.entity.blockDisplays.ColorableParticle
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.randomNegativedInclude
import io.github.yoonseo.pastellib.utils.interpolate
import io.github.yoonseo.pastellib.utils.whenR
import org.bukkit.Color
import org.bukkit.util.Vector


val LIGHT: Color = Color.fromARGB(48,245,238,39)

class LightParticle : ColorableParticle(LIGHT){
    override val expiresAt: Int = 10
    init {
        velocity = (Vector(randomNegativedInclude()/100, (Math.random()-1)/100, randomNegativedInclude()/100))
    }
    override fun tickedBehavior() {
        whenR(time){
            inRange(1..10){
                updateColor(color.interpolate(Color.WHITE.setAlpha(20),0.1))
            }
        }
    }

    override fun clone(): LightParticle {
        return LightParticle()
    }
}
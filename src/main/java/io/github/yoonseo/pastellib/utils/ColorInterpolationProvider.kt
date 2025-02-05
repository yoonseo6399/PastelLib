package io.github.yoonseo.pastellib.utils

import org.bukkit.Bukkit
import org.bukkit.Color

class ColorInterpolationProvider(start : Color, to : Color, val steps : Int = 100) {
    var now = start
    val rStep = (to.red - start.red) / steps
    val gStep = (to.green - start.green) / steps
    val bStep = (to.blue - start.blue) / steps
    val aStep = (to.alpha - start.alpha) / steps
    var step = 0
    fun next(stepSize : Int = 1) : Color {
        if(step > steps) {
            Bukkit.getLogger().warning("ColorInterpolationProvider exhausted. Current step: $step")
            return now
        }
        step += stepSize
        now = Color.fromARGB(
            now.alpha+aStep*stepSize,
            now.red+rStep*stepSize,
            now.green+gStep*stepSize,
            now.blue+bStep*stepSize
        )
        return now
    }

}
fun Color.interpolateProvider(to : Color, steps : Int) : ColorInterpolationProvider = ColorInterpolationProvider(this,to,steps)
fun Color.interpolate(to : Color, intensity : Double): Color {
    val rStep = (to.red - red) * intensity
    val gStep = (to.green - green) *intensity
    val bStep = (to.blue - blue) *intensity
    val aStep = (to.alpha - alpha) *intensity
    return Color.fromARGB(alpha+aStep.toInt(),red+rStep.toInt(), green+gStep.toInt(), blue+bStep.toInt())
}
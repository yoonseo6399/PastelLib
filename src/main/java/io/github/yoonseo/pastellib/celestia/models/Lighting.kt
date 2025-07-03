package io.github.yoonseo.pastellib.celestia.models

import io.github.yoonseo.pastellib.utils.entity.model.*
import io.github.yoonseo.pastellib.utils.entity.particle.randomVector
import io.github.yoonseo.pastellib.utils.tasks.later
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

class Lighting(val owner : Player,val spreadPower : Int,val direction: Vector,val straightness : Double = 4.0,val size : Double = 1.0) : Model<BlockDisplay>("boss-lighting-branch") {
    var isMain = true
    val branches = mutableListOf<Lighting>()

    override val renderer: ModelRenderer<BlockDisplay>
        get() = ModelRenderer(this)

    override fun initialize(location: Location, renderResult: RenderResult<BlockDisplay>) {
        super.initialize(location, renderResult)

        attachModule(SingleDamageModule(10.0,owner, DamageType.LIGHTNING_BOLT,true) { it != owner })
        //applying direction to location
        location.apply {
            direction = this@Lighting.direction
            teleport(this)
        }
        val sizeModule = SizeModule<BlockDisplay>().also { attachModule(it) }
        sizeModule.multiplyGlobally(1.0,1.0,direction.length())
        require(spreadPower <= 20){ "failsafe triggered, spreadPower $spreadPower detected" }
        //spawn branch
        if(spreadPower > 1){
            repeat((1..2).random()){
                //randomize dir
                val newDir = direction
                    .clone().normalize()
                    .multiply(straightness)
                    .add(randomVector())
                    .normalize()
                    .multiply((1..3).random())
                    .multiply(size)
                Lighting(owner,spreadPower-1,newDir,size = size).also { it.isMain = false; branches.add(it) }.renderer.load(location.add(direction))
            }
        }
        if(isMain){
            val p = later((5).seconds) { this@Lighting.remove() }
            TaskModule<BlockDisplay>(p).also { attachModule(it) }
        }

    }

    override fun remove() {
        super.remove()
        branches.forEach(Lighting::remove)
    }
}
class LightingBeam(val owner : Player,val direction: Vector) : Model<BlockDisplay>("boss-lighting-beam") {
    override val renderer: ModelRenderer<BlockDisplay>
        get() = ModelRenderer(this)

    override fun initialize(location: Location, renderResult: RenderResult<BlockDisplay>) {
        super.initialize(location, renderResult)

        attachModule(SingleDamageModule(10.0,owner, DamageType.LIGHTNING_BOLT,true) { it != owner })
        //applying direction to location
        location.apply {
            direction = this@LightingBeam.direction.clone().normalize()
            teleport(this)
        }
        val sizeModule = SizeModule<BlockDisplay>().also { attachModule(it) }
        sizeModule.multiplyGlobally(1.5,1.5,direction.length())
        val p = later((5).seconds) { this@LightingBeam.remove() }
        TaskModule<BlockDisplay>(p).also { attachModule(it) }
    }
}
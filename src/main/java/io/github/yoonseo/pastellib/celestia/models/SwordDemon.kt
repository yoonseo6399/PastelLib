package io.github.yoonseo.pastellib.celestia.models

import io.github.yoonseo.pastellib.utils.entity.model.*
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.LivingEntity
import org.joml.Quaternionf
import org.joml.Vector3f

class SwordDemon(val owner : LivingEntity) : Model<BlockDisplay>("boss1-L"){

    override val renderer: ModelRenderer<BlockDisplay> = ModelRenderer(this)
    override fun initialize(location: Location,renderResult: RenderResult<BlockDisplay>) {
        super.initialize(location, renderResult)
        teleport(owner.eyeLocation)
        attachModule(SelfPropellingModule(2.0, 100.0))
        val size = SizeModule<BlockDisplay>()
        attachModule(size)
        size.multiplyGlobally(vec = Vector3f(Math.random().toFloat() * 2 + 1, 1f, 1f))
        applyGlobalRotation(Quaternionf().fromAxisAngleDeg(Vector3f(0f, 0f, 1f), (Math.random() * 360).toFloat()))
        attachModule(SimpleCollusionModule())
        attachModule(SingleDamageModule(10.0, owner, DamageType.LIGHTNING_BOLT, true) { it != owner })
    }

}
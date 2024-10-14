package io.github.yoonseo.pastellib

import io.github.yoonseo.pastellib.model.Model
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player

class Pet(val owner: Player) {
    val model = PetModel()
    val mode : PetMode = PetMode.Normal
}
enum class PetMode(val value: Int) {
    Normal(0)
}

class PetModel : Model<Display>() {
    override val location: Location
        get() = TODO("Not yet implemented")

}
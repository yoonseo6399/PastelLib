package io.github.yoonseo.pastellib.guns

import io.github.yoonseo.pastellib.utils.copyToArrayList
import io.github.yoonseo.pastellib.utils.dataContainer
import io.github.yoonseo.pastellib.utils.isAssignable
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.EnumSet
import kotlin.reflect.KClass

class GunCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {

        if(sender !is Player || args?.size != 8) return false
        var item = ItemStack(Material.STICK,1)
        val meta = item.itemMeta
        meta.displayName(Component.text(args[0]))
        val container = item.dataContainer()

        item.itemMeta = meta
        parse(args.asList(), EnumSet.allOf(ParameterType::class.java)).forEach {
            container.addValue(it.key, it.value)
        }
    }

}

fun CommandExecutor.parse(args: List<String>, parameters: EnumSet<out ParameterType>): HashMap<String,Any> {
    val result = HashMap<String, Any>()
    args.containsAll(parameters.)
    for (syntax in args){
        val (key, value) = syntax.split(":")
        val parm = parameters.firstOrNull { it.name == key } ?: throw IllegalArgumentException("Unknown parameter: $key")
        if(!value.isAssignable(parm.type)) throw IllegalArgumentException("Invalid value for parameter: $key, expected ${parm.type.simpleName}, got $value")
        result[key] = value
    }
    return result
}


@Suppress("EnumEntryName")
enum class ParameterType(val type: KClass<*>, val required: Boolean = true) {
    ammo         (Int::class,false),
    magazineSize (Int::class),
    isReloading  (Boolean::class,false),
    reloadTime   (Int::class),
    damage       (Double::class),
    bulletSpeed  (Double::class),
    recoil       (Double::class),
    fireRate     (Int::class),
    ammoType     (String::class)
}

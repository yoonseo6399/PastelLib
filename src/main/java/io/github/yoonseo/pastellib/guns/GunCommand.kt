package io.github.yoonseo.pastellib.guns

import io.github.yoonseo.pastellib.PastelLib
import io.github.yoonseo.pastellib.utils.dataContainer
import io.github.yoonseo.pastellib.utils.isAssignable
import io.github.yoonseo.pastellib.utils.toType
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.EnumSet
import kotlin.reflect.KClass

class GunCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {

        if(sender !is Player || args == null || args.isEmpty()) return false
        var item = ItemStack(Material.STICK,1)
        val meta = item.itemMeta
        meta.displayName(Component.text(args[0]))
        val container = item.dataContainer
        meta.persistentDataContainer.set(NamespacedKey(PastelLib.instance,"gun"), PersistentDataType.INTEGER,1)
        item.itemMeta = meta
        try {
            parse(args.asList(), EnumSet.allOf(ParameterType::class.java)).forEach {
                when(it.value){
                    is Int -> container.addValue(it.key,it.value as Int)
                    is Double -> container.addValue(it.key,it.value as Double)
                    is Boolean -> container.addValue(it.key,it.value as Boolean)
                    is String -> container.addValue(it.key,it.value as String)
                    else -> throw IllegalArgumentException("Unsupported type for parameter: ${it.key} provided ${it.value::class.simpleName}")
                }
            }
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("${e.message}")
            e.printStackTrace()
            return false
        }

        sender.inventory.addItem(item)
        return true
    }

}

fun CommandExecutor.parse(args: List<String>, parameters: EnumSet<out ParameterType>): HashMap<String,Any> {
    val result = HashMap<String, Any>()
    val requiredParameters = parameters.filter { it.required }
    val providedParameters = mutableListOf<String>()
    
    // Skip the first argument (gun name)
    for (syntax in args.drop(1)) {
        if (!syntax.contains(":")) {
            throw IllegalArgumentException("Invalid parameter format: $syntax. Expected format: key:value")
        }
        val (key, value) = syntax.split(":", limit = 2)
        val param = parameters.firstOrNull { it.name == key } ?: throw IllegalArgumentException("Unknown parameter: $key")
        if(!value.isAssignable(param.type)) throw IllegalArgumentException("Invalid value for parameter: $key, expected ${param.type.simpleName}, got $value")
        result[key] = value.toType(param.type)!!
        providedParameters.add(key)
    }
    
    val missingParams = requiredParameters.filter { it.name !in providedParameters }
    if(missingParams.isNotEmpty()) {
        throw IllegalArgumentException("Missing required parameters: ${missingParams.joinToString(", ") { it.name }}")
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

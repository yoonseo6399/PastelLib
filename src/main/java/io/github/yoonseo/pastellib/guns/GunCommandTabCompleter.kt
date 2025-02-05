package io.github.yoonseo.pastellib.guns

import io.github.yoonseo.pastellib.utils.copyToArrayList
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class GunCommandTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?> {
        if (sender !is Player) return emptyList()

        val completions = mutableListOf<String>()

        when (args.size) {
            1 -> {
                // Suggest gun name
                completions.add("<gun_name>")
            }
            else -> {
                // Suggest parameter names and values


                val remainParameters = ParameterType.entries.copyToArrayList()
                val currentArg = args.last()
                args.forEachIndexed { i,arg ->
                    if(i == args.size - 1) return@forEachIndexed
                    val (param,_) = arg.split(":")
                    remainParameters.removeIf { it.name == param }
                }
                if (currentArg.contains(":")) {
                    val (paramName, _) = currentArg.split(":")
                    val param = remainParameters.find { it.name == paramName }
                    if (param != null) {
                        when (param.type) {
                            Int::class -> completions.add("$paramName:<Integer>")
                            Double::class -> completions.add("$paramName:<Double>")
                            Boolean::class -> {
                                completions.add("$paramName:true")
                                completions.add("$paramName:false")
                            }
                            String::class -> completions.add("$paramName:<text>")
                        }
                    }
                } else {
                    remainParameters.forEach { param ->
                        if (!args.any { it.startsWith("${param.name}:") }) {
                            completions.add("${param.name}:")
                        }
                    }
                }
            }
        }

        return completions.filter { it.startsWith(args.last(), ignoreCase = true) }
    }

}
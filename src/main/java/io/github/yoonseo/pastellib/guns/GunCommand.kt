package io.github.yoonseo.pastellib.guns

import io.github.yoonseo.pastellib.utils.dataContainer
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
        container.addValue("")

        item.itemMeta = meta
    }
}
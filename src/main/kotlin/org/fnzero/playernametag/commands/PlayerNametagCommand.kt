package org.fnzero.playernametag.commands

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.MetaNode
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.fnzero.playernametag.PlayerNametag

class PlayerNametagCommand(private val plugin: PlayerNametag) : CommandExecutor, TabCompleter {

  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>,
  ): Boolean {
    if (args.isEmpty()) {
      sender.sendMessage("§eUsage: /playername <reload|name>")
      return true
    }

    when (args[0].lowercase()) {
      "reload" -> {
        if (!sender.hasPermission("playernametag.reload")) {
          sender.sendMessage("§cYou don't have permission to use this command.")
          return true
        }
        plugin.reload()
        sender.sendMessage("§aPlayerNametag config reloaded successfully!")
      }

      "name" -> {
        if (!sender.hasPermission("playernametag.name")) {
          sender.sendMessage("§cYou don't have permission to use this command.")
          return true
        }
        if (args.size < 3) {
          sender.sendMessage("§eUsage: /playername name <player> <string|clear>")
          return true
        }

        val target = Bukkit.getPlayer(args[1])
        if (target == null) {
          sender.sendMessage("§cPlayer not found.")
          return true
        }

        val luckPerms = LuckPermsProvider.get()
        val user = luckPerms.userManager.getUser(target.uniqueId)
        if (user == null) {
          sender.sendMessage("§cFailed to get LuckPerms user.")
          return true
        }

        if (args[2].equals("clear", ignoreCase = true)) {
          val existingNode = user.nodes.firstOrNull { it is MetaNode && it.metaKey == "pn_custom_name" }
          if (existingNode != null) {
            user.data().remove(existingNode)
          }
          luckPerms.userManager.saveUser(user)
          sender.sendMessage("§aCleared custom name for §f${target.name}")
          plugin.nametagManager.refresh(target)
        } else {
          val nameString = args.slice(2 until args.size).joinToString(" ")
          val existingNode = user.nodes.firstOrNull { it is MetaNode && it.metaKey == "pn_custom_name" }
          if (existingNode != null) {
            user.data().remove(existingNode)
          }
          user.data().add(MetaNode.builder("pn_custom_name", nameString).build())
          luckPerms.userManager.saveUser(user)
          sender.sendMessage("§aSet custom name for §f${target.name}§a to: $nameString")
          plugin.nametagManager.refresh(target)
        }
      }

      else -> {
        sender.sendMessage("§eUsage: /playername <reload|name>")
      }
    }
    return true
  }

  override fun onTabComplete(
    sender: CommandSender,
    command: Command,
    alias: String,
    args: Array<out String>,
  ): List<String> {
    return when (args.size) {
      1 -> {
        val subs = mutableListOf<String>()
        if (sender.hasPermission("playernametag.reload")) subs.add("reload")
        if (sender.hasPermission("playernametag.name")) subs.add("name")
        subs.filter { it.startsWith(args[0].lowercase()) }
      }
      2 -> {
        if (args[0].lowercase() == "name" && sender.hasPermission("playernametag.name")) {
          Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.startsWith(args[1], ignoreCase = true) }
        } else emptyList()
      }
      3 -> {
        if (args[0].lowercase() == "name") {
          listOf("clear").filter { it.startsWith(args[2], ignoreCase = true) }
        } else emptyList()
      }
      else -> emptyList()
    }
  }
}

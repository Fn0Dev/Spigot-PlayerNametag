package org.fnzero.playernametag.display

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.fnzero.playernametag.PlayerNametag
import org.fnzero.playernametag.config.ConfigManager.GroupFormat

object NametagFormatter {

  private val miniMessage = MiniMessage.miniMessage()
  private val legacySerializer = LegacyComponentSerializer.legacyAmpersand()

    fun buildDisplayName(player: Player): String {
        val config = PlayerNametag.instance.configManager
        val groupFormat = getGroupFormat(player)
        val nameFormat = groupFormat.nameFormat

        val groupName = getLuckPermsGroup(player)
        val groupDisplay = getGroupDisplayName(player)
        val name = getCustomName(player)
        val level = getPlayerLevel(player)
        val health = String.format("%.1f", player.health)
        val maxHealth = String.format("%.1f", player.maxHealth)

        var formatted =
            nameFormat
                .replace("{prefix}", config.globalPrefix)
                .replace("{suffix}", config.globalSuffix)
                .replace("{group_name}", groupName)
                .replace("{group_display}", groupDisplay)
                .replace("{name}", name)
                .replace("{level}", level.toString())
                .replace("{health}", health)
                .replace("{max_health}", maxHealth)

        formatted = applyMiniMessage(formatted)
        formatted = applyPlaceholderAPI(player, formatted)

        return formatted
    }

    fun buildChatFormat(player: Player, message: String): String {
        val config = PlayerNametag.instance.configManager
        val groupFormat = getGroupFormat(player)
        val chatFormat = groupFormat.chatFormat

        val groupName = getLuckPermsGroup(player)
        val groupDisplay = getGroupDisplayName(player)
        val name = getCustomName(player)
        val level = getPlayerLevel(player)
        val health = String.format("%.1f", player.health)
        val maxHealth = String.format("%.1f", player.maxHealth)

        var formatted =
            chatFormat
                .replace("{prefix}", config.globalPrefix)
                .replace("{suffix}", config.globalSuffix)
                .replace("{group_name}", groupName)
                .replace("{group_display}", groupDisplay)
                .replace("{name}", name)
                .replace("{level}", level.toString())
                .replace("{health}", health)
                .replace("{max_health}", maxHealth)
                .replace("{message}", message)

        formatted = applyMiniMessage(formatted)
        formatted = applyPlaceholderAPI(player, formatted)

        return formatted
    }

  private fun getGroupFormat(player: Player): GroupFormat {
    val config = PlayerNametag.instance.configManager
    try {
      val luckPerms = LuckPermsProvider.get()
      val user = luckPerms.getUserManager().getUser(player.uniqueId)
      val primaryGroup = user?.primaryGroup ?: "default"
      return config.groupFormats[primaryGroup] ?: config.groupFormats["default"]
        ?: GroupFormat("{name}", "{name}: {message}")
    } catch (e: Exception) {
      return config.groupFormats["default"] ?: GroupFormat("{name}", "{name}: {message}")
    }
  }

  private fun getLuckPermsGroup(player: Player): String {
    return try {
      val luckPerms = LuckPermsProvider.get()
      val user = luckPerms.getUserManager().getUser(player.uniqueId)
      user?.primaryGroup ?: "default"
    } catch (e: Exception) {
      "default"
    }
  }

  private fun getGroupDisplayName(player: Player): String {
    return try {
      val luckPerms = LuckPermsProvider.get()
      val user = luckPerms.getUserManager().getUser(player.uniqueId)
      val groupName = user?.primaryGroup ?: "default"
      val group = luckPerms.groupManager.getGroup(groupName)
      group?.displayName ?: groupName
    } catch (e: Exception) {
      "default"
    }
  }

  private fun getCustomName(player: Player): String {
    return try {
      val luckPerms = LuckPermsProvider.get()
      val user = luckPerms.getUserManager().getUser(player.uniqueId)
      val metaValue = user?.cachedData?.metaData?.getMetaValue("pn_custom_name")
      if (!metaValue.isNullOrEmpty()) metaValue else player.name
    } catch (e: Exception) {
      player.name
    }
  }

  private fun getPlayerLevel(player: Player): Int {
    return try {
      player.level
    } catch (e: Exception) {
      0
    }
  }

  private fun applyMiniMessage(text: String): String {
    return try {
      val miniMsgText = toMiniMessage(text)
      val component = miniMessage.deserialize(miniMsgText)
      legacySerializer.serialize(component)
    } catch (e: Exception) {
      text
    }
  }

  private fun toMiniMessage(text: String): String {
    var result = text

    result = result.replace(Regex("&#([A-Fa-f0-9]{6})")) { "<#${it.groupValues[1]}>" }

    val clrMap =
      mapOf(
        "&0" to "<black>",
        "&1" to "<dark_blue>",
        "&2" to "<dark_green>",
        "&3" to "<dark_aqua>",
        "&4" to "<dark_red>",
        "&5" to "<dark_purple>",
        "&6" to "<gold>",
        "&7" to "<gray>",
        "&8" to "<dark_gray>",
        "&9" to "<blue>",
        "&a" to "<green>",
        "&b" to "<aqua>",
        "&c" to "<red>",
        "&d" to "<light_purple>",
        "&e" to "<yellow>",
        "&f" to "<white>",
        "&l" to "<bold>",
        "&n" to "<underlined>",
        "&o" to "<italic>",
        "&m" to "<strikethrough>",
        "&r" to "<reset>",
      )

    for ((code, tag) in clrMap) {
      result = result.replace(code, tag, ignoreCase = true)
    }

    return result
  }

  private fun applyPlaceholderAPI(player: Player, text: String): String {
    val placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
    if (placeholderApi == null) return text

    var result = text
    val regex = """\[papi:(.*?)]""".toRegex()
    result =
      regex.replace(result) { match ->
        val placeholder = match.groupValues[1]
        me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
      }

    return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result)
  }
}

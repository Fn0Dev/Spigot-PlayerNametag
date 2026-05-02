package org.fnzero.playernametag

import org.bukkit.plugin.java.JavaPlugin
import org.fnzero.playernametag.chat.ChatFormatter
import org.fnzero.playernametag.commands.PlayerNametagCommand
import org.fnzero.playernametag.config.ConfigManager
import org.fnzero.playernametag.display.NametagManager
import org.fnzero.playernametag.display.TeamManager
import org.fnzero.playernametag.listener.NametagListener

class PlayerNametag : JavaPlugin() {

  lateinit var configManager: ConfigManager
    internal set

  lateinit var nametagManager: NametagManager
    internal set

  companion object {
    lateinit var instance: PlayerNametag
      internal set
  }

  override fun onEnable() {
    instance = this

    saveDefaultConfig()
    configManager = ConfigManager(this)
    configManager.load()

    TeamManager.setupScoreboard()

    nametagManager = NametagManager(this)
    if (configManager.displayEnabled) {
      nametagManager.start()
    }

    server.pluginManager.registerEvents(NametagListener(this), this)
    if (configManager.chatEnabled) {
      server.pluginManager.registerEvents(ChatFormatter(this), this)
    }

    getCommand("playername")?.setExecutor(PlayerNametagCommand(this))

    val luckPerms = server.pluginManager.getPlugin("LuckPerms")
    val placeholderApi = server.pluginManager.getPlugin("PlaceholderAPI")

    logger.info(
      "PlayerNametag enabled (LuckPerms: ${luckPerms != null}, PAPI: ${placeholderApi != null})"
    )
  }

  override fun onDisable() {
    nametagManager.stop()
    TeamManager.cleanup()
    logger.info("PlayerNametag disabled")
  }

  fun reload() {
    configManager.load()
    nametagManager.restart()
  }
}

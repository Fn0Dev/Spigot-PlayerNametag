package org.fnzero.playernametag.listener

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.fnzero.playernametag.PlayerNametag
import org.fnzero.playernametag.display.NametagManager
import org.fnzero.playernametag.display.TeamManager

class NametagListener(private val plugin: PlayerNametag) : Listener {

  private val nametagManager: NametagManager
    get() = plugin.nametagManager

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPlayerJoin(event: PlayerJoinEvent) {
    val player = event.player
    TeamManager.addPlayerToTeam(player)
    Bukkit.getScheduler()
      .runTaskLater(
        plugin,
        Runnable {
          if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
            nametagManager.createNametag(player)
          }
        },
        10L,
      )
  }

  @EventHandler
  fun onPlayerQuit(event: PlayerQuitEvent) {
    TeamManager.removePlayerFromTeam(event.player)
    nametagManager.removeNametag(event.player)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPlayerDeath(event: PlayerDeathEvent) {
    nametagManager.removeNametag(event.entity)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPlayerRespawn(event: PlayerRespawnEvent) {
    val player = event.player
    Bukkit.getScheduler()
      .runTaskLater(
        plugin,
        Runnable {
          if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
            nametagManager.createNametag(player)
          }
        },
        10L,
      )
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  fun onPlayerTeleport(event: PlayerTeleportEvent) {
    val player = event.player
    val fromWorld = event.from.world
    val toWorld = event.to.world

    if (fromWorld != toWorld) {
      nametagManager.removeNametag(player)
      Bukkit.getScheduler()
        .runTaskLater(
          plugin,
          Runnable {
            if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
              nametagManager.createNametag(player)
            }
          },
          10L,
        )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  fun onGameModeChange(event: PlayerGameModeChangeEvent) {
    val player = event.player
    if (event.newGameMode == GameMode.SPECTATOR) {
      nametagManager.removeNametag(player)
    } else if (player.gameMode == GameMode.SPECTATOR) {
      Bukkit.getScheduler()
        .runTaskLater(
          plugin,
          Runnable {
            if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
              nametagManager.createNametag(player)
            }
          },
          5L,
        )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
    val player = event.player
    if (event.isSneaking) return
    Bukkit.getScheduler()
      .runTaskLater(
        plugin,
        Runnable {
          if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
            nametagManager.refresh(player)
          }
        },
        2L,
      )
  }
}

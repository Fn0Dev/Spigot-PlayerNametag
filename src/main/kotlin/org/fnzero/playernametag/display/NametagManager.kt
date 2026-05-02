package org.fnzero.playernametag.display

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.fnzero.playernametag.PlayerNametag
import org.fnzero.playernametag.config.ConfigManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class NametagManager(private val plugin: PlayerNametag) {

    private val nametagStands = ConcurrentHashMap<UUID, ArmorStand>()
    private val previousDisplayCache = ConcurrentHashMap<UUID, String>()
    private var updateTaskId: Int = -1

    fun start() {
        if (!plugin.configManager.displayEnabled) return
        if (updateTaskId != -1) return

        updateTaskId = Bukkit.getScheduler().runTaskTimer(
            plugin,
            Runnable { Bukkit.getOnlinePlayers().forEach { tickPlayer(it) } },
            0L,
            plugin.configManager.updateIntervalTicks
        ).taskId

        plugin.logger.info("NametagManager started")
    }

    fun stop() {
        if (updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(updateTaskId)
            updateTaskId = -1
        }

        nametagStands.keys.toList().forEach { uuid ->
            removeNametag(Bukkit.getPlayer(uuid) ?: return@forEach)
        }

        nametagStands.clear()
        previousDisplayCache.clear()
    }

    fun restart() {
        stop()
        start()
    }

    fun refresh(player: Player) {
        removeNametag(player)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { createNametag(player) }, 3L)
    }

    private fun tickPlayer(player: Player) {
        if (!plugin.configManager.displayEnabled) {
            removeNametag(player)
            return
        }

        if (!player.isOnline || player.isDead) {
            removeNametag(player)
            return
        }

        val stand = nametagStands[player.uniqueId]
        if (stand == null || !stand.isValid) {
            createNametag(player)
            return
        }

        updateNametag(player, stand)
    }

     fun createNametag(player: Player) {
        if (!player.isOnline || player.isDead) return

        nametagStands[player.uniqueId]?.let { if (it.isValid) it.remove() }

        try {
            val offset = plugin.configManager.yOffset
            val stand = player.world.spawn(player.location.clone().add(0.0, offset, 0.0), ArmorStand::class.java) {
                it.isPersistent = false
                it.isVisible = false
                it.setGravity(false)
                it.isMarker = true
                it.isSilent = true
                it.isCustomNameVisible = true
                it.isInvulnerable = true
                it.setBasePlate(false)
                it.canPickupItems = false
                 it.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(NametagFormatter.buildDisplayName(player)))
            }

            nametagStands[player.uniqueId] = stand
            player.hideEntity(plugin, stand)
            player.addPassenger(stand)
            previousDisplayCache[player.uniqueId] = NametagFormatter.buildDisplayName(player)
            updateNametag(player, stand)

        } catch (e: Exception) {
            nametagStands.remove(player.uniqueId)
            plugin.logger.warning("Failed to create nametag for ${player.name}: ${e.message}")
        }
    }

     fun removeNametag(player: Player) {
        val stand = nametagStands.remove(player.uniqueId)
        previousDisplayCache.remove(player.uniqueId)

        if (stand != null) {
            try {
                if (player.isOnline && player.passengers.contains(stand)) {
                    player.removePassenger(stand)
                }
                if (stand.isValid) stand.remove()
            } catch (e: Exception) {}
        }
    }

    private fun updateNametag(player: Player, stand: ArmorStand) {
        if (!player.isOnline || player.isDead || !stand.isValid) {
            removeNametag(player)
            return
        }

        if (stand.world != player.world) {
            removeNametag(player)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                if (player.isOnline && !player.isDead) createNametag(player)
            }, 5L)
            return
        }

        if (!player.passengers.contains(stand)) {
            val attached = runCatching { player.addPassenger(stand) }.getOrDefault(false)
            if (!attached && stand.isValid) {
                runCatching { stand.teleport(player.location.clone().add(0.0, plugin.configManager.yOffset, 0.0)) }
            }
        }
        runCatching { player.hideEntity(plugin, stand) }

        val displayName = NametagFormatter.buildDisplayName(player)
        val previousDisplay = previousDisplayCache[player.uniqueId]

        if (displayName != previousDisplay) {
            try {
                stand.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
                previousDisplayCache[player.uniqueId] = displayName
            } catch (e: Exception) {
                plugin.logger.warning("Failed to update nametag for ${player.name}: ${e.message}")
            }
        }
    }
}

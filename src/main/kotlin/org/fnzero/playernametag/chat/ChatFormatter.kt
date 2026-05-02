package org.fnzero.playernametag.chat

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.fnzero.playernametag.PlayerNametag
import org.fnzero.playernametag.display.NametagFormatter

class ChatFormatter(private val plugin: PlayerNametag) : Listener {

  private val legacySerializer = LegacyComponentSerializer.legacyAmpersand()

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  fun onPlayerChat(event: AsyncChatEvent) {
    if (!plugin.configManager.chatEnabled) return

    val player = event.player
    val message = legacySerializer.serialize(event.message())
    val formatted = NametagFormatter.buildChatFormat(player, message)
    val formattedComponent = legacySerializer.deserialize(formatted)

    event.renderer { _, _, _, _ -> formattedComponent }
  }
}

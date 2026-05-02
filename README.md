# PlayerNametag

Standalone nametag plugin using ArmorStand for Geyser/Bedrock compatibility. Features LuckPerms integration, chat
formatting, and PlaceholderAPI support.

## Commands

- `/playername reload` - Reload config
- `/playername name <player> <string>` - Set custom display name (supports color codes & MiniMessage)
- `/playername name <player> clear` - Clear custom display name

## Permissions

- `playernametag.reload` - Access to /playername reload
- `playernametag.name` - Access to /playername name command

## Placeholders

- `{group_name}` - LuckPerms group name (e.g., "admin")
- `{group_display}` - LuckPerms group display name (e.g., "Admin")
- `{name}` - Player display name
- `{level}` - Player XP level
- `{health}` - Current health
- `{max_health}` - Maximum health
- `{prefix}` - Global prefix from config
- `{suffix}` - Global suffix from config
- `{message}` - Chat message (chat format only)
- `[papi:...]` - PlaceholderAPI placeholders

## Color Code Support

Supports `&_` codes: `&0-9`, `&a-f` (colors), `&l` (bold), `&n` (underline), `&o` (italic), `&m` (strikethrough), `&r` (
reset)

Also supports hex colors (`&#RRGGBB`) and MiniMessage (`<gray>`, `<gradient:red:blue>`, etc.)

## Examples

```
/playername name Voxl_ &aGreenName
/playername name Voxl_ <gradient:#FF0000:#0000FF>Cool Name
/playername name Voxl_ clear
```

## API
<details>
<summary>API Docs</summary>
Well... it's not that good tho 
The plugin exposes `NametagFormatter` and `NametagManager` for external use:

```kotlin
import org.fnzero.playernametag.PlayerNametag
import org.fnzero.playernametag.display.NametagFormatter
import org.fnzero.playernametag.display.NametagManager

// Get plugin instance
val plugin = server.pluginManager.getPlugin("PlayerNametag") as? PlayerNametag ?: return

// NametagFormatter - build formatted text
val displayName = NametagFormatter.buildDisplayName(player)
val chatFormat = NametagFormatter.buildChatFormat(player, message)

// NametagManager - control nametags
plugin.nametagManager.createNametag(player)   // Create nametag for player
plugin.nametagManager.removeNametag(player)   // Remove nametag
plugin.nametagManager.refresh(player)         // Refresh/recreate nametag
plugin.nametagManager.start()                 // Start nametag system
plugin.nametagManager.stop()                  // Stop nametag system
plugin.nametagManager.restart()               // Restart nametag system
```
</details>

## Dependencies

- **Required**: Paper 1.20.6+, LuckPerms 5.4+, PlaceholderAPI 2.11+
- **Kotlin**: 1.9+

## Config

See `src/main/resources/config.yml` for all configuration options.

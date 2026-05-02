package org.fnzero.playernametag.display

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.fnzero.playernametag.PlayerNametag

object TeamManager {

  private val playerTeams = ConcurrentHashMap<UUID, String>()
  private const val TEAM_PREFIX = "pn"

  fun setupScoreboard() {
    Bukkit.getOnlinePlayers().forEach { player -> addPlayerToTeam(player) }
    PlayerNametag.instance.logger.info("Scoreboard teams setup complete")
  }

  fun addPlayerToTeam(player: Player) {
    val scoreboard = Bukkit.getScoreboardManager()?.mainScoreboard ?: return
    val teamName = generateTeamName(scoreboard, player)

    var team = scoreboard.getTeam(teamName)
    if (team == null) {
      team = scoreboard.registerNewTeam(teamName)
      team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
      team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
    }

    if (!team.hasPlayer(player)) {
      team.addPlayer(player)
    }

    playerTeams[player.uniqueId] = teamName
  }

  fun removePlayerFromTeam(player: Player) {
    val teamName = playerTeams.remove(player.uniqueId) ?: return
    val scoreboard = Bukkit.getScoreboardManager()?.mainScoreboard ?: return
    val team = scoreboard.getTeam(teamName)

    if (team != null && team.hasPlayer(player)) {
      team.removePlayer(player)
      if (team.players.isEmpty()) {
        team.unregister()
      }
    }
  }

  fun cleanup() {
    val scoreboard = Bukkit.getScoreboardManager()?.mainScoreboard ?: return
    playerTeams.values.toSet().forEach { teamName -> scoreboard.getTeam(teamName)?.unregister() }
    playerTeams.clear()
  }

  private fun generateTeamName(scoreboard: Scoreboard, player: Player): String {
    val uuidFragment = player.uniqueId.toString().substring(0, 4)
    repeat(10) {
      val candidate =
        "${TEAM_PREFIX}_${uuidFragment}_${java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 10000)}"
      if (scoreboard.getTeam(candidate) == null) return candidate
    }
    return "${TEAM_PREFIX}_${uuidFragment}_${java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 10000)}"
  }
}

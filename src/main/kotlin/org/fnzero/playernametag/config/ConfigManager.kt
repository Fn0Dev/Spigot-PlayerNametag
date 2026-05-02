package org.fnzero.playernametag.config

import org.fnzero.playernametag.PlayerNametag

class ConfigManager(private val plugin: PlayerNametag) {

    var displayEnabled: Boolean = true
        private set

    var updateIntervalTicks: Long = 10L
        private set

    var yOffset: Double = 1.8
        private set

    var chatEnabled: Boolean = true
        private set

    var cacheDurationTicks: Int = 40
        private set

    var globalPrefix: String = ""
        private set

    var globalSuffix: String = ""
        private set

    val groupFormats: MutableMap<String, GroupFormat> = mutableMapOf()

    data class GroupFormat(val nameFormat: String, val chatFormat: String)

    fun load() {
        plugin.reloadConfig()
        val config = plugin.config

        displayEnabled = config.getBoolean("display.enabled", true)
        updateIntervalTicks = config.getLong("display.update-interval-ticks", 10L)
        yOffset = config.getDouble("display.y-offset", 1.8)

        globalPrefix = config.getString("display.prefix") ?: ""
        globalSuffix = config.getString("display.suffix") ?: ""

        chatEnabled = config.getBoolean("chat.enabled", true)
        cacheDurationTicks = config.getInt("performance.cache-duration-ticks", 40)

        groupFormats.clear()
        val groupSect = config.getConfigurationSection("display.group-formats")
        if (groupSect != null) {
            groupSect.getKeys(false).forEach { group ->
                val path = "display.group-formats.$group"
                val nameFmt = config.getString("$path.name-format") ?: return@forEach
                val chatFmt = config.getString("$path.chat-format") ?: return@forEach
                groupFormats[group] = GroupFormat(nameFmt, chatFmt)
            }
        }

        if (!groupFormats.containsKey("default")) {
            groupFormats["default"] =
                GroupFormat(
                    "{prefix}<gray>{group}</gray> <white>{name}</white> <dark_gray>[LV {level}]</dark_gray>{suffix}",
                    "{prefix}<gray>{group}</gray> <white>{name}</white><gray>:</gray> {message}{suffix}",
                )
        }

        plugin.logger.info(
            "Config loaded: display=$displayEnabled, chat=$chatEnabled, groups=${groupFormats.keys}"
        )
    }
}

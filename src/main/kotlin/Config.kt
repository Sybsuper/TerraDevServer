package com.sybsuper.terradevserver

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class Config(
    @YamlComment("The address to bind to")
    val bindAddress: String = "0.0.0.0",
    @YamlComment("The port to bind to")
    val port: Int = 25565,
    @YamlComment("The folder to look for dev packs in")
    val devPackFolder: String = "dev",
    @YamlComment(
        "Whether to cycle through players when reload",
        "this allows for a side-by-side (before and after) view of changes."
    )
    val cycleThroughPlayers: Boolean = true,
    @YamlComment(
        "Whether to sync player positions, when enabled",
        "all players will be teleported to the same position as the player that first joined the server"
    )
    val syncPlayerPositions: Boolean = true,
    @YamlComment(
        "Whether to watch the dev pack directory for changes",
        "this will automatically reload the dev pack when a file change is detected"
    )
    val watchDevPackDirectory: Boolean = true,
    @YamlComment("Set an motd (server list name)")
    val motd: String = "Terra Dev Server\nMade by: Sybsuper",
    @YamlComment("Whether to enable the /reload command")
    val reloadCommandEnabled: Boolean = true,
    @YamlComment("Set a world seed")
    val worldSeed: Long = 0L,
) {
}

private val configFile = File("config.yml")
val config =
    (configFile.takeIf { it.exists() }?.let { Yaml.default.decodeFromString(it.readText()) } ?: Config()).also {
        configFile.writeText(Yaml.default.encodeToString(it))
    }

package com.sybsuper.terradevserver

import com.sybsuper.terradevserver.modules.ModuleManager
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.isDirectory

val logger: Logger = PlayerLogger(LoggerFactory.getLogger("TerraDevServer")!!)

fun main() {
    System.setProperty("minestom.registry.unsafe-ops", "true")
    val version = "TerraDevServer ${BuildConfig.VERSION}"
    logger.info("Starting server $version...")
    val minecraftServer = MinecraftServer.init()
    MinecraftServer.setBrandName(version)

    if (!Path(config.devPackFolder).isDirectory()) {
        logger.info("Creating dev pack directory at ${config.devPackFolder}")
        Path(config.devPackFolder).createDirectory()
    }

    initializeListeners()

    ModuleManager.loadModules()

    runCatching {
        minecraftServer.start(config.bindAddress, config.port)
    }.onFailure { e ->
        throw RuntimeException(
            "Could not bind server to '${config.bindAddress}:${config.port}' is another server already running?",
            e
        )
    }
    logger.info("Server started at ${config.bindAddress}:${config.port}")
}

private fun initializeListeners() {
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        event.spawningInstance = createInstance()
        event.player.respawnPoint = Pos(0.0, 64.0, 0.0)
    }
    globalEventHandler.addListener(PlayerSpawnEvent::class.java) { event ->
        event.player.gameMode = GameMode.SPECTATOR
    }
}
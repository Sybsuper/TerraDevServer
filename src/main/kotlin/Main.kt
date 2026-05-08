package com.sybsuper.terradevserver

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.Instance
import net.minestom.server.timer.TaskSchedule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.*

val logger: Logger = LoggerFactory.getLogger("TerraDevServer")!!

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

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        event.spawningInstance = createInstance()
        event.player.respawnPoint = Pos(0.0, 64.0, 0.0)
    }
    globalEventHandler.addListener(PlayerSpawnEvent::class.java) { event ->
        event.player.gameMode = GameMode.SPECTATOR
    }
    globalEventHandler.addListener(PlayerChatEvent::class.java) { event ->
        if (event.rawMessage != "reload") return@addListener
        event.isCancelled = true
        updateDevPackForPlayer(event.player)
    }

    if (config.syncPlayerPositions) enableSyncPositions()
    if (config.watchDevPackDirectory) watchFolder(Path(config.devPackFolder)) {
        logger.info("Detected file change at ${it.path()}")
        val msg =
            "Detected file change at ${it.path().relativeTo(Path(config.devPackFolder).absolute())} reloading dev pack"
        val player = getPlayerTarget()
        logger.info(msg)
        player.sendMessage(msg)
        updateDevPackForPlayer(player)
    }

    removeDeadInstances()

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

private fun removeDeadInstances() {
    val seenEmpty = mutableMapOf<Instance, Int>()
    MinecraftServer.getSchedulerManager().scheduleTask({
        MinecraftServer.getInstanceManager().instances.filter { it.players.isEmpty() }.forEach {
            seenEmpty[it] = (seenEmpty[it] ?: 0) + 1
            if (seenEmpty[it]!! < 30) return@forEach
            MinecraftServer.getInstanceManager().unregisterInstance(it)
            seenEmpty.remove(it)
        }
    }, TaskSchedule.seconds(1), TaskSchedule.seconds(1))
}

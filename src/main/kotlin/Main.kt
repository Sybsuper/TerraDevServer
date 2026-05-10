package com.sybsuper.terradevserver

import com.sybsuper.terradevserver.commands.CommandManager
import com.sybsuper.terradevserver.events.PrePlayerSendNewInstanceForPackEvent
import com.sybsuper.terradevserver.modules.BiomeRegistryFix
import com.sybsuper.terradevserver.modules.ModuleManager
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.trait.CancellableEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.isDirectory
import kotlin.jvm.java

val logger: Logger = PlayerLogger(LoggerFactory.getLogger("TerraDevServer")!!)

fun main() {
    System.setProperty("minestom.registry.unsafe-ops", "true")
    System.setProperty("minestom.accept-transfers", "true")
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
    CommandManager.registerCommands()

    runCatching {
        minecraftServer.start(config.bindAddress, config.port)
    }.onFailure { e ->
        throw RuntimeException(
            "Could not bind server to '${config.bindAddress}:${config.port}' is another server already running?",
            e
        )
    }
    logger.info("Server started at ${config.bindAddress}:${config.port}")

    MinecraftServer.getGlobalEventHandler().addListener(DummyEvent::class.java) { e ->
        e.isCancelled = true
    }
    MinecraftServer.getGlobalEventHandler().callCancellable(DummyEvent()) {
        logger.info("Event was not cancelled")
    }
}

class DummyEvent : CancellableEvent {
    private var isCancelled: Boolean = false
    override fun isCancelled(): Boolean = isCancelled
    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

}

private fun initializeListeners() {
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val e = PrePlayerSendNewInstanceForPackEvent(event.player, event)
        globalEventHandler.callCancellable(e) {
            logger.info("Cancelled: "+e.isCancelled.toString())
            logger.info("Sending player to new instance at configuration phase")
            val isWaiting = event.player.uuid in BiomeRegistryFix.waitPlayers
            logger.info("Player ${event.player.username} is ${if (isWaiting) "waiting" else "not waiting"} in BiomeRegistryFix.waitPlayers")
            if (isWaiting) {
                logger.error("Player ${event.player.username} is in BiomeRegistryFix.waitPlayers how?")
            }
            event.spawningInstance = createInstance(event.player)
            event.player.respawnPoint = Pos(0.0, 64.0, 0.0)
        }
    }
    globalEventHandler.addListener(PlayerSpawnEvent::class.java) { event ->
        event.player.gameMode = GameMode.SPECTATOR
    }
}
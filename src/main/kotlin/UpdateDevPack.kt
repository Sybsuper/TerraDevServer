package com.sybsuper.terradevserver

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.concurrent.Semaphore


val lock = Semaphore(1)
fun updateDevPackForPlayer(player: Player) {
    val startTime = System.currentTimeMillis()
    logger.info("Reload triggered for ${player.username}")
    if (!lock.tryAcquire()) {
        player.sendMessage("Already reloading")
        return
    }
    player.sendMessage("Reloading...")
    val oldInstance = player.instance
    player.setInstance(createInstance()).thenRun {
        val stopTime = System.currentTimeMillis()
        val durationMs = stopTime - startTime
        logger.info("Reload finished in ${durationMs/1000.0} seconds")
        MinecraftServer.getInstanceManager().unregisterInstance(oldInstance)
        player.sendMessage("Reloaded in ${durationMs / 1000.0} seconds")
        lock.release()
    }
}
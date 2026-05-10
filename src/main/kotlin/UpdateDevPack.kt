package com.sybsuper.terradevserver

import com.sybsuper.terradevserver.events.PrePlayerSendNewInstanceForPackEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.concurrent.Semaphore


val lock = Semaphore(1)
fun updateDevPackForPlayer(player: Player, force: Boolean = false) {
    val startTime = System.currentTimeMillis()
    logger.info("Reload triggered for ${player.username}")
    if (!force && !lock.tryAcquire()) {
        player.sendMessage("Already reloading")
        return
    }
    player.sendMessage("Reloading...")
    val oldInstance = player.instance
    val newInstance = createInstance(player)
    if (!force) lock.release()
    MinecraftServer.getGlobalEventHandler().callCancellable(PrePlayerSendNewInstanceForPackEvent(player)) {
        logger.info("Sending player to new instance normally")
        player.setInstance(newInstance).thenRun {
            val stopTime = System.currentTimeMillis()
            val durationMs = stopTime - startTime
            logger.info("Reload finished in ${durationMs / 1000.0} seconds")
            MinecraftServer.getInstanceManager().unregisterInstance(oldInstance)
            player.sendMessage("Reloaded in ${durationMs / 1000.0} seconds")
        }
    }
}
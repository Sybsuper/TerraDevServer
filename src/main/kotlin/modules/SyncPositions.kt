package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.network.ConnectionState

/**
 * Sync positions by following the leader (=the player that joined the server first)
 * When the leader moves, all followers will be teleported to the leader's position (but in their own instances such that views can be compared)
 * @constructor Create empty Sync positions
 */
object SyncPositions : IModule {
    override val isEnabled: Boolean
        get() = config.syncPlayerPositions

    override fun enable() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) {
            val leader = MinecraftServer.getConnectionManager().onlinePlayers.firstOrNull()
            if (it.player != leader) return@addListener
            val followers = MinecraftServer.getConnectionManager().onlinePlayers.drop(1)
            followers.forEach { player ->
                if (player.playerConnection.clientState != ConnectionState.PLAY) return@forEach
                player.teleport(it.player.position)
            }
        }
    }
}
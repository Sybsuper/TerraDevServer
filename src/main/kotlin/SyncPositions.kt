package com.sybsuper.terradevserver

import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerMoveEvent

fun enableSyncPositions() {
    MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) {
        if (it.player != MinecraftServer.getConnectionManager().onlinePlayers.firstOrNull()) return@addListener
        MinecraftServer.getConnectionManager().onlinePlayers.drop(1).forEach { player ->
            player.teleport(it.player.position)
        }
    }
}
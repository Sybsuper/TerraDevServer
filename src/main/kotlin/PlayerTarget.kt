package com.sybsuper.terradevserver

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

var cycleIndex = 0
fun getPlayerTarget(): Player {
    val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayers.toList()
    if (config.cycleThroughPlayers) {
        cycleIndex = (cycleIndex + 1) % onlinePlayers.size
    }
    return onlinePlayers.getOrNull(cycleIndex)
        ?: onlinePlayers.firstOrNull()
        ?: error("No players online")
}
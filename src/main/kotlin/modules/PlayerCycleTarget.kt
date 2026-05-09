package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

object PlayerCycleTarget : IModule {
    private var cycleIndex = 0
    fun getPlayerTarget(): Player {
        val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayers.toList()
        if (config.cycleThroughPlayers) {
            cycleIndex = (cycleIndex + 1) % onlinePlayers.size
        }
        return onlinePlayers.getOrNull(cycleIndex)
            ?: onlinePlayers.firstOrNull()
            ?: error("No players online")
    }

    override val isEnabled: Boolean = true
    override fun enable() = Unit
}
package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import com.sybsuper.terradevserver.updateDevPackForPlayer
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerChatEvent

object ReloadCommand : IModule {
    override val isEnabled: Boolean
        get() = config.reloadCommandEnabled

    override fun enable() {
        // todo: make this an actual command
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent::class.java) { event ->
            if (event.rawMessage != "reload") return@addListener
            event.isCancelled = true
            updateDevPackForPlayer(event.player)
        }
    }
}
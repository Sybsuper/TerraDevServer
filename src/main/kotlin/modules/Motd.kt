package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.ping.Status

object Motd : IModule {
    override val isEnabled: Boolean
        get() = config.motd.isNotBlank()

    override fun enable() {
        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent::class.java) { e ->
            e.status = Status.builder(e.status)
                .description(Component.text(config.motd))
                .build()
        }
    }
}
package com.sybsuper.terradevserver.events

import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.trait.CancellableEvent

data class PrePlayerSendNewInstanceForPackEvent(
    val player: Player,
    val parentEvent: AsyncPlayerConfigurationEvent? = null
) : CancellableEvent {
    private var isCancelled: Boolean = false
    override fun isCancelled(): Boolean = isCancelled
    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }
}
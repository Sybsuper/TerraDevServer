package com.sybsuper.terradevserver.events

import com.dfsek.terra.api.config.ConfigPack
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.instance.Instance

/**
 * Called after a pack has been loaded, and an instance has been created for it, but before any players have joined it.
 *
 * @property pack The pack that was loaded.
 * @property instance The instance that was created to hold the pack.
 * @property player The player whom this event was triggered for, if any.
 */
class PackPostLoadEvent(
    val pack: ConfigPack,
    val instance: Instance,
    val player: Player? = null,
) : Event
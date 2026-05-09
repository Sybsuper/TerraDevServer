package com.sybsuper.terradevserver.commands

import com.sybsuper.terradevserver.updateDevPackForPlayer
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.minestom.actor.MinestomCommandActor

class Reload : ICommand {
    @Command("reload")
    @Description("Reload the pack for a player (default is yourself)")
    fun reload(actor: MinestomCommandActor, @Optional target: Player = actor.requirePlayer()) {
        updateDevPackForPlayer(target)
    }
}
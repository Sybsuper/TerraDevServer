package com.sybsuper.terradevserver.commands

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named

class Teleport : ICommand {
    @Command("teleport", "tp")
    fun teleport(sender: Player, @Named("x") x: Double, @Named("y") y: Double, @Named("z") z: Double) {
        val location = Pos(x, y, z)
        sender.teleport(location)
    }
}
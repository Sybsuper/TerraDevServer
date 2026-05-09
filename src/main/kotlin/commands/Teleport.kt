package com.sybsuper.terradevserver.commands

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command

class Teleport : ICommand {
    @Command("teleport", "tp")
    fun teleport(sender: Player, x: Double, y: Double, z: Double) {
        val location = Pos(x, y, z)
        sender.teleport(location)
    }
}
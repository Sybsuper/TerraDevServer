package com.sybsuper.terradevserver.commands

import com.sybsuper.terradevserver.modules.BiomeRegistryFix
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description

class ReregisterBiomes : ICommand {
    @Command("reregisterbiomes", "reloadbiomes", "rlbiomes", "rlb")
    @Description("Reregisters all biomes by reentering the configuration phase.")
    fun reregisterBiomes(actor: Player) {
        actor.sendMessage("Reconfiguring biomes")
        BiomeRegistryFix.reconfigureBiomesForPlayer(actor, actor.instance)
    }
}
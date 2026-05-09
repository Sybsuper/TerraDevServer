package com.sybsuper.terradevserver.commands

import com.sybsuper.terradevserver.config
import revxrsal.commands.minestom.MinestomLamp

object CommandManager {
    private val lamp = MinestomLamp.builder().build()
    val commands = mutableListOf<ICommand>(
        Reload()
    )

    fun registerCommands() {
        val enabledCommands = config.enabledCommands.map { it.lowercase() }.toSet()
        commands
            .asSequence()
            .filter { it.name.lowercase() in enabledCommands }
            .forEach {
                lamp.register(it)
            }
    }
}
package com.sybsuper.terradevserver.commands

import com.dfsek.terra.api.world.biome.Biome
import com.sybsuper.terradevserver.config
import revxrsal.commands.minestom.MinestomLamp

object CommandManager {
    private val lamp = MinestomLamp.builder()
        .parameterTypes {
            it.addParameterType(Biome::class.java, BiomeParameterType())
        }
        .build()
    val commands = mutableListOf(
        Reload(),
        Locate(),
        Teleport(),
        ReregisterBiomes(),
        Marks(),
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
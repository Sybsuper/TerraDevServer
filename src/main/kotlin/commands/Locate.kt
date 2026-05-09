package com.sybsuper.terradevserver.commands

import com.dfsek.terra.api.world.biome.Biome
import com.dfsek.terra.minestom.world.MinestomChunkGeneratorWrapper
import com.dfsek.terra.minestom.world.TerraMinestomWorld
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Range
import revxrsal.commands.annotation.Suggest
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.minestom.actor.MinestomCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

private val MinestomChunkGeneratorWrapper.world: TerraMinestomWorld
    get() {
        val field = MinestomChunkGeneratorWrapper::class.java.getDeclaredField("world")
        field.isAccessible = true
        return field.get(this) as TerraMinestomWorld
    }

class Locate : ICommand {
    @Command("locate biome")
    fun locate(
        actor: Player,
        biome: Biome,
        @Range(min = 100.0) @Suggest("100", "200", "300", "500", "1000") @Optional radius: Int = 100,
        @Range(min = 1.0) @Optional step: Int = 16,
    ) {
        val world = (actor.instance.generator() as? MinestomChunkGeneratorWrapper)?.world ?: run {
            actor.sendMessage("This command is only available in a dev pack world")
            return
        }
        val biomeProvider = world.biomeProvider
        val center = actor.position.asBlockVec()
        val steps = radius / step
        val seed = world.seed
        var found = false
        for (r in 0 until steps) {
            actor.sendActionBar(Component.text("R: $r / $steps"))
            (-r..r).toList().chunked(10).parallelStream().forEach { iss ->
                iss.forEach { i ->
                    val north = center.add(i * step, 0, r * step)
                    val south = center.add(i * step, 0, -r * step)
                    val west = center.add(-r * step, 0, i * step)
                    val east = center.add(r * step, 0, i * step)
                    listOf(north, south, west, east).forEach {
                        if (biomeProvider.getBiome(it.blockX, it.blockY, it.blockZ, seed).id == biome.id) {
                            actor.sendMessage(
                                Component.text("Biome found at ${it.blockX} ${it.blockY} ${it.blockZ}")
                                    .clickEvent(ClickEvent.runCommand("/tp ${it.blockX} ${it.blockY} ${it.blockZ}"))
                            )
                            found = true
                        }
                    }
                }
            }
            if (found) return
        }
        actor.sendMessage("Biome not found")
    }
}


class BiomeParameterType : ParameterType<MinestomCommandActor, Biome> {
    override fun parse(
        input: MutableStringStream,
        context: ExecutionContext<MinestomCommandActor>
    ): Biome {
        val name = input.readString()
        val instance =
            (context.actor().asPlayer() ?: throw CommandErrorException("Only players can use this command")).instance
        val generator = instance.generator()
        if (generator !is MinestomChunkGeneratorWrapper) throw CommandErrorException("This command is only available in a dev pack world")
        return generator.world.biomeProvider.biomes.find { it.id == name }
            ?: throw CommandErrorException("Biome not found for id: $name")
    }

    override fun defaultSuggestions(): SuggestionProvider<MinestomCommandActor> = SuggestionProvider { context ->
        // todo: not working fsr, it only autocompletes sometimes... why?
        val instance = (context.actor().asPlayer() ?: return@SuggestionProvider emptyList()).instance
        val generator = instance.generator()
        if (generator !is MinestomChunkGeneratorWrapper) return@SuggestionProvider emptyList()
        generator.world.biomeProvider.biomes.map { it.id }.also { println(it) }
    }
}
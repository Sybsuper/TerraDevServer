package com.sybsuper.terradevserver.commands

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.command.CommandActor
import revxrsal.commands.node.ExecutionContext
import java.io.File

@Serializable
private data class Mark(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f) {
    constructor(pos: Pos) : this(pos.x, pos.y, pos.z, pos.yaw, pos.pitch)

    fun toPos(): Pos = Pos(x, y, z, yaw, pitch)
}

private val marksFile = File("marks.yml")

private val marks: MutableMap<String, Mark> =
    if (marksFile.exists()) runCatching {
        Yaml.default.decodeFromString<Map<String, Mark>>(marksFile.readText()).toMutableMap()
    }.getOrDefault(mutableMapOf()) else mutableMapOf()

private fun saveMarks() {
    marksFile.writeText(Yaml.default.encodeToString(marks))
}

class MarkNameSuggestionProvider : SuggestionProvider<CommandActor> {
    override fun getSuggestions(context: ExecutionContext<CommandActor>): Collection<String> {
        val prefix = context.input().source().substringAfterLast(' ')
        return marks.keys.filter { it.startsWith(prefix, ignoreCase = true) }
    }
}

class Marks : ICommand {
    @Command("mark")
    @Description("Save current position as a named mark")
    fun mark(actor: Player, @Named("name") name: String) {
        val pos = actor.position
        marks[name] = Mark(pos)
        saveMarks()
        actor.sendMessage("§aMark §f$name §asaved at §f${pos.blockX()} ${pos.blockY()} ${pos.blockZ()}")
    }

    @Command("marks")
    @Description("List all saved marks")
    fun marks(actor: Player) {
        if (marks.isEmpty()) {
            actor.sendMessage("§7No marks saved. Use §f/mark <name>§7 to save one.")
            return
        }
        actor.sendMessage(Component.text("§7--- §bMarks §7(${marks.size}) ---"))
        marks.forEach { (name, mark) ->
            actor.sendMessage(
                Component.text("§a$name §7(${mark.x.toInt()} ${mark.y.toInt()} ${mark.z.toInt()})")
                    .clickEvent(ClickEvent.runCommand("/goto $name"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")))
            )
        }
    }

    @Command("goto")
    @Description("Teleport to a named mark")
    fun goto(actor: Player, @Named("name") @SuggestWith(MarkNameSuggestionProvider::class) name: String) {
        val mark = marks[name]
        if (mark == null) {
            actor.sendMessage("§cMark §f$name §cnot found. Use §f/marks §cto list all marks.")
            return
        }
        actor.teleport(mark.toPos())
        actor.sendMessage("§aTeleported to §f$name")
    }

    @Command("delmark")
    @Description("Delete a named mark")
    fun delmark(actor: Player, @Named("name") @SuggestWith(MarkNameSuggestionProvider::class) name: String) {
        if (marks.remove(name) == null) {
            actor.sendMessage("§cMark §f$name §cnot found.")
            return
        }
        saveMarks()
        actor.sendMessage("§aMark §f$name §adeleted.")
    }
}

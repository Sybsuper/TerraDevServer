package com.sybsuper.terradevserver

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.adventure.audience.Audiences
import org.slf4j.Logger

class PlayerLogger(val backend: Logger) : Logger by backend {
    val players: Audience
        get() = Audiences.players()
    private val errorColor: NamedTextColor = NamedTextColor.DARK_RED
    private fun text(msg: String): Component = Component.text(sanitize(msg), errorColor)
    private fun sanitize(msg: String) = msg.replace("\t","  ")

    override fun error(message: String) {
        backend.error(message)
        players.sendActionBar(text(message))
    }

    override fun error(p0: String?, p1: Throwable?) {
        backend.error(p0, p1)
        players.sendActionBar(text(p0 ?: "[empty error message]"))
        players.sendMessage(text("ERROR: " + (p1?.message ?: "[empty error message]") + "\nSee console for details."))
    }
}
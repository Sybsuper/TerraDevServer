package com.sybsuper.terradevserver.commands

interface ICommand {
    val name: String
        get() = this::class.simpleName!!.lowercase()
}
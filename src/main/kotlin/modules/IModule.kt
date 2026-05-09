package com.sybsuper.terradevserver.modules

interface IModule {
    val isEnabled: Boolean
    fun enable()
}
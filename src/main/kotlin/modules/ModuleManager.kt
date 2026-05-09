package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.logger

object ModuleManager {
    val modules = mutableListOf(
        SyncPositions,
        WatchFolders,
        PlayerCycleTarget,
        RemoveDeadInstances,
        Motd,
        ReloadCommand,
    )

    fun loadModules() {
        modules
            .asSequence()
            .filter { it.isEnabled }
            .forEach {
                logger.info("Enabling module ${it::class.simpleName}")
                runCatching {
                    it.enable()
                }.onFailure { e ->
                    logger.error("Failed to enable module ${it::class.simpleName}", e)
                }
            }
    }
}
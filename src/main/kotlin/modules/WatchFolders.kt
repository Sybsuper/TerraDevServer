package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import com.sybsuper.terradevserver.logger
import com.sybsuper.terradevserver.updateDevPackForPlayer
import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.relativeTo

object WatchFolders : IModule {
    override val isEnabled: Boolean
        get() = config.watchDevPackDirectory

    override fun enable() {
        watchFolder(Path(config.devPackFolder)) {
            logger.info("Detected file change at ${it.path()}")
            val msg =
                "Detected file change at ${
                    it.path().relativeTo(Path(config.devPackFolder).absolute())
                } reloading dev pack"
            val player = PlayerCycleTarget.getPlayerTarget()
            logger.info(msg)
            player.sendMessage(msg)
            updateDevPackForPlayer(player, true)
        }
    }

    private fun watchFolder(path: Path, callback: (DirectoryChangeEvent) -> Unit) {
        DirectoryWatcher.builder()
            .path(path)
            .logger(logger)
            .listener { e ->
                // for some reason I get updates for e.g. 'meta.yml' and then also for 'meta.yml~', so let's ignore the latter
                if (e.path().toString().endsWith("~")) return@listener
                callback(e)
            }.build().watchAsync()
    }
}
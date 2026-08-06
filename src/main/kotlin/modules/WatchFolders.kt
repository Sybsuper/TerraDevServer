package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import com.sybsuper.terradevserver.logger
import com.sybsuper.terradevserver.updateDevPackForPlayer
import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.io.path.relativeTo

object WatchFolders : IModule {
    override val isEnabled: Boolean
        get() = config.watchDevPackDirectory

    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "watch-debounce").also { it.isDaemon = true }
    }
    private var pendingReload: ScheduledFuture<*>? = null

    private val excludeMatchers get() = config.watchExcludePatterns.map { pattern ->
        FileSystems.getDefault().getPathMatcher("glob:$pattern")
    }

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
            pendingReload?.cancel(false)
            pendingReload = scheduler.schedule({
                updateDevPackForPlayer(player, true)
            }, config.watchDebounceMs, TimeUnit.MILLISECONDS)
        }
    }

    private fun watchFolder(path: Path, callback: (DirectoryChangeEvent) -> Unit) {
        DirectoryWatcher.builder()
            .path(path)
            .logger(logger)
            .listener { e ->
                val name = e.path().name
                if (excludeMatchers.any { it.matches(Path(name)) }) return@listener
                callback(e)
            }.build().watchAsync()
    }
}
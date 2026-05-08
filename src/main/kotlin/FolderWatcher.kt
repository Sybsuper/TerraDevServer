package com.sybsuper.terradevserver

import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import java.nio.file.Path

fun watchFolder(path: Path, callback: (DirectoryChangeEvent) -> Unit) {
    DirectoryWatcher.builder()
        .path(path)
        .logger(logger)
        .listener { e ->
            // for some reason I get updates for e.g. 'meta.yml' and then also for 'meta.yml~', so let's ignore the latter
            if (e.path().toString().endsWith("~")) return@listener
            callback(e)
        }.build().watchAsync()
}
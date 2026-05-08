package com.sybsuper.terradevserver

import com.dfsek.terra.api.config.ConfigPack
import com.dfsek.terra.api.registry.key.RegistryKey
import com.dfsek.terra.config.pack.ConfigPackImpl
import com.dfsek.terra.minestom.TerraMinestomPlatform
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Semaphore
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull


private val reloadLock = Semaphore(1)
private val platform = TerraMinestomPlatform()

fun createInstance(): InstanceContainer {
    val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()
    instanceContainer.setChunkSupplier(::LightingChunk)
    reloadLock.acquire()
    createDevPack()
    customDevPackReload()
    val pack = platform.configRegistry.get(lastRegistryKey).getOrNull()
    try {
        assert(pack != null) { "Dev pack not found" }
    } catch (e: AssertionError) {
        logger.error("Dev pack not found")
    }
    platform.worldBuilder(instanceContainer).pack(pack).seed(0L).attach()
    reloadLock.release()
    instanceContainer.viewDistance(10)
    instanceContainer.timeRate = 0
    return instanceContainer
}

private var lastRegistryKey: RegistryKey = RegistryKey.of("dev", "dev_0")

private fun incrementRegistryKey(key: RegistryKey): RegistryKey {
    val namespace = key.namespace
    val id = key.id
    val splitId = id.split("_").toMutableList()
    val intId = splitId.last().toIntOrNull() ?: (0.also { splitId.add("1") })
    val newId = (splitId.dropLast(1) + (intId + 1)).joinToString("_")
    return RegistryKey.of(namespace, newId).also { lastRegistryKey = it }
}

private fun customDevPackReload() {
    try {
        val pack: ConfigPack = ConfigPackImpl(Path(config.devPackFolder), platform)
        platform.configRegistry
        platform.configRegistry.register(incrementRegistryKey(lastRegistryKey), pack)
    } catch (e: Exception) {
        logger.error("Failed to reload dev pack", e)
    }
}

private fun createDevPack(sourcePath: Path = Path.of(config.devPackFolder)) {
    val zipFile = platform.dataFolder.resolve("packs").resolve("dev.zip").toPath()
    zipFolder(sourcePath, zipFile)
    logger.info("Created dev pack at $zipFile")
}

private fun zipFolder(sourceDir: Path, zipFile: Path) {
    ZipOutputStream(Files.newOutputStream(zipFile)).use { zos ->
        Files.walk(sourceDir).use { paths ->
            paths
                .filter { Files.isRegularFile(it) }
                .forEach { file ->
                    val zipEntryName = sourceDir.relativize(file).toString()
                        .replace("\\", "/")
                    val entry = ZipEntry(zipEntryName)
                    zos.putNextEntry(entry)
                    Files.newInputStream(file).use { input ->
                        input.copyTo(zos)
                    }
                    zos.closeEntry()
                }
        }
    }
}
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
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.withLock
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

private val zipLock = ReentrantLock()
val platform = TerraMinestomPlatform()

/**
 * Compiles a new version of the dev pack and creates a new instance container using that pack as its generator.
 *
 * @return The created instance container.
 */
fun createInstance(): InstanceContainer {
    val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()
    instanceContainer.setChunkSupplier(::LightingChunk)
    zipLock.withLock {
        createDevPack()
    }
    synchronized(platform) {
        customDevPackReload()
        val pack = platform.configRegistry.get(lastRegistryKey).getOrNull()
        if (pack == null) {
            logger.error("Dev pack not found, check the server console for errors while loading the pack.")
            return instanceContainer
        }
        platform
            .worldBuilder(instanceContainer)
            .pack(pack)
            .seed(config.worldSeed)
            .attach()
    }
    instanceContainer.viewDistance(10)
    instanceContainer.timeRate = 0
    return instanceContainer
}

private var lastRegistryKey: RegistryKey = RegistryKey.of("dev", "dev_0")

/**
 * Whacky way to increment a registry key by 1 every time we reload the dev pack.
 * This is ugly, but it works.
 *
 * @param key The registry key to increment. e.g. dev_0
 * @return The new registry key. e.g. dev_1
 */
private fun incrementRegistryKey(key: RegistryKey): RegistryKey {
    val namespace = key.namespace
    val id = key.id
    val splitId = id.split("_").toMutableList()
    val intId = splitId.last().toIntOrNull() ?: (0.also { splitId.add("1") })
    val newId = (splitId.dropLast(1) + (intId + 1)).joinToString("_")
    return RegistryKey.of(namespace, newId).also { lastRegistryKey = it }
}

/**
 * Reloads the dev pack by creating a new one. Note, we do not replace the existing pack.
 * This is because we want to keep the old pack around for the instances that still use that pack.
 * Such that the player can still explore those worlds and generate new chunks to be compared to the newer versions.
 */
private fun customDevPackReload() {
    try {
        val pack: ConfigPack = ConfigPackImpl(Path(config.devPackFolder), platform)
        platform.configRegistry.register(incrementRegistryKey(lastRegistryKey), pack)
    } catch (e: Exception) {
        logger.error("Failed to reload dev pack", e)
    }
}

/**
 * Create dev pack by zipping the dev folder.
 *
 * @param sourcePath The path to the source folder. Defaults to the dev folder in the config.
 */
private fun createDevPack(sourcePath: Path = Path.of(config.devPackFolder)) {
    val zipFile = platform.dataFolder.resolve("packs").resolve("dev.zip").toPath()
    zipFolder(sourcePath, zipFile)
    logger.info("Created dev pack at $zipFile")
}

/**
 * Recursively zips a folder to a destination zip file.
 *
 * @param sourceDir The source folder to zip.
 * @param zipFile The destination zip file. This will be overwritten if it already exists.
 */
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
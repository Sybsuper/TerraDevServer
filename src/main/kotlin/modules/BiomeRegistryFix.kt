package com.sybsuper.terradevserver.modules

import com.dfsek.terra.minestom.TerraMinestomPlatform
import com.dfsek.terra.minestom.biome.MinestomUserDefinedBiomePool
import com.sybsuper.terradevserver.config
import com.sybsuper.terradevserver.events.PackPostLoadEvent
import com.sybsuper.terradevserver.events.PrePlayerSendNewInstanceForPackEvent
import com.sybsuper.terradevserver.logger
import com.sybsuper.terradevserver.platform
import net.minestom.server.MinecraftServer
import net.minestom.server.codec.Transcoder
import net.minestom.server.entity.Player
import net.minestom.server.event.EventListener
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import net.minestom.server.instance.Instance
import net.minestom.server.network.ConnectionState
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket
import net.minestom.server.registry.Registry
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.world.biome.Biome
import java.util.*

private val MinestomUserDefinedBiomePool.createdBiomes: HashSet<String>
    get() {
        val field = MinestomUserDefinedBiomePool::class.java.getDeclaredField("createdBiomes")
        field.isAccessible = true
        return field.get(this) as HashSet<String>
    }
private val TerraMinestomPlatform.biomePool: MinestomUserDefinedBiomePool
    get() {
        val field = TerraMinestomPlatform::class.java.getDeclaredField("biomePool")
        field.isAccessible = true
        return field.get(this) as MinestomUserDefinedBiomePool
    }

object BiomeRegistryFix : IModule {
    val waitPlayers: MutableSet<UUID> = mutableSetOf()
    override val isEnabled: Boolean = config.fixBiomeDisconnects
    val playerKnownBiomes = mutableMapOf<UUID, Map<String, Int>>()

    @Suppress("UnstableApiUsage")
    override fun enable() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent::class.java) { e ->
            playerKnownBiomes.remove(e.player.uuid)
        }
        MinecraftServer.getGlobalEventHandler().addListener(PackPostLoadEvent::class.java) { e ->
            val player = e.player ?: return@addListener
            if (!player.isOnline || player.playerConnection.clientState != ConnectionState.PLAY) return@addListener
            logger.info("Detected pack change")
            val newBiomesForPlayer = newBiomesForPlayer(player)
            if (!newBiomesForPlayer) return@addListener
            logger.info("Detected new biome")
            reconfigureBiomesForPlayer(player, e.instance)
        }
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketOutEvent::class.java) { e ->
            val packet = e.packet
            if (packet !is RegistryDataPacket) return@addListener
            if (!packet.registryId.contains("biome")) return@addListener
            val hash = hashPacket(packet)
            playerKnownBiomes[e.player.uuid] = hash
            logger.info("Sending entries:" + hash.entries.joinToString { "${it.key}=${it.value}" })
        }
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent::class.java) { e ->
            if (e.player.uuid in waitPlayers) {
                // make sure we resend the biome registries
                e.setSendRegistryData(true)
            }
        }
        MinecraftServer.getGlobalEventHandler().addListener(PrePlayerSendNewInstanceForPackEvent::class.java) { e ->
            if (e.player.uuid in waitPlayers) {
                e.isCancelled = true
                e.parentEvent?.spawningInstance = MinecraftServer.getInstanceManager().createInstanceContainer()
                e.player.respawnPoint = e.player.position
            }
        }
    }

    fun hashBiomes(biomes: Registry<Biome>): Map<String, Int> =
        biomes.keys().filterNot {
            it.key().namespace() == "minecraft"
        }.associate {
            val biome = biomes.get(it)
            @Suppress("UnstableApiUsage")
            it.key().toString() to Biome.REGISTRY_CODEC.encode(
                Transcoder.CRC32_HASH,
                biome
            ).orElseThrow()
        }

    fun hashPacket(packet: RegistryDataPacket): Map<String, Int> =
        packet.entries
            .filterNot { it.id.startsWith("minecraft:") }
            .associate {
                @Suppress("UnstableApiUsage")
                it.id to Biome.REGISTRY_CODEC.encode(Transcoder.CRC32_HASH,Biome.REGISTRY_CODEC.decode(Transcoder.NBT, it.data).orElseThrow()).orElseThrow()
            }

    fun newBiomesForPlayer(player: Player): Boolean {
        reregisterAllBiomes()
        val newHashes = hashBiomes(MinecraftServer.getBiomeRegistry())
        val oldBiomeHashes = playerKnownBiomes[player.uuid] ?: return true
        if (oldBiomeHashes.size != newHashes.size) {
            logger.info("One or more new biomes have been introduced to this player.")
            return true
        }
        val biomeChanged = newHashes.entries.find { it.value != oldBiomeHashes[it.key] }
        logger.info("New hashes: ${newHashes.entries.joinToString { "${it.key}=${it.value}" }}")
        if (biomeChanged == null) return false
        logger.info("Biome changed: ${biomeChanged.key} -> old: ${oldBiomeHashes[biomeChanged.key]} new: ${biomeChanged.value} ")
        return true
    }

    fun reregisterAllBiomes() {
        platform.biomePool.createdBiomes.clear()
        platform.initializeRegistry()
    }


    fun reconfigureBiomesForPlayer(
        player: Player,
        instanceToBeSentAfter: Instance,
    ) {
        reregisterAllBiomes()
        waitPlayers.add(player.uuid)
        player.sendMessage("Reentering configuration phase to apply new biomes...")

        logger.info("Restarting configuration phase")
        player.startConfigurationPhase()
        sendToInstanceAfterConfigurationPhaseFinish(player, instanceToBeSentAfter)
    }

    @Suppress("UnstableApiUsage")
    private fun sendToInstanceAfterConfigurationPhaseFinish(
        player: Player,
        instanceToBeSentAfter: Instance
    ) {
        val playerPos = player.position
        var sending = false
        var listener: EventListener<PlayerPacketOutEvent>? = null
        listener = EventListener.of(PlayerPacketOutEvent::class.java) { ev ->
            if (ev.packet !is FinishConfigurationPacket || sending) return@of
            sending = true
            player.scheduler().scheduleEndOfTick {
                logger.info("Sending player to new instance")
                waitPlayers.remove(player.uuid)
                player.eventNode().removeListener(listener)
                player.setInstance(instanceToBeSentAfter, playerPos)
                TaskSchedule.stop()
            }
        }
        player.eventNode().addListener(listener)
    }
}
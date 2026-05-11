package com.sybsuper.terradevserver.modules

import com.sybsuper.terradevserver.config
import com.sybsuper.terradevserver.events.PackPostLoadEvent
import com.sybsuper.terradevserver.events.PrePlayerSendNewInstanceForPackEvent
import com.sybsuper.terradevserver.logger
import com.sybsuper.terradevserver.platform
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventListener
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import net.minestom.server.network.packet.server.configuration.FinishConfigurationPacket
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket
import net.minestom.server.timer.TaskSchedule
import java.util.*

object BiomeRegistryFix : IModule {
    val waitPlayers: MutableSet<UUID> = mutableSetOf()
    override val isEnabled: Boolean = config.fixBiomeDisconnects
    val playerKnownBiomes = mutableMapOf<UUID, MutableSet<String>>()

    @Suppress("UnstableApiUsage")
    override fun enable() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent::class.java) { e ->
            playerKnownBiomes.remove(e.player.uuid)
        }
        MinecraftServer.getGlobalEventHandler().addListener(PackPostLoadEvent::class.java) { e ->
            val player = e.player ?: return@addListener
            logger.info("Detected pack change")
            val biomes = e.pack.biomeProvider.biomes.map { it.id }
            val biomesThePlayerKnows = playerKnownBiomes[player.uuid] ?: run {
                // this is the first time we've seen this player, so we can assume they know all biomes, right?
                playerKnownBiomes[player.uuid] = biomes.toMutableSet()
                return@addListener
            }
            if (!biomesThePlayerKnows.addAll(biomes)) return@addListener
            logger.info("Detected new biome")
            waitPlayers.add(player.uuid)
            player.sendMessage("Reentering configuration phase to apply new biomes...")

            // make sure the new biomes are added to Minestom's BiomeRegistry
            platform.initializeRegistry()

            logger.info("Restarting configuration phase")
            player.startConfigurationPhase()
            val playerPos = player.position
            var sending = false
            var listener: EventListener<PlayerPacketOutEvent>? = null
            listener = EventListener.of(PlayerPacketOutEvent::class.java) { ev ->
                if (ev.packet !is FinishConfigurationPacket || sending) return@of
                sending = true
                player.scheduler().scheduleEndOfTick {
                    logger.info("Sending player to new instance")
                    waitPlayers.remove(e.player.uuid)
                    player.eventNode().removeListener(listener)
                    player.setInstance(e.instance, playerPos)
                    TaskSchedule.stop()
                }
            }
            player.eventNode().addListener(listener)
        }
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketOutEvent::class.java) { e ->
            val packet = e.packet
            if (packet !is RegistryDataPacket) return@addListener
            if (!packet.registryId.contains("biome")) return@addListener
            logger.info("Sending entries:" + packet.entries.joinToString { it.id })
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
}
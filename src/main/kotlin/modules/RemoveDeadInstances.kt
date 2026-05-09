package com.sybsuper.terradevserver.modules

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.Instance
import net.minestom.server.timer.TaskSchedule

/**
 * Remove dead instances
 * If an instance has had 0 players in it for 30 seconds, it will be removed.
 * This is to kill hanging instances and prevent memory leaks.
 *
 * @constructor Create empty Remove dead instances
 */
object RemoveDeadInstances : IModule {
    override val isEnabled: Boolean = true
    private val secondsSeenEmpty = mutableMapOf<Instance, Int>()

    override fun enable() {
        MinecraftServer.getSchedulerManager().scheduleTask({
            MinecraftServer.getInstanceManager().instances.filter { it.players.isEmpty() }.forEach {
                secondsSeenEmpty[it] = (secondsSeenEmpty[it] ?: 0) + 1
                if (secondsSeenEmpty[it]!! < 30) return@forEach
                MinecraftServer.getInstanceManager().unregisterInstance(it)
                secondsSeenEmpty.remove(it)
            }
        }, TaskSchedule.seconds(1), TaskSchedule.seconds(1))
    }
}
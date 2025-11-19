package net.immortaldevs.bindcmd

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.immortaldevs.bindcmd.config.ConfigEntry
import net.immortaldevs.bindcmd.config.ConfigLoader
import net.minecraft.server.network.ServerPlayerEntity

fun initServer() {
    PayloadTypeRegistry.playS2C().register(
        ConfigS2CPayload.ID, ConfigS2CPayload.CODEC
    )

    var bindings = listOf<ConfigEntry>()
    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { minecraftServer ->
        val loader = ConfigLoader(minecraftServer.runDirectory.toFile())
        bindings = loader.read() ?: emptyList()
    })
    ServerEntityEvents.ENTITY_LOAD.register(ServerEntityEvents.Load { entity, _ ->
        if (entity !is ServerPlayerEntity) return@Load
        val payload = ConfigS2CPayload(bindings)
        ServerPlayNetworking.send(entity, payload)
    })
}

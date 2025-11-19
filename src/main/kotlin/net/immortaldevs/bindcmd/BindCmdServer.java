package net.immortaldevs.bindcmd;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.immortaldevs.bindcmd.config.ConfigEntry;
import net.immortaldevs.bindcmd.config.ConfigLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class BindCmdServer implements DedicatedServerModInitializer {
    private static java.util.List<ConfigEntry> bindings = java.util.List.of();

    @Override
    public void onInitializeServer() {
        PayloadTypeRegistry.playS2C().register(
                ConfigS2CPayload.ID, ConfigS2CPayload.CODEC
        );

        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer minecraftServer) -> {
            ConfigLoader loader = new ConfigLoader(minecraftServer.getRunDirectory().toFile());
            java.util.List<ConfigEntry> data = loader.read();
            bindings = data != null ? data : java.util.List.of();
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            ConfigS2CPayload payload = new ConfigS2CPayload(bindings);
            ServerPlayNetworking.send(player, payload);
        });
    }
}

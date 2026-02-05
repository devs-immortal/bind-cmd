package net.immortaldevs.bindcmd;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.immortaldevs.bindcmd.config.ConfigEntry;
import net.immortaldevs.bindcmd.config.ConfigLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class BindCmdServer implements DedicatedServerModInitializer {
    private static List<ConfigEntry> bindings = List.of();

    @Override
    public void onInitializeServer() {
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ConfigPayload.ID, ConfigPayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer minecraftServer) -> {
            ConfigLoader loader = new ConfigLoader(minecraftServer.getServerDirectory().toFile());
            bindings = loader.read();
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, _) -> {
            if (!(entity instanceof ServerPlayer player)) return;
            ConfigPayload payload = new ConfigPayload(bindings);
            ServerPlayNetworking.send(player, payload);
        });
    }
}

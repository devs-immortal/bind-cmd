package net.immortaldevs.bindcmd;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.client.Minecraft;

public final class BindCmdClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ConfigPayload.ID, ConfigPayload.CODEC);

        Config.load();

        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> onPlayerJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> onPlayerDisconnect());

        ClientPlayNetworking.registerGlobalReceiver(
                ConfigPayload.ID,
                (payload, _) -> Config.setServerBindings(payload.config())
        );
    }

    private static void onPlayerJoin(Minecraft client) {
        Config.clearServerBindings();
        if (client.getSingleplayerServer() != null && client.getSingleplayerServer().isSingleplayer()) {
            var server = client.getSingleplayerServer();
            var dir = server.storageSource.getLevelDirectory().path();
            Config.loadWorldConfig(dir);
        }
    }

    private static void onPlayerDisconnect() {
        Config.clearServerBindings();
    }
}

package net.immortaldevs.bindcmd;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.client.MinecraftClient;

public final class BindCmdClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(
                ConfigS2CPayload.ID, ConfigS2CPayload.CODEC
        );

        Config.load();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onPlayerJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onPlayerDisconnect());

        ClientPlayNetworking.registerGlobalReceiver(
                ConfigS2CPayload.ID,
                (payload, context) -> Config.setServerBindings(payload.config())
        );
    }

    private static void onPlayerJoin(MinecraftClient client) {
        Config.clearServerBindings();
        if (client.getServer() != null && client.getServer().isSingleplayer()) {
            var server = client.getServer();
            var dir = server.session.getDirectory().path();
            Config.loadWorldConfig(dir);
        }
    }

    private static void onPlayerDisconnect() {
        Config.clearServerBindings();
    }
}

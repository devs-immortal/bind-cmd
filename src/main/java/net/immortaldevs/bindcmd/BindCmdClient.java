package net.immortaldevs.bindcmd;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.client.Minecraft;

public final class BindCmdClient implements ClientModInitializer {
    private static long lastKeyPress = 0L;
    private static final long COOLDOWN = 200L;

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ConfigPayload.ID, ConfigPayload.CODEC);

        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(BindCmdClient::onEndClientTick);
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

    private static void onEndClientTick(Minecraft client) {
        if (System.currentTimeMillis() - lastKeyPress < COOLDOWN) return;
        for (CommandBinding binding : Config.getBindings()) {
            handleBinding(client, binding);
        }
    }

    private static void handleBinding(Minecraft client, CommandBinding binding) {
        var networkHandler = client.getConnection();

        if (binding.isUnknown() || networkHandler == null) {
            return;
        }

        if (binding.isPressed() && !binding.wasPressed) {
            var commands = Config.getBindings().stream()
                    .filter(b -> b.getTranslationKey().equals(binding.getTranslationKey()))
                    .map(b -> b.command)
                    .toList();

            for (String command : commands) {
                Command cmd = new Command(command);
                switch (cmd.getType()) {
                    case COMMAND -> networkHandler.sendCommand(cmd.getCommand());
                    case MESSAGE -> networkHandler.sendChat(cmd.getCommand());
                    case NONE -> {
                        return;
                    }
                }
            }
            lastKeyPress = System.currentTimeMillis();
            binding.wasPressed = true;
        }

        if (!binding.isPressed() && binding.wasPressed) {
            binding.wasPressed = false;
        }
    }
}

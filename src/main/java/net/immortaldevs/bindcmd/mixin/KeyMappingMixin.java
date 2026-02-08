package net.immortaldevs.bindcmd.mixin;

import net.immortaldevs.bindcmd.Command;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = KeyMapping.class, priority = 999)
public abstract class KeyMappingMixin {
    @Unique
    private static long lastKeyPress = 0L;
    @Unique
    private static final long COOLDOWN = 200L;

    @Inject(method = "onKeyPressed", at = @At("HEAD"))
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        if (System.currentTimeMillis() - lastKeyPress < COOLDOWN) return;

        final var client = Minecraft.getInstance();
        final var networkHandler = client.getConnection();

        if (networkHandler == null) return;

        final var commands = Config.getCommands(key.());
        handleCommands(commands, networkHandler);
    }

    @Unique
    private static void handleCommands(List<String> commands, ClientPacketListener networkHandler) {
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
    }
}
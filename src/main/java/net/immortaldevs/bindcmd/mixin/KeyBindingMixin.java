package net.immortaldevs.bindcmd.mixin;

import net.immortaldevs.bindcmd.Command;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = KeyBinding.class, priority = 999)
public abstract class KeyBindingMixin {
    @Unique
    private static long lastKeyPress = 0L;
    @Unique
    private static final long COOLDOWN = 200L;

    @Inject(method = "onKeyPressed", at = @At("HEAD"))
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        if (System.currentTimeMillis() - lastKeyPress < COOLDOWN) return;

        final var client = MinecraftClient.getInstance();
        final var networkHandler = client.getNetworkHandler();

        if (networkHandler == null) return;

        final var commands = Config.getCommands(key.getTranslationKey());
        handleCommands(commands, networkHandler);
    }

    @Unique
    private static void handleCommands(List<String> commands, ClientPlayNetworkHandler networkHandler) {
        for (String command : commands) {
            Command cmd = new Command(command);
            switch (cmd.getType()) {
                case COMMAND -> networkHandler.sendChatCommand(cmd.getCommand());
                case MESSAGE -> networkHandler.sendChatMessage(cmd.getCommand());
                case NONE -> {
                    return;
                }
            }
        }
        lastKeyPress = System.currentTimeMillis();
    }
}
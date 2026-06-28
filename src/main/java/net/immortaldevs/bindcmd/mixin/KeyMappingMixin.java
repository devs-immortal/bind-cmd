package net.immortaldevs.bindcmd.mixin;

import com.mojang.blaze3d.platform.InputConstants;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = KeyMapping.class, priority = 999)
public abstract class KeyMappingMixin {
    @Unique
    private static final Map<String, Long> lastKeyPress = new HashMap<>();
    @Unique
    private static final long COOLDOWN = 200L;

    @Inject(method = "click", at = @At("HEAD"))
    private static void click(InputConstants.Key key, CallbackInfo ci) {
        final String name = key.getName();
        final List<String> commands = Config.getCommands(name);
        if (commands.isEmpty()) return;

        final long now = System.currentTimeMillis();
        final Long previous = lastKeyPress.get(name);
        if (previous != null && now - previous < COOLDOWN) return;

        final var networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler == null) return;

        lastKeyPress.put(name, now);
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
    }
}

package net.immortaldevs.bindcmd.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.immortaldevs.bindcmd.BindSource;
import net.immortaldevs.bindcmd.CommandBinding;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.world.level.GameType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class KeybindCooldownRegressionGameTest implements FabricClientGameTest {
    private static final String GAMEMODE_COMMAND_KEY = "key.keyboard.k";
    private static final String UNBOUND_KEY = "key.keyboard.g";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = joinWorldWithCheatsEnabled(context)) {
            CommandBinding gamemodeBinding = bindKey(context, GAMEMODE_COMMAND_KEY, "/gamemode creative");

            try {
                setServerGameMode(singleplayer, GameType.SURVIVAL);
                waitForClientGameMode(context, GameType.SURVIVAL);

                pressKeyImmediately(context, UNBOUND_KEY);
                pressKeyImmediately(context, GAMEMODE_COMMAND_KEY);

                waitForClientGameMode(context, GameType.CREATIVE);
            } finally {
                removeBindings(context, gamemodeBinding);
            }
        }
    }

    private static TestSingleplayerContext joinWorldWithCheatsEnabled(ClientGameTestContext context) {
        TestSingleplayerContext singleplayer = context.worldBuilder()
                .adjustSettings(settings -> settings.setAllowCommands(true))
                .create();
        singleplayer.getClientLevel().waitForChunksRender();
        context.waitFor(client -> client.player != null);
        return singleplayer;
    }

    private static CommandBinding bindKey(ClientGameTestContext context, String translationKey, String command) {
        CommandBinding binding = new CommandBinding(List.of(command), translationKey, BindSource.CLIENT);
        context.runOnClient(_ -> Config.add(binding));
        return binding;
    }

    private static void pressKeyImmediately(ClientGameTestContext context, String translationKey) {
        context.getInput().holdKeyFor(InputConstants.getKey(translationKey), 0);
    }

    private static void setServerGameMode(TestSingleplayerContext singleplayer, GameType gameMode) {
        singleplayer.getServer().runCommand("gamemode " + gameMode.getName() + " @a");
    }

    private static void waitForClientGameMode(ClientGameTestContext context, GameType gameMode) {
        context.waitFor(client -> client.gameMode != null && client.gameMode.getPlayerMode() == gameMode);
    }

    private static void removeBindings(ClientGameTestContext context, CommandBinding... bindings) {
        context.runOnClient(_ -> {
            for (CommandBinding binding : bindings) {
                Config.remove(binding);
            }
        });
    }
}

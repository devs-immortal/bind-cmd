package net.immortaldevs.bindcmd.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.immortaldevs.bindcmd.BindSource;
import net.immortaldevs.bindcmd.CommandBinding;
import net.immortaldevs.bindcmd.config.Config;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public class KeybindFlowGameTest implements FabricClientGameTest {
    private static final String GAMEMODE_COMMAND_KEY = "key.keyboard.k";
    private static final String CHAT_MESSAGE_KEY = "key.keyboard.j";
    private static final String CHAT_MESSAGE = "hello from bindcmd";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = joinWorldWithCheatsEnabled(context)) {
            ServerChatWatcher chatWatcher = ServerChatWatcher.attach();

            CommandBinding gamemodeBinding = bindKey(context, GAMEMODE_COMMAND_KEY, "/gamemode creative");
            CommandBinding messageBinding = bindKey(context, CHAT_MESSAGE_KEY, CHAT_MESSAGE);

            try {
                setServerGameMode(singleplayer, GameType.SURVIVAL);
                waitForClientGameMode(context, GameType.SURVIVAL);

                pressKey(context, GAMEMODE_COMMAND_KEY);
                waitForClientGameMode(context, GameType.CREATIVE);

                pressKey(context, CHAT_MESSAGE_KEY);
                chatWatcher.waitForMessage(context, CHAT_MESSAGE);
            } finally {
                removeBindings(context, gamemodeBinding, messageBinding);
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

    private static void pressKey(ClientGameTestContext context, String translationKey) {
        context.getInput().pressKey(InputConstants.getKey(translationKey));
        waitForKeybindCooldown(context);
    }

    private static void waitForKeybindCooldown(ClientGameTestContext context) {
        context.waitTicks(20);
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

    private static final class ServerChatWatcher {
        private final AtomicReference<String> lastMessage = new AtomicReference<>();

        private static ServerChatWatcher attach() {
            ServerChatWatcher watcher = new ServerChatWatcher();
            ServerMessageEvents.CHAT_MESSAGE.register(
                    (message, _, _) -> watcher.lastMessage.set(message.signedContent())
            );
            return watcher;
        }

        private void waitForMessage(ClientGameTestContext context, String expected) {
            context.waitFor(_ -> expected.equals(lastMessage.get()));
        }
    }
}

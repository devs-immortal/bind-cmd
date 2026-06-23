package net.immortaldevs.bindcmd.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.immortaldevs.bindcmd.Command;

@SuppressWarnings("UnstableApiUsage")
public class HistoryGameTest implements FabricClientGameTest {
    private static final String HISTORY_COMMAND = "/home";
    private static final String HISTORY_MESSAGE = "hello from bindcmd";
    private static final String HISTORY_REFERENCE = "@0";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext _ = joinWorld(context)) {
            assertCommand(context, HISTORY_COMMAND, Command.CmdType.COMMAND, "home");

            seedChatHistory(context, HISTORY_COMMAND);
            assertCommand(context, HISTORY_REFERENCE, Command.CmdType.COMMAND, "home");

            seedChatHistory(context, HISTORY_MESSAGE);
            assertCommand(context, HISTORY_REFERENCE, Command.CmdType.MESSAGE, HISTORY_MESSAGE);
        }
    }

    private static TestSingleplayerContext joinWorld(ClientGameTestContext context) {
        TestSingleplayerContext singleplayer = context.worldBuilder().create();
        singleplayer.getClientLevel().waitForChunksRender();
        context.waitFor(client -> client.player != null);
        return singleplayer;
    }

    private static void seedChatHistory(ClientGameTestContext context, String entry) {
        context.runOnClient(client -> {
            client.gui.getChat().getRecentChat().addLast(entry);
            if (!client.gui.getChat().getRecentChat().contains(entry)) {
                throw new AssertionError(
                        "could not seed chat history. getRecentChat() did not retain \"" + entry + "\""
                );
            }
        });
    }

    private static void assertCommand(
            ClientGameTestContext context, String input, Command.CmdType expectedType, String expectedCommand
    ) {
        context.runOnClient(_ -> {
            Command command = new Command(input);
            if (command.getType() != expectedType) {
                throw new AssertionError(
                        "new Command(\"" + input + "\").getType() = " + command.getType() + ", expected " + expectedType
                );
            }
            if (!expectedCommand.equals(command.getCommand())) {
                throw new AssertionError(
                        "new Command(\"" + input + "\").getCommand() = \"" + command.getCommand() + "\", expected \"" + expectedCommand + "\""
                );
            }
        });
    }
}

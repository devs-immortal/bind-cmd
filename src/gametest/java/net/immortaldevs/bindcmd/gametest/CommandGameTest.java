package net.immortaldevs.bindcmd.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.immortaldevs.bindcmd.Command;
import net.minecraft.client.player.LocalPlayer;

@SuppressWarnings("UnstableApiUsage")
public class CommandGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientLevel().waitForChunksRender();
            context.waitFor(client -> client.player != null);

            double y = context.computeOnClient(client -> client.player.getY());
            singleplayer.getServer().runCommand("tp @a 100.5 " + y + " -100.5");
            context.waitFor(client -> client.player != null && client.player.getBlockX() == 100);

            context.runOnClient(client -> {
                LocalPlayer player = client.player;
                if (player == null) {
                    throw new AssertionError("expected a player to be present");
                }

                assertCommand("${health}", String.valueOf(player.getHealth()));

                assertCommand("${x + 0}", String.valueOf(player.getBlockX()));
                assertCommand("${z + 0}", String.valueOf(player.getBlockZ()));

                assertCommand("${min(3, 9)}", "3");

                assertCommand("${max(3, 9)}", "9");
            });
        }
    }

    private static void assertCommand(String input, String expected) {
        String actual = new Command(input).getCommand();
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "Command(\"" + input + "\").getCommand() = \"" + actual + "\", expected \"" + expected + "\""
            );
        }
    }
}

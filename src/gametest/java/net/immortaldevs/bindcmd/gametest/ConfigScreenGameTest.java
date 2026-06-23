package net.immortaldevs.bindcmd.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.immortaldevs.bindcmd.CommandBinding;
import net.immortaldevs.bindcmd.config.BindingsListWidget;
import net.immortaldevs.bindcmd.config.Config;
import net.immortaldevs.bindcmd.config.ModConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ConfigScreenGameTest implements FabricClientGameTest {
    private static final String ADD_COMMAND_KEY = "text.bindcmd.config.add_command";
    private static final String DONE_KEY = "gui.done";
    private static final String REBIND_KEY = "key.keyboard.b";

    @Override
    public void runTest(ClientGameTestContext context) {
        Screen lastScreen = context.computeOnClient(client -> client.gui.screen());
        if (lastScreen == null) {
            throw new AssertionError("expected a non-null initial screen to use as the parent screen");
        }

        List<CommandBinding> snapshot = context.computeOnClient(_ -> new ArrayList<>(Config.getBindings()));
        try {
            testDoneButton(context, lastScreen);
            testAddCommandButton(context, lastScreen);
            testEditRebindFlow(context, lastScreen);
            testDeleteButton(context, lastScreen);
            testInputFieldEditing(context, lastScreen);
        } finally {
            context.runOnClient(client -> {
                for (CommandBinding binding : new ArrayList<>(Config.getBindings())) {
                    if (!snapshot.contains(binding)) {
                        Config.remove(binding);
                    }
                }
                client.gui.setScreen(lastScreen);
            });
        }
    }

    private static void testDoneButton(ClientGameTestContext context, Screen lastScreen) {
        openConfigScreen(context, lastScreen);

        context.clickScreenButton(DONE_KEY);

        context.waitFor(client -> client.gui.screen() == lastScreen);
    }

    private static void testAddCommandButton(ClientGameTestContext context, Screen lastScreen) {
        openConfigScreen(context, lastScreen);

        int bindingsBefore = context.computeOnClient(_ -> Config.getBindings().size());
        int entriesBefore = context.computeOnClient(client -> findBindingsList(currentScreen(client)).children().size());

        context.clickScreenButton(ADD_COMMAND_KEY);

        CommandBinding added = context.computeOnClient(client -> {
            List<CommandBinding> bindings = Config.getBindings();
            if (bindings.size() != bindingsBefore + 1) {
                throw new AssertionError("expected " + (bindingsBefore + 1) + " bindings, found " + bindings.size());
            }

            CommandBinding last = bindings.getLast();
            if (last.commands.size() != 1 || !"/".equals(last.commands.getFirst())) {
                throw new AssertionError("added binding commands = " + last.commands + ", expected [/]");
            }

            int entriesAfter = findBindingsList(currentScreen(client)).children().size();
            if (entriesAfter != entriesBefore + 1) {
                throw new AssertionError("expected " + (entriesBefore + 1) + " list entries, found " + entriesAfter);
            }

            return last;
        });

        context.runOnClient(_ -> Config.remove(added));
    }

    private static void testEditRebindFlow(ClientGameTestContext context, Screen lastScreen) {
        CommandBinding binding = new CommandBinding("/test-edit");
        context.runOnClient(_ -> Config.add(binding));
        try {
            openConfigScreen(context, lastScreen);

            context.runOnClient(client -> {
                ModConfigScreen screen = currentScreen(client);
                pressButton(editButton(lastEntry(findBindingsList(screen))));
                if (screen.getSelectedKeyMapping() != binding.getKey()) {
                    throw new AssertionError("Edit button did not select the binding");
                }
            });

            InputConstants.Key target = InputConstants.getKey(REBIND_KEY);
            context.getInput().pressKey(target);

            context.runOnClient(_ -> {
                String actual = binding.getKey().getName();
                if (!target.getName().equals(actual)) {
                    throw new AssertionError("key is \"" + actual + "\", expected \"" + target.getName() + "\"");
                }
            });
        } finally {
            context.runOnClient(_ -> {
                binding.unbind();
                Config.remove(binding);
            });
        }
    }

    private static void testDeleteButton(ClientGameTestContext context, Screen lastScreen) {
        CommandBinding binding = new CommandBinding("/test-delete");
        context.runOnClient(_ -> Config.add(binding));
        boolean[] removedBySut = {false};
        try {
            openConfigScreen(context, lastScreen);

            int bindingsBefore = context.computeOnClient(_ -> Config.getBindings().size());

            context.runOnClient(client -> {
                pressButton(deleteButton(lastEntry(findBindingsList(currentScreen(client)))));

                List<CommandBinding> bindings = Config.getBindings();
                if (bindings.size() != bindingsBefore - 1 || bindings.contains(binding)) {
                    throw new AssertionError("Remove button did not delete the binding (size " + bindings.size() + ")");
                }
                removedBySut[0] = true;
            });
        } finally {
            context.runOnClient(_ -> {
                if (!removedBySut[0]) {
                    Config.remove(binding);
                }
            });
        }
    }

    private static void testInputFieldEditing(ClientGameTestContext context, Screen lastScreen) {
        CommandBinding binding = new CommandBinding("/before");
        context.runOnClient(_ -> Config.add(binding));
        try {
            openConfigScreen(context, lastScreen);

            context.runOnClient(client -> inputField(lastEntry(findBindingsList(currentScreen(client)))).setValue("/after"));

            context.runOnClient(_ -> {
                if (!"/after".equals(binding.commands.getFirst())) {
                    throw new AssertionError("editing the text field didn't update the command: " + binding.commands);
                }
            });
        } finally {
            context.runOnClient(_ -> Config.remove(binding));
        }
    }

    private static void openConfigScreen(ClientGameTestContext context, Screen lastScreen) {
        context.setScreen(() -> new ModConfigScreen(lastScreen));
        context.waitForScreen(ModConfigScreen.class);
        context.waitTick();
    }

    private static ModConfigScreen currentScreen(Minecraft client) {
        if (client.gui.screen() instanceof ModConfigScreen screen) {
            return screen;
        }
        throw new AssertionError("expected config screen, found " + client.gui.screen());
    }

    private static BindingsListWidget findBindingsList(ModConfigScreen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof BindingsListWidget list) {
                return list;
            }
        }
        throw new AssertionError("list widget not found in screen.children()");
    }

    private static BindingsListWidget.BindingEntry lastEntry(BindingsListWidget list) {
        List<? extends GuiEventListener> entries = list.children();
        if (entries.isEmpty()) {
            throw new AssertionError("bindings list has no entries");
        }
        return (BindingsListWidget.BindingEntry) entries.getLast();
    }

    private static Button editButton(BindingsListWidget.BindingEntry entry) {
        return (Button) entry.children().getFirst();
    }

    private static Button deleteButton(BindingsListWidget.BindingEntry entry) {
        return (Button) entry.children().get(1);
    }

    private static EditBox inputField(BindingsListWidget.BindingEntry entry) {
        return (EditBox) entry.children().get(2);
    }

    private static void pressButton(Button button) {
        button.onPress(new MouseButtonInfo(GLFW.GLFW_KEY_UNKNOWN, 0));
    }
}

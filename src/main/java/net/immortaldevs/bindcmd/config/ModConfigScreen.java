package net.immortaldevs.bindcmd.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.immortaldevs.bindcmd.CommandBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class ModConfigScreen extends OptionsSubScreen {
    private KeyMapping selectedKeyMapping;
    private BindingsListWidget bindingsList;

    public ModConfigScreen(Screen lastScreen) {
        super(lastScreen, null, Component.translatable("text.bindcmd.config.title"));
    }

    public void setSelectedBinding(CommandBinding binding) {
        selectedKeyMapping = binding.getKey();
    }

    public KeyMapping getSelectedKeyMapping() {
        return selectedKeyMapping;
    }

    public void clearSelectedBinding() {
        selectedKeyMapping = null;
    }

    @Override
    protected void addContents() {
        bindingsList = layout.addToContents(new BindingsListWidget(this, minecraft));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addTitle() {
        layout.addTitleHeader(Component.translatable("text.bindcmd.config.title"), font);
    }

    @Override
    protected void addFooter() {
        Button addButton = Button.builder(Component.translatable("text.bindcmd.config.add_command"), _ -> addButtonPressed()).build();
        Button doneButton = Button.builder(CommonComponents.GUI_DONE, _ -> doneButtonPressed()).build();

        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(addButton);
        footer.addChild(doneButton);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        bindingsList.updateSize(width, layout);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        if (selectedKeyMapping != null) {
            for (CommandBinding binding : Config.getBindings()) {
                if (binding.getKey() == selectedKeyMapping) {
                    binding.setBoundMouse(click.button());
                }
            }
            clearSelectedBinding();
            bindingsList.update();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        if (selectedKeyMapping != null) {
            for (CommandBinding binding : Config.getBindings()) {
                if (binding.getKey() == selectedKeyMapping) {
                    binding.setBoundKey(input);
                }
            }
            clearSelectedBinding();
            bindingsList.update();
            return true;
        }
        return super.keyPressed(input);
    }

    private void doneButtonPressed() {
        Config.save();
        minecraft.setScreen(lastScreen);
    }

    private void addButtonPressed() {
        CommandBinding binding = new CommandBinding("/");
        Config.add(binding);
        bindingsList.addBinding(binding);
        bindingsList.update();
    }
}

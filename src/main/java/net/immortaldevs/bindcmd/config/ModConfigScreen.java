package net.immortaldevs.bindcmd.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.immortaldevs.bindcmd.CommandBinding;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class ModConfigScreen extends GameOptionsScreen {
    private KeyBinding selectedKeyBinding;
    private BindingsListWidget bindingsList;

    public ModConfigScreen(Screen parent) {
        super(parent, null, Text.translatable("text.bindcmd.config.title"));
    }

    public void setSelectedBinding(CommandBinding binding) {
        selectedKeyBinding = binding.getKey();
    }

    public KeyBinding getSelectedKeyBinding() {
        return selectedKeyBinding;
    }

    public void clearSelectedBinding() {
        selectedKeyBinding = null;
    }

    @Override
    protected void initBody() {
        bindingsList = layout.addBody(new BindingsListWidget(this, client));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void initHeader() {
        layout.addHeader(new TextWidget(Text.translatable("text.bindcmd.config.title"), textRenderer));
    }

    @Override
    protected void initFooter() {
        ButtonWidget addButton = ButtonWidget.builder(Text.translatable("text.bindcmd.config.add_command"), button -> addButtonPressed()).build();
        ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> doneButtonPressed()).build();

        DirectionalLayoutWidget footer = layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        footer.add(addButton);
        footer.add(doneButton);
    }

    @Override
    protected void refreshWidgetPositions() {
        layout.refreshPositions();
        bindingsList.position(width, layout);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (selectedKeyBinding != null) {
            for (CommandBinding binding : Config.getBindings()) {
                if (binding.getKey() == selectedKeyBinding) {
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
    public boolean keyPressed(KeyInput input) {
        if (selectedKeyBinding != null) {
            for (CommandBinding binding : Config.getBindings()) {
                if (binding.getKey() == selectedKeyBinding) {
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
        if (client == null) return;
        client.setScreen(parent);
    }

    private void addButtonPressed() {
        CommandBinding binding = new CommandBinding("/");
        Config.add(binding);
        bindingsList.addBinding(binding);
        bindingsList.update();
    }
}

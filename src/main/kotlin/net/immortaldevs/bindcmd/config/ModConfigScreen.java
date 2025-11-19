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
    public KeyBinding selectedKeyBinding;
    private BindingsListWidget bindingsList;

    public ModConfigScreen(Screen parent) {
        super(parent, null, Text.translatable("text.bindcmd.config.title"));
    }

    @Override
    protected void initBody() {
        this.bindingsList = this.layout.addBody(new BindingsListWidget(this, this.client));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void initHeader() {
        this.layout.addHeader(new TextWidget(Text.translatable("text.bindcmd.config.title"), this.textRenderer));
    }

    @Override
    protected void initFooter() {
        ButtonWidget addButton = ButtonWidget.builder(Text.translatable("text.bindcmd.config.add_command"), button -> this.addButtonPressed()).build();
        ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> this.doneButtonPressed()).build();

        DirectionalLayoutWidget footer = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        footer.add(addButton);
        footer.add(doneButton);
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        this.bindingsList.position(this.width, this.layout);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.selectedKeyBinding != null) {
            for (CommandBinding binding : Config.getBindings()) {
                if (binding.getKey() == this.selectedKeyBinding) {
                    binding.setBoundMouse(click.button());
                }
            }
            this.selectedKeyBinding = null;
            this.bindingsList.update();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.selectedKeyBinding != null) {
            for (CommandBinding binding : Config.getBindings()) {
                if (binding.getKey() == this.selectedKeyBinding) {
                    binding.setBoundKey(input);
                }
            }
            this.selectedKeyBinding = null;
            this.bindingsList.update();
            return true;
        }
        return super.keyPressed(input);
    }

    private void doneButtonPressed() {
        Config.save();
        if (this.client == null) return;
        this.client.setScreen(this.parent);
    }

    private void addButtonPressed() {
        CommandBinding binding = new CommandBinding("/");
        Config.add(binding);
        this.bindingsList.addBinding(binding);
        this.bindingsList.update();
    }
}

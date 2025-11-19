package net.immortaldevs.bindcmd.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.immortaldevs.bindcmd.BindSource;
import net.immortaldevs.bindcmd.CommandBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class BindingsListWidget extends ElementListWidget<BindingsListWidget.BindingEntry> {
    final ModConfigScreen parent;
    private final Tooltip serverBindingTooltip = Tooltip.of(Text.translatable("text.bindcmd.config.server_setting"));
    private final Tooltip worldBindingTooltip = Tooltip.of(Text.translatable("text.bindcmd.config.world_setting"));

    public BindingsListWidget(ModConfigScreen parent, MinecraftClient client) {
        super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 20);
        this.parent = parent;
        for (CommandBinding binding : Config.getBindings()) {
            this.addEntry(new BindingEntry(binding));
        }
    }

    public void update() {
        KeyBinding.updateKeysByCode();
        this.updateChildren();
    }

    public void addBinding(CommandBinding binding) {
        this.addEntry(new BindingEntry(binding));
    }

    private void updateChildren() {
        for (BindingEntry entry : this.children()) {
            entry.update();
        }
    }

    @Override
    public int getScrollbarX() {
        return super.getScrollbarX() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Environment(EnvType.CLIENT)
    public class BindingEntry extends ElementListWidget.Entry<BindingEntry> {
        private final CommandBinding binding;
        private final ButtonWidget editButton;
        private final ButtonWidget deleteButton;
        private final TextFieldWidget inputField;
        private boolean hovered = false;
        private boolean duplicate = false;

        public BindingEntry(CommandBinding binding) {
            this.binding = binding;
            this.editButton = ButtonWidget.builder(Text.empty(), button -> this.editButtonPressed())
                    .dimensions(0, 0, 75, 20)
                    .build();
            this.deleteButton = ButtonWidget.builder(Text.translatable("text.bindcmd.config.remove"), button -> this.deleteButtonPressed())
                    .dimensions(0, 0, 50, 20)
                    .build();
            this.inputField = new TextFieldWidget(
                    MinecraftClient.getInstance().textRenderer,
                    0,
                    0,
                    124,
                    16,
                    binding.getKey().getBoundKeyLocalizedText()
            );
            this.inputField.setChangedListener(this::inputFieldChanged);
            this.inputField.setMaxLength(256);
            this.inputField.setText(binding.command);
            this.update();
        }

        private void inputFieldChanged(String text) {
            this.binding.command = text;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            TextRenderer textRenderer = BindingsListWidget.this.client.textRenderer;
            int textWidth = this.getWidth() - this.editButton.getWidth() - this.deleteButton.getWidth() - 3;
            boolean isClient = this.binding.getSource() == BindSource.CLIENT;
            Tooltip tooltip = null;
            if (this.binding.getSource() == BindSource.SERVER) {
                tooltip = serverBindingTooltip;
            } else if (this.binding.getSource() == BindSource.WORLD) {
                tooltip = worldBindingTooltip;
            }

            int x = this.getX();
            int y = this.getY();
            int height = this.getHeight();

            if (mouseX > x - 5 && mouseX < x + 123 && mouseY > y && mouseY < y + 20) {
                this.inputField.setX(x - 4);
                this.inputField.setY(y + 2);
                this.inputField.setWidth(textWidth);
                this.inputField.setTooltip(tooltip);
                this.inputField.setEditable(isClient);
                this.inputField.render(context, mouseX, mouseY, delta);
                if (!this.hovered && this.inputField.isFocused()) {
                    this.inputField.setFocused(false);
                    this.inputField.setCursorToStart(false);
                }
                this.hovered = true;
            } else {
                int yPosition = y + height / 2 - 2;
                String text = this.cutString(this.binding.command, textRenderer, textWidth - 12);
                context.drawTextWithShadow(textRenderer, text, x, yPosition, Colors.WHITE);
                this.hovered = false;
            }

            this.editButton.setTooltip(tooltip);
            this.editButton.active = isClient;
            this.editButton.setX(x + this.getWidth() - this.editButton.getWidth() - this.deleteButton.getWidth() - 2);
            this.editButton.setY(y);

            this.deleteButton.setTooltip(tooltip);
            this.deleteButton.active = isClient;
            this.deleteButton.setX(x + this.getWidth() - this.deleteButton.getWidth());
            this.deleteButton.setY(y);
            this.deleteButton.render(context, mouseX, mouseY, delta);

            if (this.duplicate) {
                int j = this.editButton.getX() - 6;
                context.fill(j, y + 2, j + 3, y + height + 2, 0xFFFF0000);
            }

            this.editButton.render(context, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends Element> children() {
            List<Element> list = new ArrayList<>();
            list.add(this.editButton);
            list.add(this.deleteButton);
            list.add(this.inputField);
            return list;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> list = new ArrayList<>();
            list.add(this.editButton);
            list.add(this.deleteButton);
            list.add(this.inputField);
            return list;
        }

        public void update() {
            this.editButton.setMessage(this.binding.getKey().getBoundKeyLocalizedText());
            this.duplicate = false;

            Text mutableText = Text.empty();
            if (!this.binding.getKey().isUnbound()) {
                KeyBinding[] allKeys = BindingsListWidget.this.client.options.allKeys;
                for (KeyBinding keyBinding : allKeys) {
                    if (keyBinding != this.binding.getKey() && this.binding.getKey().equals(keyBinding)) {
                        if (this.duplicate) {
                            mutableText = mutableText.copy().append(", ");
                        }
                        this.duplicate = true;
                        mutableText = mutableText.copy().append(keyBinding.getBoundKeyLocalizedText());
                    }
                }
            }

            if (this.duplicate) {
                Text key = this.editButton.getMessage().copy().formatted(Formatting.WHITE);
                Text tooltip = Text.translatable("controls.keybinds.duplicateKeybinds", mutableText);
                this.editButton.setMessage(Text.literal("[ ").append(key).append(" ]").formatted(Formatting.RED));
                this.editButton.setTooltip(Tooltip.of(tooltip));
            } else {
                this.editButton.setTooltip(null);
            }

            if (BindingsListWidget.this.parent.selectedKeyBinding == this.binding.getKey()) {
                Text key = this.editButton.getMessage().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE);
                this.editButton.setMessage(Text.literal("> ").append(key).append(" <").formatted(Formatting.YELLOW));
            }
        }

        private void editButtonPressed() {
            BindingsListWidget.this.parent.selectedKeyBinding = this.binding.getKey();
            BindingsListWidget.this.update();
        }

        private void deleteButtonPressed() {
            this.binding.unbind();
            Config.remove(this.binding);

            BindingsListWidget list = BindingsListWidget.this;
            list.parent.selectedKeyBinding = null;
            list.removeEntry(this);
            list.setScrollY(list.getScrollY() - 20);
            list.update();
        }

        private String cutString(String text, TextRenderer textRenderer, int maxWidth) {
            int width = textRenderer.getWidth(text);
            if (width <= maxWidth) {
                return text;
            }
            int len = text.length();
            while (width > maxWidth && len > 0) {
                len--;
                width = textRenderer.getWidth(text.substring(0, len));
            }
            return text.substring(0, len) + "…";
        }
    }
}

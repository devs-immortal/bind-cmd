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
            addEntry(new BindingEntry(binding));
        }
    }

    public void update() {
        KeyBinding.updateKeysByCode();
        updateChildren();
    }

    public void addBinding(CommandBinding binding) {
        addEntry(new BindingEntry(binding));
    }

    private void updateChildren() {
        for (BindingEntry entry : children()) {
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
    public class BindingEntry extends Entry<BindingEntry> {
        private final CommandBinding binding;
        private final ButtonWidget editButton;
        private final ButtonWidget deleteButton;
        private final TextFieldWidget inputField;
        private boolean hovered = false;
        private boolean duplicate = false;

        public BindingEntry(CommandBinding binding) {
            this.binding = binding;
            editButton = ButtonWidget.builder(Text.empty(), button -> editButtonPressed())
                    .dimensions(0, 0, 75, 20)
                    .build();
            deleteButton = ButtonWidget.builder(Text.translatable("text.bindcmd.config.remove"), button -> deleteButtonPressed())
                    .dimensions(0, 0, 50, 20)
                    .build();
            inputField = new TextFieldWidget(
                    MinecraftClient.getInstance().textRenderer,
                    0,
                    0,
                    124,
                    16,
                    binding.getKey().getBoundKeyLocalizedText()
            );
            inputField.setChangedListener(this::inputFieldChanged);
            inputField.setMaxLength(256);
            inputField.setText(binding.command);
            update();
        }

        private void inputFieldChanged(String text) {
            binding.command = text;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            TextRenderer textRenderer = BindingsListWidget.this.client.textRenderer;
            int textWidth = getWidth() - editButton.getWidth() - deleteButton.getWidth() - 3;
            boolean isClient = binding.getSource() == BindSource.CLIENT;
            Tooltip tooltip = null;
            if (binding.getSource() == BindSource.SERVER) {
                tooltip = serverBindingTooltip;
            } else if (binding.getSource() == BindSource.WORLD) {
                tooltip = worldBindingTooltip;
            }

            int x = getX();
            int y = getY();
            int height = getHeight();

            if (mouseX > x - 5 && mouseX < x + 123 && mouseY > y && mouseY < y + 20) {
                inputField.setX(x - 4);
                inputField.setY(y + 2);
                inputField.setWidth(textWidth);
                inputField.setTooltip(tooltip);
                inputField.setEditable(isClient);
                inputField.render(context, mouseX, mouseY, delta);
                if (!this.hovered && inputField.isFocused()) {
                    inputField.setFocused(false);
                    inputField.setCursorToStart(false);
                }
                this.hovered = true;
            } else {
                int yPosition = y + height / 2 - 2;
                String text = cutString(binding.command, textRenderer, textWidth - 12);
                context.drawTextWithShadow(textRenderer, text, x, yPosition, Colors.WHITE);
                this.hovered = false;
            }

            editButton.setTooltip(tooltip);
            editButton.active = isClient;
            editButton.setX(x + getWidth() - editButton.getWidth() - deleteButton.getWidth() - 2);
            editButton.setY(y);

            deleteButton.setTooltip(tooltip);
            deleteButton.active = isClient;
            deleteButton.setX(x + getWidth() - deleteButton.getWidth());
            deleteButton.setY(y);
            deleteButton.render(context, mouseX, mouseY, delta);

            if (duplicate) {
                int j = editButton.getX() - 6;
                context.fill(j, y + 2, j + 3, y + height + 2, 0xFFFF0000);
            }

            editButton.render(context, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends Element> children() {
            List<Element> list = new ArrayList<>();
            list.add(editButton);
            list.add(deleteButton);
            list.add(inputField);
            return list;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            List<Selectable> list = new ArrayList<>();
            list.add(editButton);
            list.add(deleteButton);
            list.add(inputField);
            return list;
        }

        public void update() {
            editButton.setMessage(binding.getKey().getBoundKeyLocalizedText());
            duplicate = false;

            Text mutableText = Text.empty();
            if (!binding.getKey().isUnbound()) {
                KeyBinding[] allKeys = BindingsListWidget.this.client.options.allKeys;
                for (KeyBinding keyBinding : allKeys) {
                    if (keyBinding != binding.getKey() && binding.getKey().equals(keyBinding)) {
                        if (duplicate) {
                            mutableText = mutableText.copy().append(", ");
                        }
                        duplicate = true;
                        mutableText = mutableText.copy().append(keyBinding.getBoundKeyLocalizedText());
                    }
                }
            }

            if (duplicate) {
                Text key = editButton.getMessage().copy().formatted(Formatting.WHITE);
                Text tooltip = Text.translatable("controls.keybinds.duplicateKeybinds", mutableText);
                editButton.setMessage(Text.literal("[ ").append(key).append(" ]").formatted(Formatting.RED));
                editButton.setTooltip(Tooltip.of(tooltip));
            } else {
                editButton.setTooltip(null);
            }

            if (BindingsListWidget.this.parent.getSelectedKeyBinding() == binding.getKey()) {
                Text key = editButton.getMessage().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE);
                editButton.setMessage(Text.literal("> ").append(key).append(" <").formatted(Formatting.YELLOW));
            }
        }

        private void editButtonPressed() {
            BindingsListWidget.this.parent.setSelectedBinding(binding);
            BindingsListWidget.this.update();
        }

        private void deleteButtonPressed() {
            binding.unbind();
            Config.remove(binding);

            BindingsListWidget list = BindingsListWidget.this;
            list.parent.clearSelectedBinding();
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

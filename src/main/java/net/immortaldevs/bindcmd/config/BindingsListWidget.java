package net.immortaldevs.bindcmd.config;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.immortaldevs.bindcmd.BindSource;
import net.immortaldevs.bindcmd.CommandBinding;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class BindingsListWidget extends ContainerObjectSelectionList<BindingsListWidget.BindingEntry> {
    final ModConfigScreen parent;
    private final Tooltip serverBindingTooltip = Tooltip.create(Component.translatable("text.bindcmd.config.server_setting"));
    private final Tooltip worldBindingTooltip = Tooltip.create(Component.translatable("text.bindcmd.config.world_setting"));

    public BindingsListWidget(ModConfigScreen parent, Minecraft client) {
        super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 20);
        this.parent = parent;
        rebuildEntries();
    }

    public void update() {
        KeyMapping.resetMapping();
        updateChildren();
    }

    public void addBinding(CommandBinding binding) {
        rebuildEntries();
    }

    private void rebuildEntries() {
        clearEntries();
        for (CommandBinding binding : Config.getBindings()) {
            for (int i = 0; i < binding.commands.size(); i++) {
                addEntry(new BindingEntry(binding, i));
            }
        }
    }

    private void updateChildren() {
        for (BindingEntry entry : children()) {
            entry.update();
        }
    }

    @Override
    public int scrollBarX() {
        return super.scrollBarX() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Environment(EnvType.CLIENT)
    public class BindingEntry extends Entry<BindingEntry> {
        private final CommandBinding binding;
        private final int commandIndex;
        private final Button editButton;
        private final Button deleteButton;
        private final EditBox inputField;
        private boolean duplicate = false;

        public BindingEntry(CommandBinding binding, int commandIndex) {
            this.binding = binding;
            this.commandIndex = commandIndex;
            editButton = Button.builder(Component.empty(), button -> editButtonPressed())
                    .bounds(0, 0, 75, 20)
                    .build();
            deleteButton = Button.builder(Component.translatable("text.bindcmd.config.remove"), button -> deleteButtonPressed())
                    .bounds(0, 0, 50, 20)
                    .build();
            inputField = new EditBox(
                    Minecraft.getInstance().font,
                    0,
                    0,
                    124,
                    16,
                    binding.getKey().getTranslatedKeyMessage()
            );
            inputField.setResponder(this::inputFieldChanged);
            inputField.setMaxLength(256);
            inputField.setValue(binding.commands.get(commandIndex));
            update();
        }

        private void inputFieldChanged(String text) {
            if (text != null && !text.trim().isEmpty()) {
                binding.commands.set(commandIndex, text);
            }
        }

        @Override
        public void renderContent(@NonNull GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            Font font = BindingsListWidget.this.minecraft.font;
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

            if (hovered || inputField.isFocused()) {
                inputField.setX(x - 4);
                inputField.setY(y + 4);
                inputField.setWidth(textWidth);
                inputField.setTooltip(tooltip);
                inputField.setEditable(isClient);
                inputField.render(graphics, mouseX, mouseY, delta);
            } else {
                int yPosition = y + height / 2 - 2;
                String text = cutString(binding.commands.get(commandIndex), font, textWidth - 12);
                graphics.drawString(font, text, x, yPosition, -1);
            }

            editButton.setTooltip(tooltip);
            editButton.active = isClient;
            editButton.setX(x + getWidth() - editButton.getWidth() - deleteButton.getWidth() - 2);
            editButton.setY(y);

            deleteButton.setTooltip(tooltip);
            deleteButton.active = isClient;
            deleteButton.setX(x + getWidth() - deleteButton.getWidth());
            deleteButton.setY(y);
            deleteButton.render(graphics, mouseX, mouseY, delta);

            if (duplicate) {
                int j = editButton.getX() - 6;
                graphics.fill(j, y + 2, j + 3, y + height + 2, 0xFFFF0000);
            }

            editButton.render(graphics, mouseX, mouseY, delta);
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(editButton, deleteButton, inputField);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(editButton, deleteButton, inputField);
        }

        public void update() {
            editButton.setMessage(binding.getKey().getTranslatedKeyMessage());
            duplicate = false;

            MutableComponent tooltip = Component.empty();
            if (!binding.getKey().isUnbound()) {
                KeyMapping[] allKeys = BindingsListWidget.this.minecraft.options.keyMappings;
                for (KeyMapping keyBinding : allKeys) {
                    if (keyBinding != binding.getKey() && binding.getKey().equals(keyBinding)) {
                        if (duplicate) {
                            tooltip.append(", ");
                        }

                        duplicate = true;
                        tooltip.append(keyBinding.getTranslatedKeyMessage());
                    }
                }
            }

            if (duplicate) {
                Component key = editButton.getMessage().copy().withStyle(ChatFormatting.WHITE);
                Component text = Component.translatable("controls.keybinds.duplicateKeybinds", tooltip);
                editButton.setMessage(Component.literal("[ ").append(key).append(" ]").withStyle(ChatFormatting.YELLOW));
                editButton.setTooltip(Tooltip.create(text));
            } else {
                editButton.setTooltip(null);
            }

            if (BindingsListWidget.this.parent.getSelectedKeyMapping() == binding.getKey()) {
                Component key = editButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE);
                editButton.setMessage(Component.literal("> ").append(key).append(" <").withStyle(ChatFormatting.YELLOW));
            }
        }

        private void editButtonPressed() {
            BindingsListWidget.this.parent.setSelectedBinding(binding);
            BindingsListWidget.this.update();
        }

        private void deleteButtonPressed() {
            binding.commands.remove(commandIndex);

            if (binding.commands.isEmpty()) {
                binding.unbind();
                Config.remove(binding);
            }

            BindingsListWidget list = BindingsListWidget.this;
            list.parent.clearSelectedBinding();
            list.setScrollAmount(list.scrollAmount() - 20);

            list.rebuildEntries();
            list.update();
        }

        private String cutString(String text, Font font, int maxWidth) {
            int width = font.width(text);
            if (width <= maxWidth) {
                return text;
            }
            int len = text.length();
            while (width > maxWidth && len > 0) {
                len--;
                width = font.width(text.substring(0, len));
            }
            return text.substring(0, len) + "…";
        }
    }
}

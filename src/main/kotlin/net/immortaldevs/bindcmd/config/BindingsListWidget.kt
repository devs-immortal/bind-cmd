package net.immortaldevs.bindcmd.config

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.immortaldevs.bindcmd.BindSource
import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.option.KeyBinding
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Formatting
import java.util.function.Consumer

class BindingsListWidget(val parent: ModConfigScreen, client: MinecraftClient?) :
    ElementListWidget<BindingsListWidget.BindingEntry>(
        client,
        parent.width,
        parent.layout.contentHeight,
        parent.layout.headerHeight,
        20
    ) {

    private val serverBindingTooltip: Tooltip = Tooltip.of(Text.translatable("text.bindcmd.config.server_setting"))
    private val worldBindingTooltip: Tooltip = Tooltip.of(Text.translatable("text.bindcmd.config.world_setting"))

    init {
        Config.bindings.forEach { binding -> addEntry(BindingEntry(binding)) }
    }

    fun update() {
        KeyBinding.updateKeysByCode()
        updateChildren()
    }

    fun addBinding(binding: CommandBinding) {
        addEntry(BindingEntry(binding))
    }

    private fun updateChildren() {
        children().forEach(Consumer { obj: BindingEntry -> obj.update() })
    }

    override fun getScrollbarX(): Int {
        return super.getScrollbarX() + 15
    }

    override fun getRowWidth(): Int {
        return super.getRowWidth() + 32
    }

    @Environment(EnvType.CLIENT)
    inner class BindingEntry(private val binding: CommandBinding) : Entry<BindingEntry>() {
        private var hovered = false
        private var duplicate = false
        private var editButton: ButtonWidget = ButtonWidget.builder(Text.empty()) { editButtonPressed() }
            .dimensions(0, 0, 75, 20).build()
        private var deleteButton: ButtonWidget =
            ButtonWidget.builder(Text.translatable("text.bindcmd.config.remove")) { deleteButtonPressed() }
                .dimensions(0, 0, 50, 20).build()
        private var inputField: TextFieldWidget = TextFieldWidget(
            MinecraftClient.getInstance().textRenderer,
            0,
            0,
            124,
            16,
            Text.of(binding.key.translationKey)
        )

        init {
            inputField.setChangedListener { text -> inputFieldChanged(text) }
            inputField.setMaxLength(256)
            inputField.text = binding.command
            update()
        }

        private fun inputFieldChanged(text: String) {
            binding.command = text
        }

        override fun render(
            context: DrawContext?,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            val textRenderer: TextRenderer = this@BindingsListWidget.client.textRenderer
            val textWidth: Int = entryWidth - editButton.width - deleteButton.width - 3
            val isClient: Boolean = binding.source == BindSource.CLIENT
            val tooltip = when (binding.source) {
                BindSource.SERVER -> serverBindingTooltip
                BindSource.WORLD -> worldBindingTooltip
                else -> null
            }

            if (mouseX > x - 5 && mouseX < x + 123 && mouseY > y && mouseY < y + 20) {
                inputField.x = x - 4
                inputField.y = y + 2
                inputField.width = textWidth
                inputField.setTooltip(tooltip)
                inputField.active = isClient
                inputField.render(context, mouseX, mouseY, tickDelta)
                if (!this.hovered && inputField.isFocused) {
                    inputField.isFocused = false
                    inputField.setCursorToStart(false)
                }
                this.hovered = true
            } else {
                val yPosition = y + entryHeight / 2 - 2
                val text = cutString(binding.command, textRenderer, textWidth - 12)
                context?.drawTextWithShadow(textRenderer, text, x, yPosition, Colors.WHITE)
                this.hovered = false
            }

            editButton.setTooltip(tooltip)
            editButton.active = isClient
            editButton.x = x + entryWidth - editButton.width - deleteButton.width - 2
            editButton.y = y

            deleteButton.setTooltip(tooltip)
            deleteButton.active = isClient
            deleteButton.x = x + entryWidth - deleteButton.width
            deleteButton.y = y
            deleteButton.render(context, mouseX, mouseY, tickDelta)

            if (duplicate) {
                val j = editButton.x - 6
                context?.fill(j, y + 2, j + 3, y + entryHeight + 2, -65536)
            }

            editButton.render(context, mouseX, mouseY, tickDelta)
        }

        override fun children(): List<Element?>? {
            return listOf(editButton, deleteButton, inputField)
        }

        override fun selectableChildren(): List<Selectable?>? {
            return listOf(editButton, deleteButton, inputField)
        }

        fun update() {
            editButton.message = binding.key.boundKeyLocalizedText
            duplicate = false

            val mutableText = Text.empty()
            if (!binding.key.isUnbound) {
                var allKeys: Array<KeyBinding> = this@BindingsListWidget.client.options.allKeys
                allKeys = allKeys.filter { it != binding.key && binding.key.equals(it) }.toTypedArray()
                for (keyBinding in allKeys) {
                    if (duplicate)
                        mutableText.append(", ")
                    duplicate = true
                    mutableText.append(Text.translatable(keyBinding.translationKey))
                }
            }

            if (duplicate) {
                val key = editButton.message.copy().formatted(Formatting.WHITE)
                val tooltip = Text.translatable("controls.keybinds.duplicateKeybinds", *arrayOf<Any>(mutableText))
                editButton.message = Text.literal("[ ").append(key).append(" ]").formatted(Formatting.RED)
                editButton.setTooltip(Tooltip.of(tooltip))
            } else {
                editButton.setTooltip(null)
            }

            if (this@BindingsListWidget.parent.selectedKeyBinding === binding.key) {
                val key = editButton.message.copy().formatted(*arrayOf(Formatting.WHITE, Formatting.UNDERLINE))
                editButton.message = Text.literal("> ").append(key).append(" <").formatted(Formatting.YELLOW)
            }
        }

        private fun editButtonPressed() {
            this@BindingsListWidget.parent.selectedKeyBinding = binding.key
            this@BindingsListWidget.update()
        }

        private fun deleteButtonPressed() {
            binding.unbind()
            Config.remove(binding)

            val list = this@BindingsListWidget
            list.parent.selectedKeyBinding = null
            list.removeEntry(this)

            scrollY -= 20
            list.update()
        }

        private fun cutString(text: String, textRenderer: TextRenderer, maxWidth: Int): String {
            var width = textRenderer.getWidth(text)
            if (width <= maxWidth) return text
            var len = text.length
            while (width > maxWidth && len > 0) {
                len--
                width = textRenderer.getWidth(text.take(len))
            }
            return text.take(len) + "…"
        }
    }
}

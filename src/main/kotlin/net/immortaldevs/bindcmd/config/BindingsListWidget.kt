package net.immortaldevs.bindcmd.config

import com.google.common.collect.ImmutableList
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.function.Consumer

class BindingsListWidget(val parent: ModConfigScreen, client: MinecraftClient?) :
    ElementListWidget<BindingsListWidget.BindingEntry>(
        client,
        parent.width + 45,
        parent.height,
        20,
        parent.height - 32,
        20
    ) {

    var maxKeyNameLength = 150

    init {
        for (binding in parent.bindings) {
            addEntry(BindingEntry(binding, Text.of(binding.translationKey)))
        }
    }

    fun update() {
        KeyBinding.updateKeysByCode()
        updateChildren()
    }

    fun addBinding(binding: KeyBinding, bindingName: Text) {
        addEntry(BindingEntry(binding, bindingName))
    }

    private fun updateChildren() {
        children().forEach(Consumer { obj: BindingEntry -> obj.update() })
    }

    override fun getScrollbarPositionX(): Int {
        return super.getScrollbarPositionX() + 15
    }

    override fun getRowWidth(): Int {
        return super.getRowWidth() + 32
    }

    @Environment(EnvType.CLIENT)
    inner class BindingEntry(private val binding: KeyBinding, private val bindingName: Text) : Entry<BindingEntry>() {
        private var duplicate = false
        private var editButton: ButtonWidget = ButtonWidget.builder(bindingName) { _ -> editButtonPressed() }
            .dimensions(0, 0, 75, 20).build()
        private var deleteButton: ButtonWidget =
            ButtonWidget.builder(Text.translatable("text.bindcmd.config.remove")) { _ -> deleteButtonPressed() }
            .dimensions(0, 0, 50, 20).build()

        init {
            update()
        }

        override fun render(
            matrices: MatrixStack?,
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
            val maxKeyNameLength: Float = (x + 90 - this@BindingsListWidget.maxKeyNameLength).toFloat()
            val yPosition = y + entryHeight / 2

            textRenderer.draw(matrices, bindingName, maxKeyNameLength, (yPosition - 9 / 2).toFloat(), 16777215)

            deleteButton.x = x + 190
            deleteButton.y = y
            deleteButton.render(matrices, mouseX, mouseY, tickDelta)

            editButton.x = x + 105
            editButton.y = y
            if (duplicate) {
                val j = editButton.x - 6
                fill(matrices, j, y + 2, j + 3, y + entryHeight + 2, Formatting.RED.colorValue ?: -16777216)
            }

            editButton.render(matrices, mouseX, mouseY, tickDelta)
        }

        override fun children(): List<Element?>? {
            return ImmutableList.of(editButton, deleteButton)
        }

        override fun selectableChildren(): List<Selectable?>? {
            return ImmutableList.of(editButton, deleteButton)
        }

        fun update() {
            editButton.message = binding.boundKeyLocalizedText
            duplicate = false

            val mutableText = Text.empty()
            if (!binding.isUnbound) {
                var allKeys: Array<KeyBinding> = this@BindingsListWidget.client.options.allKeys;
                allKeys = allKeys.filter { it != binding && binding.equals(it) }.toTypedArray()
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
                editButton.setTooltip(null as Tooltip?)
            }

            if (this@BindingsListWidget.parent.selectedKeyBinding === binding) {
                val key = editButton.message.copy().formatted(*arrayOf(Formatting.WHITE, Formatting.UNDERLINE));
                editButton.message = Text.literal("> ").append(key).append(" <").formatted(Formatting.YELLOW)
            }
        }

        private fun editButtonPressed() {
            this@BindingsListWidget.parent.selectedKeyBinding = binding
            this@BindingsListWidget.update()
        }

        private fun deleteButtonPressed() {
            val list = this@BindingsListWidget
            list.parent.bindings.remove(binding)
            list.parent.selectedKeyBinding = null
            list.removeEntry(this)
            list.update()
        }
    }
}

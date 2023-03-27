package net.immortaldevs.bindcmd.config

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.option.GameOptionsScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Util

@Environment(EnvType.CLIENT)
class ModConfigScreen(parent: Screen?) : GameOptionsScreen(
    parent,
    null,
    Text.translatable("text.bindcmd.config.title")
) {
    var selectedKeyBinding: KeyBinding? = null
    private var lastKeyCodeUpdateTime: Long = 0
    private var bindingsList: BindingsListWidget? = null

    override fun init() {
        bindingsList = BindingsListWidget(this, client)
        addSelectableChild(bindingsList)

        val addButton =
            ButtonWidget.builder(Text.translatable("text.bindcmd.config.add_command")) { addButtonPressed() }
                .dimensions(width / 2 - 155, height - 29, 150, 20)

        val doneButton = ButtonWidget.builder(ScreenTexts.DONE) { doneButtonPressed() }
            .dimensions(width / 2 - 155 + 160, height - 29, 150, 20)

        addDrawableChild(addButton.build()) as ButtonWidget

        addDrawableChild(doneButton.build())
    }

    override fun tick() {
        bindingsList?.tick()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (selectedKeyBinding != null) {
            Config.bindings.forEach { binding ->
                if (binding.key == selectedKeyBinding)
                    binding.setBoundMouse(button)
            }
            selectedKeyBinding = null
            bindingsList?.update()
            true
        } else {
            super.mouseClicked(mouseX, mouseY, button)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (selectedKeyBinding != null) {
            Config.bindings.forEach { binding ->
                if (binding.key == selectedKeyBinding)
                    binding.setBoundKey(keyCode, scanCode)
            }
            selectedKeyBinding = null
            lastKeyCodeUpdateTime = Util.getMeasuringTimeMs()
            bindingsList?.update()
            true
        } else {
            super.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        bindingsList?.render(matrices, mouseX, mouseY, delta)
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 8, 16777215)
        super.render(matrices, mouseX, mouseY, delta)
    }

    private fun doneButtonPressed() {
        Config.save()
        client?.setScreen(parent)
    }

    private fun addButtonPressed() {
        val binding = CommandBinding("/")
        Config.bindings.add(binding)
        bindingsList?.addBinding(binding)
        bindingsList?.update()
    }
}
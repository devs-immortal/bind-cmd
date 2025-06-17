package net.immortaldevs.bindcmd.config

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.option.GameOptionsScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.option.KeyBinding
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
class ModConfigScreen(parent: Screen?) : GameOptionsScreen(
    parent,
    null,
    Text.translatable("text.bindcmd.config.title")
) {
    var selectedKeyBinding: KeyBinding? = null
    private var bindingsList: BindingsListWidget? = null

    override fun initBody() {
        bindingsList = this.layout.addBody(
            BindingsListWidget(this, client)
        )
    }

    override fun addOptions() {
    }

    override fun initHeader() {
        this.layout.addHeader(
            TextWidget(
                Text.translatable("text.bindcmd.config.title"),
                this.textRenderer,
            )
        )
    }

    override fun initFooter() {
        val addButton = ButtonWidget.builder(Text.translatable("text.bindcmd.config.add_command")) {
            addButtonPressed()
        }.build()
        val doneButton = ButtonWidget.builder(ScreenTexts.DONE) { doneButtonPressed() }.build()

        val directionalLayoutWidget = layout.addFooter(
            DirectionalLayoutWidget.horizontal().spacing(8)
        ) as DirectionalLayoutWidget
        directionalLayoutWidget.add(addButton)
        directionalLayoutWidget.add(doneButton)
    }

    override fun refreshWidgetPositions() {
        this.layout.refreshPositions()
        bindingsList?.position(this.width, this.layout)
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
            bindingsList?.update()
            true
        } else {
            super.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    private fun doneButtonPressed() {
        Config.save()
        client?.setScreen(parent)
    }

    private fun addButtonPressed() {
        val binding = CommandBinding("/")
        Config.add(binding)
        bindingsList?.addBinding(binding)
        bindingsList?.update()
    }
}

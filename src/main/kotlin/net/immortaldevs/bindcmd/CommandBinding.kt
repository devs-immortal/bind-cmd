package net.immortaldevs.bindcmd

import net.minecraft.client.input.KeyInput
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier

private val CATEGORY = KeyBinding.Category(Identifier.of("bindcmd", "category"))

class CommandBinding(var command: String, var key: KeyBinding) {
    constructor(command: String) : this(command, KeyBinding("key.keyboard.unknown", -1, CATEGORY))

    constructor(command: String, translationKey: String, source: BindSource = BindSource.CLIENT) : this(command) {
        this.command = command
        this.source = source
        val keyCode = InputUtil.fromTranslationKey(translationKey).code
        val type = if (translationKey.startsWith("key.mouse")) InputUtil.Type.MOUSE else InputUtil.Type.KEYSYM
        this.key = KeyBinding(translationKey, type, keyCode, CATEGORY)
    }

    var source: BindSource = BindSource.CLIENT
    val translationKey: String get() = key.boundKeyTranslationKey
    val isUnknown: Boolean get() = translationKey.endsWith("unknown")
    val isPressed: Boolean get() = key.isPressed
    var wasPressed: Boolean = false

    fun setBoundKey(keyInput: KeyInput) {
        unbind()
        val key = if (keyInput.isEscape) InputUtil.UNKNOWN_KEY else InputUtil.fromKeyCode(keyInput)
        this.key = KeyBinding(key.translationKey, key.code, CATEGORY)
    }

    fun setBoundMouse(button: Int) {
        unbind()
        val key = InputUtil.Type.MOUSE.createFromCode(button)
        this.key = KeyBinding(key.translationKey, InputUtil.Type.MOUSE, key.code, CATEGORY)
    }

    fun unbind() {
        KeyBinding.KEYS_BY_ID.remove(translationKey)
        KeyBinding.updateKeysByCode()
    }
}
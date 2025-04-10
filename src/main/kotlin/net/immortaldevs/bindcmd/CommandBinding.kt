package net.immortaldevs.bindcmd

import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

class CommandBinding(var command: String, var key: KeyBinding) {

    constructor(command: String) : this(command, KeyBinding("key.keyboard.unknown", -1, "key.categories.bindcmd"))

    constructor(command: String, translationKey: String, source: BindSource = BindSource.CLIENT) : this(command) {
        this.command = command
        this.source = source
        val keyCode = InputUtil.fromTranslationKey(translationKey).code
        val type = if (translationKey.startsWith("key.mouse")) InputUtil.Type.MOUSE else InputUtil.Type.KEYSYM
        this.key = KeyBinding(translationKey, type, keyCode, "key.categories.bindcmd")
    }

    constructor(command: String, key: Int) : this(command) {
        this.command = command
        setBoundKey(key, 0)
    }

    var source: BindSource = BindSource.CLIENT
    val isUnknown: Boolean get() = key.translationKey.endsWith("unknown")
    val isPressed: Boolean get() = key.isPressed
    var wasPressed: Boolean = false

    fun setBoundKey(keyCode: Int, scanCode: Int) {
        unbind()
        val key = InputUtil.fromKeyCode(if (keyCode == 256) -1 else keyCode, scanCode)
        this.key = KeyBinding(key.translationKey, key.code, "key.categories.bindcmd")
    }

    fun setBoundMouse(button: Int) {
        unbind()
        val key = InputUtil.Type.MOUSE.createFromCode(button)
        this.key = KeyBinding(key.translationKey, InputUtil.Type.MOUSE, key.code, "key.categories.bindcmd")
    }

    fun unbind() {
        this.key.setBoundKey(InputUtil.UNKNOWN_KEY)
    }
}
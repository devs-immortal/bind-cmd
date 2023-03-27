package net.immortaldevs.bindcmd

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

var bindings = mutableListOf(
    CommandBinding("/help", 72),
)

@Suppress("unused")
fun init() {
    ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client -> onEndClientTick(client) })
}

fun onEndClientTick(client: MinecraftClient) {
    bindings.forEach { binding ->
        if (binding.key.wasPressed()) {
            val command = binding.command.substring(1)
            client.networkHandler?.sendChatCommand(command)
        }
    }
}

class CommandBinding(var command: String, val key: KeyBinding) {
    constructor(command: String) : this(command, KeyBinding(command, -1, "key.categories.bindcmd"))

    constructor(command: String, key: Int) : this(command, KeyBinding(command, key, "key.categories.bindcmd"))

    fun setBoundKey(keyCode: Int, scanCode: Int) {
        val key = if (keyCode == 256) InputUtil.UNKNOWN_KEY else InputUtil.fromKeyCode(keyCode, scanCode)
        setBoundKey(key)
    }

    fun setBoundKey(key: InputUtil.Key) {
        this.key.setBoundKey(key)
    }
}
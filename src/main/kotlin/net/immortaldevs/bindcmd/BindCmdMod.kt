package net.immortaldevs.bindcmd

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.immortaldevs.bindcmd.config.Config
import net.minecraft.client.MinecraftClient

private var lastKeyPress: Long = 0
private const val COOLDOWN: Long = 200

@Suppress("unused")
fun init() {
    Config.load()
    ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client -> onEndClientTick(client) })
}

private fun onEndClientTick(client: MinecraftClient) {
    if (System.currentTimeMillis() - lastKeyPress < COOLDOWN) return
    Config.bindings.forEach { binding -> handleBinding(client, binding) }
}

private fun handleBinding(client: MinecraftClient, binding: CommandBinding) {
    if (binding.isUnknown) return

    if (binding.isPressed && !binding.wasPressed) {
        if (binding.command[0] != '/') {
            client.networkHandler?.sendChatMessage(binding.command)
        } else {
            val command = binding.command.substring(1)
            client.networkHandler?.sendChatCommand(command)
        }
        lastKeyPress = System.currentTimeMillis()
        binding.wasPressed = true
    }

    if (!binding.isPressed && binding.wasPressed) {
        binding.wasPressed = false
    }
}
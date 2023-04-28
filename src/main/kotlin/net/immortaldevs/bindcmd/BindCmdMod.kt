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
        val cmd = Command(binding.command)
        when (cmd.type) {
            CmdType.COMMAND -> client.networkHandler?.sendChatCommand(cmd.command)
            CmdType.MESSAGE -> client.networkHandler?.sendChatMessage(cmd.command)
            CmdType.NONE -> return
        }
        lastKeyPress = System.currentTimeMillis()
        binding.wasPressed = true
    }

    if (!binding.isPressed && binding.wasPressed) {
        binding.wasPressed = false
    }
}
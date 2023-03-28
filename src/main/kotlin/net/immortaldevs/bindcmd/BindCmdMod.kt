package net.immortaldevs.bindcmd

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.immortaldevs.bindcmd.config.Config
import net.minecraft.client.MinecraftClient

@Suppress("unused")
fun init() {
    Config.load()
    ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client -> onEndClientTick(client) })
}

fun onEndClientTick(client: MinecraftClient) {
    Config.bindings.forEach { binding ->
        if (!binding.isUnknown && binding.key.wasPressed()) {
            if (binding.command[0] != '/') {
                client.networkHandler?.sendChatMessage(binding.command)
            } else {
                val command = binding.command.substring(1)
                client.networkHandler?.sendChatCommand(command)
            }
        }
    }
}
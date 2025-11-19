package net.immortaldevs.bindcmd

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.immortaldevs.bindcmd.config.Config
import net.minecraft.client.MinecraftClient

private var lastKeyPress: Long = 0
private const val COOLDOWN: Long = 200

fun initClient() {
    PayloadTypeRegistry.playS2C().register(
        ConfigS2CPayload.ID, ConfigS2CPayload.CODEC
    )

    Config.load()

    ClientTickEvents.END_CLIENT_TICK.register { client -> onEndClientTick(client) }
    ClientPlayConnectionEvents.JOIN.register { _, _, client -> onPlayerJoin(client) }
    ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> onPlayerDisconnect() }

    ClientPlayNetworking.registerGlobalReceiver<ConfigS2CPayload>(ConfigS2CPayload.ID) { payload, _ ->
        Config.setServerBindings(payload.config)
    }
}

private fun onPlayerJoin(client: MinecraftClient) {
    Config.clearServerBindings()
    if (client.server?.isSingleplayer == true) {
        val dir = client.server?.session?.directory?.path()
        Config.loadWorldConfig(dir)
    }
}

private fun onPlayerDisconnect() {
    Config.clearServerBindings()
}

private fun onEndClientTick(client: MinecraftClient) {
    if (System.currentTimeMillis() - lastKeyPress < COOLDOWN) return
    Config.getBindings().forEach { binding -> handleBinding(client, binding) }
}

private fun handleBinding(client: MinecraftClient, binding: CommandBinding) {
    if (binding.isUnknown) return

    if (binding.isPressed && !binding.wasPressed) {
        val commands = Config.getBindings().filter { it.translationKey == binding.translationKey }.map { it.command }
        for (command in commands) {
            val cmd = Command(command)
            when (cmd.type) {
                CmdType.COMMAND -> client.networkHandler?.sendChatCommand(cmd.command)
                CmdType.MESSAGE -> client.networkHandler?.sendChatMessage(cmd.command)
                CmdType.NONE -> return
            }
        }
        lastKeyPress = System.currentTimeMillis()
        binding.wasPressed = true
    }

    if (!binding.isPressed && binding.wasPressed) {
        binding.wasPressed = false
    }
}
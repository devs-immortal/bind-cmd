package net.immortaldevs.bindcmd

import net.minecraft.client.MinecraftClient

enum class CmdType {
    MESSAGE,
    COMMAND,
    NONE
}

class Command(command: String) {
    var type: CmdType private set
    var command: String private set

    init {
        if (command[0] == '/') {
            this.type = CmdType.COMMAND
            this.command = command.substring(1)
        } else if (command[0] == '@') {
            val (type, cmd) = getLastMessage(command) ?: Pair(CmdType.NONE, "")
            this.type = type
            this.command = cmd
        } else {
            this.type = CmdType.MESSAGE
            this.command = command
        }
    }

    private fun getLastMessage(cmd: String): Pair<CmdType, String>? {
        val history = MinecraftClient.getInstance().inGameHud?.chatHud?.messageHistory ?: return null
        val offset = if (cmd.length > 1) cmd.substring(1).toIntOrNull() ?: 1 else 1
        if (history.size - offset < 0) return null
        val message = history[history.size - offset]
        if (message[0] == '/') {
            return Pair(CmdType.COMMAND, message.substring(1))
        }
        return Pair(CmdType.MESSAGE, message)
    }
}
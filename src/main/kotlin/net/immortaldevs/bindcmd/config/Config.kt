package net.immortaldevs.bindcmd.config

import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.MinecraftClient

class Config {
    companion object {
        @JvmStatic
        var bindings = mutableListOf(
            CommandBinding("/help", 72),
        )

        @JvmStatic
        fun load() {
            val file = MinecraftClient.getInstance().runDirectory.resolve("config/bind_cmd.ini")
            if (!file.exists()) {
                file.createNewFile()
                save()
                return
            }
            try {
                val inputStream = file.inputStream()
                bindings = decode(inputStream.readBytes().decodeToString()).toMutableList()
            } catch (e: Exception) {
                createBackup()
                save()
            }
        }

        @JvmStatic
        fun save() {
            val file = MinecraftClient.getInstance().runDirectory.resolve("config/bind_cmd.ini")
            file.writeText(encode(bindings))
        }

        @JvmStatic
        private fun createBackup() {
            val file = MinecraftClient.getInstance().runDirectory.resolve("config/bind_cmd.ini")
            val backupFile = MinecraftClient.getInstance().runDirectory.resolve("config/bind_cmd.ini.bak")
            file.copyTo(backupFile, true)
        }

        @JvmStatic
        private fun encode(data: List<CommandBinding>): String {
            return data.joinToString("\n") { binding ->
                "${binding.key.translationKey}=\"${binding.command}\""
            }
        }

        @JvmStatic
        private fun decode(input: String): List<CommandBinding> {
            val lines = input.split("\n").filter { it.isNotEmpty() }
            return lines.map { line ->
                val parts = line.split("=\"")
                val translationKey = parts[0]
                val command = parts[1].substring(0, parts[1].length - 1)
                CommandBinding(command, translationKey)
            }
        }
    }
}
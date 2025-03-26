package net.immortaldevs.bindcmd.config

import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class Config {
    companion object {
        private const val CONFIG_DIR = "config"
        private const val CONFIG_FILE = "$CONFIG_DIR/bind_cmd.ini"

        @JvmStatic
        var bindings = mutableListOf(
            CommandBinding("/help", InputUtil.GLFW_KEY_H),
        )

        @JvmStatic
        fun load() {
            ensureConfigDirExists()
            val file = MinecraftClient.getInstance().runDirectory.resolve(CONFIG_FILE)
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
            ensureConfigDirExists()
            val file = MinecraftClient.getInstance().runDirectory.resolve(CONFIG_FILE)
            file.writeText(encode(bindings))
        }

        @JvmStatic
        private fun createBackup() {
            val file = MinecraftClient.getInstance().runDirectory.resolve(CONFIG_FILE)
            val backupFile = MinecraftClient.getInstance().runDirectory.resolve("$CONFIG_FILE.bak")
            file.copyTo(backupFile, true)
        }

        @JvmStatic
        private fun ensureConfigDirExists() {
            val configDir = MinecraftClient.getInstance().runDirectory.resolve(CONFIG_DIR)
            if (!configDir.exists()) {
                configDir.mkdir()
            }
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
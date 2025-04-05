package net.immortaldevs.bindcmd.config

import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class Config {
    companion object {
        private val loader: ConfigLoader by lazy {
            ConfigLoader(MinecraftClient.getInstance().runDirectory)
        }

        @JvmStatic
        var bindings = mutableListOf(
            CommandBinding("/help", InputUtil.GLFW_KEY_H),
        )

        @JvmStatic
        fun load() {
            val data = loader.read()
            if (data == null) {
                save(true)
                return
            }
            bindings = fromMap(data).toMutableList()
        }

        @JvmStatic
        fun save(backup: Boolean = false) {
            loader.write(toMap(bindings), backup)
        }

        @JvmStatic
        private fun toMap(data: List<CommandBinding>): Map<String, String> {
            return data.associate { binding ->
                binding.key.translationKey to binding.command
            }
        }

        @JvmStatic
        private fun fromMap(data: Map<String, String>): List<CommandBinding> {
            return data.map { (key, command) ->
                CommandBinding(command, key)
            }
        }
    }
}
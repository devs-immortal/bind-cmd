package net.immortaldevs.bindcmd.config

import net.immortaldevs.bindcmd.BindSource
import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.MinecraftClient
import java.nio.file.Path

class Config {
    companion object {
        private val loader: ConfigLoader by lazy {
            ConfigLoader(MinecraftClient.getInstance().runDirectory)
        }
        private var serverBindings = listOf<CommandBinding>()
        private var clientBindings = mutableListOf<CommandBinding>()

        val bindings: List<CommandBinding>
            get() = clientBindings + serverBindings

        @JvmStatic
        fun load() {
            val data = loader.read()
            if (data == null) {
                save(true)
                return
            }
            clientBindings = fromMap(data).toMutableList()
        }

        @JvmStatic
        fun loadWorldConfig(path: Path?) {
            if (path == null) return
            val data = ConfigLoader(path.toFile()).read() ?: return
            serverBindings = fromMap(data, BindSource.WORLD)
        }

        @JvmStatic
        fun remove(binding: CommandBinding) {
            clientBindings.remove(binding)
        }

        @JvmStatic
        fun add(binding: CommandBinding) {
            clientBindings.add(binding)
        }

        @JvmStatic
        fun save(backup: Boolean = false) {
            loader.write(toMap(clientBindings), backup)
        }

        @JvmStatic
        fun setServerBindings(data: Map<String, String>) {
            serverBindings = fromMap(data, BindSource.SERVER)
        }

        @JvmStatic
        fun clearServerBindings() {
            serverBindings = emptyList()
        }

        @JvmStatic
        private fun toMap(data: List<CommandBinding>): Map<String, String> {
            return data.associate { binding ->
                binding.key.translationKey to binding.command
            }
        }

        @JvmStatic
        private fun fromMap(data: Map<String, String>, source: BindSource = BindSource.CLIENT): List<CommandBinding> {
            return data.map { (key, command) ->
                CommandBinding(command, key, source)
            }
        }
    }
}
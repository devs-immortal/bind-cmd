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
            clientBindings = fromTuples(data).toMutableList()
        }

        @JvmStatic
        fun loadWorldConfig(path: Path?) {
            if (path == null) return
            val data = ConfigLoader(path.toFile()).read() ?: return
            serverBindings = fromTuples(data, BindSource.WORLD)
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
            loader.write(toTuples(clientBindings), backup)
        }

        @JvmStatic
        fun setServerBindings(data: List<Pair<String, String>>) {
            serverBindings = fromTuples(data, BindSource.SERVER)
        }

        @JvmStatic
        fun clearServerBindings() {
            serverBindings = emptyList()
        }

        @JvmStatic
        private fun toTuples(data: List<CommandBinding>): List<Pair<String, String>> {
            return data.map { binding ->
                binding.key.translationKey to binding.command
            }
        }

        @JvmStatic
        private fun fromTuples(
            data: List<Pair<String, String>>,
            source: BindSource = BindSource.CLIENT
        ): List<CommandBinding> {
            return data.map { (key, command) ->
                CommandBinding(command, key, source)
            }
        }
    }
}
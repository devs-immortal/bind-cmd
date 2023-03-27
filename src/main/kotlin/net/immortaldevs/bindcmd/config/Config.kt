package net.immortaldevs.bindcmd.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.immortaldevs.bindcmd.CommandBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class Config {
    companion object {
        @JvmStatic
        var bindings = mutableListOf(
            CommandBinding("/help", InputUtil.GLFW_KEY_H),
        )

        @JvmStatic
        fun load() {
            val file = MinecraftClient.getInstance().runDirectory.resolve("config/bind_cmd.yaml")
            if (!file.exists()) {
                file.createNewFile()
                save()
                return
            }
            val inputStream = file.inputStream()
            val result = Yaml.default.decodeFromString(Bindings.serializer(), inputStream.bufferedReader().readText())
            bindings = result.bindings.map { binding ->
                CommandBinding(binding.command, binding.key)
            }.toMutableList()
        }

        @JvmStatic
        fun save() {
            val input = bindings.map { binding ->
                Binding(binding.command, binding.key.translationKey)
            }
            val result = Yaml.default.encodeToString(Bindings.serializer(), Bindings(input))
            val file = MinecraftClient.getInstance().runDirectory.resolve("config/bind_cmd.yaml")
            file.writeText(result)
        }
    }

    @Serializable
    data class Binding(
        val command: String,
        val key: String,
    )

    @Serializable
    data class Bindings(
        val bindings: List<Binding>,
    )
}
package net.immortaldevs.bindcmd

import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class ConfigS2CPayload(val config: Map<String, String>) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        private val CONFIG_PAYLOAD_ID: Identifier = Identifier.of("bindcmd", "config")
        val ID: CustomPayload.Id<ConfigS2CPayload?> = CustomPayload.Id(CONFIG_PAYLOAD_ID)
        private val MAP_CODEC: PacketCodec<ByteBuf, Map<String, String>> = PacketCodecs.map(
            { HashMap<String, String>() },
            PacketCodecs.STRING,
            PacketCodecs.STRING,
            100
        )
        val CODEC: PacketCodec<in RegistryByteBuf, ConfigS2CPayload?> = PacketCodec.tuple(
            MAP_CODEC, ConfigS2CPayload::config, ::ConfigS2CPayload
        )
    }
}
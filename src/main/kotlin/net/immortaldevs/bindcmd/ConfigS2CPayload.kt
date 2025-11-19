package net.immortaldevs.bindcmd

import io.netty.buffer.ByteBuf
import net.immortaldevs.bindcmd.config.ConfigEntry
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.encoding.StringEncoding
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class ConfigS2CPayload(val config: List<ConfigEntry>) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        private val CONFIG_PAYLOAD_ID: Identifier = Identifier.of("bindcmd", "config")
        val ID: CustomPayload.Id<ConfigS2CPayload?> = CustomPayload.Id(CONFIG_PAYLOAD_ID)
        val TUPLES_CODEC: PacketCodec<ByteBuf, List<ConfigEntry>> =
            object : PacketCodec<ByteBuf, List<ConfigEntry>> {
                override fun decode(buf: ByteBuf?): List<ConfigEntry> {
                    if (buf == null) return emptyList()
                    val size = buf.readInt()
                    val list = mutableListOf<ConfigEntry>()
                    for (i in 0 until size) {
                        val key = StringEncoding.decode(buf, 32767)
                        val command = StringEncoding.decode(buf, 32767)
                        list.add(ConfigEntry(key, command))
                    }
                    return list
                }

                override fun encode(
                    buf: ByteBuf?,
                    value: List<ConfigEntry>?
                ) {
                    if (buf == null || value == null) return
                    buf.writeInt(value.size)
                    for (entry in value) {
                        StringEncoding.encode(buf, entry.key, 32767)
                        StringEncoding.encode(buf, entry.value, 32767)
                    }
                }

            }
        val CODEC: PacketCodec<in RegistryByteBuf, ConfigS2CPayload?> = PacketCodec.tuple(
            TUPLES_CODEC, ConfigS2CPayload::config, ::ConfigS2CPayload
        )
    }
}
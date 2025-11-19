package net.immortaldevs.bindcmd;

import io.netty.buffer.ByteBuf;
import net.immortaldevs.bindcmd.config.ConfigEntry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ConfigS2CPayload(List<ConfigEntry> config) implements CustomPayload {
    private static final Identifier CONFIG_PAYLOAD_ID = Identifier.of("bindcmd", "config");
    public static final CustomPayload.Id<ConfigS2CPayload> ID = new CustomPayload.Id<>(CONFIG_PAYLOAD_ID);

    public static final PacketCodec<ByteBuf, List<ConfigEntry>> TUPLES_CODEC = new PacketCodec<>() {
        @Override
        public List<ConfigEntry> decode(ByteBuf buf) {
            if (buf == null) return Collections.emptyList();
            int size = buf.readInt();
            List<ConfigEntry> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                String key = StringEncoding.decode(buf, 32767);
                String command = StringEncoding.decode(buf, 32767);
                list.add(new ConfigEntry(key, command));
            }
            return list;
        }

        @Override
        public void encode(ByteBuf buf, List<ConfigEntry> value) {
            if (buf == null || value == null) return;
            buf.writeInt(value.size());
            for (ConfigEntry entry : value) {
                StringEncoding.encode(buf, entry.key(), 32767);
                StringEncoding.encode(buf, entry.value(), 32767);
            }
        }
    };

    public static final PacketCodec<RegistryByteBuf, ConfigS2CPayload> CODEC =
            PacketCodec.tuple(TUPLES_CODEC, ConfigS2CPayload::config, ConfigS2CPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

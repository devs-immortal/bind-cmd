package net.immortaldevs.bindcmd;

import io.netty.buffer.ByteBuf;
import net.immortaldevs.bindcmd.config.ConfigEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ConfigPayload(List<ConfigEntry> config) implements CustomPacketPayload {
    private static final Identifier CONFIG_PAYLOAD_ID = Identifier.fromNamespaceAndPath("bindcmd", "config");
    public static final Type<ConfigPayload> ID = new Type<>(CONFIG_PAYLOAD_ID);

    public static final StreamCodec<ByteBuf, List<ConfigEntry>> TUPLES_CODEC = new StreamCodec<>() {
        @Override
        public List<ConfigEntry> decode(ByteBuf buf) {
            if (buf == null) return Collections.emptyList();
            int size = buf.readInt();
            List<ConfigEntry> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                String key = Utf8String.read(buf, 32767);
                String command = Utf8String.read(buf, 32767);
                list.add(new ConfigEntry(key, command));
            }
            return list;
        }

        @Override
        public void encode(ByteBuf buf, List<ConfigEntry> value) {
            if (buf == null || value == null) return;
            buf.writeInt(value.size());
            for (ConfigEntry entry : value) {
                Utf8String.write(buf, entry.key(), 32767);
                Utf8String.write(buf, entry.value(), 32767);
            }
        }
    };

    public static final StreamCodec<FriendlyByteBuf, ConfigPayload> CODEC =
            StreamCodec.composite(TUPLES_CODEC, ConfigPayload::config, ConfigPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

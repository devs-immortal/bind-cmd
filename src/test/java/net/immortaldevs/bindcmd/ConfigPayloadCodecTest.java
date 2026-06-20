package net.immortaldevs.bindcmd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.immortaldevs.bindcmd.config.ConfigEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigPayloadCodecTest {
    private static List<ConfigEntry> roundTrip(List<ConfigEntry> entries) {
        ByteBuf buf = Unpooled.buffer();
        ConfigPayload.TUPLES_CODEC.encode(buf, entries);
        return ConfigPayload.TUPLES_CODEC.decode(buf);
    }

    @Test
    @DisplayName("an empty list round-trips to an empty list")
    void roundTrip_empty() {
        assertEquals(List.of(), roundTrip(List.of()));
    }

    @Test
    @DisplayName("a single entry round-trips intact")
    void roundTrip_single() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("key.keyboard.g", "/gamemode creative"));
        assertEquals(entries, roundTrip(entries));
    }

    @Test
    @DisplayName("multiple entries round-trip in order")
    void roundTrip_multiple() {
        List<ConfigEntry> entries = List.of(
                new ConfigEntry("key.keyboard.g", "/spawn"),
                new ConfigEntry("key.keyboard.g", "/time set day"),
                new ConfigEntry("key.mouse.left", "hello")
        );
        assertEquals(entries, roundTrip(entries));
    }

    @Test
    @DisplayName("Unicode keys and values round-trip intact")
    void roundTrip_unicode() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("key.keyboard.u", "say ✨ héllo 世界"));
        assertEquals(entries, roundTrip(entries));
    }

    @Test
    @DisplayName("empty key and value strings round-trip intact")
    void roundTrip_emptyStrings() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("", ""));
        assertEquals(entries, roundTrip(entries));
    }

    @Test
    @DisplayName("decode of a null buffer yields an empty list")
    void decode_nullBuffer() {
        assertEquals(List.of(), ConfigPayload.TUPLES_CODEC.decode(null));
    }

    @Test
    @DisplayName("encode of a null value writes nothing to the buffer")
    void encode_nullValue() {
        ByteBuf buf = Unpooled.buffer();
        ConfigPayload.TUPLES_CODEC.encode(buf, null);
        assertEquals(0, buf.readableBytes());
    }

    @Test
    @DisplayName("encode writes a 4-byte size prefix followed by the entries")
    void encode_writesSizePrefix() {
        ByteBuf buf = Unpooled.buffer();
        ConfigPayload.TUPLES_CODEC.encode(buf, List.of(new ConfigEntry("a", "b")));
        assertEquals(1, buf.getInt(0));
    }
}

package net.immortaldevs.bindcmd.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    private ConfigLoader loader() {
        return new ConfigLoader(tempDir.toFile());
    }

    private void writeRawConfig(String content) throws IOException {
        Path configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);
        Files.write(configDir.resolve("bind_cmd.ini"), content.getBytes(StandardCharsets.UTF_8));
    }

    private String readRawConfig() throws IOException {
        return Files.readString(tempDir.resolve("config/bind_cmd.ini"), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("write then read round-trips a single entry")
    void roundTrip_singleEntry() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("key.keyboard.g", "/gamemode creative"));
        loader().write(entries, false);
        assertEquals(entries, loader().read());
    }

    @Test
    @DisplayName("write then read round-trips multiple distinct keys, preserving order")
    void roundTrip_multipleEntries() {
        List<ConfigEntry> entries = List.of(
                new ConfigEntry("key.keyboard.g", "/gamemode creative"),
                new ConfigEntry("key.keyboard.h", "hello there"),
                new ConfigEntry("key.mouse.left", "/spawn")
        );
        loader().write(entries, false);
        assertEquals(entries, loader().read());
    }

    @Test
    @DisplayName("multiple commands under one key become multiple lines and read back as multiple entries")
    void roundTrip_multipleCommandsPerKey() {
        List<ConfigEntry> entries = List.of(
                new ConfigEntry("key.keyboard.g", "/gamemode creative"),
                new ConfigEntry("key.keyboard.g", "/time set day")
        );
        loader().write(entries, false);
        assertEquals(entries, loader().read());
    }

    @Test
    @DisplayName("encode writes the expected key=\"value\" line format")
    void encode_lineFormat() throws IOException {
        loader().write(List.of(
                new ConfigEntry("key.keyboard.g", "/spawn"),
                new ConfigEntry("key.keyboard.h", "hi")
        ), false);
        assertEquals("key.keyboard.g=\"/spawn\"\nkey.keyboard.h=\"hi\"", readRawConfig());
    }

    @Test
    @DisplayName("a value that contains quotes (but no =\") round-trips intact")
    void roundTrip_embeddedQuotes() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("key.keyboard.t", "say \"hello\""));
        loader().write(entries, false);
        assertEquals(entries, loader().read());
    }

    @Test
    @DisplayName("a Unicode value round-trips intact")
    void roundTrip_unicode() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("key.keyboard.u", "say héllo 世界"));
        loader().write(entries, false);
        assertEquals(entries, loader().read());
    }

    @Test
    @DisplayName("blank lines and lines without =\" are skipped")
    void decode_skipsBlankAndMalformedLines() throws IOException {
        writeRawConfig("key.a=\"cmd one\"\n\nnot a config line\nkey.b=\"cmd two\"\n");
        assertEquals(List.of(
                new ConfigEntry("key.a", "cmd one"),
                new ConfigEntry("key.b", "cmd two")
        ), loader().read());
    }

    @Test
    @DisplayName("an empty value key=\"\" decodes to an empty command without crashing")
    void decode_emptyValue() throws IOException {
        writeRawConfig("key.a=\"\"");
        assertEquals(List.of(new ConfigEntry("key.a", "")), loader().read());
    }

    @Test
    @DisplayName("reading an empty file yields an empty list")
    void read_emptyFile() throws IOException {
        writeRawConfig("");
        assertTrue(loader().read().isEmpty());
    }

    @Test
    @DisplayName("reading a non-existent config yields an empty list")
    void read_nonexistentFile() {
        assertTrue(loader().read().isEmpty());
    }

    @Test
    @DisplayName("a value containing =\" must survive a round-trip")
    void roundTrip_valueContainingEqualsQuote() {
        List<ConfigEntry> entries = List.of(new ConfigEntry("key.keyboard.k", "a=\"b"));
        loader().write(entries, false);
        assertEquals(entries, loader().read());
    }

    @Test
    @DisplayName("CRLF-saved config file must decode without a stray trailing quote")
    void decode_crlfLineEndings() throws IOException {
        writeRawConfig("key.a=\"value\"\r\nkey.b=\"two\"\r\n");
        assertEquals(List.of(
                new ConfigEntry("key.a", "value"),
                new ConfigEntry("key.b", "two")
        ), loader().read());
    }
}

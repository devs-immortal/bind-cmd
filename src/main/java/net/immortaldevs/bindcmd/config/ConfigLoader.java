package net.immortaldevs.bindcmd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigLoader {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = CONFIG_DIR + "/bind_cmd.ini";
    private static final String CONFIG_BACKUP_FILE = CONFIG_DIR + "/bind_cmd.ini.bak";

    private final File runDirectory;

    public ConfigLoader(File runDirectory) {
        this.runDirectory = runDirectory;
    }

    public List<ConfigEntry> read() {
        ensureConfigDirExists();
        File file = new File(runDirectory, CONFIG_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
            return Collections.emptyList();
        }
        try (FileInputStream stream = new FileInputStream(file)) {
            byte[] contentBytes = stream.readAllBytes();
            String content = new String(contentBytes, StandardCharsets.UTF_8);
            return decode(content);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public void write(List<ConfigEntry> data, boolean backup) {
        ensureConfigDirExists();
        String fileName = backup ? CONFIG_BACKUP_FILE : CONFIG_FILE;
        File file = new File(runDirectory, fileName);
        String encoded = encode(data);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(encoded.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    private void ensureConfigDirExists() {
        File configDir = new File(runDirectory, CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    private List<ConfigEntry> decode(String input) {
        String[] lines = input.split("\n");
        List<ConfigEntry> data = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("=\"");
            if (parts.length != 2) continue;

            String translationKey = parts[0];
            String rawCommandPart = parts[1];
            if (!rawCommandPart.isEmpty()) {
                String command = rawCommandPart.substring(0, rawCommandPart.length() - 1);
                data.add(new ConfigEntry(translationKey, command));
            }
        }
        return data;
    }

    private String encode(List<ConfigEntry> data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            ConfigEntry entry = data.get(i);
            sb.append(entry.key()).append("=\"").append(entry.value()).append("\"");
            if (i < data.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

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
        File file = getConfigFile(CONFIG_FILE);
        if (file == null) {
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

    public boolean write(List<ConfigEntry> data, boolean backup) {
        String fileName = backup ? CONFIG_BACKUP_FILE : CONFIG_FILE;
        File file = getConfigFile(fileName);
        if (file == null) {
            return false;
        }

        String encoded = encode(data);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(encoded.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private File getConfigFile(String fileName) {
        File file = new File(runDirectory, fileName);
        if (!ensureConfigDirExists()) {
            try {
                if (!file.createNewFile()) {
                    return null;
                }
            } catch (IOException _) {
            }
            return null;
        }
        return file;
    }

    private boolean ensureConfigDirExists() {
        File configDir = new File(runDirectory, CONFIG_DIR);
        return configDir.exists() || configDir.mkdirs();
    }

    private List<ConfigEntry> decode(String input) {
        String[] lines = input.split("\r?\n");
        List<ConfigEntry> data = new ArrayList<>();
        for (String line : lines) {
            int sep = line.indexOf("=\"");
            if (sep < 0) continue;

            String translationKey = line.substring(0, sep);

            String rawCommandPart = line.substring(sep + 2);
            if (rawCommandPart.isEmpty() || rawCommandPart.charAt(rawCommandPart.length() - 1) != '"')
                continue;
            String command = rawCommandPart.substring(0, rawCommandPart.length() - 1);

            data.add(new ConfigEntry(translationKey, command));
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

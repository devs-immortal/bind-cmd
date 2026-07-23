package net.immortaldevs.bindcmd.config;

import static net.immortaldevs.bindcmd.BindCmd.LOGGER;

import net.immortaldevs.bindcmd.BindSource;
import net.immortaldevs.bindcmd.CommandBinding;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.*;

public final class Config {
    private static final ConfigLoader loader = new ConfigLoader(Minecraft.getInstance().gameDirectory);

    private static List<CommandBinding> clientBindings = new ArrayList<>();
    private static List<CommandBinding> serverBindings = Collections.emptyList();

    private static List<CommandBinding> allBindings = Collections.emptyList();
    private static Map<String, List<String>> commandsMap = Collections.emptyMap();

    public static List<CommandBinding> getBindings() {
        return allBindings;
    }

    public static List<String> getCommands(String translationKey) {
        return commandsMap.getOrDefault(translationKey, Collections.emptyList());
    }

    public static void load() {
        clientBindings = fromEntries(loader.read());
        LOGGER.info("Loaded {} client bindings from config", clientBindings.size());
        if (clientBindings.isEmpty() && !save(true)) {
            LOGGER.warn("Failed to write backup config");
        }
        updateBindings();
    }

    public static void loadWorldConfig(Path path) {
        if (path == null) return;
        
        List<ConfigEntry> data = new ConfigLoader(path.toFile()).read();
        serverBindings = fromEntries(data, BindSource.WORLD);
        LOGGER.info("Loaded {} world bindings from {}", serverBindings.size(), path);
        updateBindings();
    }

    public static void remove(CommandBinding binding) {
        if (clientBindings.remove(binding)) {
            updateBindings();
        }
    }

    public static void add(CommandBinding binding) {
        clientBindings.add(binding);
        updateBindings();
    }

    public static void setServerBindings(List<ConfigEntry> data) {
        LOGGER.info("Applied {} server binding entries", data.size());
        serverBindings = fromEntries(data, BindSource.SERVER);
        updateBindings();
    }

    public static void clearServerBindings() {
        LOGGER.debug("Cleared {} server bindings", serverBindings.size());
        serverBindings = Collections.emptyList();
        updateBindings();
    }

    public static void refresh() {
        updateBindings();
    }

    private static void updateBindings() {
        List<CommandBinding> combined = new ArrayList<>(clientBindings.size() + serverBindings.size());
        combined.addAll(clientBindings);
        combined.addAll(serverBindings);
        allBindings = Collections.unmodifiableList(combined);

        Map<String, List<String>> map = new HashMap<>();
        for (CommandBinding binding : combined) {
            map.computeIfAbsent(binding.getTranslationKey(), k -> new ArrayList<>()).addAll(binding.commands);
        }
        commandsMap = Collections.unmodifiableMap(map);
    }

    public static boolean save() {
        return save(false);
    }

    public static boolean save(boolean backup) {
        boolean ok = loader.write(toEntries(clientBindings), backup);
        if (ok) {
            LOGGER.info("Saved {} client bindings {}", clientBindings.size(), backup ? " (backup)" : "");
        } else {
            LOGGER.error("Failed to save client bindings {}", backup ? " (backup)" : "");
        }
        return ok;
    }

    private static List<ConfigEntry> toEntries(List<CommandBinding> data) {
        List<ConfigEntry> result = new ArrayList<>();
        for (CommandBinding binding : data) {
            for (String command : binding.commands) {
                result.add(new ConfigEntry(binding.getTranslationKey(), command));
            }
        }
        return result;
    }

    private static List<CommandBinding> fromEntries(List<ConfigEntry> data) {
        return fromEntries(data, BindSource.CLIENT);
    }

    private static List<CommandBinding> fromEntries(List<ConfigEntry> data, BindSource source) {
        Map<String, List<String>> commandsByKey = new LinkedHashMap<>();
        for (ConfigEntry entry : data) {
            commandsByKey.computeIfAbsent(entry.key(), k -> new ArrayList<>()).add(entry.value());
        }

        List<CommandBinding> result = new ArrayList<>(commandsByKey.size());
        for (Map.Entry<String, List<String>> entry : commandsByKey.entrySet()) {
            result.add(new CommandBinding(entry.getValue(), entry.getKey(), source));
        }
        return result;
    }
}

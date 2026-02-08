package net.immortaldevs.bindcmd.config;

import net.immortaldevs.bindcmd.BindSource;
import net.immortaldevs.bindcmd.CommandBinding;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Config {
    private static final ConfigLoader loader = new ConfigLoader(MinecraftClient.getInstance().runDirectory);

    private static List<CommandBinding> serverBindings = Collections.emptyList();
    private static List<CommandBinding> clientBindings = new ArrayList<>();

    public static List<CommandBinding> getBindings() {
        List<CommandBinding> result = new ArrayList<>(clientBindings.size() + serverBindings.size());
        result.addAll(clientBindings);
        result.addAll(serverBindings);
        return result;
    }

    public static List<String> getCommands(String translationKey) {
        List<String> result = new ArrayList<>();
        for (CommandBinding binding : getBindings()) {
            if (binding.getTranslationKey().equals(translationKey)) {
                result.add(binding.command);
            }
        }
        return result;
    }

    public static void load() {
        clientBindings = fromEntries(loader.read());
        if (clientBindings.isEmpty()) save(true);
    }

    public static void loadWorldConfig(Path path) {
        if (path == null) return;
        List<ConfigEntry> data = new ConfigLoader(path.toFile()).read();
        serverBindings = fromEntries(data, BindSource.WORLD);
    }

    public static void remove(CommandBinding binding) {
        clientBindings.remove(binding);
    }

    public static void add(CommandBinding binding) {
        clientBindings.add(binding);
    }

    public static void save() {
        save(false);
    }

    public static void save(boolean backup) {
        loader.write(toEntries(clientBindings), backup);
    }

    public static void setServerBindings(List<ConfigEntry> data) {
        serverBindings = fromEntries(data, BindSource.SERVER);
    }

    public static void clearServerBindings() {
        serverBindings = Collections.emptyList();
    }

    private static List<ConfigEntry> toEntries(List<CommandBinding> data) {
        List<ConfigEntry> result = new ArrayList<>(data.size());
        for (CommandBinding binding : data) {
            result.add(new ConfigEntry(binding.getTranslationKey(), binding.command));
        }
        return result;
    }

    private static List<CommandBinding> fromEntries(List<ConfigEntry> data) {
        return fromEntries(data, BindSource.CLIENT);
    }

    private static List<CommandBinding> fromEntries(List<ConfigEntry> data, BindSource source) {
        List<CommandBinding> result = new ArrayList<>(data.size());
        for (ConfigEntry entry : data) {
            String key = entry.key();
            String command = entry.value();
            result.add(new CommandBinding(command, key, source));
        }
        return result;
    }
}

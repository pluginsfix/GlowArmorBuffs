package pluginsfix.glowarmorbuffs.text;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Messages {
    private final Plugin plugin;
    private FileConfiguration config;
    private String prefix = "";

    public Messages(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.prefix = ColorUtils.colorize(config.getString("prefix", ""));
    }

    public String get(String path, Object... replacements) {
        String raw = config.getString(path);
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String formatted = applyReplacements(raw, replacements);
        return ColorUtils.colorize(prefix + formatted);
    }

    public String getWithoutPrefix(String path, Object... replacements) {
        String raw = config.getString(path);
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String formatted = applyReplacements(raw, replacements);
        return ColorUtils.colorize(formatted);
    }

    public List<String> getList(String path, Object... replacements) {
        if (!config.isList(path)) {
            String single = get(path, replacements);
            if (single.isEmpty()) {
                return Collections.emptyList();
            }
            return Collections.singletonList(single);
        }
        List<String> rawList = config.getStringList(path);
        List<String> result = new ArrayList<>(rawList.size());
        for (String line : rawList) {
            String formatted = applyReplacements(line, replacements);
            result.add(ColorUtils.colorize(formatted));
        }
        return result;
    }

    public void send(CommandSender sender, String path, Object... replacements) {
        if (config.isList(path)) {
            for (String line : getList(path, replacements)) {
                sender.sendMessage(line);
            }
            return;
        }
        String message = get(path, replacements);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    private String applyReplacements(String text, Object... replacements) {
        if (replacements == null || replacements.length == 0) {
            return text;
        }
        String result = text;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            result = result.replace(placeholder, value);
        }
        return result;
    }
}

package pluginsfix.glowarmorbuffs.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PlaceholderAPIHook {
    private final boolean available;

    public PlaceholderAPIHook() {
        this.available = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    public String setPlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (!available || player == null) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public List<String> setPlaceholders(Player player, List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        if (!available || player == null) {
            return list;
        }
        return PlaceholderAPI.setPlaceholders(player, list);
    }
}

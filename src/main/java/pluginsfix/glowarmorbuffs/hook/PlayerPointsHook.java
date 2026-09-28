package pluginsfix.glowarmorbuffs.hook;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class PlayerPointsHook {
    private final Plugin plugin;
    private PlayerPointsAPI pointsAPI;

    public PlayerPointsHook(Plugin plugin) {
        this.plugin = plugin;
        tryHook();
    }

    private void tryHook() {
        Plugin ppPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (ppPlugin instanceof PlayerPoints playerPoints) {
            this.pointsAPI = playerPoints.getAPI();
        }
    }

    public boolean isAvailable() {
        if (this.pointsAPI == null) {
            tryHook();
        }
        return this.pointsAPI != null;
    }

    public int getPoints(UUID uuid) {
        if (!isAvailable()) {
            return 0;
        }
        try {
            return pointsAPI.look(uuid);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean hasPoints(UUID uuid, int amount) {
        if (amount <= 0) {
            return true;
        }
        return getPoints(uuid) >= amount;
    }

    public boolean takePoints(UUID uuid, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }
        try {
            return pointsAPI.take(uuid, amount);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean givePoints(UUID uuid, int amount) {
        if (amount <= 0) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }
        try {
            return pointsAPI.give(uuid, amount);
        } catch (Exception e) {
            return false;
        }
    }
}

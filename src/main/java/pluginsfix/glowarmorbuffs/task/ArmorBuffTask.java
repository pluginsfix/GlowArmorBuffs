package pluginsfix.glowarmorbuffs.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pluginsfix.glowarmorbuffs.domain.BuffManager;

public final class ArmorBuffTask extends BukkitRunnable {
    private final BuffManager buffManager;

    public ArmorBuffTask(BuffManager buffManager) {
        this.buffManager = buffManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            buffManager.updatePlayerBuffs(player);
        }
    }
}

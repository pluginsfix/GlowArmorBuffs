package pluginsfix.glowarmorbuffs.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowarmorbuffs.domain.ArmorSlotType;
import pluginsfix.glowarmorbuffs.domain.BuffManager;

public final class ArmorListener implements Listener {
    private final Plugin plugin;
    private final BuffManager buffManager;

    public ArmorListener(Plugin plugin, BuffManager buffManager) {
        this.plugin = plugin;
        this.buffManager = buffManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                buffManager.updatePlayerBuffs(event.getPlayer());
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        buffManager.clearPlayerBuffs(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                buffManager.updatePlayerBuffs(event.getPlayer());
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        buffManager.clearPlayerBuffs(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryType.SlotType slotType = event.getSlotType();
        if (slotType == InventoryType.SlotType.ARMOR || event.isShiftClick()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    buffManager.updatePlayerBuffs(player);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (ArmorSlotType.match(item).isArmor()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    buffManager.updatePlayerBuffs(player);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(PlayerItemBreakEvent event) {
        if (ArmorSlotType.match(event.getBrokenItem()).isArmor()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    buffManager.updatePlayerBuffs(player);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (ArmorSlotType.match(event.getItemDrop().getItemStack()).isArmor()) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    buffManager.updatePlayerBuffs(player);
                }
            });
        }
    }
}

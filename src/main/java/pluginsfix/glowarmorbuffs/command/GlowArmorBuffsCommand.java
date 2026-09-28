package pluginsfix.glowarmorbuffs.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowarmorbuffs.config.PluginConfig;
import pluginsfix.glowarmorbuffs.domain.ArmorSlotType;
import pluginsfix.glowarmorbuffs.domain.BuffDefinition;
import pluginsfix.glowarmorbuffs.domain.BuffManager;
import pluginsfix.glowarmorbuffs.gui.BuffMenu;
import pluginsfix.glowarmorbuffs.text.Messages;

import java.util.*;
import java.util.stream.Collectors;

public final class GlowArmorBuffsCommand implements CommandExecutor, TabCompleter {
    private final PluginConfig config;
    private final Messages messages;
    private final BuffManager buffManager;
    private final BuffMenu buffMenu;

    public GlowArmorBuffsCommand(
            PluginConfig config,
            Messages messages,
            BuffManager buffManager,
            BuffMenu buffMenu
    ) {
        this.config = config;
        this.messages = messages;
        this.buffManager = buffManager;
        this.buffMenu = buffMenu;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            if (sender instanceof Player player && sender.hasPermission("glowarmorbuffs.menu")) {
                buffMenu.open(player);
                return true;
            }
            messages.send(sender, "command.usage");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("glowarmorbuffs.admin")) {
                messages.send(sender, "command.no-permission");
                return true;
            }
            config.reload();
            messages.reload();
            buffMenu.reload();
            messages.send(sender, "command.reloaded");
            return true;
        }

        if (sub.equals("menu")) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "command.player-only");
                return true;
            }
            if (!player.hasPermission("glowarmorbuffs.menu")) {
                messages.send(player, "command.no-permission");
                return true;
            }
            buffMenu.open(player);
            return true;
        }

        if (sub.equals("remove") || sub.equals("clear")) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "command.player-only");
                return true;
            }
            if (!player.hasPermission("glowarmorbuffs.remove")) {
                messages.send(player, "command.no-permission");
                return true;
            }

            ItemStack held = player.getInventory().getItemInMainHand();
            if (!ArmorSlotType.match(held).isArmor()) {
                messages.send(player, "buff.target-not-armor");
                return true;
            }
            if (!buffManager.hasBuffs(held)) {
                messages.send(player, "buff.no-buffs");
                return true;
            }

            ItemStack updated = buffManager.removeBuffs(held);
            player.getInventory().setItemInMainHand(updated);
            buffManager.updatePlayerBuffs(player);
            messages.send(player, "buff.removed");
            return true;
        }

        if (sub.equals("give")) {
            if (!sender.hasPermission("glowarmorbuffs.admin")) {
                messages.send(sender, "command.no-permission");
                return true;
            }
            if (args.length < 3) {
                messages.send(sender, "command.usage");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                messages.send(sender, "admin.player-not-found", "%player%", args[1]);
                return true;
            }

            BuffDefinition def = config.getEffect(args[2]);
            if (def == null) {
                messages.send(sender, "buff.invalid-effect", "%effect%", args[2]);
                return true;
            }

            int level = 1;
            if (args.length >= 4) {
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    messages.send(sender, "buff.invalid-level", "%max%", def.getMaxLevel());
                    return true;
                }
            }
            if (level < 1 || level > def.getMaxLevel()) {
                messages.send(sender, "buff.invalid-level", "%max%", def.getMaxLevel());
                return true;
            }

            ItemStack held = target.getInventory().getItemInMainHand();
            ArmorSlotType slot = ArmorSlotType.match(held);
            if (!slot.isArmor()) {
                messages.send(sender, "buff.target-not-armor");
                return true;
            }
            if (!def.isApplicable(slot)) {
                messages.send(sender, "buff.slot-not-allowed", "%effect%", def.getDisplayName());
                return true;
            }

            ItemStack updated = buffManager.applyBuff(held, def.getKey(), level);
            target.getInventory().setItemInMainHand(updated);
            buffManager.updatePlayerBuffs(target);

            messages.send(sender, "admin.given", "%player%", target.getName(), "%effect%", def.getDisplayName(), "%level%", level);
            messages.send(target, "buff.applied", "%effect%", def.getDisplayName(), "%level%", level);
            return true;
        }

        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }

        if (!player.hasPermission("glowarmorbuffs.apply")) {
            messages.send(player, "command.no-permission");
            return true;
        }

        BuffDefinition def = config.getEffect(args[0]);
        if (def == null) {
            messages.send(player, "buff.invalid-effect", "%effect%", args[0]);
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        ArmorSlotType slot = ArmorSlotType.match(held);
        if (!slot.isArmor()) {
            messages.send(player, "buff.target-not-armor");
            return true;
        }

        if (!def.isApplicable(slot)) {
            messages.send(player, "buff.slot-not-allowed", "%effect%", def.getDisplayName());
            return true;
        }

        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                messages.send(player, "buff.invalid-level", "%max%", def.getMaxLevel());
                return true;
            }
        }
        if (level < 1 || level > def.getMaxLevel()) {
            messages.send(player, "buff.invalid-level", "%max%", def.getMaxLevel());
            return true;
        }

        Map<String, Integer> currentBuffs = buffManager.getBuffs(held);
        if (!currentBuffs.containsKey(def.getKey()) && currentBuffs.size() >= config.getMaxEffectsPerArmor()) {
            messages.send(player, "buff.max-effects-reached", "%max%", config.getMaxEffectsPerArmor());
            return true;
        }

        ItemStack updated = buffManager.applyBuff(held, def.getKey(), level);
        player.getInventory().setItemInMainHand(updated);
        buffManager.updatePlayerBuffs(player);

        messages.send(player, "buff.applied", "%effect%", def.getDisplayName(), "%level%", level);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("glowarmorbuffs.menu")) {
                list.add("menu");
            }
            if (sender.hasPermission("glowarmorbuffs.remove")) {
                list.add("remove");
            }
            if (sender.hasPermission("glowarmorbuffs.admin")) {
                list.add("reload");
                list.add("give");
            }
            if (sender.hasPermission("glowarmorbuffs.apply")) {
                list.addAll(config.getEffects().keySet());
            }
            String prefix = args[0].toLowerCase();
            return list.stream()
                    .filter(s -> s.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("glowarmorbuffs.admin")) {
                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }

            BuffDefinition def = config.getEffect(args[0]);
            if (def != null && sender.hasPermission("glowarmorbuffs.apply")) {
                List<String> levels = new ArrayList<>();
                for (int i = 1; i <= def.getMaxLevel(); i++) {
                    levels.add(String.valueOf(i));
                }
                return levels;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("glowarmorbuffs.admin")) {
            String prefix = args[2].toLowerCase();
            return config.getEffects().keySet().stream()
                    .filter(k -> k.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give") && sender.hasPermission("glowarmorbuffs.admin")) {
            BuffDefinition def = config.getEffect(args[2]);
            if (def != null) {
                List<String> levels = new ArrayList<>();
                for (int i = 1; i <= def.getMaxLevel(); i++) {
                    levels.add(String.valueOf(i));
                }
                return levels;
            }
        }

        return Collections.emptyList();
    }
}

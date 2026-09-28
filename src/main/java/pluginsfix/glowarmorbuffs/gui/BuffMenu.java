package pluginsfix.glowarmorbuffs.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowarmorbuffs.config.PluginConfig;
import pluginsfix.glowarmorbuffs.domain.ArmorSlotType;
import pluginsfix.glowarmorbuffs.domain.BuffDefinition;
import pluginsfix.glowarmorbuffs.domain.BuffManager;
import pluginsfix.glowarmorbuffs.hook.PlaceholderAPIHook;
import pluginsfix.glowarmorbuffs.hook.PlayerPointsHook;
import pluginsfix.glowarmorbuffs.text.ColorUtils;
import pluginsfix.glowarmorbuffs.text.Messages;

import java.io.File;
import java.util.*;

public final class BuffMenu {
    private final Plugin plugin;
    private final PluginConfig config;
    private final Messages messages;
    private final BuffManager buffManager;
    private final PlayerPointsHook pointsHook;
    private final PlaceholderAPIHook papiHook;

    private String title = "Эффекты брони";
    private int size = 54;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final Map<Integer, MenuItem> slotMap = new HashMap<>();

    public BuffMenu(
            Plugin plugin,
            PluginConfig config,
            Messages messages,
            BuffManager buffManager,
            PlayerPointsHook pointsHook,
            PlaceholderAPIHook papiHook
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.buffManager = buffManager;
        this.pointsHook = pointsHook;
        this.papiHook = papiHook;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "menu.yml");
        if (!file.exists()) {
            plugin.saveResource("menu.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        this.title = ColorUtils.colorize(yaml.getString("title", "Эффекты брони"));
        this.size = yaml.getInt("size", 54);

        this.menuItems.clear();
        this.slotMap.clear();

        ConfigurationSection itemsSec = yaml.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                ConfigurationSection sec = itemsSec.getConfigurationSection(key);
                if (sec == null) continue;

                String matName = sec.getString("material", "STONE");
                Material material = Material.matchMaterial(matName);
                if (material == null) {
                    material = Material.STONE;
                }

                String name = sec.getString("name", "");
                List<String> lore = sec.getStringList("lore");

                List<Integer> slots = new ArrayList<>();
                if (sec.isList("slots")) {
                    slots = sec.getIntegerList("slots");
                } else if (sec.contains("slots") || sec.contains("slot")) {
                    slots.add(sec.getInt("slot", sec.getInt("slots", 0)));
                }

                String effect = sec.getString("effect", "");
                String action = sec.getString("action", "NONE");
                List<String> leftClickCommands = sec.getStringList("left_click_commands");
                String leftClickRequirement = sec.getString("left_click_requirement", "");

                MenuItem item = new MenuItem(key, material, name, lore, slots, effect, action, leftClickCommands, leftClickRequirement);
                menuItems.add(item);
                for (int slot : slots) {
                    if (slot >= 0 && slot < size) {
                        slotMap.put(slot, item);
                    }
                }
            }
        }
    }

    public void open(Player player) {
        BuffMenuHolder holder = new BuffMenuHolder(player);
        Inventory inventory = Bukkit.createInventory(holder, size, ColorUtils.toComponent(title));
        holder.setInventory(inventory);

        populateInventory(player, inventory);
        player.openInventory(inventory);
    }

    public void refresh(Player player, Inventory inventory) {
        inventory.clear();
        populateInventory(player, inventory);
    }

    private void populateInventory(Player player, Inventory inventory) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ArmorSlotType slotType = ArmorSlotType.match(heldItem);
        boolean isArmor = slotType.isArmor();
        Map<String, Integer> currentBuffs = isArmor ? buffManager.getBuffs(heldItem) : Collections.emptyMap();

        int points = pointsHook.getPoints(player.getUniqueId());

        for (MenuItem item : menuItems) {
            ItemStack stack = createItemStack(player, item, heldItem, slotType, isArmor, currentBuffs, points);
            for (int slot : item.slots()) {
                if (slot >= 0 && slot < size) {
                    inventory.setItem(slot, stack);
                }
            }
        }
    }

    private ItemStack createItemStack(
            Player player,
            MenuItem item,
            ItemStack heldItem,
            ArmorSlotType slotType,
            boolean isArmor,
            Map<String, Integer> currentBuffs,
            int playerPoints
    ) {
        Material mat = item.material();
        if (item.action().equalsIgnoreCase("PREVIEW_ARMOR") && isArmor) {
            mat = heldItem.getType();
        }

        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        if (meta instanceof SkullMeta skullMeta && mat == Material.PLAYER_HEAD) {
            skullMeta.setOwningPlayer(player);
        }

        String displayName = item.name();
        displayName = papiHook.setPlaceholders(player, displayName);
        displayName = displayName
                .replace("%player_name%", player.getName())
                .replace("%playerpoints_points%", String.valueOf(playerPoints));
        meta.displayName(ColorUtils.toComponent(ColorUtils.colorize(displayName)));

        List<String> processedLore = new ArrayList<>();
        BuffDefinition buffDef = (!item.effect().isEmpty()) ? config.getEffect(item.effect()) : null;
        int currentLevel = (buffDef != null && isArmor) ? currentBuffs.getOrDefault(buffDef.getKey(), 0) : 0;
        int maxLevel = buffDef != null ? buffDef.getMaxLevel() : 1;
        int nextLevel = Math.min(maxLevel, currentLevel + 1);
        int price = (buffDef != null) ? buffDef.getPrice(nextLevel) : 0;

        String statusLore = computeStatusLore(isArmor, slotType, buffDef, currentBuffs, currentLevel, maxLevel, nextLevel, price, playerPoints);

        for (String line : item.lore()) {
            if (line.contains("%status_lore%")) {
                for (String statusLine : statusLore.split("\n")) {
                    if (!statusLine.isEmpty()) {
                        processedLore.add(statusLine);
                    }
                }
                continue;
            }

            String formatted = line
                    .replace("%player_name%", player.getName())
                    .replace("%playerpoints_points%", String.valueOf(playerPoints))
                    .replace("%item_name%", isArmor ? getItemDisplayName(heldItem) : "&cБроня не выбрана")
                    .replace("%item_effects%", formatEffectsSummary(currentBuffs))
                    .replace("%current_level%", String.valueOf(currentLevel))
                    .replace("%max_level%", String.valueOf(maxLevel))
                    .replace("%next_level%", String.valueOf(nextLevel))
                    .replace("%price%", String.valueOf(price));

            formatted = papiHook.setPlaceholders(player, formatted);
            processedLore.add(ColorUtils.colorize(formatted));
        }

        List<Component> componentLore = new ArrayList<>(processedLore.size());
        for (String l : processedLore) {
            componentLore.add(ColorUtils.toComponent(l));
        }
        meta.lore(componentLore);
        stack.setItemMeta(meta);
        return stack;
    }

    private String computeStatusLore(
            boolean isArmor,
            ArmorSlotType slotType,
            BuffDefinition buffDef,
            Map<String, Integer> currentBuffs,
            int currentLevel,
            int maxLevel,
            int nextLevel,
            int price,
            int playerPoints
    ) {
        if (buffDef == null) {
            return "";
        }
        if (!isArmor) {
            return "&c[!] Возьмите броню в руку";
        }
        if (!buffDef.isApplicable(slotType)) {
            return "&c[!] Недоступно для этого слота";
        }
        if (currentLevel >= maxLevel) {
            return "&6✔ Максимальный уровень прокачки";
        }
        if (currentLevel == 0 && currentBuffs.size() >= config.getMaxEffectsPerArmor()) {
            return "&c[!] Лимит эффектов на предмете исчерпан";
        }

        String actionText = currentLevel == 0 ? "&a▶ Нажмите для покупки (Уровень 1)" : "&e▶ Нажмите для улучшения (Уровень " + nextLevel + ")";
        String priceText = playerPoints >= price ? "&7Стоимость: &#FF8600" + price + " ⛁" : "&7Стоимость: &c" + price + " ⛁ &7(не хватает)";

        return actionText + "\n" + priceText;
    }

    private String getItemDisplayName(ItemStack item) {
        if (item == null) return "";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        String name = item.getType().name().toLowerCase().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String formatEffectsSummary(Map<String, Integer> buffs) {
        if (buffs == null || buffs.isEmpty()) {
            return "&7Отсутствуют";
        }
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : buffs.entrySet()) {
            BuffDefinition def = config.getEffect(entry.getKey());
            String name = def != null ? def.getDisplayName() : entry.getKey();
            list.add(name + " " + entry.getValue());
        }
        return String.join("&7, ", list);
    }

    public void handleClick(Player player, int slot, InventoryClickEvent event) {
        event.setCancelled(true);

        MenuItem item = slotMap.get(slot);
        if (item == null) {
            return;
        }

        String action = item.action().toUpperCase();
        switch (action) {
            case "CLOSE_MENU" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                player.closeInventory();
            }
            case "REMOVE_BUFFS" -> handleRemoveBuffs(player, event.getInventory());
            case "BUY_BUFF" -> handleBuyBuff(player, item, event.getInventory());
            default -> {}
        }

        executeClickCommands(player, item.leftClickCommands());
    }

    private void handleRemoveBuffs(Player player, Inventory inventory) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ArmorSlotType slotType = ArmorSlotType.match(heldItem);
        if (!slotType.isArmor()) {
            messages.send(player, "buff.target-not-armor");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (!buffManager.hasBuffs(heldItem)) {
            messages.send(player, "buff.no-buffs");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        ItemStack updated = buffManager.removeBuffs(heldItem);
        player.getInventory().setItemInMainHand(updated);
        buffManager.updatePlayerBuffs(player);

        messages.send(player, "buff.removed");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        refresh(player, inventory);
    }

    private void handleBuyBuff(Player player, MenuItem item, Inventory inventory) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        ArmorSlotType slotType = ArmorSlotType.match(heldItem);
        if (!slotType.isArmor()) {
            messages.send(player, "buff.target-not-armor");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        BuffDefinition def = config.getEffect(item.effect());
        if (def == null) {
            messages.send(player, "buff.invalid-effect", "%effect%", item.effect());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (!def.isApplicable(slotType)) {
            messages.send(player, "buff.slot-not-allowed", "%effect%", def.getDisplayName());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        Map<String, Integer> currentBuffs = buffManager.getBuffs(heldItem);
        int currentLevel = currentBuffs.getOrDefault(def.getKey(), 0);
        if (currentLevel >= def.getMaxLevel()) {
            messages.send(player, "buff.already-max-level");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (currentLevel == 0 && currentBuffs.size() >= config.getMaxEffectsPerArmor()) {
            messages.send(player, "buff.max-effects-reached", "%max%", config.getMaxEffectsPerArmor());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        int nextLevel = currentLevel + 1;
        int price = def.getPrice(nextLevel);

        if (price > 0) {
            if (!pointsHook.isAvailable()) {
                messages.send(player, "purchase.points-unavailable");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }

            if (!pointsHook.hasPoints(player.getUniqueId(), price)) {
                messages.send(player, "purchase.not-enough-points", "%price%", price);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }

            boolean withdrawn = pointsHook.takePoints(player.getUniqueId(), price);
            if (!withdrawn) {
                messages.send(player, "purchase.points-unavailable");
                return;
            }
        }

        ItemStack updated = buffManager.applyBuff(heldItem, def.getKey(), nextLevel);
        player.getInventory().setItemInMainHand(updated);
        buffManager.updatePlayerBuffs(player);

        messages.send(player, "purchase.success", "%effect%", def.getDisplayName(), "%level%", nextLevel, "%price%", price);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        refresh(player, inventory);
    }

    private void executeClickCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        for (String cmd : commands) {
            String formatted = cmd.replace("{player}", player.getName()).replace("%player%", player.getName());
            if (formatted.startsWith("[console]")) {
                String consoleCmd = formatted.substring("[console]".length()).trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd);
            } else if (formatted.startsWith("[player]")) {
                String playerCmd = formatted.substring("[player]".length()).trim();
                player.performCommand(playerCmd);
            }
        }
    }
}

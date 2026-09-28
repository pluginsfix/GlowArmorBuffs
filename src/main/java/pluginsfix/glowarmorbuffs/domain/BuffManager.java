package pluginsfix.glowarmorbuffs.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pluginsfix.glowarmorbuffs.config.PluginConfig;
import pluginsfix.glowarmorbuffs.text.ColorUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BuffManager {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final String LORE_MARKER = "§z§g§a§b";

    private final Plugin plugin;
    private final PluginConfig config;
    private final NamespacedKey buffsKey;
    private final Map<UUID, Set<PotionEffectType>> activePlayerEffects = new ConcurrentHashMap<>();

    public BuffManager(Plugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.buffsKey = new NamespacedKey(plugin, "buffs");
    }

    public Map<String, Integer> getBuffs(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Collections.emptyMap();
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String json = pdc.get(buffsKey, PersistentDataType.STRING);
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Integer> map = GSON.fromJson(json, MAP_TYPE);
            return map != null ? map : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public boolean hasBuffs(ItemStack item) {
        return !getBuffs(item).isEmpty();
    }

    public int getBuffLevel(ItemStack item, String buffKey) {
        if (buffKey == null) return 0;
        return getBuffs(item).getOrDefault(buffKey.toUpperCase(), 0);
    }

    public ItemStack applyBuff(ItemStack item, String buffKey, int level) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Map<String, Integer> map = new LinkedHashMap<>(getBuffs(item));
        map.put(buffKey.toUpperCase(), level);

        String json = GSON.toJson(map);
        meta.getPersistentDataContainer().set(buffsKey, PersistentDataType.STRING, json);
        item.setItemMeta(meta);

        return updateLore(item, map);
    }

    public ItemStack removeBuffs(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer().remove(buffsKey);
        item.setItemMeta(meta);

        return updateLore(item, Collections.emptyMap());
    }

    public ItemStack updateLore(ItemStack item, Map<String, Integer> buffs) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> currentLore = meta.lore();
        List<String> cleanLore = new ArrayList<>();

        if (currentLore != null) {
            for (Component comp : currentLore) {
                String legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(comp);
                if (legacy.contains(LORE_MARKER)) {
                    continue;
                }
                String stripped = ColorUtils.stripColor(legacy).trim();
                if (stripped.startsWith("Эффект:") || stripped.startsWith("Эффекты:")) {
                    continue;
                }
                cleanLore.add(legacy);
            }
        }

        List<String> newBuffLines = new ArrayList<>();
        if (!buffs.isEmpty()) {
            for (Map.Entry<String, Integer> entry : buffs.entrySet()) {
                BuffDefinition def = config.getEffect(entry.getKey());
                String effectName = def != null ? def.getDisplayName() : entry.getKey();
                int lvl = entry.getValue();

                for (String formatLine : config.getLoreFormat()) {
                    String formatted = formatLine
                            .replace("%effect%", effectName)
                            .replace("%effect_name%", effectName)
                            .replace("%level%", String.valueOf(lvl))
                            .replace("%level_number%", String.valueOf(lvl))
                            .replace("%level_roman%", ColorUtils.toRoman(lvl));
                    newBuffLines.add(ColorUtils.colorize(LORE_MARKER + formatted));
                }
            }
        }

        List<String> finalLoreStrings = new ArrayList<>(newBuffLines);
        finalLoreStrings.addAll(cleanLore);

        List<Component> finalComponents = new ArrayList<>(finalLoreStrings.size());
        for (String line : finalLoreStrings) {
            finalComponents.add(ColorUtils.toComponent(line));
        }

        meta.lore(finalComponents);
        item.setItemMeta(meta);
        return item;
    }

    public void updatePlayerBuffs(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Map<PotionEffectType, Integer> requiredEffects = new HashMap<>();

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getType().isAir()) {
                continue;
            }
            ArmorSlotType slotType = ArmorSlotType.match(armor);
            if (!slotType.isArmor()) {
                continue;
            }

            Map<String, Integer> buffs = getBuffs(armor);
            for (Map.Entry<String, Integer> entry : buffs.entrySet()) {
                BuffDefinition def = config.getEffect(entry.getKey());
                if (def == null || !def.isApplicable(slotType)) {
                    continue;
                }

                PotionEffectType type = def.getPotionType();
                int level = entry.getValue();
                requiredEffects.merge(type, level, Math::max);
            }
        }

        Set<PotionEffectType> currentTracked = activePlayerEffects.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());

        for (PotionEffectType tracked : new HashSet<>(currentTracked)) {
            if (!requiredEffects.containsKey(tracked)) {
                player.removePotionEffect(tracked);
                currentTracked.remove(tracked);
            }
        }

        int durationTicks = config.getEffectDurationSeconds() * 20;
        boolean ambient = config.isAmbientEffects();
        boolean particles = config.isShowParticles();
        boolean icon = config.isShowIcon();

        for (Map.Entry<PotionEffectType, Integer> entry : requiredEffects.entrySet()) {
            PotionEffectType type = entry.getKey();
            int amplifier = Math.max(0, entry.getValue() - 1);

            PotionEffect existing = player.getPotionEffect(type);
            if (existing == null || existing.getAmplifier() != amplifier || existing.getDuration() < 60) {
                player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, ambient, particles, icon));
                currentTracked.add(type);
            }
        }
    }

    public void clearPlayerBuffs(Player player) {
        if (player == null) return;
        Set<PotionEffectType> tracked = activePlayerEffects.remove(player.getUniqueId());
        if (tracked != null) {
            for (PotionEffectType type : tracked) {
                player.removePotionEffect(type);
            }
        }
    }
}

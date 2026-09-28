package pluginsfix.glowarmorbuffs.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import pluginsfix.glowarmorbuffs.domain.ArmorSlotType;
import pluginsfix.glowarmorbuffs.domain.BuffDefinition;
import pluginsfix.glowarmorbuffs.domain.BuffLevel;

import java.io.File;
import java.util.*;

public final class PluginConfig {
    private final Plugin plugin;
    private int checkIntervalTicks;
    private int effectDurationSeconds;
    private boolean showParticles;
    private boolean showIcon;
    private boolean ambientEffects;
    private int maxEffectsPerArmor;
    private String currency;
    private List<String> loreFormat;
    private final Map<String, BuffDefinition> effects = new LinkedHashMap<>();

    public PluginConfig(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        this.checkIntervalTicks = config.getInt("settings.check-interval-ticks", 20);
        this.effectDurationSeconds = config.getInt("settings.effect-duration-seconds", 10);
        this.showParticles = config.getBoolean("settings.show-particles", false);
        this.showIcon = config.getBoolean("settings.show-icon", true);
        this.ambientEffects = config.getBoolean("settings.ambient-effects", true);
        this.maxEffectsPerArmor = config.getInt("settings.max-effects-per-armor", 1);
        this.currency = config.getString("settings.currency", "PLAYER_POINTS");

        this.loreFormat = config.getStringList("lore.format");
        if (this.loreFormat.isEmpty()) {
            this.loreFormat = List.of("&7Эффект: &#FF8600%effect% %level%");
        }

        this.effects.clear();
        ConfigurationSection effectsSection = config.getConfigurationSection("effects");
        if (effectsSection != null) {
            for (String key : effectsSection.getKeys(false)) {
                ConfigurationSection sec = effectsSection.getConfigurationSection(key);
                if (sec == null) continue;

                String displayName = sec.getString("display-name", key);
                String potionTypeName = sec.getString("potion-type", key);
                PotionEffectType potionType = BuffDefinition.resolvePotionType(potionTypeName);
                if (potionType == null) {
                    continue;
                }

                int maxLevel = sec.getInt("max-level", 1);
                List<String> slotNames = sec.getStringList("applicable-slots");
                Set<ArmorSlotType> slots = EnumSet.noneOf(ArmorSlotType.class);
                for (String slotName : slotNames) {
                    try {
                        slots.add(ArmorSlotType.valueOf(slotName.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }

                Map<Integer, BuffLevel> levels = new HashMap<>();
                ConfigurationSection levelsSec = sec.getConfigurationSection("levels");
                if (levelsSec != null) {
                    for (String lvlKey : levelsSec.getKeys(false)) {
                        try {
                            int lvl = Integer.parseInt(lvlKey);
                            int price = levelsSec.getInt(lvlKey + ".price", 0);
                            levels.put(lvl, new BuffLevel(lvl, price));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (levels.isEmpty()) {
                    for (int i = 1; i <= maxLevel; i++) {
                        levels.put(i, new BuffLevel(i, 0));
                    }
                }

                BuffDefinition definition = new BuffDefinition(key.toUpperCase(), displayName, potionType, maxLevel, slots, levels);
                effects.put(key.toUpperCase(), definition);
            }
        }
    }

    public int getCheckIntervalTicks() {
        return checkIntervalTicks;
    }

    public int getEffectDurationSeconds() {
        return effectDurationSeconds;
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public boolean isAmbientEffects() {
        return ambientEffects;
    }

    public int getMaxEffectsPerArmor() {
        return maxEffectsPerArmor;
    }

    public String getCurrency() {
        return currency;
    }

    public List<String> getLoreFormat() {
        return loreFormat;
    }

    public Map<String, BuffDefinition> getEffects() {
        return Collections.unmodifiableMap(effects);
    }

    public BuffDefinition getEffect(String key) {
        if (key == null) return null;
        BuffDefinition def = effects.get(key.toUpperCase());
        if (def != null) return def;
        for (BuffDefinition d : effects.values()) {
            if (d.getPotionType().getName().equalsIgnoreCase(key)) {
                return d;
            }
        }
        return null;
    }
}

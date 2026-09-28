package pluginsfix.glowarmorbuffs.domain;

import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class BuffDefinition {
    private final String key;
    private final String displayName;
    private final PotionEffectType potionType;
    private final int maxLevel;
    private final Set<ArmorSlotType> applicableSlots;
    private final Map<Integer, BuffLevel> levels;

    public BuffDefinition(
            String key,
            String displayName,
            PotionEffectType potionType,
            int maxLevel,
            Set<ArmorSlotType> applicableSlots,
            Map<Integer, BuffLevel> levels
    ) {
        this.key = key;
        this.displayName = displayName;
        this.potionType = potionType;
        this.maxLevel = maxLevel;
        this.applicableSlots = applicableSlots.isEmpty() ? EnumSet.of(ArmorSlotType.HELMET, ArmorSlotType.CHESTPLATE, ArmorSlotType.LEGGINGS, ArmorSlotType.BOOTS) : Collections.unmodifiableSet(applicableSlots);
        this.levels = Collections.unmodifiableMap(levels);
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PotionEffectType getPotionType() {
        return potionType;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Set<ArmorSlotType> getApplicableSlots() {
        return applicableSlots;
    }

    public boolean isApplicable(ArmorSlotType slot) {
        return applicableSlots.contains(slot);
    }

    public BuffLevel getLevel(int level) {
        return levels.get(level);
    }

    public int getPrice(int level) {
        BuffLevel buffLevel = levels.get(level);
        return buffLevel != null ? buffLevel.price() : 0;
    }

    public static PotionEffectType resolvePotionType(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String upper = name.toUpperCase();
        PotionEffectType type = PotionEffectType.getByName(upper);
        if (type != null) {
            return type;
        }
        return switch (upper) {
            case "STRENGTH" -> PotionEffectType.INCREASE_DAMAGE;
            case "RESISTANCE" -> PotionEffectType.DAMAGE_RESISTANCE;
            case "HASTE" -> PotionEffectType.FAST_DIGGING;
            case "MINING_FATIGUE" -> PotionEffectType.SLOW_DIGGING;
            case "SLOWNESS" -> PotionEffectType.SLOW;
            case "INSTANT_DAMAGE", "HARMING" -> PotionEffectType.HARM;
            case "INSTANT_HEALTH", "HEALING" -> PotionEffectType.HEAL;
            case "JUMP_BOOST" -> PotionEffectType.JUMP;
            case "NAUSEA" -> PotionEffectType.CONFUSION;
            default -> null;
        };
    }
}

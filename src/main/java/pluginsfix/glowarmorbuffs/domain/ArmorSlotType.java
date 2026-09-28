package pluginsfix.glowarmorbuffs.domain;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public enum ArmorSlotType {
    HELMET(EquipmentSlot.HEAD),
    CHESTPLATE(EquipmentSlot.CHEST),
    LEGGINGS(EquipmentSlot.LEGS),
    BOOTS(EquipmentSlot.FEET),
    UNKNOWN(EquipmentSlot.HAND);

    private final EquipmentSlot equipmentSlot;

    ArmorSlotType(EquipmentSlot equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public static ArmorSlotType match(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return UNKNOWN;
        }
        return match(item.getType());
    }

    public static ArmorSlotType match(Material material) {
        if (material == null) {
            return UNKNOWN;
        }
        String name = material.name();
        if (name.endsWith("_HELMET") || name.equals("TURTLE_HELMET")) {
            return HELMET;
        }
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) {
            return CHESTPLATE;
        }
        if (name.endsWith("_LEGGINGS")) {
            return LEGGINGS;
        }
        if (name.endsWith("_BOOTS")) {
            return BOOTS;
        }
        return UNKNOWN;
    }

    public boolean isArmor() {
        return this != UNKNOWN;
    }
}

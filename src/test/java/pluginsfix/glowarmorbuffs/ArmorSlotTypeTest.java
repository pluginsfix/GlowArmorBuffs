package pluginsfix.glowarmorbuffs;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import pluginsfix.glowarmorbuffs.domain.ArmorSlotType;

import static org.assertj.core.api.Assertions.assertThat;

public class ArmorSlotTypeTest {

    @Test
    public void testHelmetDetection() {
        assertThat(ArmorSlotType.match(Material.DIAMOND_HELMET)).isEqualTo(ArmorSlotType.HELMET);
        assertThat(ArmorSlotType.match(Material.NETHERITE_HELMET)).isEqualTo(ArmorSlotType.HELMET);
        assertThat(ArmorSlotType.match(Material.TURTLE_HELMET)).isEqualTo(ArmorSlotType.HELMET);
    }

    @Test
    public void testChestplateDetection() {
        assertThat(ArmorSlotType.match(Material.DIAMOND_CHESTPLATE)).isEqualTo(ArmorSlotType.CHESTPLATE);
        assertThat(ArmorSlotType.match(Material.NETHERITE_CHESTPLATE)).isEqualTo(ArmorSlotType.CHESTPLATE);
        assertThat(ArmorSlotType.match(Material.ELYTRA)).isEqualTo(ArmorSlotType.CHESTPLATE);
    }

    @Test
    public void testLeggingsDetection() {
        assertThat(ArmorSlotType.match(Material.DIAMOND_LEGGINGS)).isEqualTo(ArmorSlotType.LEGGINGS);
        assertThat(ArmorSlotType.match(Material.NETHERITE_LEGGINGS)).isEqualTo(ArmorSlotType.LEGGINGS);
    }

    @Test
    public void testBootsDetection() {
        assertThat(ArmorSlotType.match(Material.DIAMOND_BOOTS)).isEqualTo(ArmorSlotType.BOOTS);
        assertThat(ArmorSlotType.match(Material.NETHERITE_BOOTS)).isEqualTo(ArmorSlotType.BOOTS);
    }

    @Test
    public void testNonArmorDetection() {
        assertThat(ArmorSlotType.match(Material.DIAMOND_SWORD)).isEqualTo(ArmorSlotType.UNKNOWN);
        assertThat(ArmorSlotType.match(Material.STONE)).isEqualTo(ArmorSlotType.UNKNOWN);
        assertThat(ArmorSlotType.match(Material.AIR)).isEqualTo(ArmorSlotType.UNKNOWN);
    }
}

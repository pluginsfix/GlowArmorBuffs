package pluginsfix.glowarmorbuffs.gui;

import org.bukkit.Material;

import java.util.List;

public record MenuItem(
        String key,
        Material material,
        String name,
        List<String> lore,
        List<Integer> slots,
        String effect,
        String action,
        List<String> leftClickCommands,
        String leftClickRequirement
) {
}

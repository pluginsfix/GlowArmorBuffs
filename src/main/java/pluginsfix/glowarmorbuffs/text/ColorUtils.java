package pluginsfix.glowarmorbuffs.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ColorUtils() {}

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1).toLowerCase();
            StringBuilder replacement = new StringBuilder("§x");
            for (char ch : hex.toCharArray()) {
                replacement.append('§').append(ch);
            }
            matcher.appendReplacement(builder, replacement.toString());
        }
        matcher.appendTail(builder);
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    public static List<String> colorize(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>(list.size());
        for (String s : list) {
            result.add(colorize(s));
        }
        return result;
    }

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> toComponents(List<String> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<Component> components = new ArrayList<>(list.size());
        for (String line : list) {
            components.add(toComponent(line));
        }
        return components;
    }

    public static String stripColor(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.stripColor(colorize(text));
    }

    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }
}

package pluginsfix.glowarmorbuffs;

import org.junit.jupiter.api.Test;
import pluginsfix.glowarmorbuffs.text.ColorUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class ColorUtilsTest {

    @Test
    public void testRomanNumerals() {
        assertThat(ColorUtils.toRoman(1)).isEqualTo("I");
        assertThat(ColorUtils.toRoman(2)).isEqualTo("II");
        assertThat(ColorUtils.toRoman(3)).isEqualTo("III");
        assertThat(ColorUtils.toRoman(4)).isEqualTo("IV");
        assertThat(ColorUtils.toRoman(5)).isEqualTo("V");
        assertThat(ColorUtils.toRoman(10)).isEqualTo("X");
    }

    @Test
    public void testColorize() {
        String input = "&#FF8600Test &aMessage";
        String colorized = ColorUtils.colorize(input);
        assertThat(colorized).contains("§x§f§f§8§6§0§0");
        assertThat(colorized).contains("§aMessage");
    }

    @Test
    public void testStripColor() {
        String input = "§x§f§f§8§6§0§0Test §aMessage";
        String stripped = ColorUtils.stripColor(input);
        assertThat(stripped).isEqualTo("Test Message");
    }
}

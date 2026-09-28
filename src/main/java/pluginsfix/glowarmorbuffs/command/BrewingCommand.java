package pluginsfix.glowarmorbuffs.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowarmorbuffs.gui.BuffMenu;
import pluginsfix.glowarmorbuffs.text.Messages;

import java.util.Collections;
import java.util.List;

public final class BrewingCommand implements CommandExecutor, TabCompleter {
    private final BuffMenu buffMenu;
    private final Messages messages;

    public BrewingCommand(BuffMenu buffMenu, Messages messages) {
        this.buffMenu = buffMenu;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
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

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        return Collections.emptyList();
    }
}

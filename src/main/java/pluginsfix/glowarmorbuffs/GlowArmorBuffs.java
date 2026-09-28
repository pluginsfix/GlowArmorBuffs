package pluginsfix.glowarmorbuffs;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.glowarmorbuffs.command.BrewingCommand;
import pluginsfix.glowarmorbuffs.command.GlowArmorBuffsCommand;
import pluginsfix.glowarmorbuffs.config.PluginConfig;
import pluginsfix.glowarmorbuffs.domain.BuffManager;
import pluginsfix.glowarmorbuffs.gui.BuffMenu;
import pluginsfix.glowarmorbuffs.hook.PlaceholderAPIHook;
import pluginsfix.glowarmorbuffs.hook.PlayerPointsHook;
import pluginsfix.glowarmorbuffs.listener.ArmorListener;
import pluginsfix.glowarmorbuffs.listener.MenuListener;
import pluginsfix.glowarmorbuffs.task.ArmorBuffTask;
import pluginsfix.glowarmorbuffs.text.Messages;

public final class GlowArmorBuffs extends JavaPlugin {
    private PluginConfig pluginConfig;
    private Messages messages;
    private BuffManager buffManager;
    private BuffMenu buffMenu;
    private PlayerPointsHook playerPointsHook;
    private PlaceholderAPIHook placeholderAPIHook;
    private ArmorBuffTask armorBuffTask;

    @Override
    public void onEnable() {
        this.pluginConfig = new PluginConfig(this);
        this.messages = new Messages(this);
        this.buffManager = new BuffManager(this, pluginConfig);
        this.playerPointsHook = new PlayerPointsHook(this);
        this.placeholderAPIHook = new PlaceholderAPIHook();
        this.buffMenu = new BuffMenu(this, pluginConfig, messages, buffManager, playerPointsHook, placeholderAPIHook);

        getServer().getPluginManager().registerEvents(new ArmorListener(this, buffManager), this);
        getServer().getPluginManager().registerEvents(new MenuListener(buffMenu), this);

        PluginCommand mainCommand = getCommand("glowarmorbuffs");
        if (mainCommand != null) {
            GlowArmorBuffsCommand executor = new GlowArmorBuffsCommand(pluginConfig, messages, buffManager, buffMenu);
            mainCommand.setExecutor(executor);
            mainCommand.setTabCompleter(executor);
        }

        PluginCommand brewingCommand = getCommand("brewing");
        if (brewingCommand != null) {
            BrewingCommand executor = new BrewingCommand(buffMenu, messages);
            brewingCommand.setExecutor(executor);
            brewingCommand.setTabCompleter(executor);
        }

        this.armorBuffTask = new ArmorBuffTask(buffManager);
        this.armorBuffTask.runTaskTimer(this, 20L, pluginConfig.getCheckIntervalTicks());
    }

    @Override
    public void onDisable() {
        if (armorBuffTask != null) {
            armorBuffTask.cancel();
        }
        if (buffManager != null) {
            getServer().getOnlinePlayers().forEach(buffManager::clearPlayerBuffs);
        }
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public Messages getMessages() {
        return messages;
    }

    public BuffManager getBuffManager() {
        return buffManager;
    }

    public BuffMenu getBuffMenu() {
        return buffMenu;
    }

    public PlayerPointsHook getPlayerPointsHook() {
        return playerPointsHook;
    }

    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }
}

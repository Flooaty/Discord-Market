package plugin.discordMarket;

import org.bukkit.plugin.java.JavaPlugin;
import plugin.discordMarket.discord.DiscordBotManager;
import plugin.discordMarket.minecraft.ListingCommand;

public final class DiscordMarket extends JavaPlugin {
    private static JavaPlugin plugin;
    private static DiscordBotManager discordBotManager;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

        discordBotManager = new DiscordBotManager();
        discordBotManager.startBot();

        getServer().getPluginCommand("listing").setExecutor(new ListingCommand());
    }

    @Override
    public void onDisable() {
        discordBotManager.shutdown();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static DiscordBotManager getDiscordBotManager() {
        return discordBotManager;
    }
}

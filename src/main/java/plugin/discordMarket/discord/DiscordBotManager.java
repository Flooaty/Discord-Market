package plugin.discordMarket.discord;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static plugin.discordMarket.DiscordMarket.getPlugin;

public class DiscordBotManager {
    String botToken = getPlugin().getConfig().getString("bot.token");
    long channelID = getPlugin().getConfig().getLong("bot.channelID");
    private JDA jda;

    public void startBot() {
        if (botToken == null || botToken.isEmpty()) {
            getPlugin().getLogger().severe("Bot token is missing in config.yml!");
            return;
        }

        try {
            this.jda = JDABuilder.createDefault(botToken)
                    .build()
                    .awaitReady();

            getPlugin().getLogger().info("Discord Bot connected successfully!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            getPlugin().getLogger().severe("Failed to login to Discord: " + e.getMessage());
        }
    }

    public void sendListingEmbed(ItemStack listedItem, Player player, String price) {
        TextChannel marketChannel = jda.getTextChannelById(String.valueOf(channelID));

        if (marketChannel == null) {
            getPlugin().getLogger().warning("Could not find TextChannel with ID: " + channelID);
            return;
        }

        ItemMeta meta = listedItem.getItemMeta();

        String displayName = listedItem.getType().name();
        if (meta.hasDisplayName()) displayName = meta.getDisplayName();
        if (meta.hasCustomName()) displayName = meta.getItemName();

        System.out.println(displayName);

        String description = getItemInfo(listedItem);

        Color color;
        if (meta.hasRarity()) color = Color.decode(meta.getRarity().color().asHexString());
        else color = Color.decode(listedItem.getType().getDefaultData(DataComponentTypes.RARITY).color().asHexString());

        MessageEmbed embed = new EmbedBuilder()
                .setTitle(displayName)
                .setColor(color)
                .setDescription(description)
                .addField("Price:", price, true)
                .setFooter(player.getName(), "https://render.crafty.gg/3d/bust/" + player.getUniqueId())
                .setTimestamp(Instant.now())
                .build();

        marketChannel.sendMessageEmbeds(embed).queue();
    }

    public void shutdown() {
        jda.shutdown();
    }

    public String getItemInfo(ItemStack item) {
        if (item == null || item.getType().isAir()) return "No Item";

        StringBuilder sb = new StringBuilder();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // 2. Get Enchants
            if (meta.hasEnchants()) {
                sb.append("**Enchantments:**\n");
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    String enchantName = entry.getKey().getKey().getKey(); // Gets names like "sharpness"
                    sb.append("> ").append(enchantName).append(" ").append(entry.getValue()).append("\n");
                }
            }

            // 3. Get Lore
            List<String> lore = meta.getLore();

            if (lore != null) {
                sb.append("**Lore:**\n");

                for (String line : lore) {
                    sb.append("> ").append(line).append("\n");
                }
            }

        }

        return sb.toString();
    }
}
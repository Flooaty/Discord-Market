package plugin.discordMarket.discord;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

    public void sendListingEmbed(ItemStack listedItem, Player player, String price, String discordName) {
        TextChannel marketChannel = jda.getTextChannelById(String.valueOf(channelID));

        if (marketChannel == null) {
            getPlugin().getLogger().warning("Could not find TextChannel with ID: " + channelID);
            return;
        }

        ItemMeta meta = listedItem.getItemMeta();

        String itemName = listedItem.getType().name().replace("_", " ").toLowerCase();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                itemName = meta.getDisplayName();
            } else if (meta.hasCustomName()) {
                itemName = meta.getItemName();
            }
        }

        itemName = org.bukkit.ChatColor.stripColor(itemName).toUpperCase();

        String itemDetails = getItemInfo(listedItem);
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("### Item: ")
                .append("__")
                .append(itemName)
                .append("__")
                .append("\n\n");

        if (!itemDetails.isEmpty()) {
            descriptionBuilder.append(itemDetails);
        }

        Color color;
        try {
            if (meta != null && meta.hasRarity()) {
                color = Color.decode(meta.getRarity().color().asHexString());
            } else {
                color = Color.decode(listedItem.getType().getDefaultData(DataComponentTypes.RARITY).color().asHexString());
            }
        } catch (Exception e) {
            color = Color.RED;
        }


        MessageEmbed embed = new EmbedBuilder()
                .setTitle("📦 New Listing")
                .setColor(color)
                .setDescription(descriptionBuilder.toString())
                .addField("Price:", price, true)
                .addField("Discord:", discordName, true)
                .setFooter("Seller: " + player.getName(), "https://render.crafty.gg/3d/bust/" + player.getUniqueId())
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
            if (meta.hasEnchants()) {
                sb.append("**Enchantments:**\n");
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    String enchantName = entry.getKey().getKey().getKey();
                    sb.append("> ").append(enchantName).append(" ").append(entry.getValue()).append("\n");
                }
            }

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
package plugin.discordMarket.minecraft;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static plugin.discordMarket.DiscordMarket.getDiscordBotManager;

public class ListingCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        //listing create <Amount> <Material> [DiscordName]
        //[0]=create, [1]=amount, [2]=material
        if (args.length < 3 || !args[0].equalsIgnoreCase("create")) {
            player.sendMessage("§cUsage: /listing create <Amount> <Price-Material> [DC Username]");
            return true;
        }

        int priceAmount;
        try {
            priceAmount = Integer.parseInt(args[1]);
            if (priceAmount <= 0) {
                player.sendMessage("§cAmount must be positive!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c'" + args[1] + "' is not a valid number!");
            return true;
        }

        Material priceMaterial = Material.matchMaterial(args[2].toUpperCase());
        if (priceMaterial == null || !priceMaterial.isItem()) {
            player.sendMessage("§cInvalid material: " + args[2]);
            return true;
        }

        ItemStack soldItem = player.getInventory().getItemInMainHand();
        if (soldItem.getType() == Material.AIR) {
            player.sendMessage("§cYou must hold an item in your hand!");
            return true;
        }

        String priceString = priceAmount + "x " + priceMaterial.name().toUpperCase().replace("_", " ");

        String discordName = (args.length >= 4) ? args[3] : player.getName();

        getDiscordBotManager().sendListingEmbed(soldItem, player, priceString, "@" + discordName);

        player.sendMessage("§aListing created for §f" + priceString + "§a!");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create");
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 2) {
                return List.of("1", "16", "64");
            }

            if (args.length == 3) {
                String input = args[2].toLowerCase();
                return Arrays.stream(Material.values())
                        .filter(Material::isItem)
                        .map(m -> m.name().toLowerCase())
                        .filter(name -> name.startsWith(input))
                        .limit(20)
                        .collect(Collectors.toList());
            }

            if (args.length == 4) {
                return List.of("<DC-Username>");
            }
        }

        return List.of();
    }
}
package de.pesacraft.cannonfight.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arena;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.GameManager;
import de.pesacraft.cannonfight.lobby.shops.MainShop;

public class ShopCommand {

	@SuppressWarnings("deprecation")
	public static boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				// only players can have coins
				sender.sendMessage(Language.get("command.shop-only-players")); 
				return true;
			}
			if (!sender.hasPermission("cannonfight.command.shop") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			MainShop.openShopPage(c);
		}
		else if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.shop.other") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
		
			CannonFighter c = CannonFighter.get(Bukkit.getPlayer(args[0]));
			MainShop.openShopPage(c);
			
			sender.sendMessage(Language.get("info.shop-open-other").replaceAll("%player%", c.getName()));
		}
		else {
			sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/cannonfight shop [player]"));
		}
		return true;
	}
}

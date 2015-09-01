package de.pesacraft.cannonfight.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arena;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.GameManager;
import de.pesacraft.cannonfight.lobby.shops.MainShop;

public class ShopCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				// only players can have coins
				sender.sendMessage(Language.get("command.shop-only-players")); 
				return true;
			}
			if (!sender.hasPermission("cannonfight.command.shop")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			MainShop.openShopPage(c);
			return true;
		}
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.shop.other")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
		
			Player p = Bukkit.getPlayer(args[0]);
			
			if (p == null) {
				sender.sendMessage(Language.get("error.player-not-online").replaceAll("%player%", args[0]));
				return true;
			}
			
			CannonFighter c = CannonFighter.get(p);
			
			
			MainShop.openShopPage(c);
			
			sender.sendMessage(Language.get("info.shop-open-other").replaceAll("%player%", c.getName()));
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [player]"));
		return true;
	}
}

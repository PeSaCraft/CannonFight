package de.pesacraft.cannonfight.util.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.shop.implemented.MainShop;
import de.pesacraft.cannonfight.util.CannonFighter;

public class ShopCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				// only players can have coins
				sender.sendMessage(Language.get("command.shop-only-players", true)); 
				return true;
			}
			if (!sender.hasPermission("cannonfight.command.shop")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			MainShop.openShopPage(c);
			return true;
		}
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.shop.other")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
		
			Player p = Bukkit.getPlayer(args[0]);
			
			if (p == null) {
				sender.sendMessage(Language.getStringMaker("error.player-not-online", true).replace("%player%", args[0]).getString());
				return true;
			}
			
			CannonFighter c = CannonFighter.get(p);
			
			
			MainShop.openShopPage(c);
			
			sender.sendMessage(Language.getStringMaker("info.shop-open-other", true).replace("%player%", c.getName()).getString());
			return true;
		}
		
		sender.sendMessage(Language.getStringMaker("error.wrong-usage", true).replace("%command%", "/" + label + " [player]").getString());
		return true;
	}
}

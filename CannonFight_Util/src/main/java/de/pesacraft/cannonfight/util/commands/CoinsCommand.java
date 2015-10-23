package de.pesacraft.cannonfight.util.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.CannonFighter;

public class CoinsCommand implements CommandExecutor {
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				// only players can have coins
				sender.sendMessage(Language.get("command.coins-only-players"));
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			
			if (!sender.hasPermission("cannonfight.command.coins")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			sender.sendMessage(Language.getStringMaker("info.coins-own", true).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
			return true;
		}
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.coins.other")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
		
			Player p = Bukkit.getPlayer(args[0]);
			
			if (p == null) {
				sender.sendMessage(Language.getStringMaker("error.player-not-online", true).replace("%player%", args[0]).getString());
				return true;
			}
			
			CannonFighter c = CannonFighter.get(p);
			
			
			sender.sendMessage(Language.getStringMaker("info.coins.other", true).replace("%player%", c.getName()).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
			return true;
		}
		
		if (args.length == 3) {
			OfflinePlayer p;
			CannonFighter c;
			int amount;
			
			switch (args[1].toLowerCase()) {
			case "set":
				if (!sender.hasPermission("cannonfight.command.coins.set")) {
					sender.sendMessage(Language.get("error.no-permission"));
					return true;
				}
			
				p = Bukkit.getOfflinePlayer(args[0]);
				
				if (p == null) {
					sender.sendMessage(Language.getStringMaker("error.player-not-known", true).replace("%player%", args[0]).getString());
					return true;
				}
				
				c = CannonFighter.get(p);
				
				amount = Integer.parseInt(args[2]);
				
				if (c.setCoins(amount)) {
					sender.sendMessage(Language.getStringMaker("info.coins.set", true).replace("%player%", c.getName()).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
					
					if (p.isOnline())
						c.sendMessage(Language.getStringMaker("info.coins.set.by-other", true).replace("%player%", sender.getName()).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
				}
				else {
					sender.sendMessage(Language.getStringMaker("info.coins.set.failed", true).replace("%player%", c.getName()).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
				}
				return true;
			case "give":
				if (!sender.hasPermission("cannonfight.command.coins.give")) {
					sender.sendMessage(Language.get("error.no-permission"));
					return true;
				}
			
				p = Bukkit.getOfflinePlayer(args[0]);
				
				if (p == null) {
					sender.sendMessage(Language.getStringMaker("error.player-not-known", true).replace("%player%", args[0]).getString());
					return true;
				}
				
				c = CannonFighter.get(p);
				
				amount = Integer.parseInt(args[2]);
				
				if (c.giveCoins(amount, Language.getStringMaker("info.coins.log.give", false).replace("%player%", sender.getName()).getString())) {
					sender.sendMessage(Language.getStringMaker("info.coins.give", true).replace("%player%", c.getName()).replace("%coins%", Language.formatCoins(amount)).getString());
					
					if (p.isOnline())
						c.sendMessage(Language.getStringMaker("info.coins.give.by-other", true).replace("%player%", sender.getName()).replace("%coins%", Language.formatCoins(amount)).getString());
				}
				else {
					sender.sendMessage(Language.getStringMaker("info.coins.give.failed", true).replace("%player%", c.getName()).getString());
				}
				return true;
			case "take":
				if (!sender.hasPermission("cannonfight.command.coins.take")) {
					sender.sendMessage(Language.get("error.no-permission"));
					return true;
				}
			
				p = Bukkit.getOfflinePlayer(args[0]);
				
				if (p == null) {
					sender.sendMessage(Language.getStringMaker("error.player-not-known", true).replace("%player%", args[0]).getString());
					return true;
				}
				
				c = CannonFighter.get(p);
				
				amount = Integer.parseInt(args[2]);
				
				if (c.takeCoins(amount, sender.toString() + Language.get("info.coins.log.take"))) {
					sender.sendMessage(Language.getStringMaker("info.coins.take", true).replace("%player%", c.getName()).replace("%coins%", Language.formatCoins(amount)).getString());
					
					if (p.isOnline())
						c.sendMessage(Language.getStringMaker("info.coins.take.by-other", true).replace("%player%", sender.getName()).replace("%coins%", Language.formatCoins(amount)).getString());
				}
				else {
					sender.sendMessage(Language.getStringMaker("info.coins.take.failed", true).replace("%player%", c.getName()).getString());
				}
				return true;
			};
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [player [(set|give|take) amount]]"));
		return true;
	}
}

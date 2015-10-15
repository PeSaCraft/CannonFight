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
				sender.sendMessage(Language.get("command.coins-only-players"));  //$NON-NLS-1$
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			
			if (!sender.hasPermission("cannonfight.command.coins")) { //$NON-NLS-1$
				sender.sendMessage(Language.get("error.no-permission")); //$NON-NLS-1$
				return true;
			}
			
			sender.sendMessage(Language.get("info.coins-own").replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return true;
		}
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.coins.other")) { //$NON-NLS-1$
				sender.sendMessage(Language.get("error.no-permission")); //$NON-NLS-1$
				return true;
			}
		
			Player p = Bukkit.getPlayer(args[0]);
			
			if (p == null) {
				sender.sendMessage(Language.get("error.player-not-online").replaceAll("%player%", args[0])); //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
			
			CannonFighter c = CannonFighter.get(p);
			
			
			sender.sendMessage(Language.get("info.coins-other").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return true;
		}
		
		if (args.length == 3) {
			OfflinePlayer p;
			CannonFighter c;
			int amount;
			
			switch (args[1].toLowerCase()) {
			case "set": //$NON-NLS-1$
				if (!sender.hasPermission("cannonfight.command.coins.set")) { //$NON-NLS-1$
					sender.sendMessage(Language.get("error.no-permission")); //$NON-NLS-1$
					return true;
				}
			
				p = Bukkit.getOfflinePlayer(args[0]);
				
				if (p == null) {
					sender.sendMessage(Language.get("error.player-not-known").replaceAll("%player%", args[0])); //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
				
				c = CannonFighter.get(p);
				
				amount = Integer.parseInt(args[2]);
				
				if (c.setCoins(amount)) {
					sender.sendMessage(Language.get("info.coins-set").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					
					if (p.isOnline())
						c.sendMessage(Language.get("info.coins-set-by-other").replaceAll("%player%", sender.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				else {
					sender.sendMessage(Language.get("info.coins-set-failed").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				return true;
			case "give": //$NON-NLS-1$
				if (!sender.hasPermission("cannonfight.command.coins.give")) { //$NON-NLS-1$
					sender.sendMessage(Language.get("error.no-permission")); //$NON-NLS-1$
					return true;
				}
			
				p = Bukkit.getOfflinePlayer(args[0]);
				
				if (p == null) {
					sender.sendMessage(Language.get("error.player-not-known").replaceAll("%player%", args[0])); //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
				
				c = CannonFighter.get(p);
				
				amount = Integer.parseInt(args[2]);
				
				if (c.giveCoins(amount, sender.toString() + Language.get("info.coins.log.give"))) { //$NON-NLS-1$
					sender.sendMessage(Language.get("info.coins-give").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					
					if (p.isOnline())
						c.sendMessage(Language.get("info.coins-give-by-other").replaceAll("%player%", sender.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				else {
					sender.sendMessage(Language.get("info.coins-give-failed").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				return true;
			case "take": //$NON-NLS-1$
				if (!sender.hasPermission("cannonfight.command.coins.take")) { //$NON-NLS-1$
					sender.sendMessage(Language.get("error.no-permission")); //$NON-NLS-1$
					return true;
				}
			
				p = Bukkit.getOfflinePlayer(args[0]);
				
				if (p == null) {
					sender.sendMessage(Language.get("error.player-not-known").replaceAll("%player%", args[0])); //$NON-NLS-1$ //$NON-NLS-2$
					return true;
				}
				
				c = CannonFighter.get(p);
				
				amount = Integer.parseInt(args[2]);
				
				if (c.takeCoins(amount, sender.toString() + Language.get("info.coins.log.take"))) { //$NON-NLS-1$
					sender.sendMessage(Language.get("info.coins-take").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					
					if (p.isOnline())
						c.sendMessage(Language.get("info.coins-take-by-other").replaceAll("%player%", sender.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				else {
					sender.sendMessage(Language.get("info.coins-take-failed").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				return true;
			};
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [player [(set|give|take) amount]]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return true;
	}
}

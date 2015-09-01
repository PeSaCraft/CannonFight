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

public class CoinsCommand implements CommandExecutor {
	
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
			
			sender.sendMessage(Language.get("info.coins-own").replaceAll("%coins%", c.getCoins() + ""));
			return true;
		}
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.coins.other")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
		
			CannonFighter c = CannonFighter.get(Bukkit.getPlayer(args[0]));
			
			if (c == null) {
				sender.sendMessage(Language.get("error.player-not-online").replaceAll("%player%", c.getName()));
				return true;
			}
			
			sender.sendMessage(Language.get("info.coins-other").replaceAll("%player%", c.getName()).replaceAll("%coins%", c.getCoins() + ""));
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [player]"));
		return true;
	}
}

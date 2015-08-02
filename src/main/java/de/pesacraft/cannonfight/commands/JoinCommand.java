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

public class JoinCommand {

	@SuppressWarnings("deprecation")
	public static boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			// only players can join
			sender.sendMessage(Language.get("command.join-only-players")); 
			return true;
		}
		
		CannonFighter c = CannonFighter.get((Player) sender);
		Arena a;
		
		if (args.length == 0) {
			if (!sender.hasPermission("cannonfight.command.join.random") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			a = Arenas.random();
			sender.sendMessage(Language.get("command.join-successful-random")); 
		}
		else if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.join") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
			return true;
			}
			a = Arenas.getArena(args[0]);
			sender.sendMessage(Language.get("command.join-successful")); 
		}
		else {
			if (!sender.hasPermission("cannonfight.command.join.others") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			a = Arenas.getArena(args[0]);
			c = CannonFighter.get(Bukkit.getPlayer(args[1]));
			sender.sendMessage(Language.get("command.join-successful-other")); 
		}

		GameManager.addPlayer(a, c);
		
		return true;
	}

}

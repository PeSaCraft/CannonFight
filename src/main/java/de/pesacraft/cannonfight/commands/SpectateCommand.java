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

public class SpectateCommand {

	@SuppressWarnings("deprecation")
	public static boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			// only players can join
			sender.sendMessage(Language.get("command.spectate-only-players")); 
			return true;
		}
		
		CannonFighter c = CannonFighter.get((Player) sender);
		Arena a;
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.spectate") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			a = Arenas.getArena(args[0]);
		
			if (GameManager.getForArena(a).addSpectator(c))
				sender.sendMessage(Language.get("command.spectate-successful"));
			else
				sender.sendMessage(Language.get("command.spectate-failed"));
				
		}
		
		return true;
	}

}

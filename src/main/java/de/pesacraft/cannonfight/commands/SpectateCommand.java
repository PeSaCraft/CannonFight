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
			
			if (c.isInGame() || c.isInQueue()) {
				c.sendMessage(Language.get("error.has-to-leave-before-join"));
				return true;
			}
			
			a = Arenas.getArena(args[0]);
		
			spectate(c, a);
				
		}
		else if (args.length == 2) {
			if (!sender.hasPermission("cannonfight.command.spectate.others") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			c = CannonFighter.get(Bukkit.getPlayer(args[1]));
			
			if (c.isInGame() || c.isInQueue()) {
				c.sendMessage(Language.get("error.has-to-leave-before-join-other"));
				return true;
			}
			
			a = Arenas.getArena(args[0]);
			
			if (spectate(c, a))
				sender.sendMessage(Language.get("command.spectate-other-successful"));
			else
				sender.sendMessage(Language.get("command.spectate-other-failed"));
		}
		
		return true;
	}

	private static boolean spectate(CannonFighter c, Arena a) {
		GameManager g = GameManager.getForArena(a);
		
		if (g.isGameRunning()) {
			// Spiel läuft -> hinzufügen
			if (g.addSpectator(c)) {
				// konnte rein
				c.sendMessage(Language.get("command.join-spectate-successful"));
				return true;
			}	
			// konnte nicht rein
		}
		// kein Spiel -> kann nicht zugucken
		c.sendMessage(Language.get("command.join-spectate-failed"));
		return false;
	}
}

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

public class JoinCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {		
		CannonFighter c = CannonFighter.get((Player) sender);
		Arena a;
		
		if (args.length == 0) {
			// zufällige Arena
			
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.join-only-players")); 
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.join.random")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			if (c.isInGame() || c.isInQueue()) {
				c.sendMessage(Language.get("error.has-to-leave-before-join"));
				return true;
			}
			a = Arenas.random();
			
			join(c, a);
			return true;
		}
		
		if (args.length == 1) {
			// angegebene Arena
			
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.join-only-players")); 
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.join")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			if (c.isInGame() || c.isInQueue()) {
				c.sendMessage(Language.get("error.has-to-leave-before-join"));
				return true;
			}
			
			a = Arenas.getArena(args[0]);
			
			join(c, a);
			return true;
		}
		
		if (args.length == 2) {
			// anderen in eine arena
			if (!sender.hasPermission("cannonfight.command.join.other")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			c = CannonFighter.get(Bukkit.getPlayer(args[1]));
			
			if (c.isInGame() || c.isInQueue()) {
				c.sendMessage(Language.get("error.has-to-leave-before-join-other"));
				return true;
			}
			
			if (join(c, Arenas.getArena(args[0])))
				sender.sendMessage(Language.get("command.join-successful-other")); 
			else
				sender.sendMessage(Language.get("command.join-failed-other")); 
			
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}

	
	private static boolean join(CannonFighter c, Arena a) {
		GameManager g = GameManager.getForArena(a);
		
		if (!g.isGameRunning()) {
			// kein Spiel läuft -> zur queue
			if (g.addToQueue(c)) {
				// kann in queue
				c.sendMessage(Language.get("command.join-queue-succesful"));
				return true;
			}
			
			// kann nicht in queue
			c.sendMessage(Language.get("command.join-queue-failed"));
			return false;
		}
		
		// Spiel läuft -> hinzufügen
		if (g.addPlayer(c)) {
			// konnte rein
			c.sendMessage(Language.get("command.join-successful"));
			return true;
		}
		
		// konnte nicht rein
		c.sendMessage(Language.get("command.join-failed"));
		return false;
	}
}

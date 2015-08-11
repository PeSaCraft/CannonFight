package de.pesacraft.cannonfight.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.GameManager;

public class LeaveCommand {

	public static boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			// only players can join
			sender.sendMessage(Language.get("command.leave-only-players")); 
			return true;
		}
		
		if (args.length == 0) {
			// dieser Spieler
			CannonFighter c = CannonFighter.get((Player) sender);
			
			if (c.isInGame()) {
				// leave game
				leaveGame(c);
			}
			
			if (c.isInQueue()) {
				// leave queue
				leaveQueue(c);
			}
			
		}
		else if (args.length == 1) {
			// ein anderer spieler
			CannonFighter c = CannonFighter.get(Bukkit.getPlayer(args[0]));
			
			if (!sender.hasPermission("cannonfight.command.leave.others") && !sender.hasPermission("cannonfight.command.*")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			if (c.isInGame()) {
				// leave game
				if (leaveGame(c)) {
					sender.sendMessage(Language.get("command.leave-game-other-successful"));	
				}
				else {
					sender.sendMessage(Language.get("command.leave-game-other-failed"));
				}
			}		
			
			if (c.isInQueue()) {
				// leave queue
				if (leaveQueue(c)) {
					sender.sendMessage(Language.get("command.leave-queue-other-successful"));	
				}
				else {
					sender.sendMessage(Language.get("command.leave-queue-other-failed"));
				}
			}
		}
		return true;
	}
	
	private static boolean leaveGame(CannonFighter c) {
		if (!c.hasPermission("cannonfight.command.leave.game") && !c.hasPermission("cannonfight.command.*")) {
			c.sendMessage(Language.get("error.no-permission"));
			return false;
		}
		
		if (c.getCurrentGame().leave(c)) {
			// spiel verlassen
			c.sendMessage(Language.get("command.leave-game-successful"));
			return true;
		}
		c.sendMessage(Language.get("command.leave-game-failed"));	
		return false;
	}

	private static boolean leaveQueue(CannonFighter c) {
		if (!c.hasPermission("cannonfight.command.leave.queue") && !c.hasPermission("cannonfight.command.*")) {
			c.sendMessage(Language.get("error.no-permission"));
			return false;
		}
		
		if (GameManager.getForArena(c.getArenaQueuing()).leaveQueue(c)) {
			// queue verlassen
			c.sendMessage(Language.get("command.leave-queue-successful"));
			return true;
		}
		// queue nicht verlassen
		c.sendMessage(Language.get("command.leave-queue-failed"));	
		return false;
	}
		
}

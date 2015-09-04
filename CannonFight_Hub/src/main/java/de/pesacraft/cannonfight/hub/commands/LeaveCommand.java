package de.pesacraft.cannonfight.hub.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.game.Game;
//import de.pesacraft.cannonfight.util.game.GameManager;

public class LeaveCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			// dieser Spieler
			
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.leave-only-players")); 
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.leave")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			
			leaveAll(c);
			return true;
		}
		else if (args.length == 1) {
			// ein anderer spieler
			if (!sender.hasPermission("cannonfight.command.leave.other")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			Player p = Bukkit.getPlayer(args[0]);
			
			if (p == null) {
				sender.sendMessage(Language.get("error.player-not-online").replaceAll("%player%", args[0]));
				return true;
			}
			
			CannonFighter c = CannonFighter.get(p);
			
			
			if (c.isInGame()) {
				// leave game
				if (leaveGame(c))
					sender.sendMessage(Language.get("command.leave-game-other-successful"));	
				else
					sender.sendMessage(Language.get("command.leave-game-other-failed"));
			}		
			
			if (c.isInQueue()) {
				// leave queue
				if (leaveQueue(c))
					sender.sendMessage(Language.get("command.leave-queue-other-successful"));
				else
					sender.sendMessage(Language.get("command.leave-queue-other-failed"));
			}
			
			
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}
	
	public static void leaveAll(CannonFighter c) {
		if (c.isInGame())
			// leave game
			leaveGame(c);
		
		if (c.isInQueue())
			// leave queue
			leaveQueue(c);
	}

	private static boolean leaveGame(CannonFighter c) {
		Game g = c.getCurrentGame();
		if (g.leave(c)) {
			// spiel verlassen
			g.removePlayer(c);
			g.removeSpectator(c);
			c.sendMessage(Language.get("command.leave-game-successful"));
			return true;
		}
		c.sendMessage(Language.get("command.leave-game-failed"));	
		return false;
	}

	private static boolean leaveQueue(CannonFighter c) {
		/*GameManager man = GameManager.getForArena(c.getArenaQueuing());
		
		man.updateSigns();
		
		if (man.leaveQueue(c)) {
			// queue verlassen
			c.sendMessage(Language.get("command.leave-queue-successful"));
			return true;
		}
		// queue nicht verlassen
		*/c.sendMessage(Language.get("command.leave-queue-failed"));	
		return false;
	}
		
}

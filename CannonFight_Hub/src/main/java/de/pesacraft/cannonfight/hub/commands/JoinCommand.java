package de.pesacraft.cannonfight.hub.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.hub.communication.CommunicationHubClient;
import de.pesacraft.cannonfight.util.Language;
/*
import de.pesacraft.cannonfight.util.game.Arenas;
import de.pesacraft.cannonfight.util.game.GameManager;
*/
public class JoinCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {		
		if (args.length == 1) {
			// angegebene Arena
			
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.join-only-players", true)); 
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.join")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			CommunicationHubClient.getInstance().sendPlayer(args[0], ((Player) sender).getName());
			return true;
		}
		
		if (args.length == 2) {
			// anderen in eine arena
			if (!sender.hasPermission("cannonfight.command.join.other")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			Player p = Bukkit.getPlayer(args[1]);
			
			if (p == null) {
				sender.sendMessage(Language.get("error.player-not-online", true).replaceAll("%player%", args[1]));
				return true;
			}
			
			CommunicationHubClient.getInstance().sendPlayer(args[0], p.getName());
			
			sender.sendMessage(Language.get("command.join-tried-other", true)); 
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage", true).replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}

	
//	public static boolean join(CannonFighter c, Arena a) {
//		/*GameManager g = GameManager.getForArena(a);
//		
//		if (g.isGameRunning()) {
//			// Spiel läuft -> hinzufügen
//			if (g.addPlayer(c)) {
//				// konnte rein
//				c.sendMessage(Language.get("command.join-successful").replaceAll("%game%", a.getName()));
//				return true;
//			}
//		}
//		
//		// kein Spiel läuft oder konnte nicht joinen -> zur queue
//		if (g.addToQueue(c)) {
//			// kann in queue
//			c.sendMessage(Language.get("command.join-queue-succesful").replaceAll("%arena%", a.getName()));
//			return true;
//		}
//		*/
//		// kann nicht in queue
//		c.sendMessage(Language.get("command.join-queue-failed"));
//		return false;
//	}
}

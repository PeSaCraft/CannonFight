package de.pesacraft.cannonfight.hub.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;

public class SpectateCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.spectate-only-players")); 
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			//Arena a;
			
			if (!sender.hasPermission("cannonfight.command.spectate")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			//a = Arenas.getArena(args[0]);
		
			//spectate(c, a);
			
			return true;	
		}
		else if (args.length == 2) {
			if (!sender.hasPermission("cannonfight.command.spectate.other")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			Player p = Bukkit.getPlayer(args[1]);
			
			if (p == null) {
				sender.sendMessage(Language.get("error.player-not-online").replaceAll("%player%", args[1]));
				return true;
			}
			
			CannonFighter c = CannonFighter.get(p);
			//Arena a;
			
			//a = Arenas.getArena(args[0]);
			
			/*if (spectate(c, a))
				sender.sendMessage(Language.get("command.spectate-other-successful"));
			else
				sender.sendMessage(Language.get("command.spectate-other-failed"));
			*/
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}

//	private static boolean spectate(CannonFighter c, Arena a) {
//		/*GameManager g = GameManager.getForArena(a);
//		
//		if (g.isGameRunning()) {
//			// Spiel läuft -> hinzufügen
//			if (g.addSpectator(c)) {
//				// konnte rein
//				c.sendMessage(Language.get("command.join-spectate-successful"));
//				return true;
//			}	
//			// konnte nicht rein
//		}
//		*/// kein Spiel -> kann nicht zugucken
//		c.sendMessage(Language.get("command.join-spectate-failed"));
//		return false;
//	}
}

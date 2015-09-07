package de.pesacraft.cannonfight.hub.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;

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
			
			
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}	
}

package de.pesacraft.cannonfight.game.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import de.pesacraft.cannonfight.util.Language;

public class LeaveCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			// dieser Spieler
			
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.leave-only-players", true)); 
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.leave")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			// fake player leave, triggers teleport back
			Bukkit.getPluginManager().callEvent(new PlayerQuitEvent((Player) sender, null));
			
			return true;
		}
		else if (args.length == 1) {
			// ein anderer spieler
			if (!sender.hasPermission("cannonfight.command.leave.other")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			Player p = Bukkit.getPlayer(args[0]);
			
			if (p == null) {
				sender.sendMessage(Language.get("error.player-not-online", true).replaceAll("%player%", args[0]));
				return true;
			}
			
			// fake player leave, triggers teleport back
			Bukkit.getPluginManager().callEvent(new PlayerQuitEvent(p, null));
			
			sender.sendMessage(Language.get("command.leave-game-other-successful", true));	
			
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage", true).replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}
		
}

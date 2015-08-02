package de.pesacraft.cannonfight.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;

public class LeaveCommand {

	public static boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			// only players can join
			sender.sendMessage(Language.get("command.leave-only-players")); 
			return true;
		}
		
		CannonFighter c = CannonFighter.get((Player) sender);
		
		c.leave();
		sender.sendMessage(Language.get("command.leave-successful")); 
		return true;
	}

}

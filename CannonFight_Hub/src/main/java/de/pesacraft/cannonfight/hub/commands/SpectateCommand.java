package de.pesacraft.cannonfight.hub.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.hub.communication.CommunicationHubClient;
import de.pesacraft.cannonfight.util.Language;

public class SpectateCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (!(sender instanceof Player)) {
				// only players can join
				sender.sendMessage(Language.get("command.spectate-only-players")); 
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.spectate")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			CommunicationHubClient.getInstance().sendSpectator(sender.getName(), args[0]);
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
			
			CommunicationHubClient.getInstance().sendSpectator(p.getName(), args[0]);
			
			sender.sendMessage(Language.get("command.spectate-tried-other-successful"));
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [arena] [player]"));
		return true;
	}
}

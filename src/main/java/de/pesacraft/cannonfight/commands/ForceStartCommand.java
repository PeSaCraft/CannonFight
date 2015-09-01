package de.pesacraft.cannonfight.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.GameManager;

public class ForceStartCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				// only players can force without given command
				sender.sendMessage(Language.get("command.force-only-player-without-arg")); // ChatColor.RED + "Only players can force start without a given arena!"
				return true;
			}
			
			if (!sender.hasPermission("cannonfight.command.force")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			CannonFighter c = CannonFighter.get((Player) sender);
			
			if (!c.isInQueue()) {
				// wartet nicht auf spielstart
				sender.sendMessage(Language.get("command.force-not-in-queue"));
				return true;
			}
			
			// spieler in queue
			if (GameManager.getForArena(c.getArenaQueuing()).startGame(true)) {
				// Spiel gestartet
				sender.sendMessage(Language.get("command.force-own-successful"));
				return true;
			}
			
			// spiel konnte nicht gestartet werden
			sender.sendMessage(Language.get("command.force-own-failed"));
			return true;
		}
		
		if (args.length == 1) {
			if (!sender.hasPermission("cannonfight.command.force.specific")) {
				sender.sendMessage(Language.get("error.no-permission"));
				return true;
			}
			
			if (GameManager.getForArena(Arenas.getArena(args[0])).startGame(true)) {
				// Spiel gestartet
				sender.sendMessage(Language.get("command.force-specific-successful"));
				return true;
			}
			// spiel konnte nicht gestartet werden
			sender.sendMessage(Language.get("command.force-specific-failed"));
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label + " [arena]"));
		return true;
	}

}

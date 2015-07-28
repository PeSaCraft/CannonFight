package de.pesacraft.cannonfight.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arena;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.GameManager;

public class ForceStartCommand {

	public static boolean execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				// only players can
				sender.sendMessage(ChatColor.RED + "Only players can force start without a given arena!");
				return true;
			}
			CannonFighter c = CannonFighter.get((Player) sender);
			c.getCurrentGame().start();
		}
		else if (args.length == 1) {
			GameManager.getForArena(Arenas.getArena(args[0])).start();
		}
		else 
			return false;
		
		return true;
	}

}

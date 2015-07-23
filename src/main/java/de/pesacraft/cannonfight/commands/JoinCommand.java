package de.pesacraft.cannonfight.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arena;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.GameManager;
import de.pesacraft.cannonfight.util.Database;
import de.pesacraft.cannonfight.util.money.DatabaseMoney;

public class JoinCommand {

	public static boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			// only players can join
			sender.sendMessage(ChatColor.RED + "Only players can join!");
			return true;
		}
		
		CannonFighter c = CannonFighter.get((Player) sender);
		Arena a;
		
		if (args.length == 0) {
			a = Arenas.random();
		}
		else if (args.length == 1) {
			a = Arenas.getArena(args[0]);
		}
		else {
			a = Arenas.getArena(args[0]);
			c = CannonFighter.get(Bukkit.getPlayer(args[1]));
		}

		GameManager.addPlayer(a, c);
		
		return false;
	}

}

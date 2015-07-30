package de.pesacraft.cannonfight.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.game.Setup;

public class SetupCommand {
	private static Map<Player, Setup> activeSetups = new HashMap<Player, Setup>();
	
	public static boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can create arenas!");
			return true;
		}
		
		Player p = (Player) sender;
		Setup s = activeSetups.get(p);
		
		if (s == null) {
			s = new Setup();
			activeSetups.put(p, s);
		}
		
		if (args.length == 0) {
			// setup hilfe
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup pos1 " + ChatColor.BLUE + "Erste Ecke der Arena an aktueller Position festlegen");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup pos2 " + ChatColor.BLUE + "Zweite Ecke der Arena an aktueller Position festlegen");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup name <name> " + ChatColor.BLUE + "Name der Arena festlegen");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup reqPlayers <amount> " + ChatColor.BLUE + "Anzahl der zum Start des Spiels benötigten Spieler festlegen.");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup specPos " + ChatColor.BLUE + "Zuschauer Spawn an aktueller Position festlegen");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup spawn " + ChatColor.BLUE + "Fügt an aktueller Position einen Spawn hinzu, nur möglich wenn vorherige Schritte gemacht wurden.");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup done " + ChatColor.BLUE + "Schließt die Erstellung dieser Arena ab.");
		}
		else if (args.length == 1) {
			// pos1 pos2 specPos spawn
			if (args[0].equalsIgnoreCase("pos1")) {
				s.setLocation1(p.getLocation());
			}
			else if (args[0].equalsIgnoreCase("pos2")) {
				s.setLocation2(p.getLocation());
			}
			else if (args[0].equalsIgnoreCase("specPos")) {
				s.setSpectatorLocation(p.getLocation());
			}
			else if (args[0].equalsIgnoreCase("spawn")) {
				s.addSpawn(p.getLocation());
			}
			else if (args[0].equalsIgnoreCase("done")) {
				activeSetups.remove(p);
				return true;
			}
		}
		else if (args.length == 2) {
			// name reqPlayers
			if (args[0].equalsIgnoreCase("name")) {
				s.setName(args[1]);
			}
			else if (args[0].equalsIgnoreCase("reqPlayers")) {
				s.setRequiredPlayers(Integer.parseInt(args[1]));
			}
		}
		return false;
	}

}

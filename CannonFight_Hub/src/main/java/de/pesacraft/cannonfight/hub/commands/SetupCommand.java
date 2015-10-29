package de.pesacraft.cannonfight.hub.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.hub.CannonFightHub;
import de.pesacraft.cannonfight.hub.game.Setup;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.cannons.Cannons;

public class SetupCommand implements CommandExecutor {
	private static Map<Player, Setup> activeSetups = new HashMap<Player, Setup>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Language.get("command.setup-only-players", true)); 
			return true;
		}
		
		if (!sender.hasPermission("cannonfight.command.setup")) {
			sender.sendMessage(Language.get("error.no-permission", true));
			return true;
		}
		
		Player p = (Player) sender;
		Setup s = activeSetups.get(p);
		
		if (args.length == 0) {
			// setup hilfe
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup name <name> " + ChatColor.BLUE + "Name der Arena festlegen");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup reqPlayers <amount> " + ChatColor.BLUE + "Anzahl der zum Start des Spiels benötigten Spieler festlegen.");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup specPos " + ChatColor.BLUE + "Zuschauer Spawn an aktueller Position festlegen");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup spawn " + ChatColor.BLUE + "Fügt an aktueller Position einen Spawn hinzu, nur möglich wenn vorherige Schritte gemacht wurden.");
			p.sendMessage(ChatColor.AQUA + "[CannonFight] " + ChatColor.GOLD + "/cannonfight setup done " + ChatColor.BLUE + "Schließt die Erstellung dieser Arena ab.");
			
			return true;
		}
		if (args.length == 1) {
			// specPos spawn lobby cannons
			if (args[0].equalsIgnoreCase("specPos")) {
				if (s == null) {
					sender.sendMessage(Language.get("command.setup.not-loaded", true)); 
					return true;
				}
				s.setSpectatorLocation(p.getLocation());
				sender.sendMessage(Language.get("command.setup.set.specPos", true)); 
				return true;
			}
			else if (args[0].equalsIgnoreCase("spawn")) {
				if (s == null) {
					sender.sendMessage(Language.get("command.setup.not-loaded", true)); 
					return true;
				}
				s.addSpawn(p.getLocation());
				sender.sendMessage(Language.get("command.setup.set.spawn", true)); 
				return true;
			}
			else if (args[0].equalsIgnoreCase("lobby")) {
				CannonFightHub.setLobbyLocation(((Player) sender).getLocation());
				sender.sendMessage(Language.get("command.setup.set.lobby", true));
				return true;
			}
			else if (args[0].equalsIgnoreCase("cannons")) {
				Cannons.getUpgradeSetupShop().openInventory(CannonFighter.get((OfflinePlayer) p));
				return true;
			}
		}
		
		if (args.length == 2) {
			// name reqPlayers
			if (args[0].equalsIgnoreCase("reqPlayers")) {
				s.setRequiredPlayers(Integer.parseInt(args[1]));
				sender.sendMessage(Language.get("command.setup.set.reqPlayers", true)); 
			}
			else if (args[0].equalsIgnoreCase("load")) {
				activeSetups.put(p, Setup.get(args[1]));
				sender.sendMessage(Language.get("command.setup.loaded", true)); 
			}
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage", true).replaceAll("%command%", "/" + label));
		return true;
	}
}

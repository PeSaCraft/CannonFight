package de.pesacraft.cannonfight.hub.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.hub.CannonFightHub;
import de.pesacraft.cannonfight.hub.game.Setup;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;

public class SetupCommand implements CommandExecutor {
	private static Map<Player, Setup> activeSetups = new HashMap<Player, Setup>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Language.get("command.setup-only-players")); 
			return true;
		}
		
		if (!sender.hasPermission("cannonfight.command.setup")) {
			sender.sendMessage(Language.get("error.no-permission"));
			return true;
		}
		
		
		Player p = (Player) sender;
		Setup s = activeSetups.get(p);
		
		if (s == null) {
			s = new Setup();
			activeSetups.put(p, s);
			sender.sendMessage(Language.get("command.setup-new-setup")); 
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
			
			return true;
		}
		if (args.length == 1) {
			// pos1 pos2 specPos spawn done lobby
			if (args[0].equalsIgnoreCase("pos1")) {
				s.setLocation1(p.getLocation());
				sender.sendMessage(Language.get("command.setup-set-pos1"));
				return true;
			}
			else if (args[0].equalsIgnoreCase("pos2")) {
				s.setLocation2(p.getLocation());
				sender.sendMessage(Language.get("command.setup-set-pos2"));
				return true;
			}
			else if (args[0].equalsIgnoreCase("specPos")) {
				s.setSpectatorLocation(p.getLocation());
				sender.sendMessage(Language.get("command.setup-set-specPos")); 
				return true;
			}
			else if (args[0].equalsIgnoreCase("spawn")) {
				s.addSpawn(p.getLocation());
				sender.sendMessage(Language.get("command.setup-set-spawn")); 
				return true;
			}
			else if (args[0].equalsIgnoreCase("done")) {
				//Arenas.put(s.getName(), new Arena(s.getName()));
				activeSetups.remove(p);
				sender.sendMessage(Language.get("command.setup-done"));
				return true;
			}
			else if (args[0].equalsIgnoreCase("lobby")) {
				CannonFightHub.setLobbyLocation(((Player) sender).getLocation());
				sender.sendMessage(Language.get("command.setup-set-lobby"));
				return true;
			}
			else if (args[0].equalsIgnoreCase("cannons")) {
				Cannons.getSetupShop().openInventory(CannonFighter.get((OfflinePlayer) p));
				return true;
			}
		}
		
		if (args.length == 2) {
			// name reqPlayers
			if (args[0].equalsIgnoreCase("name")) {
				s.setName(args[1]);
				sender.sendMessage(Language.get("command.setup-set-name")); 
			}
			else if (args[0].equalsIgnoreCase("reqPlayers")) {
				s.setRequiredPlayers(Integer.parseInt(args[1]));
				sender.sendMessage(Language.get("command.setup-set-reqPlayers")); 
			}
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage").replaceAll("%command%", "/" + label));
		return true;
	}
}

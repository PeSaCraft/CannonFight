package de.pesacraft.cannonfight;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.pesacraft.cannonfight.commands.CoinsCommand;
import de.pesacraft.cannonfight.commands.ForceStartCommand;
import de.pesacraft.cannonfight.commands.JoinCommand;
import de.pesacraft.cannonfight.commands.LeaveCommand;
import de.pesacraft.cannonfight.commands.SetupCommand;
import de.pesacraft.cannonfight.commands.ShopCommand;
import de.pesacraft.cannonfight.commands.SpectateCommand;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.game.cannons.usable.FireballCannon;
import de.pesacraft.cannonfight.lobby.shops.MainShop;
import de.pesacraft.cannonfight.lobby.signs.JoinSigns;
import de.pesacraft.cannonfight.util.MongoDatabase;
import de.pesacraft.cannonfight.util.money.CraftConomyMoney;
import de.pesacraft.cannonfight.util.money.DatabaseMoney;
import de.pesacraft.cannonfight.util.money.Money;

public class CannonFight extends JavaPlugin implements Listener {
	public static Logger LOGGER;
	public static CannonFight PLUGIN;
	public static Money MONEY;
	
	private static Location lobbyLocation;
	
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		if (!new File(this.getDataFolder() + "config.yml").exists())
			this.saveDefaultConfig();
		
		Language.loadLanguage(this.getConfig().getString("language"));
		
		MongoDatabase.setup();
		Arenas.load();
		
		loadMoney();
		
		lobbyLocation = (Location) this.getConfig().get("lobby");
		Bukkit.getServer().setSpawnRadius(0);
		lobbyLocation.getWorld().setSpawnLocation(lobbyLocation.getBlockX(), lobbyLocation.getBlockY(), lobbyLocation.getBlockZ());
		
		Bukkit.getPluginManager().registerEvents(this, this);
		new JoinSigns();
		
		setupCommands();
		//LobbySystem.registerGame(this, Game.class);
	}
	 
	private void setupCommands() {
		PluginCommand coins = this.getCommand("coins");
		coins.setExecutor(new CoinsCommand());
		
		PluginCommand force = this.getCommand("force");
		force.setExecutor(new ForceStartCommand());
		
		PluginCommand join = this.getCommand("join");
		join.setExecutor(new JoinCommand());
		
		PluginCommand leave = this.getCommand("leave");
		leave.setExecutor(new LeaveCommand());
		
		PluginCommand setup = this.getCommand("setup");
		setup.setExecutor(new SetupCommand());
		
		PluginCommand shop = this.getCommand("shop");
		shop.setExecutor(new ShopCommand());
		
		PluginCommand spectate = this.getCommand("spectate");
		spectate.setExecutor(new SpectateCommand());
		
	}

	private void loadMoney() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Craftconomy3");
		
		if (plugin != null) {
			MONEY = new CraftConomyMoney(plugin);
			LOGGER.info(Language.get("info.using-craftconomy"));
		}
		else {
			MONEY = new DatabaseMoney();
			LOGGER.info(Language.get("info.using-buildinmoney"));
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			String subcommand = args[0];
			String[] subArgs;
			if (args.length == 1)
				// keine weiteren argumente da -> erzeugt fehler
				subArgs = new String[0];
			else
				subArgs = Arrays.copyOfRange(args, 1, args.length);
				
			if (subcommand.equalsIgnoreCase("join"))
				return new JoinCommand().onCommand(sender, cmd, label, subArgs);
			if (subcommand.equalsIgnoreCase("setup"))
				return new SetupCommand().onCommand(sender, cmd, label, subArgs);
			if (subcommand.equalsIgnoreCase("leave"))
				return new LeaveCommand().onCommand(sender, cmd, label, subArgs);
			if (subcommand.equalsIgnoreCase("force") || subcommand.equalsIgnoreCase("start"))
				return new ForceStartCommand().onCommand(sender, cmd, label, subArgs);
			if (subcommand.equalsIgnoreCase("spectate"))
				return new SpectateCommand().onCommand(sender, cmd, label, subArgs);
			if (subcommand.equalsIgnoreCase("shop"))
				return new ShopCommand().onCommand(sender, cmd, label, subArgs);
			if (subcommand.equalsIgnoreCase("coins"))
				return new CoinsCommand().onCommand(sender, cmd, label, subArgs);
		}
		return super.onCommand(sender, cmd, label, args);
	}

	public void onDisable() { 
		MongoDatabase.close();
		
		this.getConfig().set("lobby", lobbyLocation);
		this.saveConfig();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().teleport(lobbyLocation);
		event.setJoinMessage(Language.get("info.join-lobby").replaceAll("%player%", event.getPlayer().getName()));
		
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
	}
	
	public static void setLobbyLocation(Location l) {
		lobbyLocation.setWorld(l.getWorld());
		
		lobbyLocation.setX(l.getBlockX());
		lobbyLocation.setY(l.getBlockY());
		lobbyLocation.setZ(l.getBlockZ());
		
		lobbyLocation.setYaw(l.getYaw());
		lobbyLocation.setPitch(l.getPitch());
	}
}

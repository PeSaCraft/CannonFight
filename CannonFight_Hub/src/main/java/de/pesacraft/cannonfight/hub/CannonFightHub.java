package de.pesacraft.cannonfight.hub;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.pesacraft.cannonfight.hub.commands.JoinCommand;
import de.pesacraft.cannonfight.hub.commands.LeaveCommand;
import de.pesacraft.cannonfight.hub.commands.SetupCommand;
import de.pesacraft.cannonfight.hub.commands.SpectateCommand;
import de.pesacraft.cannonfight.hub.communication.CommunicationHubClient;
import de.pesacraft.cannonfight.hub.lobby.signs.JoinSigns;
import de.pesacraft.cannonfight.hub.lobby.signs.SignHandler;
import de.pesacraft.cannonfight.util.commands.CoinsCommand;
import de.pesacraft.cannonfight.util.commands.ShopCommand;
import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.MongoDatabase;

public class CannonFightHub extends JavaPlugin implements Listener {
	public static Logger LOGGER;
	public static CannonFightHub PLUGIN;
	
	private static Location lobbyLocation;
	
	private static Map<String, Set<Location>> signs;
	
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		if (!new File(this.getDataFolder() + "config.yml").exists())
			this.saveDefaultConfig();
		
		CannonFightUtil.use(this);
		Language.loadLanguage(this, this.getConfig().getString("language"));
		
		LOGGER.info(Language.get("info.using-buildinmoney"));
		
		lobbyLocation = (Location) this.getConfig().get("lobby");
		Bukkit.getServer().setSpawnRadius(0);
		lobbyLocation.getWorld().setSpawnLocation(lobbyLocation.getBlockX(), lobbyLocation.getBlockY(), lobbyLocation.getBlockZ());
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		new SignHandler();
		
		setupCommands();
		
		new CommunicationHubClient().start();
		//LobbySystem.registerGame(this, Game.class);
	}
	 
	private void setupCommands() {
		PluginCommand coins = this.getCommand("coins");
		coins.setExecutor(new CoinsCommand());
		
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
		
		SignHandler.getInstance().save();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().teleport(lobbyLocation);
		event.setJoinMessage(Language.get("info.join-lobby").replaceAll("%player%", event.getPlayer().getName()));
		
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(Language.get("info.leave-lobby").replaceAll("%player%", event.getPlayer().getName()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatMonitor(AsyncPlayerChatEvent event) {
		event.setFormat(Language.get("general.chat-format").replaceAll("%player%", event.getPlayer().getName()).replaceAll("%message%", event.getMessage()));
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		// nobody looses food!
		event.setFoodLevel(20);
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

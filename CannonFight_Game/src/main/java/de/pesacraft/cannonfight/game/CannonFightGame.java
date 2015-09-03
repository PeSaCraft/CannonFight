package de.pesacraft.cannonfight.game;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.game.api.CannonFighterDeathEvent;
import de.pesacraft.cannonfight.game.commands.CoinsCommand;
import de.pesacraft.cannonfight.game.commands.LeaveCommand;
import de.pesacraft.cannonfight.game.commands.ShopCommand;
import de.pesacraft.cannonfight.game.util.MongoDatabase;
import de.pesacraft.cannonfight.game.Language;
import de.pesacraft.cannonfight.game.players.CannonFighter;
import de.pesacraft.cannonfight.game.util.money.DatabaseMoney;
import de.pesacraft.cannonfight.game.util.money.Money;

public class CannonFightGame extends JavaPlugin implements Listener {

	public static Logger LOGGER;
	public static CannonFightGame PLUGIN;
	public static Money MONEY;
	
	private static World WORLD_LOBBY;
	private static World WORLD_GAME;
	
	private static Arena ARENA;
	private static Game GAME;
	
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		if (!new File(this.getDataFolder() + "config.yml").exists())
			this.saveDefaultConfig();
		
		Language.loadLanguage(this.getConfig().getString("language"));
		
		MongoDatabase.setup();
		
		MONEY = new DatabaseMoney();
		LOGGER.info(Language.get("info.using-buildinmoney"));
	
		Bukkit.getPluginManager().registerEvents(this, this);
		
		Iterator<World> worlds = Bukkit.getWorlds().iterator();
		
		while (worlds.hasNext()) {
			World w = worlds.next();
			Bukkit.getServer().unloadWorld(w, false);
		}
		
		String arenaName = this.getConfig().getString("arena");
		
		WORLD_LOBBY = Bukkit.getServer().createWorld(new WorldCreator("lobby"));
		WORLD_GAME = Bukkit.getServer().createWorld(new WorldCreator(arenaName));
		
		ARENA = new Arena(arenaName);
		
		setupCommands();
		
		BukkitRunnable playerCheck = new BukkitRunnable() {
			
			@Override
			public void run() {
				if (isRunning() || isPreparing()) {
					// game running start no new one
					cancel();
					return;
				}
				
				if (Bukkit.getOnlinePlayers().size() >= ARENA.getRequiredPlayers()) {
					// start countdown
					System.out.println("Start!");
					cancel();
					
					tryToStart();
				}
				else {
					// not enough: no countdown
					System.out.println("noch nicht genug!");
				}
			}
		};
		
		playerCheck.runTaskTimer(this, 0, 600);
	}
	 
	private void setupCommands() {
		PluginCommand coins = this.getCommand("coins");
		coins.setExecutor(new CoinsCommand());
		
		PluginCommand leave = this.getCommand("leave");
		leave.setExecutor(new LeaveCommand());

		PluginCommand shop = this.getCommand("shop");
		shop.setExecutor(new ShopCommand());
	}

	public void onDisable() { 
		MongoDatabase.close();
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (isRunning()) {
			// game running cannot join
			event.disallow(Result.KICK_OTHER, "Game running");
			return;
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// player has passed login: join lobby
		event.getPlayer().teleport(WORLD_LOBBY.getSpawnLocation());
		event.setJoinMessage(Language.get("info.join-lobby").replaceAll("%player%", event.getPlayer().getName()));
		
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
	}
	
	private void tryToStart() {
		if (!isRunning() && !isPreparing()) {
			if (Bukkit.getOnlinePlayers().size() >= ARENA.getRequiredPlayers()) {
				GAME = new Game(ARENA);
				
				for (Player p : Bukkit.getOnlinePlayers()) {	
					if (!GAME.addPlayer(CannonFighter.get(p)))
						// spieler konnte nicht beitreten
						break;
					
				}
				System.out.println("start genug");
				//Messager.updateSigns();
				
				GAME.start();
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (isPreparing())
			event.setQuitMessage(Language.get("info.leave-lobby").replaceAll("%player%", event.getPlayer().getName()));
		
		// if not preparing the game handles it
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChatMonitor(AsyncPlayerChatEvent event) {
		event.setFormat(Language.get("general.chat-format").replaceAll("%player%", event.getPlayer().getName()).replaceAll("%message%", event.getMessage()));
	}
	
	private static boolean isRunning() {
		return GAME != null && GAME.getState() != GameState.PREPARE;
	}
	
	private static boolean isPreparing() {
		return GAME != null && GAME.getState() == GameState.PREPARE;
	}

	public static void gameOver() {
		Bukkit.getServer().shutdown();
	}
}

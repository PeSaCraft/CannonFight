package de.pesacraft.cannonfight.game;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.game.api.CannonFighterDeathEvent;
import de.pesacraft.cannonfight.game.api.CannonFighterJoinGameEvent;
import de.pesacraft.cannonfight.game.api.CannonFighterLeaveEvent;
import de.pesacraft.cannonfight.game.api.CannonFighterPreJoinGameEvent;
import de.pesacraft.cannonfight.game.api.CannonFighterSpectatorJoinGameEvent;
import de.pesacraft.cannonfight.game.api.CannonFighterSpectatorPreJoinGameEvent;
import de.pesacraft.cannonfight.game.api.GameOverEvent;
import de.pesacraft.cannonfight.game.commands.LeaveCommand;
import de.pesacraft.cannonfight.game.communication.CommunicationGameClient;
import de.pesacraft.cannonfight.game.players.ActivePlayer;
import de.pesacraft.cannonfight.game.players.FuturePlayer;
import de.pesacraft.cannonfight.game.players.Participant;
import de.pesacraft.cannonfight.game.players.Spectator;
import de.pesacraft.cannonfight.util.CannonFightPlugin;
import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.MongoDatabase;
import de.pesacraft.cannonfight.util.Language.TimeOutputs;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.commands.CoinsCommand;
import de.pesacraft.cannonfight.util.commands.LanguageReloadCommand;
import de.pesacraft.cannonfight.util.commands.ShopCommand;

public class CannonFightGame extends CannonFightPlugin implements Listener {
	
	public static Logger LOGGER;
	public static CannonFightGame PLUGIN;
	
	private static Arena ARENA;
	
	private static World WORLD_LOBBY;
	private static World WORLD_GAME;
	
	private static Set<ActivePlayer> players;
	private static Set<Spectator> spectators;
	
	private static Set<FuturePlayer> upcomingPlayers;
	private static Set<FuturePlayer> upcomingSpectators;
	
	private static GameState gameState;
	
	private static BukkitRunnable playerCheck;
	
	@Override
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		Language.loadLanguage(this, this.getConfig().getString("language"));
		
		if (!new File(this.getDataFolder() + "config.yml").exists()) {
			LOGGER.info(Language.get("info.new-config-created", false));
			this.saveDefaultConfig();
		}
		
		CannonFightUtil.use(this);
		
		LOGGER.info(Language.get("info.unloading-worlds.begin", false));
		Iterator<World> worlds = Bukkit.getWorlds().iterator();
		while (worlds.hasNext()) {
			World w = worlds.next();
			Bukkit.getServer().unloadWorld(w, true);
		}
		LOGGER.info(Language.get("info.unloading-worlds.finish", false));
		
		LOGGER.info(Language.get("info.load-lobby", false));
		WORLD_LOBBY = Bukkit.getServer().createWorld(new WorldCreator("lobby"));
		// disable pvp
		WORLD_LOBBY.setPVP(false);
				
		setupCommands();
		
		players = new HashSet<ActivePlayer>();
		spectators = new HashSet<Spectator>();
		
		upcomingPlayers = new HashSet<FuturePlayer>();
		upcomingSpectators = new HashSet<FuturePlayer>();
		
		// check players all 30 sec
		playerCheck = new PlayerCheck();
		playerCheck.runTaskTimer(this, 0, 30 * 20); 
		
		gameState = GameState.WAITING;
		
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		CommunicationGameClient.tryToStart();
	}
	
	@Override
	public void onDisable() { 
		if (WORLD_GAME != null) {
			// restore backup
			LOGGER.info(Language.get("info.restore-backup.begin", false));
			File original = WORLD_GAME.getWorldFolder();
			File backup = new File(original.getParentFile().getAbsolutePath(), "_backup");
			Bukkit.unloadWorld(WORLD_GAME, true);
			LOGGER.info(Language.get("info.game.unloaded", false));
			CannonFightUtil.copyWorld(backup, original);
			LOGGER.info(Language.get("info.unloading-worlds.finished", false));
		}
		
		// unregister from proxy
		CommunicationGameClient.getInstance().sendGameOver();
		
		CannonFighter.saveAll();
		MongoDatabase.close();
	}
	
	public static void setArena(String arena) {
		LOGGER.info(Language.getStringMaker("info.arena-set", false).replace("%arena%", arena).getString());
		WORLD_GAME = Bukkit.getServer().createWorld(new WorldCreator(arena));
		
		LOGGER.info(Language.get("info.create-backup", false));
		// create backup
		File original = WORLD_GAME.getWorldFolder();
		File backup = new File(original.getParentFile().getAbsolutePath(), "_backup");
		backup.mkdir();
		CannonFightUtil.copyWorld(original, backup);
		
		ARENA = new Arena(arena);	
	}
	
	private void setupCommands() {
		LOGGER.info(Language.get("info.register-commands", false));
		PluginCommand coins = this.getCommand("coins");
		coins.setExecutor(new CoinsCommand());
		
		PluginCommand leave = this.getCommand("leave");
		leave.setExecutor(new LeaveCommand());

		PluginCommand shop = this.getCommand("shop");
		shop.setExecutor(new ShopCommand());
		
		PluginCommand langrl = this.getCommand("language-reload");
		langrl.setExecutor(new LanguageReloadCommand());
	}
	
	private void tryToStart() {
		BukkitRunnable startTimer = new BukkitRunnable() {
			private int time = CannonFightGame.PLUGIN.getConfig().getInt("game.time.untilStart");
			
			@Override
			public void run() {
				if (time == 0) {
					start();
					
					this.cancel();
					return;
				}
				
				if (players.size() < ARENA.getRequiredPlayers()) {
					// to less players again
					this.cancel();
					
					// check players again all 30 sec
					playerCheck = new PlayerCheck();
					playerCheck.runTaskTimer(CannonFightGame.PLUGIN, 0, 30 * 20); 
				
					return;
				}
				
				time--;
				
				Bukkit.broadcastMessage(Language.getStringMaker("info.time-till-start", true).replace("%time%", Language.formatTime(time, TimeOutputs.SECONDS)).getString());
			}
		};
		
		startTimer.runTaskTimer(this, 0, 20); // check every second	
	}
	
	private static void start() {
		gameState = GameState.STARTING;
		
		//setup players
		for (ActivePlayer active : players) {
			CannonFighter c = active.getPlayer();
			Player p = c.getPlayer();
			ARENA.teleport(c);
			
			active.createCage();
			
			Configuration config = CannonFightUtil.PLUGIN.getConfig();
			
			p.setMaxHealth(config.getDouble("game.livesPerLive"));
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			
			Inventory inv = p.getInventory();
			
			inv.clear();
			
			for (int i = 0; i < c.getSlots(); i++) {
				Cannon cannon = c.getActiveItem(i);
				if (cannon == null)
					// skip empty slots
					continue;
				inv.setItem(i, cannon.getItem());
			}
			
		}
		
		// setup spectators
		for (Spectator spectator : spectators) {
			CannonFighter c = spectator.getPlayer();
			Player p = c.getPlayer();
			ARENA.teleportSpectator(c);
			
			p.setGameMode(GameMode.SPECTATOR);
		}
		
		new BukkitRunnable() {
			private int time = CannonFightUtil.PLUGIN.getConfig().getInt("game.time.start");
			
			@Override
			public void run() {
				switch (gameState) {
				case STARTING:
					if (time == 0) {
						CommunicationGameClient.getInstance().sendGameStarted();
						gameState = GameState.INGAME;
						time = CannonFightUtil.PLUGIN.getConfig().getInt("game.time.play");
						
						for (ActivePlayer player : players) {
							player.destroyCage();
							
							player.createPlatform();
						}
						Bukkit.broadcastMessage(Language.get("info.game.start", true));
						
						break;	
					}
					
					Bukkit.broadcastMessage(Language.getStringMaker("info.game.start.countdown", true).replace("%time%", Language.formatTime(time, TimeOutputs.SECONDS)).getString());
					
					break;
				case INGAME:
					if (players.size() <= 1) {
						
						gameState = GameState.GAMEOVER;
						time = CannonFightUtil.PLUGIN.getConfig().getInt("game.time.gameover");
						
						Set<CannonFighter> winner = new HashSet<CannonFighter>();
						
						for (ActivePlayer active : players) {
							// spieler der das spiel gewonnen hat	
							winner.add(active.getPlayer());				
							break;
						}
						
						Bukkit.getServer().getPluginManager().callEvent(new GameOverEvent(winner));
						
						break;
					}
					
					if (time == 0) {
						
						gameState = GameState.GAMEOVER;
						time = CannonFightUtil.PLUGIN.getConfig().getInt("game.time.gameover");
						
						Set<CannonFighter> winner = new HashSet<CannonFighter>();
					
						for (ActivePlayer active : players)
							winner.add(active.getPlayer());
						
						Bukkit.getServer().getPluginManager().callEvent(new GameOverEvent(winner));
						
						break;	
					}
					// jede Minute uebrige Zeit, wenn weniger als 1 min alle 15 sek und die letzten 15 sek.
					if (time % 60 == 0)
						Bukkit.broadcastMessage(Language.getStringMaker("info.game.remaining", true).replace("%time%", Language.formatTime(time, TimeOutputs.MINUTES)).getString());

					if ((time < 60 && time % 15 == 0) || (time < 15))
						Bukkit.broadcastMessage(Language.getStringMaker("info.game.remaining", true).replace("%time%", Language.formatTime(time, TimeOutputs.SECONDS)).getString());
					
					for (ActivePlayer a: players)
						a.sendActionBar();
					
					break;
				case GAMEOVER:
					if (time % 5 == 0 || (time < 5 && time != 0))
						Bukkit.broadcastMessage(Language.getStringMaker("info.game.over.teleport.countdown", true).replace("%time%", Language.formatTime(time, TimeOutputs.SECONDS)).getString());
					
					if (time == 0) {
						Bukkit.broadcastMessage(Language.get("info.game.over.teleport", true));
						
						this.cancel();

						for (ActivePlayer a : players)
							leave(a);
						
						for (Spectator s : spectators)
							leave(s);
						
						players.clear();
						spectators.clear();
						
						CommunicationGameClient.getInstance().sendGameOver();
						new BukkitRunnable() {
							
							@Override
							public void run() {
								Bukkit.getServer().spigot().restart();		
							}
						}.runTaskLater(CannonFightGame.PLUGIN, 20);
					}
					break;
				default:
					break;
				}
				time--;
			}
		}.runTaskTimer(CannonFightUtil.PLUGIN, 0, 20);
		
	}
	
	private static void leave(Participant part) {
		CannonFighter c = part.getPlayer();
		
		if (part instanceof ActivePlayer) {
			// normal player: leave event
			Bukkit.getServer().getPluginManager().callEvent(new CannonFighterLeaveEvent(c));
			
			// spectators are hidden for normal players
			for (Spectator s : spectators)
				c.show(s.getPlayer());

			ActivePlayer active = (ActivePlayer) part;
			
			if (active.hasCage())
				active.destroyCage();
		}
		else if (part instanceof Spectator) {
			// spectators are hidden for normal players
			for (ActivePlayer a : players)
				a.getPlayer().show(c);
		}
		else
			// not player and not specator: not in game -> cannot leave
			return;
		
		// normal player or spectator
		c.resetCannons();
		CannonFighter.remove(c);
		
		Player p = c.getPlayer();
		
		p.setMaxHealth(20);
		p.setHealth(p.getPlayer().getMaxHealth());
		p.setFoodLevel(20);
		p.setFireTicks(0);
		
		p.setGameMode(GameMode.ADVENTURE);
		
		p.getInventory().clear();
		
		CommunicationGameClient.getInstance().sendBackToHub(part);
	}
	
	private static void respawn(ActivePlayer a) {
		CannonFighter c = a.getPlayer();
		Player p = c.getPlayer();
		
		ARENA.randomRespawn(p);
		
		a.createPlatform();
		
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.setFireTicks(0);
		
		c.sendMessage(Language.getStringMaker("info.respawned", true).replace("%lives%", String.valueOf(a.getLives())).getString());
	}
	
	public static void addFuturePlayer(String name, String server) {
		FuturePlayer f = new FuturePlayer(name, server);
		upcomingPlayers.add(f);
	}
	
	public static void addFutureSpectator(String name, String server) {
		FuturePlayer f = new FuturePlayer(name, server);
		upcomingSpectators.add(f);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		String name = event.getPlayer().getName();
		
		FuturePlayer futurePlayer = null;
		for (FuturePlayer f : upcomingPlayers) {
			if (f.getName().equals(name)) {
				futurePlayer = f;
				break;
			}
		}
		
		if (futurePlayer != null) {
			CannonFighter c = CannonFighter.get((OfflinePlayer) event.getPlayer());
			
			CannonFighterPreJoinGameEvent preJoinEvent = new CannonFighterPreJoinGameEvent(c);
			Bukkit.getPluginManager().callEvent(preJoinEvent);
			
			if (preJoinEvent.isCancelled())
				event.disallow(Result.KICK_OTHER, Language.get("info.kick.game-running", false));
			return;
		}
		// no player, maybe spectator
		
		futurePlayer = null;
		for (FuturePlayer f : upcomingSpectators) {
			if (f.getName().equals(name)) {
				futurePlayer = f;
				break;
			}
		}
		
		if (futurePlayer != null) {
			CannonFighter c = CannonFighter.get((OfflinePlayer) event.getPlayer());
			
			CannonFighterSpectatorPreJoinGameEvent preJoinEvent = new CannonFighterSpectatorPreJoinGameEvent(c);
			Bukkit.getPluginManager().callEvent(preJoinEvent);
			
			if (preJoinEvent.isCancelled())
				event.disallow(Result.KICK_FULL, Language.get("info.kick.server-full", false));
			return;
		}
		
		// neither player nor spectator: cannot join
		event.disallow(Result.KICK_WHITELIST, Language.get("info.kick.not-authenticated", false));
	}
	
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		// no join message, gets handled somewhere else
		event.setJoinMessage(null);
		
		// handle everything one second later so the leave event on the hub has passed and data is saved
		new BukkitRunnable() {
			
			@Override
			public void run() {
				FuturePlayer futurePlayer = null;
				for (FuturePlayer f : upcomingPlayers) {
					if (f.getName().equals(event.getPlayer().getName())) {
						futurePlayer = f;
						break;
					}
				}
				
				if (futurePlayer != null) {
					CannonFighter newPlayer = CannonFighter.reload((OfflinePlayer) event.getPlayer());
					
					CannonFighterJoinGameEvent joinEvent = new CannonFighterJoinGameEvent(newPlayer);
					Bukkit.getPluginManager().callEvent(joinEvent);
					
					upcomingPlayers.remove(futurePlayer);
					players.add(new ActivePlayer(newPlayer, futurePlayer.getServer()));
					
					if (players.size() == ARENA.getMaxPlayers())
						CommunicationGameClient.getInstance().sendGameFull();
					return;
				}
				
				// not a player, is spectator
				futurePlayer = null;
				for (FuturePlayer f : upcomingSpectators) {
					if (f.getName().equals(event.getPlayer().getName())) {
						futurePlayer = f;
						break;
					}
				}
				
				if (futurePlayer != null) {
					CannonFighter newPlayer = CannonFighter.reload((OfflinePlayer) event.getPlayer());
					
					CannonFighterSpectatorJoinGameEvent joinEvent = new CannonFighterSpectatorJoinGameEvent(newPlayer);
					Bukkit.getPluginManager().callEvent(joinEvent);
					
					upcomingSpectators.remove(futurePlayer);
					spectators.add(new Spectator(newPlayer, futurePlayer.getServer()));
				}
			}
		}.runTaskLater(this, 20);
	}
	
	@EventHandler
	public void onCannonFighterPreJoin(CannonFighterPreJoinGameEvent event) {
		if (gameState != GameState.WAITING && gameState != GameState.STARTING)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onCannonFighterJoin(CannonFighterJoinGameEvent event) {
		CannonFighter c = event.getPlayer();
		
		// show and hide players
		for (ActivePlayer active : players) {
			active.getPlayer().show(c);
			c.show(active.getPlayer());
		}
		
		for (Spectator spectator : spectators) {
			spectator.getPlayer().show(c);
			c.hide(spectator.getPlayer());
		}
		
		Player p = c.getPlayer();
		
		p.setGameMode(GameMode.ADVENTURE);
		
		if (gameState == GameState.STARTING) {
			// game already starting, teleport directly into arena
			ARENA.teleport(c);
			
			for (ActivePlayer active : players) {
				if (active.getPlayer().equals(c)) {
					active.createCage();
					break;
				}
			}
			
			Configuration config = CannonFightUtil.PLUGIN.getConfig();
			
			p.setMaxHealth(config.getDouble("game.livesPerLive"));
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			
			Inventory inv = p.getInventory();
			
			inv.clear();
			
			for (int i = 0; i < c.getSlots(); i++) {
				Cannon cannon = c.getActiveItem(i);
				if (cannon == null)
					// skip empty slots
					continue;
				inv.setItem(i, cannon.getItem());
			}
			
			Bukkit.broadcastMessage(Language.getStringMaker("info.join.game.others", true).getString());
		}
		else {
			p.teleport(WORLD_LOBBY.getSpawnLocation());
			
			Bukkit.broadcastMessage(Language.getStringMaker("info.join.lobby.others", true).replace("%player%", event.getPlayer().getName()).replace("%players%", String.valueOf(players.size() + 1)).replace("%maxPlayers%", String.valueOf(ARENA.getMaxPlayers())).getString());
		}
	}
	
	@EventHandler
	public void onCannonFighterSpectatorJoin(CannonFighterSpectatorJoinGameEvent event) {
		CannonFighter c = event.getPlayer();
		
		// show and hide players
		for (ActivePlayer active : players) {
			active.getPlayer().hide(c);
			c.show(active.getPlayer());
		}
		
		for (Spectator spectator : spectators) {
			spectator.getPlayer().show(c);
			c.show(spectator.getPlayer());
		}
		
		Player p = c.getPlayer();
		if (gameState == GameState.WAITING) {
			p.teleport(WORLD_LOBBY.getSpawnLocation());
			
			p.setGameMode(GameMode.ADVENTURE);
		}
		else {
			ARENA.teleportSpectator(c);
			
			p.setGameMode(GameMode.SPECTATOR);	
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player p = (Player) event.getEntity();
		if (p.getHealth() > event.getFinalDamage())
			// player will not die
			return;
		
		// player will die
		// check if it is a player in this game
		CannonFighter victim = CannonFighter.get((OfflinePlayer) p);
		Participant part = new Participant(victim);
		
		if (!players.contains(part))
			// player not in this game
			return;
		
		CannonFighter killer = p.getKiller() != null ? CannonFighter.get((OfflinePlayer) p.getKiller()) : null;
		
		ActivePlayer victimSession = null;
		
		for (ActivePlayer a : players) {
			if (a.equals(part)) {
				victimSession = a;
				break;
			}
		}
		
		victimSession.looseLife();
		
		// player death => reward
		Bukkit.getServer().getPluginManager().callEvent(new CannonFighterDeathEvent(victim, killer, victimSession.isDead()));
			
		if (victimSession.isDead()) {
			// player lost last life: out of game
			
			
			CannonFighterSpectatorJoinGameEvent joinEvent = new CannonFighterSpectatorJoinGameEvent(victim);
			Bukkit.getPluginManager().callEvent(joinEvent);
			
			players.remove(victimSession);
			spectators.add(new Spectator(victim, victimSession.getServer()));
		}
		else
			// player not dead, respawn
			respawn(victimSession);
		
		if (killer != null && killer != victim) {
			// if there is a killer and the person didn't killed himself add a kill
			for (ActivePlayer a : players) {
				if (a.equals(new Participant(killer))) {
					a.addKill();
					break;
				}
			}
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		// original action will be cancelled
		event.setCancelled(true);
		
		if (gameState != GameState.INGAME)
			// not in game: no item usable
			return;
		
		CannonFighter c = CannonFighter.get((OfflinePlayer) event.getPlayer());
					
		if (players.contains(new Participant(c)))
			c.use(event.getItem());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		CannonFighter c = CannonFighter.get((OfflinePlayer) event.getPlayer());
		
		for (ActivePlayer a : players) {
			if (a.getPlayer().equals(c)) {
				leave(a);
				players.remove(a);
				return;
			}
		}
		
		for (Spectator s : spectators) {
			if (s.getPlayer().equals(c)) {
				leave(s);
				spectators.remove(s);
				return;
			}
		}
		
		event.setQuitMessage(null);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		CannonFighter sender = CannonFighter.get((OfflinePlayer) event.getPlayer());
		
		if (spectators.contains(new Participant(sender))) {
			// sender is spectator, message only for spectators
			
			Iterator<Player> recipients = event.getRecipients().iterator();
			
			while (recipients.hasNext()) {
				CannonFighter c = CannonFighter.get((OfflinePlayer) recipients.next());
				
				if (!spectators.contains(new Participant(c)))
					// not a spectator, will not receive message
					recipients.remove();
			}
		}
		
		// player messages are visible to everyone
		
		event.setFormat(Language.getStringMaker("general.chat-format", true).replace("%player%", event.getPlayer().getName()).replace("%message%", event.getMessage()).getString());
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		if (gameState != GameState.INGAME)
			// cancel all damage if not ingame
			event.setCancelled(true);

		// disable pvp
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		// nobody looses food!
		event.setFoodLevel(20);
	}
	
	@EventHandler
	public void onGameOver(GameOverEvent event) {
		Bukkit.broadcastMessage(Language.get("info.game.over", true));
		
		int reward = CannonFightUtil.PLUGIN.getConfig().getInt("game.reward");
		String msg = Language.getStringMaker("info.game.over.won", true).replace("%coins%", Language.formatCoins(reward)).getString();
		for (CannonFighter c : event.getWinner()) {
			c.sendMessage(msg);
			c.giveCoins(reward / event.getWinner().size());
		}
	}
	
	@EventHandler
	public void onCannonFighterDeath(CannonFighterDeathEvent event) {
		String msg;
		
		if (event.getKiller() != null) {
			// von spieler getoetet
			msg = Language.getStringMaker("info.killed.broadcast", true).replace("%killer%", event.getKiller().getName()).replace("%victim%", event.getVictim().getName()).getString();
			
			event.getKiller().sendMessage(Language.getStringMaker("info.killed.victim", true).replace("%victim%", event.getVictim().getName()).getString());
			event.getVictim().sendMessage(Language.getStringMaker("info.killed.killer", true).replace("%killer%", event.getKiller().getName()).getString());
		}
		else {
			// anderer todes grund
			msg = Language.getStringMaker("info.died.broadcast", true).replace("%player%", event.getVictim().getName()).getString();
			
			event.getVictim().sendMessage(Language.get("info.died", true));
		}
		
		Bukkit.broadcastMessage(msg);
		
		if (event.isOutOfGame())
			Bukkit.broadcastMessage(Language.getStringMaker("info.remaining-players", true).replace("%player%", String.valueOf(players.size() - 1)).getString());
	}
	
	@EventHandler
	public void onCannonFighterLeave(CannonFighterLeaveEvent event) {
		if (gameState == GameState.STARTING) {	
			for (ActivePlayer p : players) {
				if (p.getPlayer().equals(event.getFighter())) {
					try {
						if (p.hasCage())
							p.destroyCage();
					}
					catch (IllegalStateException ex) {
						// no cage existing: ignore error
					}
				}
			}
		}
		if (gameState == GameState.INGAME || gameState == GameState.STARTING) {
			String msg = Language.getStringMaker("info.leave-game", true).replace("%player%", event.getFighter().getName()).getString();
			String msg2 = Language.getStringMaker("info.remaining-players", true).replace("%player%", String.valueOf((players.size() - 1))).getString();
		
			Bukkit.broadcastMessage(msg);
			Bukkit.broadcastMessage(msg2);
		}
		else if (gameState == GameState.WAITING) {
			Bukkit.broadcastMessage(Language.getStringMaker("info.leave.lobby", true).replace("%player%", event.getFighter().getName()).replace("%players%", String.valueOf(players.size() - 1)).replace("%maxPlayers%", String.valueOf(ARENA.getMaxPlayers())).getString());
		}
	}
	
	private class PlayerCheck extends BukkitRunnable {

		@Override
		public void run() {
			if (CannonFightGame.ARENA == null)
				return;
			
			if (players.size() == 0 && spectators.size() == 0)
				return;
			
			if (players.size() >= ARENA.getRequiredPlayers()) {
				// start countdown
				this.cancel();
				
				Bukkit.broadcastMessage(Language.get("info.game.wait.enough-players", true));
				tryToStart();
			}
			else {
				// not enough: no countdown
				Bukkit.broadcastMessage(Language.get("info.game.wait.not-enough-players", true));
			}
		}
	}

	@Override
	public boolean isActivePlayer(CannonFighter c) {
		if (gameState != gameState.INGAME)
			// active players are only active ingame
			return false;
		
		for (ActivePlayer a : players)
			if (a.getPlayer().equals(c))
				return true;
		
		// no active player that is this CannonFighter
		return false;
	}
}

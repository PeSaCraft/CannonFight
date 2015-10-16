package de.pesacraft.cannonfight.game;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.Language.TimeOutputs;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.commands.CoinsCommand;
import de.pesacraft.cannonfight.util.commands.ShopCommand;

public class CannonFightGame extends JavaPlugin implements Listener {
	
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
		
		if (!new File(this.getDataFolder() + "config.yml").exists())
			this.saveDefaultConfig();
		
		CannonFightUtil.use(this);
		
		Language.loadLanguage(this, this.getConfig().getString("language"));
		
		Iterator<World> worlds = Bukkit.getWorlds().iterator();
		
		while (worlds.hasNext()) {
			World w = worlds.next();
			Bukkit.getServer().unloadWorld(w, true);
		}
		
		WORLD_LOBBY = Bukkit.getServer().createWorld(new WorldCreator("lobby"));
		
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
			File original = WORLD_GAME.getWorldFolder();
			File backup = new File(original.getParentFile().getAbsolutePath(), "_backup");
			Bukkit.unloadWorld(WORLD_GAME, false);
			LOGGER.info(Language.get("info.game.unloaded"));
			CannonFightUtil.copyWorld(backup, original);
		}
		
		// unregister from proxy
		CommunicationGameClient.getInstance().sendGameOver();
	}
	
	public static void setArena(String arena) {
		WORLD_GAME = Bukkit.getServer().createWorld(new WorldCreator(arena));
		
		// create backup
		File original = WORLD_GAME.getWorldFolder();
		File backup = new File(original.getParentFile().getAbsolutePath(), "_backup");
		backup.mkdir();
		CannonFightUtil.copyWorld(original, backup);
		
		ARENA = new Arena(arena);
	}
	
	private void setupCommands() {
		PluginCommand coins = this.getCommand("coins");
		coins.setExecutor(new CoinsCommand());
		
		PluginCommand leave = this.getCommand("leave");
		leave.setExecutor(new LeaveCommand());

		PluginCommand shop = this.getCommand("shop");
		shop.setExecutor(new ShopCommand());
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
						Bukkit.broadcastMessage(Language.get("info.game.start"));
						
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
					
					break;
				case GAMEOVER:
					if (time % 5 == 0 || (time < 5 && time != 0))
						Bukkit.broadcastMessage(Language.getStringMaker("info.game.over.teleport.countdown", true).replace("%time%", Language.formatTime(time, TimeOutputs.SECONDS)).getString());
					
					if (time == 0) {
						Bukkit.broadcastMessage(Language.get("info.game.over.teleport"));
						
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
		
		Player p = c.getPlayer();
		
		p.setMaxHealth(20);
		p.setHealth(p.getPlayer().getMaxHealth());
		p.setFoodLevel(20);
		p.setFireTicks(0);
		
		p.setGameMode(GameMode.ADVENTURE);
		
		p.getInventory().clear();
		
		CommunicationGameClient.getInstance().sendBackToHub(part);
	}
	
	public static boolean locIsInArena(Location loc) {
		Location lower = ARENA.getLowerBound();
		Location upper = ARENA.getUpperBound();
		return loc.getBlockX() >= lower.getBlockX() && loc.getBlockX() <= upper.getBlockX()
				&& loc.getBlockY() >= lower.getBlockY() && loc.getBlockY() <= upper.getBlockY()
				&& loc.getBlockZ() >= lower.getBlockZ() && loc.getBlockZ() <= upper.getBlockZ();
	}
	
	private static Set<Block> getBlocksForWall(Block base) {
		Set<Block> relatives = new HashSet<Block>();
		
		for (int i = 1; i <= 5; i++) {
			Block relative = base.getRelative(BlockFace.NORTH, i);
			
			if (!locIsInArena(relative.getLocation()) || !locIsInArena((relative = base.getRelative(BlockFace.SOUTH, i)).getLocation())) {
				// block nearby outside arena:
				// show particle wall
				relatives.add(relative);
				
				Block up = relative.getRelative(BlockFace.UP);
				
				relatives.add(up);
				relatives.add(up.getRelative(BlockFace.UP));
				
				relatives.add(relative.getRelative(BlockFace.DOWN));
				
				relatives.add(relative.getRelative(BlockFace.EAST));
				relatives.add(up.getRelative(BlockFace.EAST));
				
				relatives.add(relative.getRelative(BlockFace.WEST));
				relatives.add(up.getRelative(BlockFace.WEST));
				
				return relatives;
			}
			
			if (!locIsInArena((relative = base.getRelative(BlockFace.EAST, i)).getLocation()) || !locIsInArena((relative = base.getRelative(BlockFace.WEST, i)).getLocation())) {
				// block nearby outside arena:
				// show particle wall
				relatives.add(relative);
				
				Block up = relative.getRelative(BlockFace.UP);
				
				relatives.add(up);
				relatives.add(up.getRelative(BlockFace.UP));
				
				relatives.add(relative.getRelative(BlockFace.DOWN));
				
				relatives.add(relative.getRelative(BlockFace.NORTH));
				relatives.add(up.getRelative(BlockFace.NORTH));
				
				relatives.add(relative.getRelative(BlockFace.SOUTH));
				relatives.add(up.getRelative(BlockFace.SOUTH));
				
				return relatives;
			}
			
			// players head is 2 above the given location
			if (!locIsInArena((relative = base.getRelative(BlockFace.DOWN, i)).getLocation()) || !locIsInArena((relative = base.getRelative(BlockFace.UP, i + 2)).getLocation())) {
				// block nearby outside arena:
				// show particle wall
				relatives.add(relative);
				
				relatives.add(relative.getRelative(BlockFace.NORTH));
				relatives.add(relative.getRelative(BlockFace.NORTH_EAST));
				relatives.add(relative.getRelative(BlockFace.EAST));
				relatives.add(relative.getRelative(BlockFace.SOUTH_EAST));
				relatives.add(relative.getRelative(BlockFace.SOUTH));
				relatives.add(relative.getRelative(BlockFace.SOUTH_WEST));
				relatives.add(relative.getRelative(BlockFace.WEST));
				relatives.add(relative.getRelative(BlockFace.NORTH_WEST));
				
				return relatives;
			}
		}
		
		return relatives;
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
		CannonFighter c = CannonFighter.get((OfflinePlayer) event.getPlayer());
		FuturePlayer futurePlayer = null;
		for (FuturePlayer f : upcomingPlayers) {
			if (f.getName().equals(name)) {
				futurePlayer = f;
				break;
			}
		}
		
		if (futurePlayer != null) {
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
	public void onPlayerJoin(PlayerJoinEvent event) {
		CannonFighter player = CannonFighter.get((OfflinePlayer) event.getPlayer());
		
		// no join message, gets handled somewhere else
		event.setJoinMessage(null);
		
		FuturePlayer futurePlayer = null;
		for (FuturePlayer f : upcomingPlayers) {
			if (f.getName().equals(player.getName())) {
				futurePlayer = f;
				break;
			}
		}
		
		if (futurePlayer != null) {
			CannonFighterJoinGameEvent joinEvent = new CannonFighterJoinGameEvent(player);
			Bukkit.getPluginManager().callEvent(joinEvent);
			
			upcomingPlayers.remove(futurePlayer);
			players.add(new ActivePlayer(player, futurePlayer.getServer()));
			
			if (players.size() == ARENA.getMaxPlayers())
				CommunicationGameClient.getInstance().sendGameFull();
			return;
		}
		
		// not a player, is spectator
		futurePlayer = null;
		for (FuturePlayer f : upcomingSpectators) {
			if (f.getName().equals(player.getName())) {
				futurePlayer = f;
				break;
			}
		}
		
		if (futurePlayer != null) {
			CannonFighterSpectatorJoinGameEvent joinEvent = new CannonFighterSpectatorJoinGameEvent(player);
			Bukkit.getPluginManager().callEvent(joinEvent);
			
			upcomingSpectators.remove(futurePlayer);
			spectators.add(new Spectator(player, futurePlayer.getServer()));
		}
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
			
			Bukkit.broadcastMessage(Language.getStringMaker("info.join.game.others", true).replace("%player%", event.getPlayer().getName()).getString());
		}
		else {
			p.teleport(WORLD_LOBBY.getSpawnLocation());
			
			Bukkit.broadcastMessage(Language.getStringMaker("info.join.lobby.others", true).replace("%player%", event.getPlayer().getName()).getString());
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
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (gameState == GameState.WAITING || gameState == GameState.STARTING)
			// ignore start
			return;
		
		Player p = event.getPlayer();
		CannonFighter c = CannonFighter.get((OfflinePlayer) p);
		Participant par = new Participant(c);
		
		if (players.contains(par)) {
			Location to = event.getTo();
			Location from = event.getFrom();
			
			ifbreak: if (locIsInArena(to)) {
				if (!locIsInArena(to.add(0, 2, 0))) {
					// head not in arena
					break ifbreak;
				}
				// work with feet location again
				to.subtract(0, 2, 0);
				Set<Block> relatives = getBlocksForWall(to.getBlock());
				
				for (Block rel : relatives) {
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BARRIER, false, rel.getX(), rel.getY() + 0.5f, rel.getZ(), 0, 0, 0, 0, 1);
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}
				return;
			}
			
			// player will be outside arena
			
			if (!locIsInArena(from)) {
				// if player comes from outside the arena
				// he has to be pushed inside
				p.setVelocity(p.getVelocity().normalize().multiply(2));
				return;
			}
			
			Vector direction = from.toVector().subtract(to.toVector()).normalize();

			direction.setX(direction.getX() * 2);
			direction.setY(direction.getY() * 2);
			direction.setZ(direction.getZ() * 2);
 			
 			p.setVelocity(direction);
			
			event.setTo(from);
			return;
		}
		
		if (spectators.contains(par)) {
			Location to = event.getTo();
			Location from = event.getFrom();
			
			ifbreak: if (locIsInArena(to)) {
				if (!locIsInArena(to.add(0, 2, 0))) {
					// head not in arena
					break ifbreak;
				}
				// work with feet location again
				to.subtract(0, 2, 0);
				Set<Block> relatives = getBlocksForWall(to.getBlock());
				
				for (Block rel : relatives) {
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BARRIER, false, rel.getX(), rel.getY() + 0.5f, rel.getZ(), 0, 0, 0, 0, 1);
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}
				return;
			}
			
			// player will be outside arena
			if (!locIsInArena(from)) {
				// if player comes from outside the arena
				// he has to be pushed inside
				p.setVelocity(p.getVelocity().normalize());
				return;
			}
						
			Vector direction = from.toVector().subtract(to.toVector()).normalize();

			direction.setX(direction.getX() * 2);
			direction.setY(direction.getY() * 2);
			direction.setZ(direction.getZ() * 2);
 			
 			p.setVelocity(direction);
			
			event.setTo(from);
			p.sendMessage(Language.get("error.cannot-leave-arena-bounds"));
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
			
			players.remove(victimSession);
			
			// make spectator
			ARENA.teleportSpectator(victim);
			
			// show and hide players
			for (ActivePlayer active : players)
				// remaining players shouldn't see spectator
				// he can already see them
				active.getPlayer().hide(victim);
			
			for (Spectator spectator : spectators)
				// spectators can already see him
				// he has to see other spectators
				victim.show(spectator.getPlayer());
			
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
		
		CannonFighter c = CannonFighter.get((OfflinePlayer) event.getPlayer());
					
		if (players.contains(new Participant(c))) {
						
			if (gameState != GameState.INGAME)
				// not in game: no item usable
				return;
			
			c.use(event.getItem());
		}
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
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		// nobody looses food!
		event.setFoodLevel(20);
	}
	
	@EventHandler
	public void onGameOver(GameOverEvent event) {
		Bukkit.broadcastMessage(Language.get("info.game.over"));
		
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
			
			event.getVictim().sendMessage(Language.get("info.died"));
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
			Bukkit.broadcastMessage(Language.getStringMaker("info.leave.lobby", true).replace("%player%", event.getFighter().getName()).getString());
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
				
				Bukkit.broadcastMessage(Language.get("info.game.wait.enough-players"));
				tryToStart();
			}
			else {
				// not enough: no countdown
				Bukkit.broadcastMessage(Language.get("info.game.wait.not-enough-players"));
			}
		}
	}
}

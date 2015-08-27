package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.api.game.CannonFighterDeathEvent;
import de.pesacraft.cannonfight.api.game.CannonFighterJoinGameEvent;
import de.pesacraft.cannonfight.api.game.CannonFighterLeaveEvent;
import de.pesacraft.cannonfight.api.game.GameOverEvent;
import de.pesacraft.cannonfight.data.players.ActivePlayer;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.data.players.Participant;
import de.pesacraft.cannonfight.data.players.Spectator;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.util.Cage;
import de.pesacraft.cannonfight.util.CageForm;
import de.pesacraft.cannonfight.util.ModifiedBlock;
import de.pesacraft.cannonfight.util.blockrestore.MassBlockUpdate;
import de.pesacraft.cannonfight.util.blockrestore.RelightingStrategy;

public class Game implements Listener {
	private Arena arena;
	
	private List<ActivePlayer> players;
	private List<Spectator> spectators;
	
	private GameState state;
	private int time;
	
	private Set<ModifiedBlock> destroyedBlocks;
	
	private BukkitTask countdownTask;
	
	protected Game(Arena arena) {
		this.arena = arena;
		this.state = GameState.TELEPORTTOARENA;
		
		this.players = new ArrayList<ActivePlayer>();
		this.spectators = new ArrayList<Spectator>();
		
		this.destroyedBlocks = new HashSet<ModifiedBlock>();
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}
	
	public boolean addPlayer(CannonFighter p) {
		if (state != GameState.TELEPORTTOARENA)
			return false;

		if (players.size() == getMaxPlayers())
			return false;
	
		CannonFighterJoinGameEvent event = new CannonFighterJoinGameEvent(this, p);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return false;
		
		if (!teleportPlayerToArena(p))
			return false;
		
		ActivePlayer active = new ActivePlayer(p);
		
		active.createCage();
		
		players.add(active);
		
		p.sendMessage(Language.get("info.player-join").replaceAll("%player%", players.size() + "").replaceAll("%maxPlayer%", getMaxPlayers() + ""));
		
		return true;
	}
	
	private boolean teleportPlayerToArena(CannonFighter p) {
		if (!arena.teleport(p, this))
			return false;
		
		// teleport successful
		// show and hide players
		for (ActivePlayer active : players) {
			active.getPlayer().show(p);
			p.show(active.getPlayer());
		}
		
		for (Spectator spectator : spectators) {
			spectator.getPlayer().show(p);
			p.hide(spectator.getPlayer());
		}
		
		return true;
	}

	public int addPlayers(CannonFighter... players) {
		int i = 0;
		for (CannonFighter p : players) {
			if (!addPlayer(p))
				break;
			i++;
		}
		return i;
	}
	
	public boolean addSpectator(CannonFighter p) {
		if (!teleportSpectatorToArena(p))
			return false;
		
		spectators.add(new Spectator(p));
		
		return true;
	}
	
	private boolean teleportSpectatorToArena(CannonFighter p) {
		if (!arena.teleportSpectator(p, this))
			return false;
		
		// teleport successful
		// show and hide players
		for (ActivePlayer active : players) {
			active.getPlayer().hide(p);
			p.show(active.getPlayer());
		}
		
		for (Spectator spectator : spectators) {
			spectator.getPlayer().show(p);
			p.show(spectator.getPlayer());
		}
		
		return true;
	}
	
	public int addSpectators(CannonFighter... players) {
		int i = 0;
		for (CannonFighter p : players) {
			if (!addSpectator(p))
				break;
			i++;
		}
		return i;
	}
	
	private void countdown() {
		String msg;
		
		switch (state) {
		case TELEPORTTOARENA:
			if (time == 0) {
				for (ActivePlayer active : players) {
					preparePlayer(active.getPlayer());
				}
				state = GameState.START;
				nextCountdown();
			}
			break;
		case START:
			if (time == 0) {
				state = GameState.INGAME;
				
				removeCages();
				nextCountdown();
				
				sendMessageToAll(Language.get("info.game-start"));
				
				break;	
			}
			
			sendMessageToAll(Language.get("info.game-start-countdown").replaceAll("%time%", time + ""));
			
			break;
		case INGAME:
			if (players.size() <= 1) {
				
				state = GameState.GAMEOVER;
				nextCountdown();
				
				Set<CannonFighter> winner = new HashSet<CannonFighter>();
				
				for (ActivePlayer active : players) {
					// spieler der das spiel gewonnen hat	
					winner.add(active.getPlayer());				
					break;
				}
				
				Bukkit.getServer().getPluginManager().callEvent(new GameOverEvent(this, winner));
				
				break;
			}
			
			if (time == 0) {
				state = GameState.GAMEOVER;
				nextCountdown();
				
				Set<CannonFighter> winner = new HashSet<CannonFighter>();
			
				for (ActivePlayer active : players)
					winner.add(active.getPlayer());
				
				Bukkit.getServer().getPluginManager().callEvent(new GameOverEvent(this, winner));
				
				break;	
			}
			// jede Minute uebrige Zeit, wenn weniger als 1 min alle 15 sek und die letzten 15 sek.
			if (time % 60 == 0)
				sendMessageToAll(Language.get("info.game-remaining-min").replaceAll("%time%", (time / 60) + ""));

			if ((time < 60 && time % 15 == 0) || (time < 15))
				sendMessageToAll(Language.get("info.game-remaining-sec").replaceAll("%time%", time + ""));
			
			break;
		case GAMEOVER:
			if (time == 0) {
				state = GameState.TELEPORTBACK;
				countdownTask.cancel();

				for (ActivePlayer c : players)
					leave(c.getPlayer());
				
				for (Spectator c : spectators)
					leave(c.getPlayer());
				
				players.clear();
				spectators.clear();
				resetArena();
				
				HandlerList.unregisterAll(this);
				
				// notify GameManager, that the game is over
				GameManager.gameOver(this);
			}
			break;
		default:
			break;
		}
		time--;	
	}

	private void removeCages() {
		for (ActivePlayer player : players)
			player.destroyCage();
	}

	private void preparePlayer(CannonFighter c) {
		Configuration config = CannonFight.PLUGIN.getConfig();
		
		Player p = c.getPlayer();
		
		p.setMaxHealth(config.getDouble("game.lives.perLive"));
		p.setHealth(p.getPlayer().getMaxHealth());
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
	
	private void nextCountdown() {
		switch (state) {
		case TELEPORTTOARENA:
			time = 1;
			break;
		case START:
			time = CannonFight.PLUGIN.getConfig().getInt("game.time.start");
			break;
		case INGAME:
			time = CannonFight.PLUGIN.getConfig().getInt("game.time.play");
			break;
		case GAMEOVER:
			time = CannonFight.PLUGIN.getConfig().getInt("game.time.gameover");
			break;
		default:
			break;
		}
	}
	
	public boolean removePlayer(CannonFighter c) {
		if (!players.contains(new Participant(c)))
			return false;
		
		players.remove(new Participant(c));
		return true;	
	}
	
	public boolean removeSpectator(CannonFighter c) {
		if (!spectators.contains(new Participant(c)))
			return false;
		
		spectators.remove(new Participant(c));
		
		return true;
	}
	
	public int getMaxPlayers() {
		return arena.getMaxPlayers();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		CannonFighter c = CannonFighter.get(p);
		Participant par = new Participant(c);
		
		if (players.contains(par)) {
			Location to = event.getTo();
			Location from = event.getFrom();
			
			if (locIsInArena(to)) {
				Set<Block> relatives = getBlocksForWall(to.getBlock());
				
				for (Block rel : relatives) {
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BARRIER, false, rel.getX(), rel.getY() + 0.5f, rel.getZ(), 0, 0, 0, 0, 1);
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}
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
			if (locIsInArena(event.getTo())) {
				Set<Block> relatives = getBlocksForWall(event.getTo().getBlock());
				
				for (Block rel : relatives) {
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BARRIER, false, rel.getX(), rel.getY() + 0.5f, rel.getZ(), 0, 0, 0, 0, 1);
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
				}
				return;
			}
			
			event.setTo(event.getFrom());	
			p.sendMessage(Language.get("error.cannot-leave-arena-bounds"));
		}
	}

	private Set<Block> getBlocksForWall(Block base) {
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
		}
		
		return relatives;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		CannonFighter victim = CannonFighter.get(p);
		CannonFighter killer = p.getKiller() != null ? CannonFighter.get(p.getKiller()) : null;
		
		if (players.contains(new Participant(victim))) {
			removePlayer(victim);
			addSpectator(victim);
			
			// player death => reward
			Bukkit.getServer().getPluginManager().callEvent(new CannonFighterDeathEvent(this, victim, killer));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		CannonFighter c = CannonFighter.get(event.getPlayer());
		
		if (players.contains(new Participant(c))) {
			// thats a player, original action will be cancelled
			event.setCancelled(true);
			
			if (state != GameState.INGAME)
				// not in game: no item usable
				return;
			
			c.use(event.getItem());
		}
	}

	@EventHandler
	public void onGameOver(GameOverEvent event) {
		if (event.getGame() != this)
			return;
		
		sendMessageToAll(Language.get("info.game-over")); // ChatColor.RED + "Spiel vorbei!"
		
		int reward = CannonFight.PLUGIN.getConfig().getInt("game.reward");
		String msg = Language.get("info.game-won").replaceAll("%coins%", reward + "");
		for (CannonFighter c : event.getWinner()) {
			c.sendMessage(msg);
			c.giveCoins(reward);
		}
	}
	
	@EventHandler
	public void onCannonFighterDeath(CannonFighterDeathEvent event) {
		if (event.getGame() == this) {
			String msg;
			
			if (event.getKiller() != null) {
				// von spieler getoetet
				msg = Language.get("info.player-died-killed-by-other").replaceAll("%killer%", event.getKiller().getName()).replaceAll("%victim%", event.getVictim().getName()); // event.getVictim().getName() + " wurde von " + event.getKiller().getName() + " getötet."
				event.getKiller().sendMessage(Language.get("info.player-killed").replaceAll("%victim%", event.getVictim().getName()));
				event.getVictim().sendMessage(Language.get("info.player-got-killed").replaceAll("%killer%", event.getKiller().getName()));
			}
			else {
				// anderer todes grund
				msg = Language.get("info.player-died"); // event.getVictim().getName() + " ist gestorben.";	
				event.getVictim().sendMessage(Language.get("info.player-died-self"));
			}
			String msg2 = Language.get("info.remaining-players").replaceAll("%player%", players.size() + "");
			
			sendMessageToAll(msg);
			sendMessageToAll(msg2);
		}
	}
	
	@EventHandler
	public void onCannonFighterLeave(CannonFighterLeaveEvent event) {
		if (event.getGame() != this)
			return;
		
		if (state == GameState.TELEPORTTOARENA || state == GameState.START) {
			ActivePlayer p = players.get(players.indexOf(new Participant(event.getFighter())));
			
			try {
				p.destroyCage();
			}
			catch (IllegalStateException ex) {
				// no cage existing: ignore error
			}
		}
		if (state != GameState.INGAME)
			return;
			
		String msg = Language.get("info.player-left"); // event.getFighter().getName() + " hat das Spiel verlassen.";	
		String msg2 = Language.get("info.remaining-players").replaceAll("%player%", players.size() + "");
		
		sendMessageToAll(msg);
		sendMessageToAll(msg2);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCannonFighterJoin(CannonFighterJoinGameEvent event) {
		if (event.isCancelled())
			return;
		
		if (!event.getPlayer().hasPermission("cannonfight.join") && !event.getPlayer().hasPermission("cannonfight.*")) {
			event.getPlayer().sendMessage(Language.get("error.no-permission"));
			event.setCancelled(true);
			return;
		}
		
		if (event.getGame() == this && !event.isCancelled()) {
			String msg = Language.get("info.player-join-others").replaceAll("%player%", event.getPlayer().getName());
			sendMessageToAll(msg);
		}
	}
	
	public String getPosition() {
		return "CannonFight" + (state == GameState.INGAME ? " in der Arena " + arena.getPosition() : "");
	}

	public boolean start() {
		if (state != GameState.TELEPORTTOARENA)
			return false;
		
		nextCountdown();
		
		countdownTask = new BukkitRunnable() {
			
			@Override
			public void run() {
				countdown();
			}
		}.runTaskTimer(CannonFight.PLUGIN, 0, 20);
		
		return true;
	}

	public Arena getArena() {
		return this.arena;
	}

	public boolean leave(CannonFighter c) {
		Participant part = new Participant(c);
		if (players.contains(part)) {
			// normal player: leave event
			Bukkit.getServer().getPluginManager().callEvent(new CannonFighterLeaveEvent(this, c));
			
			// spectators are hidden for normal players
			for (Spectator s : spectators)
				c.getPlayer().showPlayer(s.getPlayer().getPlayer());
		}
		else if (!spectators.contains(part))
			// not player and not specator: not in game -> cannot leave
			return false;
		
		// normal player or spectator
		c.leaveGame();
		c.resetCannons();
		
		Player p = c.getPlayer();
		
		p.setMaxHealth(20);
		p.setHealth(p.getPlayer().getMaxHealth());
		p.setFoodLevel(20);
		
		p.getInventory().clear();
			
		return true;
	}
	
	public void addBlocksToRegenerate(List<Block> blocks) {
		for (Block b : blocks) {
			destroyedBlocks.add(new ModifiedBlock(b));	
		}
	}
	
	/*
	 * The regeneration uses desht's MassBlockUpdate class,
	 * using direct chunk access and consolidated lighting recalculation
	 * 
	 * Thanks for that!
	 */
	@SuppressWarnings("deprecation")
	private void resetArena() {
		// reset spawns
		arena.resetSpawns();
		
		if (destroyedBlocks.size() == 0)
			// no regeneration needed
			return;
		
		MassBlockUpdate mbu = new MassBlockUpdate(CannonFight.PLUGIN, this.arena.getLowerBound().getWorld());
		
		mbu.setRelightingStrategy(RelightingStrategy.DEFERRED);
		mbu.setMaxRelightTimePerTick(2, TimeUnit.MILLISECONDS);

		for (ModifiedBlock b : destroyedBlocks) {
			Location l = b.getLocation();
			mbu.setBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ(), b.getMaterial().getId(), b.getData());
		}

		mbu.notifyClients();
		
		destroyedBlocks.clear();
	}

	public boolean locIsInArena(Location loc) {
		Location lower = arena.getLowerBound();
		Location upper = arena.getUpperBound();
		return loc.getBlockX() >= lower.getBlockX() && loc.getBlockX() <= upper.getBlockX()
				&& loc.getBlockY() >= lower.getBlockY() && loc.getBlockY() <= upper.getBlockY()
				&& loc.getBlockZ() >= lower.getBlockZ() && loc.getBlockZ() <= upper.getBlockZ();
	}
	
	private void sendMessageToAll(String msg) {
		for (ActivePlayer p : players) {
			p.getPlayer().sendMessage(msg);
		}
		for (Spectator p : spectators) {
			p.getPlayer().sendMessage(msg);
		}
	}
}

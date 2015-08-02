package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.api.game.CannonFighterDeathEvent;
import de.pesacraft.cannonfight.api.game.CannonFighterJoinGameEvent;
import de.pesacraft.cannonfight.api.game.GameOverEvent;
import de.pesacraft.cannonfight.data.players.CannonFighter;

public class Game implements Listener {
	private Arena arena;
	private Map<CannonFighter, Role> participants;
	private GameState state;
	private int players;
	private int time;
	
	private BukkitTask countdownTask;
	
	protected Game(Arena arena) {
		this.arena = arena;
		this.state = GameState.TELEPORTTOARENA;
		this.participants = new HashMap<CannonFighter, Role>();
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}
	
	public boolean addPlayer(CannonFighter p) {
		if (state != GameState.TELEPORTTOARENA)
			return false;

		if (players == getMaxPlayers())
			return false;
	
		CannonFighterJoinGameEvent event = new CannonFighterJoinGameEvent(this, p);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return false;
		
		arena.teleport(p, this);
		
		p.sendMessage(Language.get("info.player-join"));
		
		for (Entry<CannonFighter, Role> entry : participants.entrySet()) {
			entry.getKey().show(p);
			entry.getKey().sendMessage(Language.get("info.player-join-others"));
			switch (entry.getValue()) {
			case PLAYER:
				p.show(entry.getKey());
				break;
			case SPECTATOR:
				p.hide(entry.getKey());
				break;
			}
		}
		
		participants.put(p, Role.PLAYER);
		players++;
		
		if (players == arena.getRequiredPlayers())
			start();
		
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
		arena.teleportSpectator(p, this);
		
		for (Entry<CannonFighter, Role> entry : participants.entrySet()) {
			p.show(entry.getKey());
			switch (entry.getValue()) {
			case PLAYER:
				entry.getKey().hide(p);
				break;
			case SPECTATOR:
				entry.getKey().show(p);
				break;
			}
		}
		
		participants.put(p, Role.SPECTATOR);

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
		switch (state) {
		case START:
			if (time == 0) {
				state = GameState.INGAME;
				nextCountdown();
				for (CannonFighter c : participants.keySet()) {
					c.sendMessage(Language.get("info.game-start")); // ChatColor.GREEN + "LOS!"
				}
				break;	
			}
			for (CannonFighter c : participants.keySet()) {
				c.sendMessage(Language.get("info.game-start-countdown")); // ChatColor.AQUA + "Noch " + time + " Sekunden bis zum Start!"
			}
			
			break;
		case INGAME:
			if (time == 0) {
				state = GameState.GAMEOVER;
				nextCountdown();
				
				List<CannonFighter> winner = new ArrayList<CannonFighter>();
				
				for (Entry<CannonFighter, Role> entry : participants.entrySet())
					if (entry.getValue() == Role.PLAYER)
						winner.add(entry.getKey());
				
				Bukkit.getServer().getPluginManager().callEvent(new GameOverEvent(this, winner));
				
				break;	
			}
			// jede Minute uebrige Zeit, wenn weniger als 1 min alle 15 sek und die letzten 15 sek.
			if (time % 60 == 0) {
				for (CannonFighter c : participants.keySet()) {
					c.sendMessage(Language.get("info.game-remaining-min")); // ChatColor.AQUA + "Noch " + time % 60 + " Minuten bis zum Ende!"
				}
			}
			if ((time < 60 && time % 15 == 0) || (time < 15)) {
				for (CannonFighter c : participants.keySet()) {
					c.sendMessage(Language.get("info.game-remaining-sec")); // ChatColor.AQUA + "Noch " + time + " Sekunden bis zum Ende!"
				}	
			}
			
			break;
		case GAMEOVER:
			if (time == 0) {
				state = GameState.TELEPORTBACK;
				countdownTask.cancel();

				for (CannonFighter c : participants.keySet()) {
					c.getUser().leave();
				}
			}
			break;
		default:
			break;
		}
		time--;	
	}

	private void nextCountdown() {
		switch (state) {
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
		if (participants.get(c) != Role.PLAYER)
			return false;
		
		participants.remove(c);
		players--;
		
		if (players == 1) {
			for (Entry<CannonFighter, Role> entry : participants.entrySet()) {
				if (entry.getValue() == Role.PLAYER) {
					// spieler der das spiel gewonnen hat
					state = GameState.GAMEOVER;
					nextCountdown();
					
					List<CannonFighter> winner = new ArrayList<CannonFighter>();
					
					winner.add(entry.getKey());
					
					Bukkit.getServer().getPluginManager().callEvent(new GameOverEvent(this, winner));
					
					break;
				}
			}
		}
		return true;	
	}
	
	public boolean removeSpectator(CannonFighter c) {
		if (participants.get(c) != Role.SPECTATOR)
			return false;
		
		participants.remove(c);
		
		return true;
		
	}
	
	public int getMaxPlayers() {
		return arena.getMaxPlayers();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		CannonFighter victim = CannonFighter.get(p);
		CannonFighter killer = p.getKiller() != null ? CannonFighter.get(p.getKiller()) : null;
		if (participants.get(victim) == Role.PLAYER) {
			removePlayer(victim);
			addSpectator(victim);
			
			// player death => reward
			Bukkit.getServer().getPluginManager().callEvent(new CannonFighterDeathEvent(this, victim, killer));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		CannonFighter c = CannonFighter.get(event.getPlayer());
		
		if (participants.get(c) == Role.PLAYER) {
			c.use(event.getItem());
		}
	}
	
	@EventHandler
	public void onGameOver(GameOverEvent event) {
		for (Entry<CannonFighter, Role> entry : participants.entrySet()) {
			entry.getKey().sendMessage(Language.get("info.game-over")); // ChatColor.RED + "Spiel vorbei!"
		}
		
		for (CannonFighter c : event.getWinner()) {
			c.sendMessage(Language.get("info.game-won"));
			c.giveCoins(CannonFight.PLUGIN.getConfig().getInt("game.reward"));
		}
	}
	
	@EventHandler
	public void onCannonFighterDeath(CannonFighterDeathEvent event) {
		if (event.getGame() == this) {
			String msg;
			
			if (event.getKiller() != null) {
				// von spieler getoetet
				msg = Language.get("info.player-died-killed-by-other"); // event.getVictim().getName() + " wurde von " + event.getKiller().getName() + " getötet."
				event.getKiller().sendMessage(Language.get("info.player-killed"));
				event.getVictim().sendMessage(Language.get("info.player-got-killed"));
			}
			else {
				// anderer todes grund
				msg = Language.get("info.player-died"); // event.getVictim().getName() + " ist gestorben.";	
				event.getVictim().sendMessage(Language.get("info.player-died-self"));
			}
			
			for (CannonFighter c : participants.keySet()) {
				c.sendMessage(msg);
				c.sendMessage(Language.get("info.remaining-players")); // players + " Spieler übrig!"
			}
		}
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
			for (CannonFighter c : participants.keySet()) {
				c.sendMessage(Language.get("info.player-joined")); // players + " Spieler übrig!"
			}
		}
	}
	public String getPosition() {
		return "CannonFight" + (state == GameState.INGAME ? " in der Arena " + arena.getPosition() : "");
	}

	public boolean start() {
		if (state != GameState.TELEPORTTOARENA)
			return false;
		
		state = GameState.START;
		
		nextCountdown();
		
		countdownTask = new BukkitRunnable() {
			
			@Override
			public void run() {
				countdown();
			}
		}.runTaskTimer(CannonFight.PLUGIN, 0, 20);
		
		return true;
	}
}

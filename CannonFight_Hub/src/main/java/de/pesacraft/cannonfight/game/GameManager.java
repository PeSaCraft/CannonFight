package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.defaults.GameRuleCommand;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.data.players.CannonFighter;

public class GameManager {
	private static Set<GameManager> games = new HashSet<GameManager>();
	
	public static GameManager getForArena(Arena a) {
		for (GameManager g : games) {
			if (g.arena == a) {
				return g;
			}
		}
		// gamemanager nicht gefunden
		GameManager g = new GameManager(a);
		games.add(g);
		return g;
	}
	
	public static boolean addPlayer(Arena a, CannonFighter c) {
		for (GameManager g : games) {
			if (g.arena == a) {
				// die arena
				return g.addPlayer(c);
			}
		}
		
		// arena nicht gefunden!
		GameManager g = new GameManager(a);
		games.add(g);
		return g.addPlayer(c);
	}

	private Arena arena;
	private Game game;
	private List<CannonFighter> queue;
	private Set<Sign> joinSigns;
	
	private GameManager(Arena arena) {
		this.arena = arena;
		this.queue = new ArrayList<CannonFighter>();
		this.joinSigns = new HashSet<Sign>();
	}
	
	public boolean addPlayer(CannonFighter c) {
		if (!isGameRunning())
			// kein spiel vorhanden
			return false;
		return game.addPlayer(c);
	}

	public boolean addToQueue(CannonFighter c) {
		if (queue.add(c)) {
			c.setInQueue(arena);
			
			startGame(false);
			return true;
		}
		
		// bereits in der Liste
		return false;	
	}
	public boolean addSpectator(CannonFighter c) {
		if (!isGameRunning())
			// kein spiel vorhanden
			return false;
		return game.addSpectator(c);
	}
	
	public boolean leaveQueue(CannonFighter c) {
		if (queue.remove(c)) {
			// in der Liste gewesen
			c.leaveQueue();
			return true;
		}
		// nicht in der Liste
		return false;
	}
	
	public boolean startGame(boolean force) {
		if (!isGameRunning()) {
			if (force || queue.size() == arena.getRequiredPlayers()) {
				game = new Game(arena);
				
				Iterator<CannonFighter> it = queue.iterator();
				
				while (it.hasNext()) {
					CannonFighter c = it.next();
					
					if (!game.addPlayer(c))
						// spieler konnte nicht beitreten
						break;
					
					// spieler beigetreten
					it.remove();
					c.setInQueue(null);
				}
				updateSigns();
				return game.start();
			}
		}
		// spiel läuft bereits
		return false;
	}
	
	
	public boolean isGameRunning() {
		return game != null;
	}

	public static void gameOver(Game game) {
		GameManager man = getForArena(game.getArena());
		
		if (man.game == game)
			// that is the game of that gamemanager
			// remove it and make space for a new one
			man.game = null;
	}
	
	public int getQueueSize() {
		return queue.size();
	}

	public void addJoinSign(Sign sign) {
		joinSigns.add(sign);
	}

	public void updateSigns() {
		// update sign afterwards, something could have changed
		Bukkit.getScheduler().runTaskLater(CannonFight.PLUGIN, new Runnable() {
					
			@Override
			public void run() {
				for (Sign sign : joinSigns) {
					sign.setLine(3, ChatColor.GREEN + "" + getQueueSize() + " Spieler wartend");
					sign.update();
				}
			}
		}, 1);
	}
}

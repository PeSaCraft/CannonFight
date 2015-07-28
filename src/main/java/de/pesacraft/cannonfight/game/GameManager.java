package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class GameManager {
	private static List<GameManager> games;
	
	public static void addPlayer(Arena a, CannonFighter c) {
		for (GameManager g : games) {
			if (g.arena == a) {
				// die arena
				g.addPlayer(c);
				return;
			}
		}
		
		// arena nicht gefunden!
		GameManager g = new GameManager(a);
		g.addPlayer(c);
		games.add(g);
	}

	private Arena arena;
	private Game game;
	private List<CannonFighter> queue;
	
	private GameManager(Arena arena) {
		this.arena = arena;
		this.queue = new ArrayList<CannonFighter>();
	}
	
	public void addPlayer(CannonFighter c) {
		if (game != null) {
			// es gibt ein spiel
			if (game.addPlayer(c))
				// spieler zum spiel hinzugefuegt
				return;
			else
				// kein platz -> warteschlange
				queue.add(c);
		}
		else {
			queue.add(c);
			
			if (queue.size() >= arena.getRequiredPlayers())
				createGame();
		}	
	}
	
	private void createGame() {
		game = new Game(arena);
		
		int added = game.addPlayers(queue.toArray(new CannonFighter[0]));
		
		queue.subList(added, queue.size());
	}

	public static GameManager getForArena(Arena a) {
		for (GameManager g : games) {
			if (g.arena == a) {
				return g;
			}
		}
		return null;
	}

	public boolean start() {
		if (game == null)
			return false;
		
		return game.start();
	}
	
}

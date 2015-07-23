package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class GameManager {
	private static List<Game> currentGames = new ArrayList<Game>();
	private static Map<Arena, List<CannonFighter>> waiting = new HashMap<Arena, List<CannonFighter>>();
	
	public static Game createGame(Arena arena, List<CannonFighter> players) {
		Game g = new Game(arena);
		
		g.addPlayers(players.toArray(new CannonFighter[0]));
		
		currentGames.add(g);
		
		return g;
	}
	
	public static void addPlayer(Arena a, CannonFighter c) {
		List<CannonFighter> list = waiting.remove(a);
		
		if (list == null)
			list = new ArrayList<CannonFighter>();
		
		list.add(c);
		
		if (list.size() >= a.getRequiredPlayers())
			createGame(a, list);
		else
			waiting.put(a, list);
	}
}

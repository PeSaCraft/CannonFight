package de.pesacraft.cannonfight.api.game;

import java.util.List;
import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Game;

public class GameOverEvent extends Event {
	
	private final Game game;
	private final Set<CannonFighter> winner;
	
	public GameOverEvent(Game game, Set<CannonFighter> winner) {
		this.game = game;
		this.winner = winner;
	}
	
	public Game getGame() {
		return game;
	}
	
	public Set<CannonFighter> getWinner() {
		return winner;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}

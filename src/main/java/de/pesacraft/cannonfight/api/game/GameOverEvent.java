package de.pesacraft.cannonfight.api.game;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Game;

public class GameOverEvent extends Event {
	
	private final Game game;
	private final List<CannonFighter> winner;
	
	public GameOverEvent(Game game, List<CannonFighter> winner) {
		this.game = game;
		this.winner = winner;
	}
	
	public Game getGame() {
		return game;
	}
	
	public List<CannonFighter> getWinner() {
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

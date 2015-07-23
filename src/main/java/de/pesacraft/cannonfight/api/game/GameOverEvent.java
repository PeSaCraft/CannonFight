package de.pesacraft.cannonfight.api.game;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Game;

public class GameOverEvent extends Event {
	
	private final Game game;
	private final CannonFighter winner;
	
	public GameOverEvent(Game game, CannonFighter winner) {
		this.game = game;
		this.winner = winner;
	}
	
	public Game getGame() {
		return game;
	}
	
	public CannonFighter getWinner() {
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

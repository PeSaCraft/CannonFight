package de.pesacraft.cannonfight.game.api;

import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.util.CannonFighter;

public class GameOverEvent extends Event {
	
	private final Set<CannonFighter> winner;
	
	public GameOverEvent(Set<CannonFighter> winner) {
		this.winner = winner;
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

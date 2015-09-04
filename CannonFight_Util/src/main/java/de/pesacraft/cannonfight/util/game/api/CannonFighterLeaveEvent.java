package de.pesacraft.cannonfight.util.game.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.util.game.Game;
import de.pesacraft.cannonfight.util.CannonFighter;

public class CannonFighterLeaveEvent extends Event {

	private final Game game;
	private final CannonFighter fighter;
	
	public CannonFighterLeaveEvent(Game game, CannonFighter fighter) {
		this.game = game;
		this.fighter = fighter;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public CannonFighter getFighter() {
		return this.fighter;
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

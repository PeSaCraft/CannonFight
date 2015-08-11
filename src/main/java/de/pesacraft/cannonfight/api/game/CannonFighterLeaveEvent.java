package de.pesacraft.cannonfight.api.game;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Game;

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

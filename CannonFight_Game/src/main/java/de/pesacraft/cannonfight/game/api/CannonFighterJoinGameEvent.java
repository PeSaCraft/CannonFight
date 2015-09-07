package de.pesacraft.cannonfight.game.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.util.CannonFighter;

public class CannonFighterJoinGameEvent extends Event {

	private final CannonFighter player;
	
	public CannonFighterJoinGameEvent(CannonFighter player) {
		this.player = player;
	}
	
	public CannonFighter getPlayer() {
		return this.player;
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

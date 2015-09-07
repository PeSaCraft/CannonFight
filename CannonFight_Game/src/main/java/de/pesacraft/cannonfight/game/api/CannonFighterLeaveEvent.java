package de.pesacraft.cannonfight.game.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.util.CannonFighter;

public class CannonFighterLeaveEvent extends Event {

	private final CannonFighter fighter;
	
	public CannonFighterLeaveEvent(CannonFighter fighter) {
		this.fighter = fighter;
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

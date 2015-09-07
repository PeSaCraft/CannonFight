package de.pesacraft.cannonfight.game.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.util.CannonFighter;

public class CannonFighterDeathEvent extends Event {

	private final CannonFighter victim;
	private final CannonFighter killer;
	private final boolean outOfGame;
	
	public CannonFighterDeathEvent(CannonFighter victim, CannonFighter killer, boolean outOfGame) {
		this.victim = victim;
		this.killer = killer;
		this.outOfGame = outOfGame;
	}

	public CannonFighter getVictim() {
		return this.victim;
	}
	
	public CannonFighter getKiller() {
		return this.killer;
	}
	
	public boolean isOutOfGame() {
		return outOfGame;
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

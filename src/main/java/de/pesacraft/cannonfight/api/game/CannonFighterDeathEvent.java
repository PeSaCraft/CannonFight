package de.pesacraft.cannonfight.api.game;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Game;

public class CannonFighterDeathEvent extends Event {

	private final Game game;
	private final CannonFighter victim;
	private final CannonFighter killer;
	
	public CannonFighterDeathEvent(Game game, CannonFighter victim, CannonFighter killer) {
		this.game = game;
		this.victim = victim;
		this.killer = killer;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public CannonFighter getVictim() {
		return this.victim;
	}
	
	public CannonFighter getKiller() {
		return this.killer;
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

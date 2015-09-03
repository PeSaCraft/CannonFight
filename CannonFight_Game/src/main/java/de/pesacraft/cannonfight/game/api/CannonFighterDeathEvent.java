package de.pesacraft.cannonfight.game.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.game.Game;
import de.pesacraft.cannonfight.game.players.CannonFighter;

public class CannonFighterDeathEvent extends Event {

	private final Game game;
	private final CannonFighter victim;
	private final CannonFighter killer;
	private final boolean outOfGame;
	
	public CannonFighterDeathEvent(Game game, CannonFighter victim, CannonFighter killer, boolean outOfGame) {
		this.game = game;
		this.victim = victim;
		this.killer = killer;
		this.outOfGame = outOfGame;
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

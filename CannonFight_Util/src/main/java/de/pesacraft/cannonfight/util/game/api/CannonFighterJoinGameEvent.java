package de.pesacraft.cannonfight.util.game.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.pesacraft.cannonfight.util.game.Game;
import de.pesacraft.cannonfight.util.CannonFighter;

public class CannonFighterJoinGameEvent extends Event implements Cancellable {

	private final Game game;
	private final CannonFighter player;
	
	private boolean cancelled;
	
	public CannonFighterJoinGameEvent(Game game, CannonFighter player) {
		this.game = game;
		this.player = player;
		
		this.cancelled = false;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public CannonFighter getPlayer() {
		return this.player;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
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

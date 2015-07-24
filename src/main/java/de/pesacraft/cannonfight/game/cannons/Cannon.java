package de.pesacraft.cannonfight.game.cannons;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public abstract class Cannon extends Cooldown {
	
	public Cannon(int time) {
		super(time);
	}

	public abstract ItemStack getItem();
	
	public abstract String getName();
	
	public abstract int getMaxAmmo();
	
	public abstract boolean fire(CannonFighter player) throws Exception;
	
	public abstract boolean hasAmmo();
}

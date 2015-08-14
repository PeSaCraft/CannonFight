package de.pesacraft.cannonfight.game.cannons;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.shop.Shop;

public abstract class Cannon extends Cooldown {
	private CannonConstructor constructor;
	
	public Cannon(int time, CannonConstructor constructor) {
		super(time);
		this.constructor = constructor;
	}
	
	public abstract ItemStack getItem();
	
	public abstract String getName();
	
	public abstract int getMaxAmmo();
	
	public abstract boolean fire() throws Exception;
	
	public abstract boolean hasAmmo();
	
	public CannonConstructor getConstructor() {
		return constructor;
	}
}

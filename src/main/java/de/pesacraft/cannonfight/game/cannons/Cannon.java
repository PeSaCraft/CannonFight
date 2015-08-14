package de.pesacraft.cannonfight.game.cannons;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.shop.Shop;

public abstract class Cannon extends Cooldown {
	public Cannon(int time) {
		super(time);
	}
	
	public abstract ItemStack getItem();
	
	public abstract String getName();
	
	public abstract int getMaxAmmo();
	
	public abstract boolean fire();
	
	public abstract boolean hasAmmo();
	
	public abstract CannonConstructor getCannonConstructor();
}

package de.pesacraft.cannonfight.util.cannons;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cooldown;

public abstract class Cannon extends Cooldown {
	public Cannon(int time) {
		super(time);
	}
	
	public abstract ItemStack getItem();
	
	public abstract String getName();
	
	public abstract int getMaxAmmo();
	
	public abstract boolean fire(ItemStack item);
	
	public abstract boolean hasAmmo();
	
	public abstract CannonConstructor getCannonConstructor();
	
	public abstract void openShop();

	public abstract void reset();
}

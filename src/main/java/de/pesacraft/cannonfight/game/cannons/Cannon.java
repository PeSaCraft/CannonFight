package de.pesacraft.cannonfight.game.cannons;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public abstract class Cannon {
	
	public abstract ItemStack getItemStack();
	
	public abstract String getName();
	
	public abstract int getMaxAmmo();
	
	public abstract int getCooldownTime();
	
	public abstract void fire(CannonFighter player) throws Exception;
	
}

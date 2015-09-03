package de.pesacraft.cannonfight.game.cannons;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.game.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;

public interface CannonConstructor {
	public boolean canConstruct(String name);
	
	public Cannon construct(CannonFighter fighter, Map<String, Object> map);

	public Cannon buyNew(CannonFighter fighter);
	
	public int getPrice();
	
	public ItemStack getItem();
}

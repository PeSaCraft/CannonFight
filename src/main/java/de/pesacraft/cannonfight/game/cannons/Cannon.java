package de.pesacraft.cannonfight.game.cannons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.exceptions.player.NotEnoughAmmoException;

public abstract class Cannon implements ConfigurationSerializable, Cloneable {
	/**
	 * ItemStack representing the cannon ingame
	 */
	protected static ItemStack item;
	/**
	 * The cannons cooldowns
	 */
	protected static Cooldowns cooldowns;
	/**
	 * 
	 */
	protected static long neededAmmo;
	/**
	 * The cannons name
	 */
	protected static String name;
	
	public Cannon(Map<String, Object> map) {
		item = (ItemStack) map.get("item");
		cooldowns = (Cooldowns) map.get("cooldowns");
		neededAmmo = (Long) map.get("needed ammo");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("item", item);
		map.put("cooldowns", cooldowns);
		map.put("needed ammo", neededAmmo);
		return map;
	}
	
	public Cannon clone() {
		return new Cannon(this.serialize());
	}

	public ItemStack getItemStack() {
		return item;
	}
	
	public abstract void fire(CannonFighter player) throws Exception;

	public String getName() {
		return name;
	}
}

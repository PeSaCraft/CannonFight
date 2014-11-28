package de.froznmine.cannonfight.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class Cannon implements ConfigurationSerializable, Cloneable {
	private ItemStack item;
	private long cooldown;
	
	public Cannon(Map<String, Object> map) {
		item = (ItemStack) map.get("item");
		cooldown = (Long) map.get("cooldown");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("item", item);
		map.put("cooldown", cooldown);
		return null;
	}
	
	public Cannon clone() {
		return new Cannon(this.serialize());
	}
}

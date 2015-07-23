package de.pesacraft.cannonfight.game.cannons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Cooldown implements ConfigurationSerializable {
	/**
	 * The cooldowns time
	 */
	private final long time;
	/**
	 *  The cooldowns price
	 */
	private final double price;
	
	public Cooldown(Map<String, Object> map) {
		time = (Long) map.get("cooldown time");
		price = (Double) map.get("upgrade price");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("cooldown time", time);
		map.put("upgrade price", price);
		
		return map;
	}
	
}

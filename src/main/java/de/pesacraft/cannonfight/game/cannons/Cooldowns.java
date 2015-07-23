package de.pesacraft.cannonfight.game.cannons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Cooldowns  implements ConfigurationSerializable {
	/**
	 * The cooldowns with their levels
	 */
	private final Map<Integer, Cooldown> cooldowns;
	
	@SuppressWarnings("unchecked")
	public Cooldowns(Map<String, Object> map) {
		cooldowns = (Map<Integer, Cooldown>) map.get("level");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("level", cooldowns);
		
		return map;
	}
	
	
	
}

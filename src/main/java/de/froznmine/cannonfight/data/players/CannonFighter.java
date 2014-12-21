package de.froznmine.cannonfight.data.players;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.froznmine.cannonfight.CannonFight;
import de.froznmine.lobby.game.GameUser;

public class CannonFighter {
	private UUID uuid;
	private Map<String, Integer> cannonLevels;
	private double coins;
	private List<String> selectedCannons;
	
	public CannonFighter(GameUser p) {
		uuid = p.getUUID();
		
		File file = new File(CannonFight.PLUGIN.getDataFolder() + "players/" + uuid + ".yml");
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		Set<String> keys = config.getConfigurationSection("cannons").getKeys(false);
		
		for (String key : keys)
			cannonLevels.put(key, config.getInt(key));
		
		coins = config.getDouble("coins");
		
		selectedCannons = config.getStringList("selected");
	}

}

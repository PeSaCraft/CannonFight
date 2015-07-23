package de.pesacraft.cannonfight.game;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.pesacraft.lobbysystem.game.GameUser;
import de.pesacraft.lobbysystem.game.arena.Arena;
import de.pesacraft.lobbysystem.util.FileBundle;
import de.pesacraft.lobbysystem.util.Schematic;

public class Arena implements Arena {
	private static final long serialVersionUID = 4698771078013639643L;

	private final Schematic schematic;
	private final String name;
	private final int maxPlayers;
	private final int requiredPlayers;
	
	public static void main(String[] args) {
		Map<String, FileInputStream> files = new HashMap<String, FileInputStream>();
		
		try {
			files.put("world.schem", new FileInputStream("/Users/darion/Minecraft-Server aktuellste/MCVanilla_29995_1227/plugins/SkyWars/4_Biome.schematic"));
			files.put("config.yml", new FileInputStream("/Users/darion/Minecraft-Server aktuellste/MCVanilla_29995_1227/plugins/SkyWars/config.yml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		FileBundle.zipFile("/Users/darion/testarena.cfa", files);
	}
	
	@SuppressWarnings("deprecation")
	public Arena(File baseFile) throws IOException {
		Map<String, ByteArrayInputStream> files = FileBundle.unzipFile(baseFile);
		
		this.schematic = Schematic.loadSchematic(files.get("world.schem"));
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(files.get("config.yml"));
		// load stuff
		this.name = config.getString("name");
		this.maxPlayers = config.getInt("maximum players");
		this.requiredPlayers = config.getInt("required players to start");
	}
	
	@Override
	public boolean load(Location loc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("schematic", name);
		map.put("maxPlayers", maxPlayers);
		
		return map;
	}

	@Override
	public boolean teleport(GameUser player) {
		
		return false;
	}

	@Override
	public int getRequiredPlayers() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toString() {
		String s = "###Arena### Game: CannonFight, ";
		s += "Name: " + name + ", ";
		s += "ReqPlayers: " + requiredPlayers + ", ";
		s += "MaxPlayers: " + maxPlayers;
		
		return s;
	}

	@Override
	public String getPosition() {
		return name;
	}
}

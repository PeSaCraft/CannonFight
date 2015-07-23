package de.pesacraft.cannonfight.game;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.Database;
import de.pesacraft.lobbysystem.util.FileBundle;
import de.pesacraft.lobbysystem.util.Schematic;

public class Arena {
	private final String name;
	private final Location loc1;
	private final Location loc2;
	private final Location spectatorSpawn;
	private final int requiredPlayers;
	private final Map<CannonFighter, Location> spawns;
	private final List<Location> freeSpawns;
	
	public Arena(String name) throws IOException, SQLException {
		this.name = name;
		
		ResultSet result = Database.execute("SELECT * FROM " + Database.getTablePrefix() + "arenas WHERE name = " + name);
		
		requiredPlayers = result.getInt("requiredPlayers");
		int id = result.getInt("id");
		
		World world = Bukkit.getWorld(result.getString("world"));
		loc1 = new Location(world, result.getInt("x1"), result.getInt("y1"), result.getInt("z1"));
		loc2 = new Location(world, result.getInt("x2"), result.getInt("y2"), result.getInt("z2"));
		spectatorSpawn = new Location(world, result.getInt("spectatorX"), result.getInt("spectatorY"), result.getInt("spectatorZ"));
		
		result = Database.execute("SELECT * FROM " + Database.getTablePrefix() + "spawns WHERE id =" + id);
		
		spawns = new HashMap<CannonFighter, Location>();
		freeSpawns = new ArrayList<Location>();
		do {
			Location loc = new Location(world, result.getDouble("x"), result.getDouble("y"), result.getDouble("z"), result.getFloat("yaw"), result.getFloat("pitch"));
			
			freeSpawns.add(loc);
			
			result.next();
		} while (!result.isAfterLast());
	}
	
	public boolean load(Location loc) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getMaxPlayers() {
		return this.spawns.size();
	}


	public boolean teleport(CannonFighter c) {
		if (freeSpawns.isEmpty())
			return false;
		
		Location loc = freeSpawns.remove(0);
		spawns.put(c, loc);
		c.teleportToGame(loc);
		return true;
	}
	
	public void teleportSpectator(CannonFighter c) {
		c.teleportToGame(spectatorSpawn);
	}

	public int getRequiredPlayers() {
		return requiredPlayers;
	}
	
	@Override
	public String toString() {
		String s = "###Arena### Game: CannonFight, ";
		s += "Name: " + name + ", ";
		s += "ReqPlayers: " + requiredPlayers + ", ";
		s += "MaxPlayers: " + maxPlayers;
		
		return s;
	}

	public String getPosition() {
		return name;
	}
}

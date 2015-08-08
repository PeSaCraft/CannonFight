package de.pesacraft.cannonfight.game;

import static com.mongodb.client.model.Filters.eq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.MongoDatabase;
import de.pesacraft.lobbysystem.user.User;

public class Arena {
	private static final MongoCollection<Document> COLLECTION;
	
	private final String name;
	private final Location loc1;
	private final Location loc2;
	private final Location spectatorSpawn;
	private final int requiredPlayers;
	private final Map<CannonFighter, Location> spawns;
	private final List<Location> freeSpawns;
	
	static {
		COLLECTION = Collection.ARENAS();
	}
	
	@SuppressWarnings("unchecked")
	public Arena(String name) throws IllegalArgumentException {
		this.name = name;
		
		Document doc = COLLECTION.find(eq("name", name)).first();
		
		if (doc == null)
			throw new IllegalArgumentException(Language.get("error.arena-does-not-exist"));
		
		requiredPlayers = doc.getInteger("requiredPlayers");
		
		World world = Bukkit.getWorld(doc.getString("world"));
		
		Document docLoc = (Document) doc.get("loc1");
		loc1 = new Location(world, docLoc.getInteger("x"), docLoc.getInteger("y"), docLoc.getInteger("z"));
		
		docLoc = (Document) doc.get("loc2");
		loc2 = new Location(world, docLoc.getInteger("x"), docLoc.getInteger("y"), docLoc.getInteger("z"));
		
		docLoc = (Document) doc.get("spectatorSpawn");
		spectatorSpawn = new Location(world, docLoc.getDouble("x"), docLoc.getDouble("y"), docLoc.getDouble("z"), docLoc.getDouble("yaw").floatValue(), docLoc.getDouble("pitch").floatValue());
		
		List<Document> spawnList = (List<Document>) doc.get("spawns");
		
		freeSpawns = new ArrayList<Location>();
		
		for (Document spawn : spawnList) {
			freeSpawns.add(new Location(world, spawn.getDouble("x"), spawn.getDouble("y"), spawn.getDouble("z"), spawn.getDouble("yaw").floatValue(), spawn.getDouble("pitch").floatValue()));
		}
		
		spawns = new HashMap<CannonFighter, Location>();
	}

	public int getMaxPlayers() {
		return this.spawns.size();
	}


	public boolean teleport(CannonFighter c, Game game) {
		if (freeSpawns.isEmpty())
			return false;
		
		Location loc = freeSpawns.get(0);
		if (!c.teleportToGame(loc, game)) {
			return false;
		}
		freeSpawns.remove(0);
		spawns.put(c, loc);
		return true;
	}
	
	public void teleportSpectator(CannonFighter c, Game game) {
		c.teleportToGame(spectatorSpawn, game);
	}

	public int getRequiredPlayers() {
		return requiredPlayers;
	}
	
	@Override
	public String toString() {
		String s = "###Arena### Game: CannonFight, ";
		s += "Name: " + name + ", ";
		s += "ReqPlayers: " + requiredPlayers + ", ";
		s += "MaxPlayers: " + getMaxPlayers();
		
		return s;
	}

	public String getPosition() {
		return name;
	}
}

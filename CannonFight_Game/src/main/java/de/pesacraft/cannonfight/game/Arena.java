package de.pesacraft.cannonfight.game;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.CannonFighter;

public class Arena {
	private static final MongoCollection<Document> COLLECTION;
	
	private final String name;
	private final Location loc1;
	private final Location loc2;
	private final Location spectatorSpawn;
	private final int requiredPlayers;
	private final Map<CannonFighter, Location> setSpawns;
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
		
		World world = Bukkit.getWorld(name);
		
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
		
		setSpawns = new HashMap<CannonFighter, Location>();
	}

	public String getName() {
		return name;
	}
	
	public int getMaxPlayers() {
		return this.freeSpawns.size() + this.setSpawns.size();
	}

	public void resetSpawns() {
		for (Location l : setSpawns.values()) {
			freeSpawns.add(l);
		}
		
		setSpawns.clear();
	}
	
	public boolean teleport(CannonFighter c) {
		if (freeSpawns.isEmpty())
			return false;
		
		Location loc = freeSpawns.get(0);
		if (!c.teleport(loc))
			return false;
		
		freeSpawns.remove(0);
		setSpawns.put(c, loc);
		return true;
	}
	
	public boolean teleportSpectator(CannonFighter c) {
		return c.teleport(spectatorSpawn);
	}

	public int getRequiredPlayers() {
		return requiredPlayers;
	}
	
	public Location getLowerBound() {
		return loc1;
	}
	
	public Location getUpperBound() {
		return loc2;
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

	public Location getSpawn(CannonFighter c) {
		return setSpawns.get(c);
	}

	public Location getSpectatorLocation() {
		return spectatorSpawn;
	}

	public void randomRespawn(Player p) {
		int i = (int) (Math.random() * getMaxPlayers());
		
		int free = freeSpawns.size();
		if (i < free)
			p.teleport(freeSpawns.get(i));
		else {
			i -= free;
			Iterator<Location> locs = setSpawns.values().iterator();
			
			Location loc = null;
		
			while (i >= 0) {
				loc = locs.next();
				i--;
			}
			
			if (loc != null)
				p.teleport(loc);
		}
	}
}

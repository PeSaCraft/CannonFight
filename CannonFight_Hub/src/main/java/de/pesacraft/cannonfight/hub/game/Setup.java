package de.pesacraft.cannonfight.hub.game;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.Collection;
import static com.mongodb.client.model.Filters.*;

public class Setup {
	private static final MongoCollection<Document> COLLECTION;
	
	private String name;
	
	private int reqPlayers;
	
	private Location specSpawn;
	
	private List<Location> spawns;
	
	static {
		COLLECTION = Collection.ARENAS();
	}
	
	private Setup(String name) {
		this.name = name;
		this.reqPlayers = 0;
		this.specSpawn = null;
		this.spawns = new ArrayList<Location>();
		
		Document doc = new Document("name", name);
		COLLECTION.insertOne(doc);
	}
	
	public Setup(Document doc) {
		this.name = doc.getString("name");
		this.reqPlayers = doc.getInteger("requiredPlayers", 0);
		
		World world = Bukkit.getWorld(name);
		Document docLoc = (Document) doc.get("spectatorSpawn");
		if (docLoc != null)
			this.specSpawn = new Location(world, docLoc.getDouble("x"), docLoc.getDouble("y"), docLoc.getDouble("z"), docLoc.getDouble("yaw").floatValue(), docLoc.getDouble("pitch").floatValue());
		
		List<Document> spawnList = (List<Document>) doc.get("spawns");
		
		this.spawns = new ArrayList<Location>();
		
		if (spawnList != null)
			for (Document spawn : spawnList)
				spawns.add(new Location(world, spawn.getDouble("x"), spawn.getDouble("y"), spawn.getDouble("z"), spawn.getDouble("yaw").floatValue(), spawn.getDouble("pitch").floatValue()));
	}

	public String getName() {
		return this.name;
	}
	
	public boolean setRequiredPlayers(int amount) {
		if (amount <= 1)
			return false;
		
		this.reqPlayers = amount;
		
		COLLECTION.updateOne(eq("name", name), new Document("$set", new Document("requiredPlayers", reqPlayers)));
		
		return true;
	}
	
	public boolean setSpectatorLocation(Location loc) {
		if (!loc.getWorld().getName().equals(name))
			return false;
		
		this.specSpawn = loc;

		Document doc = new Document("x", loc.getX());
		doc = doc.append("y", loc.getY());
		doc = doc.append("z", loc.getZ());
		doc = doc.append("yaw", loc.getYaw());
		doc = doc.append("pitch", loc.getPitch());
		
		COLLECTION.updateOne(eq("name", name), new Document("$set", new Document("spectatorSpawn", doc)));
		
		return true;
	}
	
	
	public boolean addSpawn(Location loc) {
		if (!loc.getWorld().getName().equals(name))
			return false;
		
		this.spawns.add(loc);
		
		Document doc = new Document("x", loc.getX());
		doc = doc.append("y", loc.getY());
		doc = doc.append("z", loc.getZ());
		doc = doc.append("yaw", loc.getYaw());
		doc = doc.append("pitch", loc.getPitch());
		
		COLLECTION.updateOne(eq("name", name), new Document("$push", new Document("spawns", doc)));
		
		return true;
	}

	public static Setup get(String name) {
		Document doc = COLLECTION.find(eq("name", name)).first();
		
		if (doc != null)
			// arena exists
			return new Setup(doc);
		else
			// arena new
			return new Setup(name);
	}
}

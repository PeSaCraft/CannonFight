package de.pesacraft.cannonfight.hub.game;

import org.bson.Document;
import org.bukkit.Location;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.Collection;
import static com.mongodb.client.model.Filters.*;

public class Setup {
	private static final MongoCollection<Document> COLLECTION;
	
	private String name;
	
	private Location loc1;
	private Location loc2;
	
	private int reqPlayers;
	
	private Location specLoc;
	
	private boolean added;
	
	private int id;
	
	static {
		COLLECTION = Collection.ARENAS();
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean setLocation1(Location loc) {
		if (added)
			return false;
		
		if (loc2 != null && loc.getWorld() != loc2.getWorld())
			return false;
		
		this.loc1 = loc;
		
		insert();
		return true;
	}
	
	public boolean setLocation2(Location loc) {
		if (added)
			return false;
		
		if (loc1 != null && loc.getWorld() != loc1.getWorld())
			return false;
		
		this.loc2 = loc;
		
		insert();
		return true;
	}
	
	public boolean setName(String name) {
		if (added)
			return false;
		
		this.name = name;
		
		insert();
		return true;
	}
	
	public boolean setRequiredPlayers(int amount) {
		if (added)
			return false;
		
		if (amount < 0)
			return false;
		
		this.reqPlayers = amount;
		
		insert();
		return true;
	}
	
	public boolean setSpectatorLocation(Location loc) {
		if (added)
			return false;
		
		if ((loc1 != null && loc.getWorld() != loc1.getWorld()) || (loc2 != null && loc.getWorld() != loc2.getWorld()))
			return false;
		
		this.specLoc = loc;
		
		insert();
		return true;
	}
	
	public boolean allSet() {
		return loc1 != null && loc2 != null && specLoc != null && name != null;
	}
	
	public void insert() {
		if (!allSet())
			return;
		
		if (added)
			return;
		
		added = true;
		
		Document doc = new Document("name", name);
		doc = doc.append("requiredPlayers", reqPlayers);
		
		// loc1 has lower, loc2 upper bounds
		int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
		int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
		int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
		int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		loc1 = new Location(loc1.getWorld(), x1, y1, z1);
		loc2 = new Location(loc2.getWorld(), x2, y2, z2);
		
		Document docLoc = new Document("x", loc1.getBlockX());
		docLoc = docLoc.append("y", loc1.getBlockY());
		docLoc = docLoc.append("z", loc1.getBlockZ());
		doc = doc.append("loc1", docLoc);
		
		docLoc = new Document("x", loc2.getBlockX());
		docLoc = docLoc.append("y", loc2.getBlockY());
		docLoc = docLoc.append("z", loc2.getBlockZ());
		doc = doc.append("loc2", docLoc);
		
		docLoc = new Document("x", specLoc.getX());
		docLoc = docLoc.append("y", specLoc.getY());
		docLoc = docLoc.append("z", specLoc.getZ());
		docLoc = docLoc.append("yaw", specLoc.getYaw());
		docLoc = docLoc.append("pitch", specLoc.getPitch());
		
		doc = doc.append("spectatorSpawn", docLoc);
		
		COLLECTION.insertOne(doc);
	}
	
	public boolean addSpawn(Location loc) {
		if (!added)
			return false;
		
		Document doc = new Document("x", loc.getX());
		doc = doc.append("y", loc.getY());
		doc = doc.append("z", loc.getZ());
		doc = doc.append("yaw", loc.getYaw());
		doc = doc.append("pitch", loc.getPitch());
		
		COLLECTION.updateOne(eq("name", name), new Document("$push", new Document("spawns", doc)));
		
		return true;
	}
}

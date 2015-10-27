package de.pesacraft.cannonfight.hub.game;

import org.bson.Document;
import org.bukkit.Location;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.Collection;
import static com.mongodb.client.model.Filters.*;

public class Setup {
	private static final MongoCollection<Document> COLLECTION;
	
	private String name;

	static {
		COLLECTION = Collection.ARENAS();
	}
	
	private Setup(String name) {
		this.name = name;

		Document doc = new Document("name", name);
		COLLECTION.insertOne(doc);
	}
	
	public Setup(Document doc) {
		this.name = doc.getString("name");
	}

	public String getName() {
		return this.name;
	}
	
	public boolean setRequiredPlayers(int amount) {
		if (amount <= 1)
			return false;
		
		COLLECTION.updateOne(eq("name", name), new Document("$set", new Document("requiredPlayers", amount)));
		
		return true;
	}
	
	public boolean setSpectatorLocation(Location loc) {
		if (!loc.getWorld().getName().equals(name))
			return false;
		
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

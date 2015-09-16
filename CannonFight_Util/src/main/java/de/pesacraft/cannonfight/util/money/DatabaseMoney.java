package de.pesacraft.cannonfight.util.money;

import static com.mongodb.client.model.Filters.eq;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Collection;

public class DatabaseMoney implements Money {

	private static final MongoCollection<Document> COLLECTION;
	
	static {
		COLLECTION = Collection.PLAYERS();
	}

	@Override
	public int getMoney(CannonFighter c) {
		return getMoney(c.getPlayer().getUniqueId());
	}

	@Override
	public boolean giveMoney(CannonFighter c, int amount, String... reason) {
		return giveMoney(c.getPlayer().getUniqueId(), amount, reason);
	}

	@Override
	public boolean takeMoney(CannonFighter c, int amount, String... reason) {
		return takeMoney(c.getPlayer().getUniqueId(), amount, reason);
	}

	@Override
	public boolean hasEnoughMoney(CannonFighter c, int amount) {
		return hasEnoughMoney(c.getPlayer().getUniqueId(), amount);
	}

	@Override
	public int getMoney(UUID uuid) {
		Document doc = COLLECTION.find(eq("uuid", uuid.toString())).first();
		
		if (doc.containsKey("coins"))
			// has coins
			return ((Number) doc.get("coins")).intValue();
		
		// no coins -> 0 coins, store that
		COLLECTION.updateOne(eq("uuid", uuid.toString()), new Document("$set", new Document("coins", 0)));
		return 0;
	}

	@Override
	public boolean setMoney(UUID uuid, int amount, String... reason) {
		if (amount < 0)
			return false;
		
		COLLECTION.updateOne(eq("uuid", uuid.toString()), new Document("$set", new Document("coins", amount)));
		
		return true;
	}

	@Override
	public boolean giveMoney(UUID uuid, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		Document doc = COLLECTION.find(eq("uuid", uuid.toString())).first();
		
		int newAmount = amount;
		if (doc.containsKey("coins"))
			newAmount += ((Number) doc.get("coins")).intValue();
		
		COLLECTION.updateOne(eq("uuid", uuid.toString()), new Document("$set", new Document("coins", newAmount)));
		
		return true;
	}

	@Override
	public boolean takeMoney(UUID uuid, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		Document doc = COLLECTION.find(eq("uuid", uuid.toString())).first();
		
		int newAmount = ((Number) doc.get("coins")).intValue() - amount;
		
		if (newAmount < 0)
			return false;
		
		COLLECTION.updateOne(eq("uuid", uuid.toString()), new Document("$set", new Document("coins", newAmount)));
		return true;
	}
	
	@Override
	public boolean hasEnoughMoney(UUID uuid, int amount) {
		return getMoney(uuid) >= amount;
	}
}

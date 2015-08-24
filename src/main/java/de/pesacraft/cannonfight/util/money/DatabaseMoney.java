package de.pesacraft.cannonfight.util.money;

import static com.mongodb.client.model.Filters.eq;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.Upgrade;

public class DatabaseMoney implements Money {

	private static final MongoCollection<Document> COLLECTION;
	
	static {
		COLLECTION = Collection.PLAYERS();
	}

	@Override
	public int getMoney(CannonFighter c) {
		Document doc = COLLECTION.find(eq("uuid", c.getPlayer().getUniqueId().toString())).first();
		
		if (doc.containsKey("coins"))
			// has coins
			return ((Number) doc.get("coins")).intValue();
		
		// no coins -> 0 coins, store that
		COLLECTION.updateOne(eq("uuid", c.getPlayer().getUniqueId().toString()), new Document("$set", new Document("coins", 0)));
		return 0;	
	}

	@Override
	public boolean giveMoney(CannonFighter c, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		Document doc = COLLECTION.find(eq("uuid", c.getPlayer().getUniqueId().toString())).first();
		
		int newAmount = amount;
		if (doc.containsKey("coins"))
			newAmount += ((Number) doc.get("coins")).intValue();
		
		COLLECTION.updateOne(eq("uuid", c.getPlayer().getUniqueId().toString()), new Document("$set", new Document("coins", newAmount)));
		
		return true;
	}

	@Override
	public boolean takeMoney(CannonFighter c, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		Document doc = COLLECTION.find(eq("uuid", c.getPlayer().getUniqueId().toString())).first();
		
		int newAmount = ((Number) doc.get("coins")).intValue() - amount;
		
		if (newAmount < 0)
			return false;
		
		COLLECTION.updateOne(eq("uuid", c.getPlayer().getUniqueId().toString()), new Document("$set", new Document("coins", newAmount)));
		return true;
	}

	@Override
	public boolean hasEnoughMoney(CannonFighter c, int amount) {
		return getMoney(c) >= amount;
	}
}

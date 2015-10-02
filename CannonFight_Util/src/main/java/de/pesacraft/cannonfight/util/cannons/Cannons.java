package de.pesacraft.cannonfight.util.cannons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.model.Filters;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.ItemSerializer;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.usable.FireballCannon;

public class Cannons {
	/**
	 * A list containing all available cannons
	 */
	private static final Map<String, CannonConstructor> cannons = new HashMap<String, CannonConstructor>();
	
	static {
		FireballCannon.setup();
	}
	
	public static Map<String, CannonConstructor> getCannons() {
		return cannons;
	}

	public static CannonConstructor getConstructorByName(String cannonName) {
		return cannons.get(cannonName);
	}

	public static void register(String name, CannonConstructor constructor) {
		cannons.put(name, constructor);
	}

	public static Set<String> getCannonSet() {
		return cannons.keySet();
	}

	public static void storeCannons() {	
		for (Entry<String, CannonConstructor> entry : cannons.entrySet()) {
			String name = entry.getKey();
			Document doc = new Document("name", name);	
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(entry.getValue().getItem())));
			
			doc.putAll(Cannon.serializeUpgrades(name));
			
			Collection.ITEMS().replaceOne(Filters.eq("name", name), doc);	 
		}
	}
	
	public static String getDefaultCannon() {
		return FireballCannon.NAME;
	}
}

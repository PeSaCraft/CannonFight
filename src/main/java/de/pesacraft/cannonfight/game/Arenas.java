package de.pesacraft.cannonfight.game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bukkit.Location;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.MongoDatabase;

public class Arenas {
	private static final MongoCollection<Document> COLLECTION;
	
	private static Map<String, Arena> arenas;
	
	static {
		COLLECTION = Collection.ARENAS();
	}
	
	public static void load() {
		arenas = new HashMap<String, Arena>();
		
		for (Document doc : COLLECTION.find()) {
			String name = doc.getString("name");
			arenas.put(name, new Arena(name));
		}
		
		CannonFight.LOGGER.info(Language.get("info.arenas-loaded"));
	}
	
	public static Arena random() {
		return new ArrayList<Arena>(arenas.values()).get((int) (Math.random() * arenas.size()));
	}

	public static Arena getArena(String name) {
		return arenas.get(name);
	}
}

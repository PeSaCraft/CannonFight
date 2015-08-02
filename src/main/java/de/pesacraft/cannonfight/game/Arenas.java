package de.pesacraft.cannonfight.game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.util.Database;

public class Arenas {
	private static Map<String, Arena> arenas;

	public static void load() {
		ResultSet result = Database.execute("SELECT name FROM " + Database.getTablePrefix() + "arenas", true);
		
		arenas = new HashMap<String, Arena>();
		try {
			do {
				String name = result.getString("name");
				arenas.put(name, new Arena(name));
			
				result.next();
			} while (!result.isAfterLast());
			CannonFight.LOGGER.info(Language.get("info.arenas-loaded"));
		} catch (SQLException e) {
			e.printStackTrace();
			CannonFight.LOGGER.severe(Language.get("error.arena-load-failed"));
		}
	}
	
	public static Arena random() {
		return new ArrayList<Arena>(arenas.values()).get((int) (Math.random() * arenas.size()));
	}

	public static Arena getArena(String name) {
		return arenas.get(name);
	}
}

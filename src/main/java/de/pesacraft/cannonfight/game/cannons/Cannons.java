package de.pesacraft.cannonfight.game.cannons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.pesacraft.cannonfight.CannonFight;

public class Cannons {
	/**
	 * A list containing all available cannons
	 */
	private static final Set<Cannon> cannons = new HashSet<Cannon>();
	
	public static void load() {
		File folder = new File(CannonFight.PLUGIN.getDataFolder() + "cannons");
	}
	
	public static Set<Cannon> getCannons() {
		return cannons;
	}

	public static Cannon getByName(String cannonName) {
		for (Cannon c : cannons)
			if (c.getName().equals(cannonName)) return c;
		return null;
	}
}

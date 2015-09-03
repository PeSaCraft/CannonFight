package de.pesacraft.cannonfight.game.cannons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.usable.FireballCannon;

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
}

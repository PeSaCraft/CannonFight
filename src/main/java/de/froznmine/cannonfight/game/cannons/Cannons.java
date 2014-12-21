package de.froznmine.cannonfight.game.cannons;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.froznmine.cannonfight.CannonFight;

public class Cannons {
	/**
	 * A list containing all available cannons
	 */
	private static final List<Cannon> cannons = new ArrayList<Cannon>();
	
	public static void load() {
		File folder = new File(CannonFight.PLUGIN.getDataFolder() + "cannons");
	}
	
	public static List<Cannon> getCannons() {
		return cannons;
	}
}

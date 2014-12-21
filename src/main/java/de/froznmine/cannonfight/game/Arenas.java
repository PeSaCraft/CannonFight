package de.froznmine.cannonfight.game;

import java.io.File;
import java.util.List;

import de.froznmine.cannonfight.CannonFight;

public class Arenas {
	private static List<Arena> arenas;

	public static void load() {
		File folder = new File(CannonFight.PLUGIN.getDataFolder() + "arenas");
		
		for (File sub : folder.listFiles()) {
			if (sub.isFile() && sub.getName().endsWith(".cfa")) {
				// load arena
			}
		}
	}
	
	public static Arena random() {
		return arenas.get((int) (Math.random() * arenas.size()));
	}

}

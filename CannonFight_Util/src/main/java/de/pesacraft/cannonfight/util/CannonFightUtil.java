package de.pesacraft.cannonfight.util;

import org.bukkit.plugin.Plugin;

import de.pesacraft.cannonfight.util.money.DatabaseMoney;
import de.pesacraft.cannonfight.util.money.Money;

public class CannonFightUtil {

	public static Plugin PLUGIN;
	public static Money MONEY;
	
	public static void use(Plugin plugin) {
		if (PLUGIN != null)
			return;
		PLUGIN = plugin;
		
		MongoDatabase.setup();
		
		MONEY = new DatabaseMoney();
	}
}

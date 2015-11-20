package de.pesacraft.cannonfight.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import de.pesacraft.cannonfight.util.Language.TimeOutputs;

public class Language {
	private static final DecimalFormat doubleFormat = new DecimalFormat("#.##");
	private static ResourceBundle MESSAGES;
	private static String brand;
	
	public static void loadLanguage(Plugin plugin, String language) {
		File folder = new File(plugin.getDataFolder() + "/lang/");
	
		File msg = new File(folder.getAbsolutePath() + "/lang_" + language + ".properties");
		
		if (!msg.exists())
			plugin.saveResource("lang/lang_" + language + ".properties", false);
		
		URL[] urls;
		try {
			urls = new URL[]{folder.toURI().toURL()};
		} catch (MalformedURLException ex){
			ex.printStackTrace();
			return;
		}  
		ClassLoader loader = new URLClassLoader(urls); 
		
		String lang[] = language.split("_");
		
		
		MESSAGES = ResourceBundle.getBundle("lang", new Locale(lang[0], lang[1]), loader);
		
		get("info.language-loaded", false);
		
		brand = get("general.brand", false);
	}
	
	public static String get(String key, boolean brand) {
		try {
			return (brand ? Language.brand : "") + ChatColor.translateAlternateColorCodes('&', MESSAGES.getString(key));
		}
		catch (Exception ex) {
			if (key.equalsIgnoreCase("error.missing-translation"))
				return "error.missing-translation is missing!";

			return getStringMaker("error.missing-translation", brand).replace("%key%", key).getString();
		}
	}
	
	public static String formatTime(double timeInSeconds, TimeOutputs outputMode) {
		switch (outputMode) {
		case MINUTES:
			int timeInMinutes = (int) (timeInSeconds / 60);
			if (timeInMinutes == 1)
				return "1 " + get("general.units.minute", false);
			return timeInMinutes + " " + get("general.units.minutes", false);
		case SECONDS:
			if (timeInSeconds == 1)
				return "1 " + get("general.units.second", false);
			return formatDouble(timeInSeconds) + " " + get("general.units.seconds", false);
		default:
			break;
		
		}
		return "";
	}
	
	public static StringMaker getStringMaker(String key, boolean brand) {
		return new StringMaker(get(key, brand));
	}
	
	public static class StringMaker {
		
		private String string;
		
		private StringMaker(String string) {
			this.string = string;
		}
		
		public StringMaker replace(String match, String replacement) {
			this.string = this.string.replaceAll(match, replacement);
			return this;
		}
		
		public String getString() {
			return this.string;
		}
		
		@Override
		public StringMaker clone() {
			return new StringMaker(this.string);
		}
	}
	
	public static enum TimeOutputs {
		SECONDS,
		MINUTES,
	}

	public static String formatLives(int lives) {
		if (lives == 1)
			return "1 " + get("general.units.life", false);
		return lives + " " + get("general.units.lives", false);
	}

	public static String formatCoins(int coins) {
		if (coins == 1)
			return "1 " + get("general.units.coin", false);
		return coins + " " + get("general.units.coins", false);
	}
	
	public static String formatPlayers(int players) {
		if (players == 1)
			return "1 " + get("general.units.player", false);
		return players + " " + get("general.units.players", false);
	}
	
	public static String formatSlots(int slots) {
		if (slots == 1)
			return "1 " + get("general.units.slot", false);
		return slots + " " + get("general.units.slots", false);
	}
	
	public static String formatAmmo(int ammo) {
		if (ammo == 1)
			return "1 " + get("general.units.ammo", false);
		return ammo + " " + get("general.units.ammos", false);
	}
	
	public static String formatDistance(double distance) {
		if (distance == 1)
			return formatDouble(distance) + " " + get("general.units.block", false);
		return formatDouble(distance) + " " + get("general.units.blocks", false);
	}

	public static String formatDamage(double damage) {
		String hearts = "";
		for (int i = 0; i < damage / 2; i++)
			hearts += "\u2764";
		
		if (damage % 2 == 1)
			hearts += "\u2765";
		
		return getStringMaker("general.units.damage-format", false).replace("%value%", formatDouble(damage)).replace("%hearts%", hearts).getString();
	}
	
	public static String formatDouble(double value) {
		return doubleFormat.format(value);
	}

	public static String formatPercentage(double percentage) {
		return doubleFormat.format(percentage * 100) + " %";
	}
}

package de.pesacraft.cannonfight.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import de.pesacraft.cannonfight.util.Language.TimeOutputs;

public class Language {
	private static ResourceBundle MESSAGES;
	private static String brand;
	
	public static void loadLanguage(Plugin plugin, String language) {
		File folder = new File(plugin.getDataFolder() + "/lang/");
	
		URL[] urls;
		try {
			urls = new URL[]{folder.toURI().toURL()};
		} catch (MalformedURLException ex){
			ex.printStackTrace();
			return;
		}  
		ClassLoader loader = new URLClassLoader(urls); 
		
		String lang[] = language.split("_");
		
		File msg = new File(folder.getAbsolutePath() + "/lang_" + language + ".properties");
		if (!msg.exists())
			plugin.saveResource("lang/lang_" + language + ".properties", false);
		
		MESSAGES = ResourceBundle.getBundle("lang", new Locale(lang[0], lang[1]), loader);
		
		get("info.language-loaded", false);
		
		brand = get("general.brand", false);
	}
	
	public static String get(String key) {
		return get(key, true);
	}
	
	public static String get(String key, boolean brand) {
		try {
			return (brand ? Language.brand : "") + ChatColor.translateAlternateColorCodes('&', MESSAGES.getString(key));
		}
		catch (Exception ex) {
			return getStringMaker(get("error.missing-translation"), brand).replace("%key%", key).getString();
		}
	}
	
	public static String formatTime(int timeInSeconds, TimeOutputs outputMode) {
		switch (outputMode) {
		case MINUTES:
			int timeInMinutes = timeInSeconds / 60;
			if (timeInMinutes == 1)
				return timeInMinutes + " " + get("general.units.minute", false);
			return timeInMinutes + " " + get("general.units.minutes", false);
		case SECONDS:
			if (timeInSeconds == 1)
				return timeInSeconds + " " + get("general.units.second", false);
			return timeInSeconds + " " + get("general.units.seconds", false);
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
			this.string.replaceAll(match, replacement);
			return this;
		}
		
		public String getString() {
			return string;
		}
	}
	
	public static enum TimeOutputs {
		SECONDS,
		MINUTES,
	}

	public static String formatLives(int lives) {
		return null;
	}

	public static String formatCoins(int coins) {
		if (coins == 1)
			return coins + " " + get("general.units.coin", false);
		return coins + " " + get("general.units.coins", false);
	}
}

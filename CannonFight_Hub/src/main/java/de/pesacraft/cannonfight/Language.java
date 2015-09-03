package de.pesacraft.cannonfight;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.ChatColor;

public class Language {
	private static ResourceBundle MESSAGES;
	private static String brand;
	
	public static void loadLanguage(String language) {
		File folder = new File(CannonFight.PLUGIN.getDataFolder() + "/lang/");
	
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
			CannonFight.PLUGIN.saveResource("lang/lang_" + language + ".properties", false);
		
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
			return get("error.missing-translation").replaceAll("%key%", key);
		}
	}
}

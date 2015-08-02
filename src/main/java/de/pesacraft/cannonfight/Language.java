package de.pesacraft.cannonfight;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

public class Language {
	private static ResourceBundle MESSAGES;
	
	public static void loadLanguage(String language) {
		File folder = CannonFight.PLUGIN.getDataFolder();
	
		URL[] urls;
		try {
			urls = new URL[]{folder.toURI().toURL()};
		} catch (MalformedURLException ex){
			ex.printStackTrace();
			return;
		}  
		ClassLoader loader = new URLClassLoader(urls); 
		
		String lang[] = language.split("_");
		
		File msg = new File(folder.getAbsolutePath() + "/lang/" + language + ".properties");
		if (!msg.exists())
			CannonFight.PLUGIN.saveResource("lang/" + language+  ".properties", false);
		
		MESSAGES = ResourceBundle.getBundle("lang", new Locale(lang[0], lang[1]), loader);
		
		get("info.language-loaded");
	}
	
	public static String get(String key) {
		return MESSAGES.containsKey(key) ? MESSAGES.getString(key) : null;
	}
}

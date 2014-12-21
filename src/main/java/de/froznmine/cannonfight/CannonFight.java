package de.froznmine.cannonfight;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import de.froznmine.cannonfight.game.Game;
import de.froznmine.lobby.LobbySystem;

public class CannonFight extends JavaPlugin {
	public static Logger LOGGER;
	public static CannonFight PLUGIN;
	
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		if (!new File(this.getDataFolder() + "config.yml").exists()) this.saveDefaultConfig();
		
		registerSerializables();
		loadConfig();
		loadArenas();
		loadCannons();
		 
		LobbySystem.registerGame(this, Game.class);
	}
	 
	private void loadCannons() {
		// TODO Auto-generated method stub
		
	}

	private void registerSerializables() {
		// TODO Auto-generated method stub
		
	}

	private void loadArenas() {
		// TODO Auto-generated method stub
		
	}

	private void loadConfig() {
		// TODO Auto-generated method stub
		
	}

	public void onDisable() { 
	 
	}
}

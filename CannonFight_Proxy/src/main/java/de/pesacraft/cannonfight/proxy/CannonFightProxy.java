package de.pesacraft.cannonfight.proxy;

import java.util.logging.Logger;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import de.pesacraft.cannonfight.util.communication.CommunicationServer;

public class CannonFightProxy extends Plugin implements Listener {
	public static Logger LOGGER;
	public static CannonFightProxy PLUGIN;
	
	@Override
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		new CommunicationServer().start();
	}
}

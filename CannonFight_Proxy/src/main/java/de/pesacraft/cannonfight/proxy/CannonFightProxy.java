package de.pesacraft.cannonfight.proxy;

import java.util.logging.Logger;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import de.pesacraft.cannonfight.util.communication.CommunicationServer;
import de.pesacraft.cannonfight.util.money.DatabaseMoney;
import de.pesacraft.cannonfight.util.money.Money;

public class CannonFightProxy extends Plugin implements Listener {
	public static Logger LOGGER;
	public static CannonFightProxy PLUGIN;
	public static Money MONEY;
	
	@Override
	public void onEnable() { 
		PLUGIN = this;
		LOGGER = this.getLogger();
		
		MONEY = new DatabaseMoney();
		
		new CommunicationServer().start();
	}
}

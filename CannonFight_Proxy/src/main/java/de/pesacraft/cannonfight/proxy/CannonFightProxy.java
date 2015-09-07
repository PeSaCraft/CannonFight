package de.pesacraft.cannonfight.proxy;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
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
		LOGGER.info("Test");
		
		new CommunicationServer().start();
	}
}

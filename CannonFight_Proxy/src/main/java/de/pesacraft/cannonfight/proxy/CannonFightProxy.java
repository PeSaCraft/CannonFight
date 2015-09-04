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
import com.google.common.net.InetAddresses;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
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
		
		this.getProxy().getPluginManager().registerListener(this, this);
		this.getProxy().registerChannel("CannonFight");
		
		//LobbySystem.registerGame(this, Game.class);
	}
	
	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
		System.out.println("nachricht kommt!");
		System.out.println("tag: " + event.getTag());
		System.out.println("data: " + event.getData().toString());
		
		if (!event.getTag().equals("CannonFight"))
			return;
		DataInput in = new DataInputStream(new ByteArrayInputStream(event.getData()));
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		String subChannel = "";
		try {
			subChannel = in.readUTF();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (subChannel.equals("Start")) {
			// Read data from server
			String arena = "";
			try {
				arena = in.readUTF();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ProcessBuilder pb = new ProcessBuilder("./start.command");
			pb.directory(new File("/home/mcserver/TestServerBungee"));
			try {
				Process p = pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String server = arena + "-" + (Math.random() * 10);
			ServerInfo info = ProxyServer.getInstance().constructServerInfo(server, new InetSocketAddress(InetAddress.getLoopbackAddress(), 26668), "NIX LOS HIER!", false);
			
			ProxyServer.getInstance().getServers().put(server, info);
			
			out.writeUTF("Server");
			out.writeUTF(arena);
			out.writeUTF(server);
			
			// sender is a server
			((Server) event.getSender()).sendData("CannonFight", out.toByteArray());
		}
	}
}

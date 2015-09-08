package de.pesacraft.cannonfight.util.communication;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import com.google.common.io.ByteArrayDataOutput;

public class CommunicationServer extends Thread {
	private static CommunicationServer instance;
	
	private Map<String, HubHandler> hubs;
	private Map<String, GameHandler> games;
	
	public CommunicationServer() {
		instance = this;
		
		hubs = new HashMap<String, HubHandler>();
		games = new HashMap<String, GameHandler>();
	}
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(26665);
		
			while (true) {
				Socket client = server.accept();
				
				DataOutputStream out = new DataOutputStream(client.getOutputStream());
				DataInputStream in = new DataInputStream(client.getInputStream());
				
				String type = in.readUTF();
				
				// if request coming from this machine use 127.0.0.1 instead of the connected address to make servers not visible from outside the machine compatible
				InetAddress address = NetworkInterface.getByInetAddress(client.getInetAddress()) == null ? client.getInetAddress() : InetAddress.getLoopbackAddress();
				System.out.println(address);
				
				if (type.equals("HubReady")) {
					String serverName = in.readUTF();
					int port = in.readInt();
					
					ServerInfo info = ProxyServer.getInstance().constructServerInfo(serverName, new InetSocketAddress(address, port), "NIX LOS IM HUB!", false);
					
					ProxyServer.getInstance().getServers().put(serverName, info);
					
					HubHandler handler = new HubHandler(serverName, client, in, out);
					handler.start();
					hubs.put(serverName, handler);
				}
				else if (type.equals("GameReady")) {
					String serverName = in.readUTF();
					int port = in.readInt();
					
					ServerInfo info = ProxyServer.getInstance().constructServerInfo(serverName, new InetSocketAddress(address, port), "NIX LOS IM GAME!", false);
					
					ProxyServer.getInstance().getServers().put(serverName, info);
					
					GameHandler handler = new GameHandler(serverName, client, in, out);
					handler.start();
					games.put(serverName, handler);
				}
				else {
					out.writeUTF("ConnectionRefused");
					out.writeUTF("Wrong start of connection");
					out.close();
					in.close();
					client.close();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static CommunicationServer getInstance() {
		return instance;
	}

	public GameHandler getGame(String server) {
		return games.get(server);
	}
	
	public GameHandler getGameForArena(String arena, boolean hasToBeJoinableForPlayer) {
		for (GameHandler g : games.values()) {
			if (!g.arenaSet())
				continue;
			if (g.getArena().equals(arena)) {
				if (hasToBeJoinableForPlayer && !g.isPlayerJoinable())
					continue;
				return g;
			}
		}
		
		return null;
	}
	
	public GameHandler getUnusedGame() {
		for (GameHandler g : games.values()) {
			if (!g.arenaSet())
				return g;
		}
		
		return null;
	}

	public void removeHub(String server) {
		hubs.remove(server);
	}
	
	public void removeGame(String server) {
		games.remove(server);
	}

	public void updatePlayerCount(String arena) {
		int amount = 0;
		
		for (GameHandler g : games.values()) {
			if (!g.getArena().equals(arena))
				continue;
			if (g.hasStarted())
				continue;
			
			amount += g.getPlayers();
		}
		for (HubHandler h : hubs.values()) {
			h.sendPlayerCount(arena, amount);
		}
	}
}

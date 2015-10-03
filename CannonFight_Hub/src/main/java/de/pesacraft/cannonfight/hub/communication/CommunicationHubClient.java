package de.pesacraft.cannonfight.hub.communication;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.hub.CannonFightHub;
import de.pesacraft.cannonfight.hub.lobby.signs.SignHandler;
import de.pesacraft.cannonfight.util.Language;

public class CommunicationHubClient extends Thread {
	private static CommunicationHubClient instance;
	
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	private CommunicationHubClient() {
		instance = this;
		try {
			this.socket = new Socket(InetAddress.getLocalHost(), 26665);
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			instance = null;
			try {
				socket.close();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			out.writeUTF("HubReady");
			out.writeUTF(CannonFightHub.PLUGIN.getConfig().getString("bungeecord.servername"));
			out.writeInt(Bukkit.getPort());
			
			String input = in.readUTF();
			
			if (!input.equals("HubConnected")) {
				CommunicationHubClient.getInstance().sendHubShutdown();
				Bukkit.getServer().spigot().restart();
				return;
			}
			
			while ((input = in.readUTF()) != null) {
				if (input.equals("PlayerCount")) {
					String arena = in.readUTF();
					int amount = in.readInt();
					SignHandler.getInstance().updateSign(arena, amount);
				}
				else if (input.equals("PlayerDenied")) {
					String player = in.readUTF();
					
					Bukkit.getPlayer(player).sendMessage(Language.get("error.cannot-join"));
				}
				else if (input.equals("SpectatorDenied")) {
					String player = in.readUTF();
					
					Bukkit.getPlayer(player).sendMessage(Language.get("error.cannot-join-spectator"));
				}
			}
		} catch (IOException ex) {
			instance = null;
			tryToStart();
		}
	}
	
	public void sendHubShutdown() {
		try {
			out.writeUTF("HubShutdown");
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendPlayer(String arena, String player) {
		try {
			out.writeUTF("Player");
			out.writeUTF(arena);
			out.writeUTF(player);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendSpectator(String player, String whoToSpectate) {
		try {
			out.writeUTF("Spectator");
			out.writeUTF(player);
			out.writeUTF(whoToSpectate);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendSpectatorToArena(String arena, String player) {
		try {
			out.writeUTF("SpectatorArena");
			out.writeUTF(arena);
			out.writeUTF(player);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static CommunicationHubClient getInstance() {
		return instance;
	}
	
	public static void tryToStart() {
		if (getInstance() != null)
			// already connected and running, don't change anything
			return;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				CannonFightHub.LOGGER.info("Trying to connect to proxy");
				// try to establish connection
				new CommunicationHubClient();
				
				if (getInstance() != null) {
					// connection established, don't have to retry
					getInstance().run();
					this.cancel();
					CannonFightHub.LOGGER.info("Connected to proxy. Back to work!");
				}
			}
		}.runTaskTimer(CannonFightHub.PLUGIN, 0, 20 * 30); // every 30 seconds
	}
}

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
				if (socket != null)
					socket.close();
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			out.writeUTF("HubReady"); //$NON-NLS-1$
			out.writeUTF(CannonFightHub.PLUGIN.getConfig().getString("bungeecord.servername")); //$NON-NLS-1$
			out.writeInt(Bukkit.getPort());
			
			String input = in.readUTF();
			
			if (!input.equals("HubConnected")) { //$NON-NLS-1$
				CommunicationHubClient.getInstance().sendHubShutdown();
				Bukkit.getServer().spigot().restart();
				return;
			}
			
			while ((input = in.readUTF()) != null) {
				if (input.equals("PlayerCount")) { //$NON-NLS-1$
					String arena = in.readUTF();
					int amount = in.readInt();
					SignHandler.getInstance().updateSign(arena, amount);
				}
				else if (input.equals("PlayerDenied")) { //$NON-NLS-1$
					String player = in.readUTF();
					
					Bukkit.getPlayer(player).sendMessage(Language.get("error.cannot-join")); //$NON-NLS-1$
				}
				else if (input.equals("SpectatorDenied")) { //$NON-NLS-1$
					String player = in.readUTF();
					
					Bukkit.getPlayer(player).sendMessage(Language.get("error.cannot-join-spectator")); //$NON-NLS-1$
				}
			}
		} catch (IOException ex) {
			instance = null;
			tryToStart();
		}
	}
	
	public void sendHubShutdown() {
		try {
			out.writeUTF("HubShutdown"); //$NON-NLS-1$
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendPlayer(String arena, String player) {
		try {
			out.writeUTF("Player"); //$NON-NLS-1$
			out.writeUTF(arena);
			out.writeUTF(player);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendSpectator(String player, String whoToSpectate) {
		try {
			out.writeUTF("Spectator"); //$NON-NLS-1$
			out.writeUTF(player);
			out.writeUTF(whoToSpectate);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendSpectatorToArena(String arena, String player) {
		try {
			out.writeUTF("SpectatorArena"); //$NON-NLS-1$
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
		if (!CannonFightHub.PLUGIN.isEnabled())
			// plugin disables/d don't try to reconnect
			return;
		
		if (getInstance() != null)
			// already connected and running, don't change anything
			return;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				CannonFightHub.LOGGER.info(Language.get("info.proxy.try-to-connect")); //$NON-NLS-1$
				// try to establish connection
				new CommunicationHubClient();
				
				if (getInstance() != null) {
					// connection established, don't have to retry
					getInstance().start();
					this.cancel();
					CannonFightHub.LOGGER.info(Language.get("info.proxy.connected")); //$NON-NLS-1$
				}
			}
		}.runTaskTimer(CannonFightHub.PLUGIN, 0, 20 * 30); // every 30 seconds
	}
}

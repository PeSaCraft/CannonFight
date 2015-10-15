package de.pesacraft.cannonfight.game.communication;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.game.CannonFightGame;
import de.pesacraft.cannonfight.game.players.Participant;
import de.pesacraft.cannonfight.util.Language;


public class CommunicationGameClient extends Thread {
	private static CommunicationGameClient instance;
	
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	private CommunicationGameClient() {
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
			out.writeUTF("GameReady");
			out.writeUTF(CannonFightGame.PLUGIN.getConfig().getString("bungeecord.servername"));
			out.writeInt(Bukkit.getPort());
			
			String input = in.readUTF();
			
			if (!input.equals("GameConnected")) {
				CommunicationGameClient.getInstance().sendGameOver();
				Bukkit.getServer().spigot().restart();
				return;
			}
			
			while ((input = in.readUTF()) != null) {
				if (input.equals("Arena")) {
					final String arena = in.readUTF();
					
					
					new BukkitRunnable() {
						
						@Override
						public void run() {
							CannonFightGame.setArena(arena);
						}
					}.runTask(CannonFightGame.PLUGIN);
					
				}
				else if (input.equals("Player")) {
					String name = in.readUTF();
					String server = in.readUTF();
					
					CannonFightGame.addFuturePlayer(name, server);
				}
				else if (input.equals("Spectator")) {
					String name = in.readUTF();
					String server = in.readUTF();
					
					CannonFightGame.addFutureSpectator(name, server);
				}
			}
		} catch (IOException ex) {
			instance = null;
			tryToStart();
		}
	}
	
	public static CommunicationGameClient getInstance() {
		return instance;
	}

	public void sendGameOver() {
		try {
			out.writeUTF("GameOver");
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendGameStarted() {
		try {
			out.writeUTF("GameStarted");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendBackToHub(Participant part) {
		try {
			out.writeUTF("PlayerLeave");
			out.writeUTF(part.getPlayer().getName());
			out.writeUTF(part.getServer());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendGameFull() {
		try {
			out.writeUTF("Full");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void tryToStart() {
		if (!CannonFightGame.PLUGIN.isEnabled())
			// plugin disables/d don't try to reconnect
			return;
		
		if (getInstance() != null)
			// already connected and running, don't change anything
			return;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				CannonFightGame.LOGGER.info(Language.get("info.proxy.try-to-connect"));
				// try to establish connection
				new CommunicationGameClient();
				
				if (getInstance() != null) {
					// connection established, don't have to retry
					getInstance().start();
					this.cancel();
					CannonFightGame.LOGGER.info(Language.get("info.proxy.connected"));
				}
			}
		}.runTaskTimer(CannonFightGame.PLUGIN, 0, 20 * 30); // every 30 seconds
	}
}

package de.pesacraft.cannonfight.game.communication;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;

import de.pesacraft.cannonfight.game.CannonFightGame;


public class CommunicationGameClient extends Thread {
	private static CommunicationGameClient instance;
	
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	public CommunicationGameClient() {
		instance = this;
		try {
			this.socket = new Socket(InetAddress.getLocalHost(), 26665);
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
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
					String arena = in.readUTF();
					
					CannonFightGame.setArena(arena);
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
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
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
	
	public void sendPlayerLeave() {
		try {
			out.writeUTF("PlayerLeave");
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
}

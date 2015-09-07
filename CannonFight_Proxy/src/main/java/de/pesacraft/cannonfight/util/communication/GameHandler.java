package de.pesacraft.cannonfight.util.communication;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import net.md_5.bungee.api.ProxyServer;

public class GameHandler extends Thread {
	private String server;
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	private String arena;
	private int players;
	private boolean playerJoinable;
	private boolean started;
	
	public GameHandler(String server, Socket socket, DataInput in, DataOutput out) {
		this.server = server;
		this.socket = socket;
		this.in = in;
		this.out = out;
		
		this.arena = null;
		this.players = 0;
		this.playerJoinable = true;
		this.started = false;
	}

	@Override
	public void run() {
		try {
			out.writeUTF("GameConnected");
			
			String input;
			
			while ((input = in.readUTF()) != null) {
				if (input.equals("Full")) {
					this.playerJoinable = false;
				}
				else if (input.equals("PlayerLeave")) {
					this.players--;
					if (!playerJoinable)
						playerJoinable = true;
				}
				else if (input.equals("GameStarted")) {
					playerJoinable = false;
					started = true;
					CommunicationServer.getInstance().updatePlayerCount(arena);
				}
				else if (input.equals("GameOver")) {
					socket.close();
					CommunicationServer.getInstance().removeGame(server);
					return;
				}
			
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void setArena(String arena) throws IOException {
		out.writeUTF("Arena");
		out.writeUTF(arena);
		
		this.arena = arena;
	}
	
	public boolean sendPlayer(String player, String server) throws IOException {
		if (!arenaSet() || !playerJoinable)
			return false;
		
		out.writeUTF("Player");
		out.writeUTF(player);
		out.writeUTF(server);
		
		ProxyServer.getInstance().getPlayer(player).connect(ProxyServer.getInstance().getServerInfo(this.server));
		
		players++;
		return true;
	}
	
	public boolean sendSpectator(String player, String server) throws IOException {
		if (!arenaSet())
			return false;

		out.writeUTF("Spectator");
		out.writeUTF(player);
		out.writeUTF(server);
		
		ProxyServer.getInstance().getPlayer(player).connect(ProxyServer.getInstance().getServerInfo(this.server));
	
		return true;
	}

	public boolean arenaSet() {
		return arena != null;
	}

	public String getArena() {
		return arena;
	}
	
	public boolean isPlayerJoinable() {
		return playerJoinable;
	}

	public boolean hasStarted() {
		return started;
	}

	public int getPlayers() {
		return players;
	}
}

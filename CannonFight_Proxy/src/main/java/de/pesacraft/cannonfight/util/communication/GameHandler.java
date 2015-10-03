package de.pesacraft.cannonfight.util.communication;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ProxyServer;

public class GameHandler extends Thread {
	private String server;
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	private String arena;
	private List<String> players;
	private boolean playerJoinable;
	private boolean started;
	
	public GameHandler(String server, Socket socket, DataInput in, DataOutput out) {
		this.server = server;
		this.socket = socket;
		this.in = in;
		this.out = out;
		
		this.arena = null;
		this.players = new ArrayList<String>();
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
					String player = in.readUTF();
					String server = in.readUTF();
					
					if (players.contains(player)) {
						// player is on the server, he may disconnect
						ProxyServer.getInstance().getPlayer(player).connect(ProxyServer.getInstance().getServerInfo(server));
						
						players.remove(player);
						
						if (!isPlayerJoinable())
							playerJoinable = true;
						
						CommunicationServer.getInstance().updatePlayerCount(arena);
					}
					// else player not on the server, mustn't disconnect
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
		if (!arenaSet() || !isPlayerJoinable())
			return false;
		
		if (players.contains(player))
			// player already trying to connect/connecting to server. would result in duplicate player counts.
			return false;
		
		players.add(player);
		
		out.writeUTF("Player");
		out.writeUTF(player);
		out.writeUTF(server);
		
		ProxyServer.getInstance().getPlayer(player).connect(ProxyServer.getInstance().getServerInfo(this.server));
		
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
		return players.size();
	}
}

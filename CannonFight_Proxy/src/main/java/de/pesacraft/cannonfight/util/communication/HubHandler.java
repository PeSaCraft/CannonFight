package de.pesacraft.cannonfight.util.communication;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HubHandler extends Thread {
	private String server;
	private Socket socket;
	private DataInput in;
	private DataOutput out;
	
	public HubHandler(String server, Socket socket, DataInput in, DataOutput out) {
		this.server = server;
		this.socket = socket;
		this.in = in;
		this.out = out;
	}

	@Override
	public void run() {
		try {
			out.writeUTF("HubConnected");

			String input;
			
			while ((input = in.readUTF()) != null) {
				if (input.equals("Player")) {
					String arena = in.readUTF();
					String player = in.readUTF();
					
					GameHandler handler = CommunicationServer.getInstance().getGameForArena(arena, true);
					
					if (handler == null) {
						// no waiting game, try creating one
						handler = CommunicationServer.getInstance().getUnusedGame();
						
						if (handler == null) {
							// cannot create one
							out.writeUTF("PlayerDenied");
							out.writeUTF(player);
							continue;
						}
						// find empty, create
						handler.setArena(arena);
					}
					
					if (handler.sendPlayer(player, this.server)) {
						// player joined game, update signs
						CommunicationServer.getInstance().updatePlayerCount(arena);
					}
					else {
						// player cannot join game
						out.writeUTF("PlayerDenied");
						out.writeUTF(player);
					}
				}
				else if (input.equals("Spectator")) {
					String arena = in.readUTF();
					String player = in.readUTF();
					
					GameHandler handler = CommunicationServer.getInstance().getGameForArena(arena, false);
					
					if (handler == null) {
						out.writeUTF("SpectatorDenied");
						out.writeUTF(player);
					}
					else {
						handler.sendSpectator(player, this.server);
					}
				}
				else if (input.equals("HubShutdown")) {
					socket.close();
					CommunicationServer.getInstance().removeHub(server);
					return;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendPlayerCount(String arena, int amount) {
		try {
			out.writeUTF("PlayerCount");
			out.writeUTF(arena);
			out.writeInt(amount);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

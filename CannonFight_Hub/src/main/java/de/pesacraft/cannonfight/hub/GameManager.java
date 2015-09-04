package de.pesacraft.cannonfight.hub;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class GameManager implements PluginMessageListener {
	private int limit;
	private Set<String> runningServers;
	
	private Map<UUID, String> players;
	
	public GameManager() {
		runningServers = new HashSet<String>();
		players = new HashMap<UUID, String>();
		
		Bukkit.getMessenger().registerIncomingPluginChannel(CannonFightHub.PLUGIN, "BungeeCord", this);
		Bukkit.getMessenger().registerIncomingPluginChannel(CannonFightHub.PLUGIN, "CannonFight", this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(CannonFightHub.PLUGIN, "BungeeCord");
		Bukkit.getMessenger().registerOutgoingPluginChannel(CannonFightHub.PLUGIN, "CannonFight");
	}
	
	public String getServerOfPlayer(Player p) {
		return players.get(p.getUniqueId());
	}

	public void sendSpectator(Player p, String server) {
		// notify the spectator will join
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		
		out.writeUTF("Forward"); // So BungeeCord knows to forward it
		out.writeUTF(server);
		out.writeUTF("CannonFight"); // The channel name to check if this your data

		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		
		try {
			msgout.writeUTF("Some kind of data here");
			msgout.writeShort(123);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		out.writeShort(msgbytes.toByteArray().length);
		out.write(msgbytes.toByteArray());
		
		// try to connect player
		out = ByteStreams.newDataOutput();
		
		out.writeUTF("Connect");
		out.writeUTF(server);
		
		p.sendPluginMessage(CannonFightHub.PLUGIN, "BungeeCord", out.toByteArray());
	}
	
	public void startNewServer(String arena) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		System.out.println("Sende anfrage!");
		out.writeUTF("Start");
		out.writeUTF(arena);
		Bukkit.getServer().sendPluginMessage(CannonFightHub.PLUGIN, "CannonFight", out.toByteArray());
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message) {
		System.out.println("Nachricht im " + channel + " von " + p.getName() + " nachricht: " + message.toString());
		
		if (channel.equals("CannonFight")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			if (subchannel.equals("Server")) {
				String arena = in.readUTF();
				String server = in.readUTF();
				
				System.out.println("Neuer Server " + server + " für Arena " + arena);
			}
		}
	}
}

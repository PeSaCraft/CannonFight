package de.pesacraft.cannonfight.game.communication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import de.pesacraft.cannonfight.game.CannonFightGame;

public class Messenger implements PluginMessageListener {
	
	public Messenger() {
		Bukkit.getMessenger().registerIncomingPluginChannel(CannonFightGame.PLUGIN, "BungeeCord", this);
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equals("CannonFight")) {
			short len = in.readShort();
			byte[] msgbytes = new byte[len];
			in.readFully(msgbytes);

			DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
			try {
				String somedata = msgin.readUTF();
				short somenumber = msgin.readShort();
					
				System.out.println("Empfangen von " + p.getName() + ": " + somedata + " " + somenumber);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}

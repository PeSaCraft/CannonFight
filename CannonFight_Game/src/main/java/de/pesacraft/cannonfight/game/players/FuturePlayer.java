package de.pesacraft.cannonfight.game.players;

public class FuturePlayer {
	private String name;
	private String server;
	
	public FuturePlayer(String name, String server) {
		this.name = name;
		this.server = server;
	}
	
	public String getName() {
		return name;
	}
	
	public String getServer() {
		return server;
	}
}

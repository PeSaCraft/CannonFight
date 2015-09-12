package de.pesacraft.cannonfight.hub;

public class RunningServer {
	private String name;
	
	private String arena;
	
	private boolean joinable;
	
	public RunningServer(String name, String arena) {
		this.name = name;
		this.arena = arena;
		this.joinable = true;
	}
}

package de.pesacraft.cannonfight.game.players;

import de.pesacraft.cannonfight.util.CannonFighter;

public class Participant {
	private final CannonFighter player;
	private final String server;
	
	public Participant(CannonFighter player, String server) {
		this.player = player;
		this.server = server;
	}
	
	public Participant(CannonFighter player) {
		this(player, null);
	}

	public CannonFighter getPlayer() {
		return player;
	}
	
	public String getServer() {
		return server;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Participant))
			return false;
		return ((Participant) obj).player.equals(this.player);
	}
}

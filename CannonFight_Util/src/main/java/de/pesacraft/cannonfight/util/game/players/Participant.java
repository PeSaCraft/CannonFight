package de.pesacraft.cannonfight.util.game.players;

import de.pesacraft.cannonfight.util.CannonFighter;

public class Participant {
	private final CannonFighter player;
	
	public Participant(CannonFighter player) {
		this.player = player;
	}
	
	public CannonFighter getPlayer() {
		return player;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Participant))
			return false;
		return ((Participant) obj).player.equals(this.player);
	}
}

package de.pesacraft.cannonfight.data.players;

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

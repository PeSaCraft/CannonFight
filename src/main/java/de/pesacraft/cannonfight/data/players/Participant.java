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
		return (obj instanceof Participant) ? ((Participant) obj).player.equals(this.player) : false;
	}
}

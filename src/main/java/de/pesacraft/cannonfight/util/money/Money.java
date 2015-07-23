package de.pesacraft.cannonfight.util.money;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public interface Money {
	public int getMoney(CannonFighter c);
	
	public boolean giveMoney(CannonFighter c, int amount, String... reason);
	
	public boolean takeMoney(CannonFighter c, int amount, String... reason);
}

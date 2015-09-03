package de.pesacraft.cannonfight.game.util.money;

import de.pesacraft.cannonfight.game.players.CannonFighter;

public interface Money {
	public int getMoney(CannonFighter c);
	
	public boolean hasEnoughMoney(CannonFighter c, int amount);
	
	public boolean giveMoney(CannonFighter c, int amount, String... reason);
	
	public boolean takeMoney(CannonFighter c, int amount, String... reason);
}

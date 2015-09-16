package de.pesacraft.cannonfight.util.money;

import java.util.UUID;

import de.pesacraft.cannonfight.util.CannonFighter;

public interface Money {
	public int getMoney(UUID uuid);
	
	public boolean setMoney(UUID uuid, int amount, String... reason);
	
	public boolean giveMoney(UUID uuid, int amount, String... reason);
	
	public boolean takeMoney(UUID uuid, int amount, String... reason);

	public boolean hasEnoughMoney(UUID uuid, int amount);
	
	@Deprecated
	public int getMoney(CannonFighter c);
	
	@Deprecated
	public boolean hasEnoughMoney(CannonFighter c, int amount);
	
	@Deprecated
	public boolean giveMoney(CannonFighter c, int amount, String... reason);
	
	@Deprecated
	public boolean takeMoney(CannonFighter c, int amount, String... reason);
}

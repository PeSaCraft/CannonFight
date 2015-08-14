package de.pesacraft.cannonfight.game.cannons;

import java.util.Map;

public interface CannonConstructor {
	public boolean canConstruct(String name);
	
	public Cannon construct(Map<String, Object> map);
}

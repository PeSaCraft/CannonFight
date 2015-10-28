package de.pesacraft.cannonfight.util.game;

import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface HitHandler {

	public void hitBlock(Location location);

	public void hitEntity(EntityDamageByEntityEvent event);

	public void flying(Location location);
}

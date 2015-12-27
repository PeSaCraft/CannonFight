package de.pesacraft.cannonfight.util.game;

import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BlockIterator;

public interface HitHandler {

	public void hitBlock(Location location, BlockIterator hitBlocks);

	public void hitEntity(EntityDamageByEntityEvent event);

	public void flying(Location location);
}

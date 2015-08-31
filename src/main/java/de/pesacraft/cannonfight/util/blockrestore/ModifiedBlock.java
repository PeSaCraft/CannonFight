package de.pesacraft.cannonfight.util.blockrestore;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class ModifiedBlock {
	private final Material mat;
	private final byte data;
	
	private final Location loc;
	
	public ModifiedBlock(Block b) {
		loc = b.getLocation().clone();
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY());
		loc.setZ(loc.getBlockZ());
		loc.setPitch(0);
		loc.setYaw(0);
		mat = b.getType();
		data = b.getData();
	}

	public Material getMaterial() {
		return mat;
	}
	
	public byte getData() {
		return data;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public int getXOffset() {
		return loc.getBlockX() & 0xF; // & 0xF is & 16, which is equal to mod 16
	}
	
	public int getYOffset() {
		return loc.getBlockY() & 0xF; // & 0xF is & 16, which is equal to mod 16
	}
	
	public int getZOffset() {
		return loc.getBlockZ() & 0xF; // & 0xF is & 16, which is equal to mod 16
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ModifiedBlock))
			return false;
		return ((ModifiedBlock) obj).loc.equals(loc);
	}
}

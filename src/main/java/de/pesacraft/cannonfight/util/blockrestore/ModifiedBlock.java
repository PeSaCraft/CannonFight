package de.pesacraft.cannonfight.util.blockrestore;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ModifiedBlock {
	private final Material mat;
	private final byte data;
	
	private final Location loc;
	
	public ModifiedBlock(Block b) {
		loc = b.getLocation();
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
}

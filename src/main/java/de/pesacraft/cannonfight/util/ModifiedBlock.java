package de.pesacraft.cannonfight.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class ModifiedBlock {
	private final Material mat;
	private final byte data;
	
	private final int xOffset;
	private final int yOffset;
	private final int zOffset;
	
	public ModifiedBlock(Block b) {
		xOffset = b.getX() & 0xF; // & 0xF is & 16, which is equal to mod 16
		yOffset = b.getY() & 0xF; // & 0xF is & 16, which is equal to mod 16
		zOffset = b.getZ() & 0xF; // & 0xF is & 16, which is equal to mod 16
		
		mat = b.getType();
		data = b.getData();
	}

	public Material getMaterial() {
		return mat;
	}
	
	public byte getData() {
		return data;
	}
	
	public int getXOffset() {
		return xOffset;
	}
	
	public int getYOffset() {
		return yOffset;
	}
	
	public int getZOffset() {
		return zOffset;
	}
}

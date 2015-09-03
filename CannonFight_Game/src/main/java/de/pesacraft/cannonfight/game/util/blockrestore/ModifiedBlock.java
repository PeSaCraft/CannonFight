package de.pesacraft.cannonfight.game.util.blockrestore;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import de.pesacraft.cannonfight.game.players.CannonFighter;

public class ModifiedBlock {
	private final Material mat;
	private final byte data;
	
	private final World w;
	private final int x;
	private final int y;
	private final int z;
	
	public ModifiedBlock(Block b) {
		w = b.getWorld();
		x = b.getX();
		y = b.getY();
		z = b.getZ();
		
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
		return new Location(w, x, y, z);
	}
	
	public int getXOffset() {
		return x & 0xF; // & 0xF is & 16, which is equal to mod 16
	}
	
	public int getYOffset() {
		return y & 0xF; // & 0xF is & 16, which is equal to mod 16
	}
	
	public int getZOffset() {
		return z & 0xF; // & 0xF is & 16, which is equal to mod 16
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ModifiedBlock))
			return false;
		ModifiedBlock m = ((ModifiedBlock) obj);
		
		if (m.w != this.w) return false;
		if (m.x != this.x) return false;
		if (m.y != this.y) return false;
		if (m.z != this.z) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return (int) (w.getUID().getMostSignificantBits() << 16 + w.getUID().getLeastSignificantBits() + x << 8 + y << 4 + z);
	}
	
	@Override
	public String toString() {
		return "ModifiedBlock{world=" + w + ", x=" + x + ", y=" + y + ", z=" + z + ", mat=" + mat + ", data=" + data + "}";
	}
}

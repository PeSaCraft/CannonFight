package de.pesacraft.cannonfight.util.game;

import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockManager {

	public void setBlock(Block block, Material material, byte data);
	
	public void setBlockTemporary(Block block, Material material, byte data, long ticks);
}

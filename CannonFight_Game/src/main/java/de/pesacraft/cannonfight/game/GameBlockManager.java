package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import de.pesacraft.cannonfight.util.game.BlockManager;
import de.pesacraft.cannonfight.util.game.blockrestore.ModifiedBlock;

public class GameBlockManager implements BlockManager {
	final private List<BlockChange> blocks;
	
	
	public GameBlockManager() {
		blocks = new ArrayList<BlockChange>();
	}
	
	@Override
	public void setBlock(Block block, Material material, byte data) {
		ModifiedBlock b = new ModifiedBlock(block);
		
		BlockChange change = getPendingBlockChange(b);
		
		if (change != null) {
			// timer running for that block
			// -> gets cancelled
			change.cancel();
			blocks.remove(change);
		}
		
		block.setType(material);
		block.setData(data);
	}

	private BlockChange getPendingBlockChange(ModifiedBlock b) {
		for (BlockChange change : blocks)
			if (change.getBlock().isSimilar(b))
				return change;
		return null;
	}

	@Override
	public void setBlockTemporary(Block block, Material material, byte data, long ticks) {
		ModifiedBlock b = new ModifiedBlock(block);
		
		BlockChange change = getPendingBlockChange(b);
		if (change != null) {
			// timer running for that block
			// -> gets cancelled
			change.cancel();
			change.getBlock().restore();

			blocks.remove(change);
		}
		
		// new block change for new changes
		change = new BlockChange(block, material, data);
		change.runTaskLater(CannonFightGame.PLUGIN, ticks);
		
		// apply change
		block.setType(material);
		block.setData(data);
	}

}

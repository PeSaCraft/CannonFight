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
	final private List<BlockChange> blockChanges;
	
	final private List<BreakingBlock> breakingBlocks;
	
	public GameBlockManager() {
		blockChanges = new ArrayList<BlockChange>();
		breakingBlocks = new ArrayList<BreakingBlock>();
	}
	
	@Override
	public void setBlock(Block block, Material material, byte data) {
		ModifiedBlock b = new ModifiedBlock(block);
		
		BlockChange change = getPendingBlockChange(b);
		
		if (change != null) {
			// timer running for that block
			// -> gets cancelled
			change.cancel();
			blockChanges.remove(change);
		}
		
		block.setType(material);
		block.setData(data);
		
		BreakingBlock breaking = getBreakingBlock(block);
		if (breaking != null)
			breakingBlocks.remove(breaking);
		
	}

	private BlockChange getPendingBlockChange(ModifiedBlock b) {
		for (BlockChange change : blockChanges)
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

			blockChanges.remove(change);
		}
		
		// new block change for new changes
		change = new BlockChange(this, block, material, data);
		change.runTaskLater(CannonFightGame.PLUGIN, ticks);
		blockChanges.add(change);
		
		// apply change
		block.setType(material);
		block.setData(data);
	}

	public void removeBlockChange(BlockChange change) {
		blockChanges.remove(change);
	}

	@Override
	public void crackBlock(Block block, int amount) {
		BreakingBlock b = getBreakingBlock(block);
		
		if (b == null)
			b = new BreakingBlock(block, this);
		
		b.damage(amount);
	}
	
	private BreakingBlock getBreakingBlock(Block block) {
		for (BreakingBlock b : breakingBlocks)
			if (b.getBlock().equals(block))
				return b;
		return null;
	}
	
	public void destroyBlock(BreakingBlock breaking) {
		breakingBlocks.remove(breaking);
		
		setBlock(breaking.getBlock(), Material.AIR, (byte) 0);
	}
}

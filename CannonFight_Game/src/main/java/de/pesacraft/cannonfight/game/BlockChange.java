package de.pesacraft.cannonfight.game;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.util.game.BlockManager;
import de.pesacraft.cannonfight.util.game.blockrestore.ModifiedBlock;

public class BlockChange extends BukkitRunnable {
	private ModifiedBlock block;
	private GameBlockManager manager;
	public BlockChange(GameBlockManager manager, Block block, Material newMaterial, byte newData) {
		this.block = new ModifiedBlock(block);
		this.manager = manager;
	}

	@Override
	public void run() {
		this.block.restore();
		
		this.manager.remove(this);
	}
	
	public ModifiedBlock getBlock() {
		return block;
	}
}

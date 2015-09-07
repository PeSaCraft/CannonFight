package de.pesacraft.cannonfight.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import de.pesacraft.cannonfight.game.CageForm;
import de.pesacraft.cannonfight.util.game.blockrestore.ModifiedBlock;

public class Cage {
	
	private final CageForm form;
	
	private final Block center;
	
	private final List<ModifiedBlock> blocks;
	
	private boolean used;
	
	public Cage(CageForm form, Block center) {
		this.form = form;
		this.center = center;
		this.blocks = new ArrayList<ModifiedBlock>();
		this.used = false;
	}
	
	public void createCage(ItemStack material) throws IllegalStateException {
		if (used)
			throw new IllegalStateException("Cage already in use!");
		
		used = true;
		
		for (Triple<Integer, Integer, Integer> offset : form.getOffsets()) {
			Block b = center.getRelative(offset.getLeft(), offset.getMiddle(), offset.getRight());
			
			if (!b.getType().isSolid()) {
				// we have to make it solid and store it to restore it later
				blocks.add(new ModifiedBlock(b));
				
				// change non solid blocks
				b.setType(material.getType());
				b.setData(material.getData().getData());
			}	
		}
	}
	
	public void destroyCage() throws IllegalStateException {
		if (!used)
			throw new IllegalStateException("Cage not used: Nothing to remove!");
		
		used = false;
		
		for (ModifiedBlock b : blocks) {
			b.getLocation().getBlock().setType(b.getMaterial());
			b.getLocation().getBlock().setData(b.getData());
		}
		
		blocks.clear();
	}
}

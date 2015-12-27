package de.pesacraft.cannonfight.game;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class BreakingBlock {
	
	private final Block block;
	private final GameBlockManager manager;
	// if >= 10 block breaks
	private int state;
	
	public BreakingBlock(Block block, GameBlockManager manager) {
		this.state = -1;
		this.block = block;
		this.manager = manager;
	}

	public void damage() {
		damage(1);
	}
	
	public void damage(int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException();
		
		state += amount;
		
		if (state >= 10) {
			state = -1;
			
			manager.destroyBlock(this);
		}
		else {
			PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(block.hashCode(), new BlockPosition(block.getX(), block.getY(), block.getZ()), state);
			((CraftServer) Bukkit.getServer()).getHandle().sendAll(packet, ((CraftWorld) block.getWorld()).getHandle());
		}
	}
	
	public Block getBlock() {
		return block;
	}
}

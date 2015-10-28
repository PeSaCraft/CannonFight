package de.pesacraft.cannonfight.util.game;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

public class BreakingBlock {

	private static final List<BreakingBlock> blocks = new ArrayList<BreakingBlock>();
	
	public static BreakingBlock get(Location loc) {
		loc = loc.getBlock().getLocation();
		
		for (BreakingBlock b : blocks)
			if (b.loc.equals(loc))
				return b;

		BreakingBlock b = new BreakingBlock(loc);
		blocks.add(b);
		return b;
	}
	
	// if >= 10 block breaks
	private int state;
	private final Location loc;
	
	private BreakingBlock(Location loc) {
		this.state = -1;
		this.loc = loc;
	}

	public void damage() {
		damage(1);
	}
	
	public void damage(int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException();
		
		state += amount;
		
		if (state >= 10) {
			loc.getBlock().setType(Material.AIR);
			state = -1;
		}
		else {
			PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(loc.hashCode(), new BlockPosition(loc.getX(), loc.getY(), loc.getZ()), state);
			((CraftServer) Bukkit.getServer()).getHandle().sendAll(packet, (World) loc.getWorld());
		}
	}
}

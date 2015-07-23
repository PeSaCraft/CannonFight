package de.pesacraft.cannonfight.game.cannons;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.material.SpawnEgg;
import org.bukkit.util.Vector;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class CreeperCannon extends Cannon {
	
	public CreeperCannon(Map<String, Object> map) {
		super(map);
	}
	
	public CreeperCannon() {
		this.item = new SpawnEgg(EntityType.CREEPER).toItemStack();
		this.cooldown = 5000L;
	}

	@Override
	public void fire(CannonFighter fighter) {
		Player p = fighter.getPlayer();
		
		Block b = p.getLocation().getBlock().getRelative(0, -1, 0);
		
		Material m = b.getType();
		
		b.setType(Material.SKULL);
		Skull skull = (Skull) b.getState();
		skull.setSkullType(SkullType.CREEPER);
		skull.setOwner("SomeName");
		skull.update(true);
		
		FallingBlock fb = p.getWorld().spawnFallingBlock(p.getLocation(), Material.SKULL, b.getData());
	
		b.setType(m);
		fb.setVelocity(new Vector(0, 50, 0));
	}
	
}

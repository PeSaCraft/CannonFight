package de.pesacraft.cannonfight.util.game;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSnowball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.util.CannonFightUtil;

public class MovingParticle implements Listener {
	
	private final Snowball snowball;
	private final BukkitRunnable particleTrailRunnable;
	private final HitHandler hitHandler;
	
	public MovingParticle(Player shooter, HitHandler hitHandler) {
		this.snowball = shooter.launchProjectile(Snowball.class);
		this.hitHandler = hitHandler;
		
		PacketPlayOutEntityDestroy destroySnowballPacket = new PacketPlayOutEntityDestroy(((CraftSnowball) snowball).getHandle().getId());
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroySnowballPacket);
		}
		
		Bukkit.getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
		
		this.particleTrailRunnable = new BukkitRunnable() {
			@Override
			public void run() {
				MovingParticle.this.hitHandler.flying(snowball.getLocation());
			}
		};
		
		particleTrailRunnable.runTaskTimer(CannonFightUtil.PLUGIN, 1, 1);
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntity() != snowball)
			return;
		
		particleTrailRunnable.cancel();
		HandlerList.unregisterAll(this);
		
		hitHandler.hitBlock(event.getEntity().getLocation());
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() != snowball)
			return;
		
		particleTrailRunnable.cancel();
		HandlerList.unregisterAll(this);
		
		hitHandler.hitEntity(event);
	}
}

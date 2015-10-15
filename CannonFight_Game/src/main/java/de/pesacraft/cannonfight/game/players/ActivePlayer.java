package de.pesacraft.cannonfight.game.players;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.game.Cage;
import de.pesacraft.cannonfight.game.CageForm;
import de.pesacraft.cannonfight.game.CannonFightGame;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;

public class ActivePlayer extends Participant {
	private int lives;
	private int kills;
	
	private Cage cage;
	
	public ActivePlayer(CannonFighter player, String server) {
		super(player, server);
		
		lives = player.getLives();
		kills = 0;
	}
	
	public void createCage() throws IllegalStateException {
		if (hasCage())
			throw new IllegalStateException(Language.get("error.only-one-cage-instance"));
		
		cage = new Cage(CageForm.PLAYER, getPlayer().getPlayer().getLocation().getBlock());
		
		cage.createCage(new ItemStack(Material.BARRIER));
	}
	
	public void destroyCage() throws IllegalStateException {
		if (!hasCage())
			throw new IllegalStateException(Language.get("error.no-cage-created"));
		
		cage.destroyCage();
		
		cage = null;
	}
	
	public void createPlatform() throws IllegalStateException {
		final Cage platform = new Cage(CageForm.PLATFORM, getPlayer().getPlayer().getLocation().getBlock());
		
		platform.createCage(new ItemStack(Material.BARRIER));
		
		final ActivePlayer a = this;
		
		new BukkitRunnable() {
			int time = CannonFightGame.PLUGIN.getConfig().getInt("game.time.platform");
			
			@Override
			public void run() {
				if (time == 0) {
					a.getPlayer().sendMessage(Language.get("info.platform.dissapears-now"));
					platform.destroyCage();
					this.cancel();
					return;
				}
				a.getPlayer().sendMessage(Language.get("info.platform.dissapears.time").replaceAll("%time%", time + "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				time--;
			}
		}.runTaskTimer(CannonFightGame.PLUGIN, 0, 20);
	}
	
	public boolean teleportToGame(Location loc) {
		return getPlayer().teleport(loc);
	}
	
	public void looseLife() {
		if (isDead())
			return;
		lives--;
	}

	public boolean isDead() {
		return lives == 0;
	}
	
	public void addKill() {
		kills++;
	}
	
	public int getLives() {
		return lives;
	}
	
	public int getKills() {
		return kills;
	}

	public boolean hasCage() {
		return cage != null;
	}
}

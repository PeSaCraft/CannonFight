package de.pesacraft.cannonfight.game.players;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.game.Cage;
import de.pesacraft.cannonfight.game.CageForm;
import de.pesacraft.cannonfight.util.CannonFighter;

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
			throw new IllegalStateException("Only one cage can be created per player at the same time");
		
		cage = new Cage(CageForm.PLAYER, getPlayer().getPlayer().getLocation().getBlock());
		
		cage.createCage(new ItemStack(Material.BARRIER));
	}
	
	public void destroyCage() throws IllegalStateException {
		if (!hasCage())
			throw new IllegalStateException("No cage created!");
		
		cage.destroyCage();
		
		cage = null;
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
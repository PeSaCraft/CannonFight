package de.pesacraft.cannonfight.data.players;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.util.Database;
import de.pesacraft.lobbysystem.user.User;
import de.pesacraft.lobbysystem.user.Users;

public class CannonFighter {
	private final User user;
	private final int id;
	private int xp;
	private int level;
	
	public CannonFighter(Player p) {
		ResultSet result = Database.execute("SELECT * FROM " + Database.getTablePrefix() + "players WHERE uuid LIKE %" + p.getUniqueId() + "% LIMIT 1");
		
		int id = -1;	
		
		try {
			id = result.getInt("id");
			this.xp = result.getInt("xp");
			this.level = result.getInt("level");
		} catch (SQLException e) {
			e.printStackTrace();
			// spieler existiert nicht -> neu hinzufügen
		}
		
		this.id = id;
		this.user = Users.getByUUID(p.getUniqueId());
	}

	public void giveCoins(int amount) {
		CannonFight.MONEY.giveMoney(this, amount);
	}
	
	public boolean buyUpgrade(String cannonName) {
		int newLevel = cannonLevels.get(cannonName) + 1;
		double price = Cannons.getByName(cannonName).getUpgradePrice(newLevel);
		
		if (price > coins) return false;
		
		coins -= price;
		
		cannonLevels.put(cannonName, newLevel);
		
		return true;
	}
	
	/**
	 * Get the players current available ammo
	 * 
	 * @return the players ammo
	 */
	public long getAmmo() {
		return this.ammo;
	}
	
	/**
	 * Get the maximum ammo the player can have
	 * 
	 * @return the players maximum ammo
	 */
	public long getMaxAmmo() {
		return this.maxAmmo;
	}
	
	public int getID() {
		return this.id;
	}
	
	public User getUser() {
		return this.user;
	}
}

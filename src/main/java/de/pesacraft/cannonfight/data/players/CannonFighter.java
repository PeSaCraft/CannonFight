package de.pesacraft.cannonfight.data.players;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.Cooldown;
import de.pesacraft.cannonfight.util.Database;
import de.pesacraft.lobbysystem.user.User;
import de.pesacraft.lobbysystem.user.Users;

public class CannonFighter {
	private final User user;
	private final int id;
	private int xp;
	private int level;
	
	private List<Cannon> cannons;
	
	
	private CannonFighter(Player p) {
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
		
		this.cannons = new ArrayList<Cannon>();
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
	
	public int getID() {
		return this.id;
	}
	
	public User getUser() {
		return this.user;
	}

	public boolean teleportToGame(Location loc) {
		return user.teleport(loc);
	}
	
	public void show(CannonFighter c) {
		user.getPlayer().showPlayer(c.user.getPlayer());
	}
	
	public void hide(CannonFighter c) {
		user.getPlayer().hidePlayer(c.user.getPlayer());
	}
	
	public void sendMessage(String msg) {
		user.sendMessage(msg);
	}
	
	public String getName() {
		return user.getPlayer().getName();
	}
	
	private static Map<String, CannonFighter> online = new HashMap<String, CannonFighter>();
	
	public static CannonFighter get(Player p) {
		if (online.containsKey(p.getName()))
			return online.get(p.getName());
		
		CannonFighter c = new CannonFighter(p);
		online.put(p.getName(), c);
		
		return c;
	}
	
	public static CannonFighter remove(Player p) {
		return online.remove(p.getName());
	}

	public Player getPlayer() {
		return user.getPlayer();
	}

	public boolean use(ItemStack item) {
		// find the used cannon
		for (Cannon c : cannons) {
			if (c.getItemStack().equals(item)) {
				// that is the used cannon
				try {
					return c.fire(this);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return false;
	}
}

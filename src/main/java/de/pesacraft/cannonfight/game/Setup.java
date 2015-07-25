package de.pesacraft.cannonfight.game;

import java.sql.SQLException;

import org.bukkit.Location;

import de.pesacraft.cannonfight.util.Database;

public class Setup {
	private String name;
	
	private Location loc1;
	private Location loc2;
	
	private int reqPlayers;
	
	private Location specLoc;
	
	private boolean added;
	
	private int id;
	
	public boolean setLocation1(Location loc) {
		if (added)
			return false;
		
		if (loc2 != null && loc.getWorld() != loc2.getWorld())
			return false;
		
		this.loc1 = loc;
		
		insert();
		return true;
	}
	
	public boolean setLocation2(Location loc) {
		if (added)
			return false;
		
		if (loc1 != null && loc.getWorld() != loc1.getWorld())
			return false;
		
		this.loc2 = loc;
		
		insert();
		return true;
	}
	
	public boolean setName(String name) {
		if (added)
			return false;
		
		this.name = name;
		
		insert();
		return true;
	}
	
	public boolean setRequiredPlayers(int amount) {
		if (added)
			return false;
		
		if (amount < 0)
			return false;
		
		this.reqPlayers = amount;
		
		insert();
		return true;
	}
	
	public boolean setSpectatorLocation(Location loc) {
		if (added)
			return false;
		
		if ((loc1 != null && loc.getWorld() != loc1.getWorld()) || (loc2 != null && loc.getWorld() != loc2.getWorld()))
			return false;
		
		this.specLoc = loc;
		
		insert();
		return true;
	}
	
	public boolean allSet() {
		return loc1 != null && loc2 != null && specLoc != null && name != null;
	}
	
	public void insert() {
		if (!allSet())
			return;
		
		if (added)
			return;
		
		added = true;
		
		String query = "INSERT INTO  `" + Database.getTablePrefix() + "arenas` (";
		query += "`id`, `name`, `requiredPlayers`, `world`, `x1`, `y1`, `z1`, `x2`, `y2`, `z2`, `spectatorX`, `spectatorY`, `spectatorZ`) ";
		query += "VALUES (NULL,";
		
		query += "'" + name + "',";
		query += "'" + reqPlayers + "',";
		query += "'" + loc1.getWorld().getName() + "',";
		
		query += "'" + loc1.getBlockX() + "',";
		query += "'" + loc1.getBlockY() + "',";
		query += "'" + loc1.getBlockZ() + "',";
		
		query += "'" + loc2.getBlockX() + "',";
		query += "'" + loc2.getBlockY() + "',";
		query += "'" + loc2.getBlockZ() + "',";
		
		query += "'" + specLoc.getBlockX() + "',";
		query += "'" + specLoc.getBlockY() + "',";
		query += "'" + specLoc.getBlockZ() + "'";
		
		query += ")";
		
		Database.execute(query, false);
		
		try {
			id = Database.execute("SELECT * FROM " + Database.getTablePrefix() + "arenas WHERE name = " + name, true).getInt("id");
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean addSpawn(Location loc) {
		if (!added)
			return false;
		
		String query = "INSERT INTO  `" + Database.getTablePrefix() + "spawns` (";
		query += "`id`, `arena`, `x`, `y`, `z`, `yaw`, `pitch`) ";
		query += "VALUES (NULL,";
		
		query += "'" + id + "',";
		
		query += "'" + loc.getX() + "',";
		query += "'" + loc.getY() + "',";
		query += "'" + loc.getZ() + "',";
		
		query += "'" + loc.getYaw() + "',";
		query += "'" + loc.getPitch() + "'";
	
		query += ")";
		
		Database.execute(query, false);
		
		return true;
	}
}

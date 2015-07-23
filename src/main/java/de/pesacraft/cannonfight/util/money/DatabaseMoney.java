package de.pesacraft.cannonfight.util.money;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.Database;

public class DatabaseMoney implements Money {

	@Override
	public int getMoney(CannonFighter c) {
		ResultSet result = Database.execute("SELECT coins FROM " + Database.getTablePrefix() + "players WHERE id = " + c.getID());
		try {
			return result.getInt("coins");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean giveMoney(CannonFighter c, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		ResultSet result = Database.execute("SELECT coins FROM " + Database.getTablePrefix() + "players WHERE id = " + c.getID());
		
		try {
			int newAmount = result.getInt("coins") + amount;
			Database.execute("UPDATE " + Database.getTablePrefix() + "players SET coins = " + newAmount + " WHERE id = " + c.getID());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean takeMoney(CannonFighter c, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		ResultSet result = Database.execute("SELECT coins FROM " + Database.getTablePrefix() + "players WHERE id = " + c.getID());
		
		try {
			int newAmount = result.getInt("coins") - amount;
			
			if (newAmount < 0)
				return false;
			
			Database.execute("UPDATE " + Database.getTablePrefix() + "players SET coins = " + newAmount + " WHERE id = " + c.getID());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

}

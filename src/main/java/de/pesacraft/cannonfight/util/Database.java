package de.pesacraft.cannonfight.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.Configuration;

import de.pesacraft.cannonfight.CannonFight;

public class Database {
	private static Connection conn;
	private static String tablePrefix;
	
	static {
		setup();
	}
	
	public static void setup() {
		Configuration conf = CannonFight.PLUGIN.getConfig();
		
		String host = conf.getString("database.host");
		int port = conf.getInt("database.port");
		
		String db = conf.getString("database.database");
		
		String user = conf.getString("database.username");
		String pass = conf.getString("database.password");
		
		tablePrefix = conf.getString("database.prefix");
	
		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
		} catch (SQLException e) {
			CannonFight.LOGGER.severe("Couldn't connect to the MySQL database " + db + " at " + host + ":" + port + " with user " + user);
			e.printStackTrace();
		}
		
		setupTables();
	}

	private static void setupTables() {
		// create players table
		String query = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "players` (";
		query += "`id` int(11) NOT NULL AUTO_INCREMENT,";
		query += "`uuid` varchar(255) NOT NULL,";
		query += "`xp` int(11) NOT NULL,";
		query += "`level` int(11) NOT NULL,";
		query += "PRIMARY KEY (`id`),";
		query += "UNIQUE KEY `uuid` (`uuid`)";
		query += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";
		
		execute(query, false);
		
		// create table for players cannon levels
		query = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "cannonLevels` (";
		query += "`id` int(11) NOT NULL,";
		query += "PRIMARY KEY (`id`)";
		query += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";
		
		execute(query, false);
		
		// create cannon table
		query = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "cannons` (";
		query += "`id` int(11) NOT NULL AUTO_INCREMENT,";
		query += "`name` varchar(255) NOT NULL,";
		query += "PRIMARY KEY (`id`),";
		query += "UNIQUE KEY `name` (`name`)";
		query += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";
		
		execute(query, false);
		
		// create cannon upgrade table
		query = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "cannonUpgrades` (";
		query += "`id` int(11) NOT NULL AUTO_INCREMENT,";
		query += "`cannon` int(11) NOT NULL,";
		query += "`level` int(11) NOT NULL,";
		query += "`ammo` int(11) NOT NULL,";
		query += "`ammoPrice` int(11) NOT NULL,";
		query += "`cooldown` int(11) NOT NULL,";
		query += "`cooldownPrice` int(11) NOT NULL,";
		query += "`custom1` double NOT NULL,";
		query += "`custom1Price` int(11) NOT NULL,";
		query += "`custom2` double NOT NULL,";
		query += "`custom2Price` int(11) NOT NULL,";
		query += "`custom3` double NOT NULL,";
		query += "`custom3Price` int(11) NOT NULL,";
		query += "`custom4` double NOT NULL,";
		query += "`custom4Price` int(11) NOT NULL,";
		query += "PRIMARY KEY (`id`)";
		query += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";

		execute(query, false);

		// create arenas table
		query = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "arenas` (";
		query += "`id` int(11) NOT NULL AUTO_INCREMENT,";
		query += "`name` varchar(255) NOT NULL,";
		query += "`requiredPlayers` int(11) NOT NULL,";
		query += "`world` varchar(255) NOT NULL,";
		query += "`x1` int(11) NOT NULL,";
		query += "`y1` int(11) NOT NULL,";
		query += "`z1` int(11) NOT NULL,";
		query += "`x2` int(11) NOT NULL,";
		query += "`y2` int(11) NOT NULL,";
		query += "`z2` int(11) NOT NULL,";
		query += "`spectatorX` int(11) NOT NULL,";
		query += "`spectatorY` int(11) NOT NULL,";
		query += "`spectatorZ` int(11) NOT NULL,";
		query += "PRIMARY KEY (`id`),";
		query += "UNIQUE KEY `name` (`name`)";
		query += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";
		
		execute(query, false);
		
		// create table for arena spawns
		query = "CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "spawns` (";
		query += "`id` int(11) NOT NULL AUTO_INCREMENT,";
		query += "`arena` int(11) NOT NULL,";
		query += "`x` double NOT NULL,";
		query += "`y` double NOT NULL,";
		query += "`z` double NOT NULL,";
		query += "`yaw` float NOT NULL,";
		query += "`pitch` float NOT NULL,";
		query += "PRIMARY KEY (`id`)";
		query += ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";

		execute(query, false);
	}

	public static ResultSet execute(String sql, boolean pointOnFirst) {
		try {
			Statement statement = conn.createStatement();
			statement.execute(sql);
			if (pointOnFirst)
				statement.getResultSet().next();
			return statement.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getTablePrefix() {
		return tablePrefix;
	}
}

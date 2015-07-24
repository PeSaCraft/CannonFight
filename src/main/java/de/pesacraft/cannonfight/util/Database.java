package de.pesacraft.cannonfight.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.configuration.Configuration;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.data.players.CannonFighter;

public class Database {
	private static boolean useMySQL;
	
	private static Connection conn;
	private static String tablePrefix;
	
	static {
		setup();
	}
	
	private static void setup() {
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
	}

	public static ResultSet execute(String sql) {
		try {
			Statement statement = conn.createStatement();
			statement.execute(sql);
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
	
	public static boolean useMySQL() {
		return useMySQL;
	}
	
	private static void setupDataFiles() {
		
	}
}

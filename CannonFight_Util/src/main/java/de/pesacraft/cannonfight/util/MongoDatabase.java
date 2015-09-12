package de.pesacraft.cannonfight.util;

import org.bukkit.configuration.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


public class MongoDatabase {
	private static com.mongodb.client.MongoDatabase db;
	private static MongoClient mongo;
	public static String PREFIX;
	
	public static void setup() {
		Configuration conf = CannonFightUtil.PLUGIN.getConfig();
		
		PREFIX = conf.getString("database.prefix");
		
		String host = conf.getString("database.host");
		int port = conf.getInt("database.port");
		
		String dbName = conf.getString("database.database");
		
		String user = conf.getString("database.auth.username");
		String pass = conf.getString("database.auth.password");
		String authDB = conf.getString("database.auth.db");
		
		mongo = new MongoClient(new MongoClientURI("mongodb://" + user + ":" + pass + "@" + host + "/?authSource=" + authDB));
		
		db = mongo.getDatabase(dbName);
		
		Collection.load(db);
	}

	public static void close() {
		mongo.close();
		
		db = null;
		mongo = null;
	}
}

package de.pesacraft.cannonfight.util;

import org.bson.Document;
import org.bukkit.configuration.Configuration;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.CannonFight;

public class MongoDatabase {
	private static com.mongodb.client.MongoDatabase db;
	private static MongoClient mongo;
	public static String PREFIX;
	
	public static void setup() {
		Configuration conf = CannonFight.PLUGIN.getConfig();
		
		PREFIX = conf.getString("database.prefix");
		
		String host = conf.getString("database.host");
		int port = conf.getInt("database.port");
		
		String dbName = conf.getString("database.database");
		
		String user = conf.getString("database.auth.username");
		String pass = conf.getString("database.auth.password");
		String authDB = conf.getString("database.auth.db");
		
		mongo = new MongoClient(new MongoClientURI("mongodb://" + user + ":" + pass + "@" + host + "/?authSource=" + authDB));
		
		db = mongo.getDatabase(dbName);
		
		setupCollections();
	}
	
	private static void setupCollections() {
		db.createCollection(PREFIX + "players");
		
		db.createCollection(PREFIX + "items");
		
		db.createCollection(PREFIX + "arenas");
		
		Collection.load(db);
	}
	
	public static void close() {
		mongo.close();
		
		db = null;
		mongo = null;
	}
}

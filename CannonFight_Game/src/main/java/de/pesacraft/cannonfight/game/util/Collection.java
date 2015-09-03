package de.pesacraft.cannonfight.game.util;

import org.bson.Document;

import com.mongodb.DB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;

import de.pesacraft.cannonfight.game.util.MongoDatabase;

public class Collection {
	private static MongoCollection<Document> PLAYERS;
	private static MongoCollection<Document> ITEMS;
	private static MongoCollection<Document> ARENAS;
	
	private static String playersCollectionName;
	private static String itemsCollectionName;	
	private static String arenasCollectionName;
	
	protected static void load(com.mongodb.client.MongoDatabase db) {
		playersCollectionName = MongoDatabase.PREFIX + "players";
		itemsCollectionName = MongoDatabase.PREFIX + "items";	
		arenasCollectionName = MongoDatabase.PREFIX + "arenas";
		
		MongoIterable<String> collections = db.listCollectionNames();
		
		boolean createPlayers = true, createItems = true, createArenas = true;
		
		for (String c : collections) {
			if (c.equals(playersCollectionName))
				createPlayers = false;
			if (c.equals(itemsCollectionName))
				createItems = false;
			if (c.equals(arenasCollectionName))
				createArenas = false;	
		}
		
		if (createPlayers)
			db.createCollection(playersCollectionName);
		
		if (createItems)
			db.createCollection(itemsCollectionName);
		
		if (createArenas)
			db.createCollection(arenasCollectionName);
		
		PLAYERS = db.getCollection(playersCollectionName);
		
		ITEMS = db.getCollection(itemsCollectionName);
		
		ARENAS = db.getCollection(arenasCollectionName);
	}
	
	public static MongoCollection<Document> PLAYERS() {
		return PLAYERS;
	}
	
	public static MongoCollection<Document> ITEMS() {
		return ITEMS;
	}
	public static MongoCollection<Document> ARENAS() {
		return ARENAS;
	}
}

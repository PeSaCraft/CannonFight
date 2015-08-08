package de.pesacraft.cannonfight.util;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

public class Collection {
	private static MongoCollection<Document> PLAYERS;
	private static MongoCollection<Document> ITEMS;
	private static MongoCollection<Document> ARENAS;

	protected static void load(com.mongodb.client.MongoDatabase db) {
		PLAYERS = db.getCollection(MongoDatabase.PREFIX + "players");
		
		ITEMS = db.getCollection(MongoDatabase.PREFIX + "items");
		
		ARENAS = db.getCollection(MongoDatabase.PREFIX + "arenas");
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

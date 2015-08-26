package de.pesacraft.cannonfight.data.players;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.game.Arena;
import de.pesacraft.cannonfight.game.Game;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.lobby.shops.UpgradeShop;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.MongoDatabase;
import de.pesacraft.cannonfight.util.Upgrade;
import de.pesacraft.lobbysystem.user.User;
import de.pesacraft.lobbysystem.user.Users;
import static com.mongodb.client.model.Filters.*;

public class CannonFighter {
	private static final MongoCollection<Document> COLLECTION;
	private final User user;
	private int xp;
	
	private int slotsLevel;
	private int slots;
	
	private Game currentGame;
	private Arena inQueue;
	
	private List<Cannon> activeItems = new ArrayList<Cannon>();
	
	private Map<String, Cannon> cannons;
	
	static {
		COLLECTION = Collection.PLAYERS();
	}
	
	@SuppressWarnings("unchecked")
	private CannonFighter(Player p) {
		Document doc = COLLECTION.find(eq("uuid", p.getUniqueId().toString())).first();
		
		if (doc != null) {
			// Player in database
			xp = ((Number) doc.get("xp")).intValue();
			
			slotsLevel = ((Number) doc.get("slotsLevel")).intValue();
			slots = UpgradeShop.getSlotsUpgradeForLevel(slotsLevel).getValue();
			
			cannons = new HashMap<String, Cannon>();
			Document cannons = (Document) doc.get("cannons");
			
			for (Entry<String, Object> entry : cannons.entrySet()) {
				CannonConstructor constructor = Cannons.getConstructorByName(entry.getKey());
				
				this.cannons.put(entry.getKey(), constructor.construct(this, (Document) entry.getValue()));
			}
			
			List<String> activeItems = (List<String>) doc.get("activeItems");
			
			for (String c : activeItems) {
				this.activeItems.add(this.cannons.get(c));
			}
			
			// fill list
			while (this.activeItems.size() < this.slots)
				this.activeItems.add(null);
		}
		else {
			// Player not in database
			xp = 0;
			
			slotsLevel = 1;
			slots = UpgradeShop.getSlotsUpgradeForLevel(slotsLevel).getValue();
			
			cannons = new HashMap<String, Cannon>();
			
			doc = new Document("uuid", p.getUniqueId().toString());
			doc = doc.append("xp", 0);
			doc = doc.append("slotsLevel", slotsLevel);
			doc = doc.append("activeItems", activeItems);
			doc = doc.append("cannons", cannons);
			
			COLLECTION.insertOne(doc);
		}
		
		this.user = Users.getByUUID(p.getUniqueId());
	}

	public void giveCoins(int amount) {
		CannonFight.MONEY.giveMoney(this, amount);
	}
	
	public boolean takeCoins(int amount, String... reason) {
		return CannonFight.MONEY.takeMoney(this, amount, reason);
	}
	
	public boolean hasEnoughCoins(int amount) {
		return CannonFight.MONEY.hasEnoughMoney(this, amount);
	}
	
	public User getUser() {
		return this.user;
	}

	public boolean teleportToGame(Location loc, Game game) {
		if (currentGame != null && currentGame != game)
			return false;
		
		if (user.teleport(loc)) { 
			this.currentGame = game;
			this.inQueue = null;
			return true;
		}
		return false;
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
	
	public boolean use(ItemStack item) {
		// find the used cannon
		for (Cannon c : activeItems) {
			if (c == null)
				// skip empty
				continue;
			
			if (c.getItem().equals(item)) {
				// that is the used cannon
				try {
					return c.fire();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return false;
	}

	public boolean leaveGame() {
		if (isInGame()) {
			currentGame = null;
			user.leave();
			return true;
		}
		// in keinem spiel
		return false;
	}

	public boolean leaveQueue() {
		if (isInQueue()) {
			inQueue = null;
			return true;
		}
		// in keiner queue
		return false;
	}
	
	public Game getCurrentGame() {
		return this.currentGame;
	}
	
	public Player getPlayer() {
		return user.getPlayer();
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

	public boolean hasPermission(String perm) {
		return getPlayer().hasPermission(perm);
	}

	public boolean setInQueue(Arena a) {
		if (inQueue != null)
			return false;
		
		inQueue = a;
		return true;
		
	}
	
	public boolean isInQueue() {
		return inQueue != null;
	}

	public boolean isInGame() {
		return currentGame != null;
	}

	public void setCurrentGame(Game game) {
		this.currentGame = game;
	}

	public Arena getArenaQueuing() {
		return inQueue;
	}

	public List<Cannon> getActiveItems() {
		return this.activeItems;
	}
	
	public boolean isSelected(String cannon) {
		return this.activeItems.contains(getCannon(cannon));
	}
	
	public int getActivePosition(String cannonName) {
		return this.activeItems.indexOf(getCannon(cannonName));
	}
	
	public boolean hasCannon(String name) {
		return cannons.containsKey(name);
	}
	
	public Cannon getCannon(String name) {
		return cannons.get(name);
	}

	public void addCannon(Cannon cannon) {
		cannons.put(cannon.getName(), cannon);
	}
	
	public int getSlotsLevel() {
		return slotsLevel;
	}
	
	public int getSlots() {
		return slots;
	}
	
	public boolean upgradeSlots() {
		Upgrade<Integer> upgrade = UpgradeShop.getSlotsUpgradeForLevel(slotsLevel + 1);
		
		if (!hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		slotsLevel++;
		
		Collection.PLAYERS().updateOne(eq("uuid", getPlayer().getUniqueId().toString()), new Document("$set", new Document("slotsLevel", slotsLevel)));
		
		slots = upgrade.getValue();
		
		takeCoins(upgrade.getPrice(), UpgradeShop.NAME_SLOTS + "-Upgrade auf Level " + slotsLevel);
		
		return true;
	}
	
	public Cannon getActiveItem(int pos) {
		try {
			return this.activeItems.get(pos);
		}
		catch (IndexOutOfBoundsException ex) {
			// not a valid index: no cannon there
			return null;
		}
	}
	
	public Cannon selectCannonToSlot(int pos, String cannon) {
		try {
			return this.activeItems.set(pos, getCannon(cannon));
		}
		catch (IndexOutOfBoundsException ex) {
			// too huge index: return null as there is no cannon
			return null;
		}
	}
	
	public void deselectCannon(String name) {
		this.activeItems.set(this.activeItems.indexOf(getCannon(name)), null);
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof CannonFighter) ? ((CannonFighter) obj).getName().equals(this.getName()) : false;
	}
}

package de.pesacraft.cannonfight.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.shop.implemented.UpgradeShop;
import de.pesacraft.cannonfight.util.shop.upgrade.Upgrade;
import static com.mongodb.client.model.Filters.*;

public class CannonFighter {
	private static final MongoCollection<Document> COLLECTION;
	
	private final UUID uuid;
	private final OfflinePlayer player;
	
	private int coins;
	private int xp;
	
	private int slotsLevel;
	private int slots;
	
	private int livesLevel;
	private int lives;
	
	private List<Cannon> activeItems = new ArrayList<Cannon>();
	
	private Map<String, Cannon> cannons;
	
	static {
		COLLECTION = Collection.PLAYERS();
	}
	
	@SuppressWarnings("unchecked")
	private CannonFighter(UUID uuid) {
		this.uuid = uuid;
		this.player = Bukkit.getOfflinePlayer(uuid);
		
		Document doc = COLLECTION.find(eq("uuid", uuid.toString())).first();
		
		if (doc != null) {
			// Player in database
			coins = ((Number) doc.get("coins")).intValue();
			xp = ((Number) doc.get("xp")).intValue();
			
			slotsLevel = ((Number) doc.get("slotsLevel")).intValue();
			slots = UpgradeShop.getSlotsUpgradeForLevel(slotsLevel).getValue();
			
			livesLevel = ((Number) doc.get("livesLevel")).intValue();
			lives = UpgradeShop.getLivesUpgradeForLevel(livesLevel).getValue();
			
			cannons = new HashMap<String, Cannon>();
			Document cannons = (Document) doc.get("cannons");
			
			for (Entry<String, Object> entry : cannons.entrySet()) {
				CannonConstructor constructor = Cannons.getConstructorByName(entry.getKey());
				
				this.addCannon(constructor.construct(this, (Document) entry.getValue()));
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
			coins = 0;
			xp = 0;
			
			slotsLevel = 1;
			slots = UpgradeShop.getSlotsUpgradeForLevel(slotsLevel).getValue();
			
			livesLevel = 1;
			slots = UpgradeShop.getLivesUpgradeForLevel(livesLevel).getValue();
			
			doc = new Document("uuid", uuid.toString());
			doc = doc.append("coins", 0);
			doc = doc.append("xp", 0);
			doc = doc.append("slotsLevel", slotsLevel);
			doc = doc.append("livesLevel", livesLevel);
			
			// cannons don't have to be written into the db here
			// they will be added when a new cannon is bought.
			COLLECTION.insertOne(doc);
			
			cannons = new HashMap<String, Cannon>();
			CannonConstructor constructor = Cannons.getConstructorByName(Cannons.getDefaultCannon());
			Cannon defaultCannon = constructor.buyNew(this);
			this.addCannon(defaultCannon);
			
			// now the active items can be stored
			List<String> activeStrings = new ArrayList<String>();
			for (Cannon c : getActiveItems()) {
				if (c == null)
					activeStrings.add("null");
				else
					activeStrings.add(c.getName());
			}
			
			COLLECTION.updateOne(eq("uuid", uuid.toString()), new Document("$set", new Document("activeItems", activeStrings)));
		}
		
		// update players name
		COLLECTION.updateOne(eq("uuid", uuid.toString()), new Document("$set", new Document("name", getName())));
	}
	
	public int getCoins() {
		return coins;
	}

	public void setCoins(int amount) {
		if (amount < 0)
			throw new IllegalArgumentException();
		coins = amount;
	}

	public void giveCoins(int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException();
		coins += amount;
	}
	
	public void takeCoins(int amount) {
		if (amount <= 0 || !hasEnoughCoins(amount))
			throw new IllegalArgumentException();
		
		coins -= amount;
	}
	
	public boolean hasEnoughCoins(int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException();
		
		return coins >= amount;
	}
	
	public void show(CannonFighter c) {
		getPlayer().showPlayer(c.getPlayer());
	}
	
	public void hide(CannonFighter c) {
		getPlayer().hidePlayer(c.getPlayer());
	}
	
	public void sendMessage(String msg) {
		getPlayer().sendMessage(msg);
	}
	
	public String getName() {
		return getOfflinePlayer().getName();
	}
	
	public boolean use(ItemStack item) {
		// find the used cannon
		for (Cannon c : activeItems) {
			if (c == null)
				// skip empty
				continue;
			
			if (c.getItem().isSimilar(item))
				// that is the used cannon
				return c.fire(item);
		}
		
		return false;
	}
	
	public Player getPlayer() {
		return this.player.getPlayer();
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return this.player;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	private static Map<UUID, CannonFighter> players = new HashMap<UUID, CannonFighter>();
	
	public static CannonFighter get(OfflinePlayer p) {
		if (players.containsKey(p.getUniqueId()))
			return players.get(p.getUniqueId());
		
		CannonFighter c = new CannonFighter(p.getUniqueId());
		players.put(p.getUniqueId(), c);
		
		return c;
	}
	
	@Deprecated
	public static CannonFighter get(Player p) {
		return get((OfflinePlayer) p);
	}
	
	public static CannonFighter remove(OfflinePlayer p) {
		CannonFighter c = players.remove(p.getUniqueId());
		if (c != null)
			c.save();
		return c;
	}

	@Deprecated
	public static CannonFighter remove(Player p) {
		return remove((OfflinePlayer) p);
	}

	public static void remove(CannonFighter c) {
		remove(c.getOfflinePlayer());
	}

	public static void saveAll() {
		for (CannonFighter c : players.values()) {
			c.save();
		}
	}
	
	public boolean hasPermission(String perm) {
		return getPlayer().hasPermission(perm);
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
		
		takeCoins(upgrade.getPrice());
		
		return true;
	}
	
	public int getLivesLevel() {
		return livesLevel;
	}
	
	public int getLives() {
		return lives;
	}
	
	public boolean upgradeLives() {
		Upgrade<Integer> upgrade = UpgradeShop.getLivesUpgradeForLevel(livesLevel + 1);
		
		if (!hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		livesLevel++;
		
		Collection.PLAYERS().updateOne(eq("uuid", getPlayer().getUniqueId().toString()), new Document("$set", new Document("livesLevel", livesLevel)));
		
		lives = upgrade.getValue();
		
		takeCoins(upgrade.getPrice());
		
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
			Cannon c = this.activeItems.set(pos, getCannon(cannon));
			return c;
		}
		catch (IndexOutOfBoundsException ex) {
			// too huge index: return null as there is no cannon
			return null;
		}
	}
	
	public void deselectCannon(String name) {
		this.activeItems.set(this.activeItems.indexOf(getCannon(name)), null);
	}
	
	public void save() {
		Document doc = new Document("uuid", uuid.toString()).append("name", this.getName());
		doc.append("coins", coins);
		doc.append("xp", xp);
		doc.append("slotsLevel", slotsLevel);
		doc.append("livesLevel", livesLevel);
		
		Map<String, Document> serializedCannons = new HashMap<String, Document>();
		for (Entry<String, Cannon> cannon : cannons.entrySet())
			serializedCannons.put(cannon.getKey(), cannon.getValue().serializeLevels());
		doc.append("cannons", serializedCannons);
		
		List<String> serializedActive = new ArrayList<String>();
		for (Cannon c : getActiveItems()) {
			if (c == null)
				serializedActive.add("null");
			else
				serializedActive.add(c.getName());
		}
		
		doc.append("activeItems", serializedActive);
		
		// update players name
		COLLECTION.replaceOne(eq("uuid", uuid.toString()), doc, new UpdateOptions().upsert(true));
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode() + getUUID().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CannonFighter))
			return false;
		return ((CannonFighter) obj).getName().equals(this.getName());
	}

	public void resetCannons() {
		for (Cannon c : getActiveItems()) {
			if (c == null)
				continue;
			c.reset();
		}
	}

	public boolean teleport(Location loc) {
		return getPlayer().teleport(loc);
	}
}

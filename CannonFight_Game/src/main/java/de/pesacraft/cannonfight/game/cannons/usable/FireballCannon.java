package de.pesacraft.cannonfight.game.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.game.CannonFightGame;
import de.pesacraft.cannonfight.game.util.Collection;
import de.pesacraft.cannonfight.game.util.ItemSerializer;
import de.pesacraft.cannonfight.game.util.Upgrade;
import de.pesacraft.cannonfight.game.util.shop.ClickHandler;
import de.pesacraft.cannonfight.game.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.game.util.shop.Shop;
import de.pesacraft.cannonfight.game.util.shop.ShopGroup;
import de.pesacraft.cannonfight.game.util.shop.ShopMaker;
import de.pesacraft.cannonfight.game.Language;
import de.pesacraft.cannonfight.game.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.game.cannons.usable.FireballCannon;

@SuppressWarnings("unchecked")
public class FireballCannon extends Cannon implements Listener {

	private static final MongoCollection<Document> COLLECTION;
	
	/**
	 * ItemStack representing the cannon ingame
	 */
	protected static final ItemStack ITEM;
	/**
	 * The cannons name
	 */
	public static final String NAME = "FireballCannon";
	
	protected static final Map<Integer, Upgrade<Integer>> COOLDOWN_MAP = new HashMap<Integer, Upgrade<Integer>>();
	
	protected static final Map<Integer, Upgrade<Integer>> AMMO_MAP = new HashMap<Integer, Upgrade<Integer>>();
	
	protected static final Map<Integer, Upgrade<Double>> RADIUS_MAP = new HashMap<Integer, Upgrade<Double>>();
	
	protected static final Map<Integer, Upgrade<Integer>> DAMAGE_MAP = new HashMap<Integer, Upgrade<Integer>>();
	
	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM = ItemSerializer.deserialize((Document) doc.get("item"));
			
			Document cooldownList = (Document) doc.get("cooldown");
			for (Entry<String, Object> cooldown : cooldownList.entrySet())
				COOLDOWN_MAP.put(Integer.parseInt(cooldown.getKey()), new Upgrade<Integer>((Document) cooldown.getValue()));
			
			Document ammoList = (Document) doc.get("ammo");
			for (Entry<String, Object> ammo : ammoList.entrySet())
				AMMO_MAP.put(Integer.parseInt(ammo.getKey()), new Upgrade<Integer>((Document) ammo.getValue()));
			
			Document radiusList = (Document) doc.get("radius");
			for (Entry<String, Object> radius : radiusList.entrySet())
				RADIUS_MAP.put(Integer.parseInt(radius.getKey()), new Upgrade<Double>((Document) radius.getValue()));
			
			Document damageList = (Document) doc.get("damage");
			for (Entry<String, Object> damage : damageList.entrySet())
				DAMAGE_MAP.put(Integer.parseInt(damage.getKey()), new Upgrade<Integer>((Document) damage.getValue()));
			
		}
		else {
			// Cannon not in database
			ITEM = new ItemStack(Material.FIREBALL);
			
			ItemMeta m = ITEM.getItemMeta();
			m.setDisplayName(NAME);
			ITEM.setItemMeta(m);
			
			Map<String, Object> coolMap = new HashMap<String, Object>();
			for (int i = 1; i <= 10; i++) {
				Upgrade<Integer> u = new Upgrade<Integer>(i * 100, 11 - i);
				COOLDOWN_MAP.put(i, u);
				coolMap.put(i + "", u);
			}
			
			Map<String, Object> ammoMap = new HashMap<String, Object>();
			for (int i = 1; i <= 10; i++) {
				Upgrade<Integer> u = new Upgrade<Integer>(i * 100, i);
				AMMO_MAP.put(i, u);
				ammoMap.put(i + "", u);
			}
			
			Map<String, Object> radiusMap = new HashMap<String, Object>();
			for (int i = 1; i <= 10; i++) {
				Upgrade<Double> u = new Upgrade<Double>(i * 100, i * 0.2 + 2);
				RADIUS_MAP.put(i, u);
				radiusMap.put(i + "", u);
			}
			
			Map<String, Object> damageMap = new HashMap<String, Object>();
			for (int i = 1; i <= 10; i++) {
				Upgrade<Integer> u = new Upgrade<Integer>(i * 100, i + 1);
				DAMAGE_MAP.put(i, u);
				damageMap.put(i + "", u);
			}
		
			doc = new Document("name", NAME);
			
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM)));
			
			doc = doc.append("cooldown", new Document(coolMap));
			doc = doc.append("ammo", new Document(ammoMap));
			doc = doc.append("radius", new Document(radiusMap));
			doc = doc.append("damage", new Document(damageMap));
			
			COLLECTION.insertOne(doc);
		}
		
	}
	
	private static CannonConstructor constructor;
	
	public static void setup() {	
		constructor = new CannonConstructor() {
			
			@Override
			public Cannon construct(CannonFighter fighter, Map<String, Object> map) {
				int ammo = ((Number) map.get("ammo")).intValue();
				int cooldown = ((Number) map.get("cooldown")).intValue();
				int radius = ((Number) map.get("radius")).intValue();
				int damage = ((Number) map.get("damage")).intValue();
				
				return new FireballCannon(fighter, ammo, cooldown, radius, damage);
			}
			
			@Override
			public boolean canConstruct(String name) {
				return name.equals(NAME);
			}

			@Override
			public Cannon buyNew(CannonFighter fighter) {
				return new FireballCannon(fighter);
			}

			@Override
			public int getPrice() {
				return AMMO_MAP.get(1).getPrice() + COOLDOWN_MAP.get(1).getPrice() + RADIUS_MAP.get(1).getPrice() + DAMAGE_MAP.get(1).getPrice();
			}

			@Override
			public ItemStack getItem() {
				return ITEM.clone();
			}
		};
		
		Cannons.register(NAME, constructor);
	}

	public static ItemStack getItemStack() {
		return ITEM.clone();
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	private int maxAmmo;
	private int currentAmmo;
	private float radius;
	private int damage;
	private ItemStack item;
	
	private CannonFighter player;
	
	private List<Integer> shoot;
	
	private int levelAmmo;
	private int levelCooldown;
	private int levelRadius;
	private int levelDamage;
	
	public FireballCannon(CannonFighter player, int levelAmmo, int levelCooldown, int levelRadius, int levelDamage) {
		super(COOLDOWN_MAP.get(levelCooldown).getValue());
		
		this.levelAmmo = levelAmmo;
		this.levelCooldown = levelCooldown;
		this.levelRadius = levelRadius;
		this.levelDamage = levelDamage;
		
		this.player = player;
		
		currentAmmo = maxAmmo = AMMO_MAP.get(levelAmmo).getValue();
			
		radius = RADIUS_MAP.get(levelRadius).getValue().floatValue();
		damage = DAMAGE_MAP.get(levelDamage).getValue();
	
		item = ITEM.clone();
		item.setAmount(currentAmmo);
		shoot = new ArrayList<Integer>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFightGame.PLUGIN);
	}

	public FireballCannon(CannonFighter player) {
		this(player, 1, 1, 1, 1);
		
		Document doc = new Document("ammo", levelAmmo);
		doc = doc.append("cooldown", levelCooldown);
		doc = doc.append("radius", levelRadius);
		doc = doc.append("damage", levelDamage);
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME, doc)));
	}

	@Override
	public ItemStack getItem() {
		return item.clone();
	}
	
	@Override
	public boolean fire(ItemStack item) {
		if (this.item != item)
			this.item = item;
		
		if (!hasFinished())
			return false;
		
		if (--currentAmmo == 0)
			start();
		else
			item.setAmount(currentAmmo);
		
		Player p = player.getPlayer();
		
		Fireball fball = p.launchProjectile(Fireball.class);
		fball.setVelocity(fball.getVelocity().multiply(2));
		fball.setYield(radius);
		
		shoot.add(fball.getEntityId());
		return true;
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!shoot.contains(event.getDamager().getEntityId()))
			return;
		
		event.setDamage(damage);
	}
	
	@EventHandler
	public void onEntityExplosion(final EntityExplodeEvent event) {
		if (!shoot.contains(event.getEntity().getEntityId()))
			return;
		
		Iterator<Block> blocks = event.blockList().iterator();
		
		while (blocks.hasNext()) {
			Block b = blocks.next();
			if (!player.getCurrentGame().locIsInArena(b.getLocation())) {
				// block not part of map -> won't be destroyed
				blocks.remove();
			}
		}
		// drop nothing
		event.setYield(0);
		
		player.getCurrentGame().addBlocksToRegenerate(event.blockList());
		
		// thats the last called event, remove the entity from the list
		new BukkitRunnable() {
			@Override
			public void run() {
				shoot.remove(new Integer(event.getEntity().getEntityId()));
			}
		}.runTaskLater(CannonFightGame.PLUGIN, 1);
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (!shoot.contains(event.getIgnitingEntity()))
			return;
		
		player.getCurrentGame().addBlocksToRegenerate(event.getBlock());
	}
	
	@Override
	public int getMaxAmmo() {
		return maxAmmo;
	}

	@Override
	public boolean hasAmmo() {
		return currentAmmo > 0;
	}

	@Override
	protected void update() {
		item.setDurability((short) (item.getType().getMaxDurability() * done()));
	}

	@Override
	protected void finished() {
		item.setDurability((short) 0);
		item.setAmount(maxAmmo);
		currentAmmo = maxAmmo;
	}
	
	@Override
	public void reset() {
		cancel();
		finished();
	}
	
	public int getLevelAmmo() {
		return levelAmmo;
	}
	
	public int getLevelCooldown() {
		return levelCooldown;
	}
	
	public int getLevelDamage() {
		return levelDamage;
	}
	
	public int getLevelRadius() {
		return levelRadius;
	}
	
	private static ShopGroup shop;
	
	static {
		shop = new ShopGroup(new ShopMaker() {
			
			@Override
			public Shop createShop(final CannonFighter c) {
				final FireballCannon cannon = (FireballCannon) c.getCannon(NAME);
				
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIGHT_BLUE.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "Du hast " + ChatColor.GOLD + c.getCoins() + " Coins");
				fill.setItemMeta(meta);
				
				final ItemStack cooldownItem = new ItemStack(Material.AIR);
				setCooldownItem(cooldownItem, cannon.levelCooldown);
				final ItemStack ammoItem = new ItemStack(Material.AIR);
				setAmmoItem(ammoItem, cannon.levelAmmo);
				final ItemStack radiusItem = new ItemStack(Material.AIR);
				setRadiusItem(radiusItem, cannon.levelRadius);
				final ItemStack damageItem = new ItemStack(Material.AIR);
				setDamageItem(damageItem, cannon.levelDamage);
				
				Shop s = new Shop(NAME + "-Shop", new ClickHandler() {
					
					@Override
					public void onItemInteract(ItemInteractEvent event) {
						if (!event.isPickUpAction())
							return;
						
						ItemStack item = event.getItemInSlot();
						
						if (item.isSimilar(fill))
							return;
						
						if (item.isSimilar(cooldownItem)) {
							// upgrade cooldown
							if (!COOLDOWN_MAP.containsKey(cannon.levelCooldown + 1)) {
								// max reached
								c.sendMessage(Language.get("error.max-upgraded"));
								return;
							}
							
							if (!cannon.upgradeCooldown()) {
								// not enough coins for upgrade
								c.sendMessage(Language.get("error.not-enough-coins"));
								return;
							}
							
							// upgrade done
							setCooldownItem(cooldownItem, cannon.levelCooldown);
							
							Bukkit.getScheduler().runTaskLater(CannonFightGame.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(ammoItem)) {
							// upgrade ammo
							if (!AMMO_MAP.containsKey(cannon.levelAmmo + 1)) {
								// max reached
								c.sendMessage(Language.get("error.max-upgraded"));
								return;
							}
							
							if (!cannon.upgradeAmmo()) {
								// not enough coins for upgrade
								c.sendMessage(Language.get("error.not-enough-coins"));
								return;
							}
							
							// upgrade done
							setAmmoItem(ammoItem, cannon.levelAmmo);
							
							Bukkit.getScheduler().runTaskLater(CannonFightGame.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(radiusItem)) {
							// upgrade radius
							if (!RADIUS_MAP.containsKey(cannon.levelRadius + 1)) {
								// max reached
								c.sendMessage(Language.get("error.max-upgraded"));
								return;
							}
							
							if (!cannon.upgradeRadius()) {
								// not enough coins for upgrade
								c.sendMessage(Language.get("error.not-enough-coins"));
								return;
							}
							
							// upgrade done
							setRadiusItem(radiusItem, cannon.levelRadius);
							
							Bukkit.getScheduler().runTaskLater(CannonFightGame.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(damageItem)) {
							// upgrade damage
							if (!DAMAGE_MAP.containsKey(cannon.levelDamage + 1)) {
								// max reached
								c.sendMessage(Language.get("error.max-upgraded"));
								return;
							}
							
							if (!cannon.upgradeDamage()) {
								// not enough coins for upgrade
								c.sendMessage(Language.get("error.not-enough-coins"));
								return;
							}
							
							// upgrade done
							setDamageItem(damageItem, cannon.levelDamage);
							
							Bukkit.getScheduler().runTaskLater(CannonFightGame.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
					}

					@Override
					public void onInventoryClose(InventoryCloseEvent event) {}
				}, 3);
				
				s.fill(fill);
				
				s.set(0 * 9 + 4, FireballCannon.getItemStack()); // (0, 4) Logo
				
				s.set(1 * 9 + 1, cooldownItem); // (1, 1) Cooldown
				s.set(1 * 9 + 3, ammoItem); // (1, 3) Ammo
				s.set(1 * 9 + 5, radiusItem); // (1, 5) Radius
				s.set(1 * 9 + 7, damageItem); // (1, 7) Damage
				
				return s;
			}
			
			private void setCooldownItem(ItemStack cooldownItem, int levelCooldown) {
				cooldownItem.setType(Material.WATCH);
				
				cooldownItem.setAmount(levelCooldown);
				
				ItemMeta meta = cooldownItem.getItemMeta();
				
				meta.setDisplayName("Cooldown Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> oldLevel = FireballCannon.COOLDOWN_MAP.get(levelCooldown);
				Upgrade<Integer> newLevel = FireballCannon.COOLDOWN_MAP.get(levelCooldown + 1);
				
				lore.add(ChatColor.GOLD + "Cooldownzeit: " + oldLevel.getValue() + " Sekunden");
				
				if (newLevel != null) {
					// upgradable
					lore.add(ChatColor.YELLOW + "Upgrade auf Level " + (levelCooldown + 1));
				
					lore.add(ChatColor.AQUA + "Preis: " + newLevel.getPrice());
				
					int change = newLevel.getValue() - oldLevel.getValue();
					lore.add(ChatColor.DARK_GREEN + "Cooldownzeit: " + newLevel.getValue() + " Sekunden"+ (change < 0 ? ChatColor.GREEN : ChatColor.RED) + " (" + new DecimalFormat("+#.##;-#.##").format(change) + ")"); // green if getting lower, red if getting higher
				}
				else {
					// not upgradable anymore
					lore.add(ChatColor.GREEN + "Die Cooldownzeit ist bereits maximal verbessert!");
				}
				
				meta.setLore(lore);
				
				cooldownItem.setItemMeta(meta);
			}
			
			private void setAmmoItem(ItemStack ammoItem, int levelAmmo) {
				ammoItem.setType(Material.MELON_SEEDS);
				
				ammoItem.setAmount(levelAmmo);
				
				ItemMeta meta = ammoItem.getItemMeta();
				
				meta.setDisplayName("Ammo Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> oldLevel = FireballCannon.AMMO_MAP.get(levelAmmo);
				Upgrade<Integer> newLevel = FireballCannon.AMMO_MAP.get(levelAmmo + 1);
				
				lore.add(ChatColor.GOLD + "Munition: " + oldLevel.getValue() + " Schuß");
				
				if (newLevel != null) {
					// upgradable
					lore.add(ChatColor.YELLOW + "Upgrade auf Level " + (levelAmmo + 1));
				
					lore.add(ChatColor.AQUA + "Preis: " + newLevel.getPrice());
				
					int change = newLevel.getValue() - oldLevel.getValue();
					lore.add(ChatColor.DARK_GREEN + "Neue Munition: " + newLevel.getValue() + " Schuß" + (change > 0 ? ChatColor.GREEN : ChatColor.RED) + " (" + new DecimalFormat("+#.##;-#.##").format(change) + ")"); // green if getting higher, red if getting lower
				}
				else {
					// not upgradable anymore
					lore.add(ChatColor.GREEN + "Die Munition ist bereits maximal verbessert!");
				}
				
				meta.setLore(lore);
				
				ammoItem.setItemMeta(meta);
			}
			
			private void setRadiusItem(ItemStack radiusItem, int levelRadius) {
				radiusItem.setType(Material.COMPASS);
				
				radiusItem.setAmount(levelRadius);
				
				ItemMeta meta = radiusItem.getItemMeta();
				
				meta.setDisplayName("Radius Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Double> oldLevel = FireballCannon.RADIUS_MAP.get(levelRadius);
				Upgrade<Double> newLevel = FireballCannon.RADIUS_MAP.get(levelRadius + 1);
				
				lore.add(ChatColor.GOLD + "Radius: " + oldLevel.getValue() + " Blöcke");
				
				if (newLevel != null) {
					// upgradable
					lore.add(ChatColor.YELLOW + "Upgrade auf Level " + (levelRadius + 1));
				
					lore.add(ChatColor.AQUA + "Preis: " + newLevel.getPrice());
				
					double change = newLevel.getValue() - oldLevel.getValue();
					lore.add(ChatColor.DARK_GREEN + "Neuer Radius: " + newLevel.getValue() + " Blöcke" + (change > 0 ? ChatColor.GREEN : ChatColor.RED) + " (" + new DecimalFormat("+#.##;-#.##").format(change) + ")"); // green if getting higher, red if getting lower
				}
				else {
					// not upgradable anymore
					lore.add(ChatColor.GREEN + "Der Radius ist bereits maximal verbessert!");
				}
				
				meta.setLore(lore);
				
				radiusItem.setItemMeta(meta);
			}
			
			private void setDamageItem(ItemStack damageItem, int levelDamage) {
				damageItem.setType(Material.REDSTONE);
				
				damageItem.setAmount(levelDamage);
				
				ItemMeta meta = damageItem.getItemMeta();
				
				meta.setDisplayName("Damage Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> oldLevel = FireballCannon.DAMAGE_MAP.get(levelDamage);
				Upgrade<Integer> newLevel = FireballCannon.DAMAGE_MAP.get(levelDamage + 1);
				
				
				String hearts = "";
				
				for (int i = 0; i < oldLevel.getValue() / 2; i++)
					hearts += "\u2764";
				
				if (oldLevel.getValue() % 2 == 1)
					hearts += "\u2765";
				
				lore.add(ChatColor.GOLD + "Schaden: " + hearts);
				
				if (newLevel != null) {
					// upgradable
					lore.add(ChatColor.YELLOW + "Upgrade auf Level " + (levelDamage + 1));
				
					lore.add(ChatColor.AQUA + "Preis: " + newLevel.getPrice());
				
					double change = newLevel.getValue() - oldLevel.getValue();
					
					hearts = "";
					
					for (int i = 0; i < newLevel.getValue() / 2; i++)
						hearts += "\u2764";
					
					if (newLevel.getValue() % 2 == 1)
						hearts += "\u2765";
					
					lore.add(ChatColor.DARK_GREEN + "Neuer Schaden: " + hearts + (change > 0 ? ChatColor.GREEN : ChatColor.RED) + " (" + new DecimalFormat("+#.##;-#.##").format((double) change / 2) + ")"); // green if getting higher, red if getting lower
				}
				else {
					// not upgradable anymore
					lore.add(ChatColor.GREEN + "Der Schaden ist bereits maximal verbessert!");
				}
				
				meta.setLore(lore);
				
				damageItem.setItemMeta(meta);
			}
		});
	}
	
	public static void openShopPage(CannonFighter c) {
		shop.open(c);
	}
	
	@Override
	public void openShop() {
		openShopPage(player);
	}

	public boolean upgradeCooldown() {
		Upgrade<Integer> upgrade = COOLDOWN_MAP.get(levelCooldown + 1);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelCooldown++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".cooldown", levelCooldown)));
		setTime(upgrade.getValue());
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Cooldown auf Level " + levelCooldown);
		
		return true;
	}

	public boolean upgradeAmmo() {
		Upgrade<Integer> upgrade = AMMO_MAP.get(levelAmmo + 1);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelAmmo++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".ammo", levelAmmo)));
		
		currentAmmo = maxAmmo = upgrade.getValue();
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Ammo auf Level " + levelAmmo);
		
		return true;
	}

	public boolean upgradeRadius() {
		Upgrade<Double> upgrade = RADIUS_MAP.get(levelRadius + 1);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelRadius++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".radius", levelRadius)));
		
		radius = upgrade.getValue().floatValue();
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Radius auf Level " + levelRadius);
		
		return true;
	}
	
	public boolean upgradeDamage() {
		Upgrade<Integer> upgrade = DAMAGE_MAP.get(levelDamage + 1);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelDamage++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".damage", levelDamage)));
		
		damage = upgrade.getValue();
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Damage auf Level " + levelDamage);
		
		return true;
	}
	
	@Override
	public CannonConstructor getCannonConstructor() {
		return constructor;
	}
}
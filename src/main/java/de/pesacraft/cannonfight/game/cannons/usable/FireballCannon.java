package de.pesacraft.cannonfight.game.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.MongoDatabase;
import de.pesacraft.cannonfight.util.Upgrade;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemSelectEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;

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
	
	protected static final Map<Integer, Upgrade<Double>> DAMAGE_MAP = new HashMap<Integer, Upgrade<Double>>();
	
	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME)).first();
		
		if (doc != null) {
			// Cannon in database
			ITEM = ItemStack.deserialize((Document) doc.get("item"));
			
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
				DAMAGE_MAP.put(Integer.parseInt(damage.getKey()), new Upgrade<Double>((Document) damage.getValue()));
			
		}
		else {
			// Cannon not in database
			ITEM = new ItemStack(Material.FIREBALL);
			
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
				Upgrade<Double> u = new Upgrade<Double>(i * 100, i * 0.1 + 1);
				RADIUS_MAP.put(i, u);
				radiusMap.put(i + "", u);
			}
			
			Map<String, Object> damageMap = new HashMap<String, Object>();
			for (int i = 1; i <= 10; i++) {
				Upgrade<Double> u = new Upgrade<Double>(i * 100, i * 0.5 + 0.5);
				DAMAGE_MAP.put(i, u);
				damageMap.put(i + "", u);
			}
		
			doc = new Document("name", NAME);
			doc = doc.append("item", new Document(ITEM.serialize()));
			
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
				int ammo = (int) map.get("ammo");
				int cooldown = (int) map.get("cooldown");
				int radius = (int) map.get("radius");
				int damage = (int) map.get("damage");
				return new FireballCannon(fighter, ammo, cooldown, radius, damage);
			}
			
			@Override
			public boolean canConstruct(String name) {
				return name.equals(NAME);
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
	private double damage;
	private ItemStack item;
	
	private CannonFighter player;
	
	private List<Fireball> shoot;
	
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
		
		shoot = new ArrayList<Fireball>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}

	@Override
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public boolean fire() {
		if (!hasFinished())
			return false;
		
		if (--currentAmmo == 0)
			start();
		else
			item.setAmount(currentAmmo);
		
		Player p = player.getPlayer();
		
		Fireball fball = p.launchProjectile(Fireball.class);
		fball.setVelocity(fball.getVelocity().multiply(2));
		shoot.add(fball);
		
		return true;
	}
	
	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		if (shoot.contains(event.getEntity()))
			event.setRadius(radius);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
		if (!shoot.contains(event.getDamager()))
			return;
		
		event.setDamage(damage);
		
		/* muss später aus der Liste entfernt werden,
		 * da es mehrere EntityDamageByEntityEvents gibt,
		 * pro getroffene Entity eins
		 */
		new BukkitRunnable() {
			@Override
			public void run() {
				shoot.remove(event.getDamager());
			}
		}.runTaskLater(CannonFight.PLUGIN, 1);
	}
	
	@EventHandler
	public void onEntityExplosion(EntityExplodeEvent event) {
		if (!shoot.contains(event.getEntity()))
			return;
		
		Iterator<Block> blocks = event.blockList().iterator();
		
		while (blocks.hasNext()) {
			Block b = blocks.next();
			
			if (!player.getCurrentGame().locIsInArena(b.getLocation()))
				// block not part of map -> won't be destroyed
				blocks.remove();
		}
		
		player.getCurrentGame().addBlocksToRegenerate(event.blockList());
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
		item.setDurability(item.getType().getMaxDurability());
		item.setAmount(maxAmmo);
		currentAmmo = maxAmmo;
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
			public Shop createShop(CannonFighter c) {
				FireballCannon cannon = (FireballCannon) c.getCannon(NAME);
				
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIGHT_BLUE.getData());
				
				final ItemStack cooldownItem = getCooldownItem(cannon.levelCooldown);
				final ItemStack ammoItem = getAmmoItem(cannon.levelAmmo);
				final ItemStack radiusItem = getRadiusItem(cannon.levelRadius);
				final ItemStack damageItem = getDamageItem(cannon.levelDamage);
				
				Shop s = new Shop(NAME + "-Shop", new ClickHandler() {
					
					@Override
					public void onItemSelect(ItemSelectEvent event) {
						ItemStack item = event.getItem();
						
						if (item.isSimilar(fill))
							return;
						
						if (item.isSimilar(cooldownItem)) {
							// upgrade cooldown
							System.out.println("Cooldown upgrade!");
							return;
						}
						
						if (item.isSimilar(ammoItem)) {
							// upgrade ammo
							System.out.println("Ammo upgrade!");
							return;
						}
						
						if (item.isSimilar(radiusItem)) {
							// upgrade radius
							System.out.println("Radius upgrade!");
							return;
						}
						
						if (item.isSimilar(damageItem)) {
							// upgrade damage
							System.out.println("Damage upgrade!");
							return;
						}
					}
				}, 3);
				s.fill(fill);
				
				s.set(0 * 9 + 4, FireballCannon.getItemStack()); // (0, 4) Logo
				
				s.set(1 * 9 + 1, cooldownItem); // (1, 1) Cooldown
				s.set(1 * 9 + 3, cooldownItem); // (1, 3) Ammo
				s.set(1 * 9 + 5, cooldownItem); // (1, 5) Radius
				s.set(1 * 9 + 7, cooldownItem); // (1, 7) Damage
				
				return null;
			}
			
			private ItemStack getCooldownItem(int levelCooldown) {
				ItemStack cooldownItem = new ItemStack(Material.WATCH);
				
				cooldownItem.setAmount(levelCooldown);
				
				cooldownItem.getItemMeta().setDisplayName("Cooldown Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> oldLevel = FireballCannon.COOLDOWN_MAP.get(levelCooldown);
				Upgrade<Integer> newLevel = FireballCannon.COOLDOWN_MAP.get(levelCooldown + 1);
				
				if (newLevel != null) {
					// upgradable
					lore.add("&eUpgrade auf Level " + (levelCooldown + 1));
				
					lore.add("&bPreis: " + newLevel.getPrice());
				
					double change = 1 - (double) newLevel.getValue() / oldLevel.getValue();
					lore.add("&2Cooldownzeit: " + newLevel.getValue() + "|" + (change < 1 ? "&a " : "&4 ") + change); // green if getting lower, red if getting higher
				}
				else {
					// not upgradable anymore
					lore.add("&4Die Cooldownzeit ist bereits maximal verbessert!");
				}
				
				cooldownItem.getItemMeta().setLore(lore);
				
				return cooldownItem;
			}
			
			private ItemStack getAmmoItem(int levelAmmo) {
				ItemStack ammoItem = new ItemStack(Material.MELON_SEEDS);
				ammoItem.setAmount(levelAmmo);
				
				ammoItem.getItemMeta().setDisplayName("Ammo Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> oldLevel = FireballCannon.AMMO_MAP.get(levelAmmo);
				Upgrade<Integer> newLevel = FireballCannon.AMMO_MAP.get(levelAmmo + 1);
				
				if (newLevel != null) {
					// upgradable
					lore.add("&eUpgrade auf Level " + (levelAmmo + 1));
				
					lore.add("&bPreis: " + newLevel.getPrice());
				
					double change = 1 - (double) newLevel.getValue() / oldLevel.getValue();
					lore.add("&2Munition: " + newLevel.getValue() + "|" + (change > 1 ? "&a " : "&4 ") + change); // green if getting higher, red if getting lower
				}
				else {
					// not upgradable anymore
					lore.add("&4Die Munition ist bereits maximal verbessert!");
				}
				
				ammoItem.getItemMeta().setLore(lore);
				
				return ammoItem;
			}
			
			private ItemStack getRadiusItem(int levelRadius) {
				ItemStack radiusItem = new ItemStack(Material.COMPASS);
				radiusItem.setAmount(levelRadius);
				
				radiusItem.getItemMeta().setDisplayName("Radius Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Double> oldLevel = FireballCannon.RADIUS_MAP.get(levelRadius);
				Upgrade<Double> newLevel = FireballCannon.RADIUS_MAP.get(levelRadius + 1);
				
				if (newLevel != null) {
					// upgradable
					lore.add("&eUpgrade auf Level " + (levelRadius + 1));
				
					lore.add("&bPreis: " + newLevel.getPrice());
				
					double change = 1 - (double) newLevel.getValue() / oldLevel.getValue();
					lore.add("&2Radius: " + newLevel.getValue() + "|" + (change > 1 ? "&a " : "&4 ") + change); // green if getting higher, red if getting lower
				}
				else {
					// not upgradable anymore
					lore.add("&4Der Radius ist bereits maximal verbessert!");
				}
				
				radiusItem.getItemMeta().setLore(lore);
				
				return radiusItem;
			}
			
			private ItemStack getDamageItem(int levelDamage) {
				ItemStack damageItem = new ItemStack(Material.REDSTONE);
				damageItem.setAmount(levelDamage);
				
				damageItem.getItemMeta().setDisplayName("Damage Upgrade");
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Double> oldLevel = FireballCannon.DAMAGE_MAP.get(levelDamage);
				Upgrade<Double> newLevel = FireballCannon.DAMAGE_MAP.get(levelDamage + 1);
				
				if (newLevel != null) {
					// upgradable
					lore.add("&eUpgrade auf Level " + (levelDamage + 1));
				
					lore.add("&bPreis: " + newLevel.getPrice());
				
					double change = 1 - (double) newLevel.getValue() / oldLevel.getValue();
					lore.add("&2Schaden: " + newLevel.getValue() + "|" + (change > 1 ? "&a " : "&4 ") + change); // green if getting higher, red if getting lower
				}
				else {
					// not upgradable anymore
					lore.add("&4Der Schaden ist bereits maximal verbessert!");
				}
				
				damageItem.getItemMeta().setLore(lore);
				
				return damageItem;
			}
		});
	}
	
	public static void openShopPage(CannonFighter c) {
		shop.open(c);
	}

	@Override
	public CannonConstructor getCannonConstructor() {
		return null;
	}

		
}
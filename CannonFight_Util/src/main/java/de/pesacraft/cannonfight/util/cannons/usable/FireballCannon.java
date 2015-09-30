package de.pesacraft.cannonfight.util.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.ItemSerializer;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.cannons.usable.FireballCannon;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;
import de.pesacraft.cannonfight.util.shop.Upgrade;

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
	
	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM = ItemSerializer.deserialize((Document) doc.get("item"));
			
			registerUpgrade(NAME, "cooldown", Integer.class, (Document) doc.get("cooldown"));
			registerUpgrade(NAME, "ammo", Integer.class, (Document) doc.get("ammo"));
			registerUpgrade(NAME, "radius", Double.class, (Document) doc.get("radius"));
			registerUpgrade(NAME, "damage", Integer.class, (Document) doc.get("damage"));
		}
		else {
			// Cannon not in database
			ITEM = new ItemStack(Material.FIREBALL);
			
			ItemMeta m = ITEM.getItemMeta();
			m.setDisplayName(NAME);
			ITEM.setItemMeta(m);
			
			registerUpgrade(NAME, "cooldown", 10, Integer.class);
			registerUpgrade(NAME, "ammo", 1, Integer.class);
			registerUpgrade(NAME, "radius", 1.0, Double.class);
			registerUpgrade(NAME, "damage", 2, Integer.class);
			
			doc = new Document("name", NAME);	
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM)));
			
			doc.putAll(serializeUpgrades(NAME));
			
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
				return getUpgrade(NAME, "ammo", 1, Integer.class).getPrice()
						+ getUpgrade(NAME, "cooldown", 1, Integer.class).getPrice()
						+ getUpgrade(NAME, "radius", 1, Double.class).getPrice()
						+ getUpgrade(NAME, "damage", 1, Integer.class).getPrice();
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
		super(getUpgrade(NAME, "cooldown", levelCooldown, Integer.class).getValue());
		
		this.levelAmmo = levelAmmo;
		this.levelCooldown = levelCooldown;
		this.levelRadius = levelRadius;
		this.levelDamage = levelDamage;
		
		this.player = player;
		
		currentAmmo = maxAmmo = getUpgrade(NAME, "ammo", levelAmmo, Integer.class).getValue();
			
		radius = getUpgrade(NAME, "radius", levelRadius, Double.class).getValue().floatValue();
		damage = getUpgrade(NAME, "damage", levelDamage, Integer.class).getValue();
	
		item = ITEM.clone();
		item.setAmount(currentAmmo);
		shoot = new ArrayList<Integer>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
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
		
		// drop nothing
		event.setYield(0);
		
		// thats the last called event, remove the entity from the list
		new BukkitRunnable() {
			@Override
			public void run() {
				shoot.remove(new Integer(event.getEntity().getEntityId()));
			}
		}.runTaskLater(CannonFightUtil.PLUGIN, 1);
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
							if (getLevelsForUpgrade(NAME, "cooldown") < cannon.levelCooldown + 1) {
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
							
							Bukkit.getScheduler().runTaskLater(CannonFightUtil.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(ammoItem)) {
							// upgrade ammo
							if (getLevelsForUpgrade(NAME, "ammo") < cannon.levelAmmo + 1) {
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
							
							Bukkit.getScheduler().runTaskLater(CannonFightUtil.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(radiusItem)) {
							// upgrade radius
							if (getLevelsForUpgrade(NAME, "radius") < cannon.levelRadius + 1) {
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
							
							Bukkit.getScheduler().runTaskLater(CannonFightUtil.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(c);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(damageItem)) {
							// upgrade damage
							if (getLevelsForUpgrade(NAME, "damage") < cannon.levelDamage + 1) {
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
							
							Bukkit.getScheduler().runTaskLater(CannonFightUtil.PLUGIN, new Runnable() {
								
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
				
				Upgrade<Integer> oldLevel = getUpgrade(NAME, "cooldown", levelCooldown, Integer.class);
				Upgrade<Integer> newLevel = getUpgrade(NAME, "cooldown", levelCooldown + 1, Integer.class);
				
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
				
				Upgrade<Integer> oldLevel = getUpgrade(NAME, "ammo", levelAmmo, Integer.class);
				Upgrade<Integer> newLevel = getUpgrade(NAME, "ammo", levelAmmo + 1, Integer.class);
				
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
				
				Upgrade<Double> oldLevel = getUpgrade(NAME, "radius", levelRadius, Double.class);
				Upgrade<Double> newLevel = getUpgrade(NAME, "radius", levelRadius + 1, Double.class);
				
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
				
				Upgrade<Integer> oldLevel = getUpgrade(NAME, "damage", levelDamage, Integer.class);
				Upgrade<Integer> newLevel = getUpgrade(NAME, "damage", levelDamage + 1, Integer.class);
				
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
		Upgrade<Integer> upgrade = getUpgrade(NAME, "cooldown", levelCooldown + 1, Integer.class);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelCooldown++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".cooldown", levelCooldown)));
		setTime(upgrade.getValue());
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Cooldown auf Level " + levelCooldown);
		
		return true;
	}

	public boolean upgradeAmmo() {
		Upgrade<Integer> upgrade = getUpgrade(NAME, "ammo", levelAmmo + 1, Integer.class);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelAmmo++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".ammo", levelAmmo)));
		
		currentAmmo = maxAmmo = upgrade.getValue();
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Ammo auf Level " + levelAmmo);
		
		return true;
	}

	public boolean upgradeRadius() {
		Upgrade<Double> upgrade = getUpgrade(NAME, "radius", levelRadius + 1, Double.class);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		levelRadius++;
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + ".radius", levelRadius)));
		
		radius = upgrade.getValue().floatValue();
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Radius auf Level " + levelRadius);
		
		return true;
	}
	
	public boolean upgradeDamage() {
		Upgrade<Integer> upgrade = getUpgrade(NAME, "damage", levelDamage + 1, Integer.class);
		
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
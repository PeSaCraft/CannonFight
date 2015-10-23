package de.pesacraft.cannonfight.util.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import de.pesacraft.cannonfight.util.Language.TimeOutputs;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.cannons.usable.FireballCannon;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;
import de.pesacraft.cannonfight.util.shop.upgrade.DoubleUpgradeChanger;
import de.pesacraft.cannonfight.util.shop.upgrade.IntegerUpgradeChanger;
import de.pesacraft.cannonfight.util.shop.upgrade.Upgrade;

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
	
	private static final String UPGRADE_COOLDOWN = "cooldown";
	private static final String UPGRADE_AMMO = "ammo";
	private static final String UPGRADE_RADIUS = "radius";
	private static final String UPGRADE_DAMAGE = "damage";

	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM = ItemSerializer.deserialize((Document) doc.get("item"));
			
			registerUpgrade(NAME, UPGRADE_COOLDOWN, Integer.class, (Document) doc.get(UPGRADE_COOLDOWN), new IntegerUpgradeChanger(25, 100, 1, 5, 250, 10));
			registerUpgrade(NAME, UPGRADE_AMMO, Integer.class, (Document) doc.get(UPGRADE_AMMO), new IntegerUpgradeChanger(25, 100, 1, 4, 250, 1));
			registerUpgrade(NAME, UPGRADE_RADIUS, Double.class, (Document) doc.get(UPGRADE_RADIUS), new DoubleUpgradeChanger(25, 100, 0.1, 0.5, 500, 1.0));
			registerUpgrade(NAME, UPGRADE_DAMAGE, Integer.class, (Document) doc.get(UPGRADE_DAMAGE), new IntegerUpgradeChanger(25, 100, 1, 4, 250, 2));
		}
		else {
			// Cannon not in database
			ITEM = new ItemStack(Material.FIREBALL);
			
			ItemMeta m = ITEM.getItemMeta();
			m.setDisplayName(NAME);
			ITEM.setItemMeta(m);
			
			registerUpgrade(NAME, UPGRADE_COOLDOWN, 10, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 5, 250, 10));
			registerUpgrade(NAME, UPGRADE_AMMO, 1, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 4, 250, 1));
			registerUpgrade(NAME, UPGRADE_RADIUS, 1.0, Double.class, new DoubleUpgradeChanger(25, 100, 0.1, 0.5, 500, 1.0));
			registerUpgrade(NAME, UPGRADE_DAMAGE, 2, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 4, 250, 2));
			
			doc = new Document("name", NAME);	
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM)));
			
			doc.putAll(serializeUpgrades(NAME));
			
			COLLECTION.insertOne(doc);
		}
		
		ItemStack cooldownItem = new ItemStack(Material.WATCH);
		ItemMeta meta = cooldownItem.getItemMeta();
		meta.setDisplayName("Cooldown Upgrade");
		cooldownItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_COOLDOWN, cooldownItem);
		
		ItemStack ammoItem = new ItemStack(Material.MELON_SEEDS);
		meta = ammoItem.getItemMeta();
		meta.setDisplayName("Ammo Upgrade");
		ammoItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_AMMO, ammoItem);
		
		ItemStack radiusItem = new ItemStack(Material.COMPASS);
		meta = radiusItem.getItemMeta();
		meta.setDisplayName("Radius Upgrade");
		radiusItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_RADIUS, radiusItem);
		
		ItemStack damageItem = new ItemStack(Material.REDSTONE);
		meta = damageItem.getItemMeta();
		meta.setDisplayName("Damage Upgrade");
		damageItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_DAMAGE, damageItem);
	}
	
	private static CannonConstructor constructor;
	
	public static void setup() {
		constructor = new CannonConstructor() {
			
			@Override
			public Cannon construct(CannonFighter fighter, Map<String, Object> map) {
				int ammo = ((Number) map.get(UPGRADE_AMMO)).intValue();
				int cooldown = ((Number) map.get(UPGRADE_COOLDOWN)).intValue();
				int radius = ((Number) map.get(UPGRADE_RADIUS)).intValue();
				int damage = ((Number) map.get(UPGRADE_DAMAGE)).intValue();
				
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
				return getUpgrade(NAME, UPGRADE_AMMO, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_COOLDOWN, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_RADIUS, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_DAMAGE, 1).getPrice();
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
	
	private CannonFighter player;
	private ItemStack item;
	private int maxAmmon;
	private List<Integer> shoot;
	
	public FireballCannon(CannonFighter player, int levelAmmo, int levelCooldown, int levelRadius, int levelDamage) {
		super(((Number) getUpgrade(NAME, UPGRADE_COOLDOWN, levelCooldown).getValue()).intValue());
		
		setUpgradeLevel(UPGRADE_AMMO, levelAmmo);
		setUpgradeLevel(UPGRADE_COOLDOWN, levelCooldown);
		setUpgradeLevel(UPGRADE_RADIUS, levelRadius);
		setUpgradeLevel(UPGRADE_DAMAGE, levelDamage);
		
		this.player = player;
		
		item = ITEM.clone();
		item.setAmount(((Number) getValue(UPGRADE_AMMO)).intValue());
		shoot = new ArrayList<Integer>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
	}

	public FireballCannon(CannonFighter player) {
		this(player, 1, 1, 1, 1);
		
		Document doc = new Document(UPGRADE_AMMO, getUpgradeLevel(UPGRADE_AMMO));
		doc = doc.append(UPGRADE_COOLDOWN, getUpgradeLevel(UPGRADE_COOLDOWN));
		doc = doc.append(UPGRADE_RADIUS, getUpgradeLevel(UPGRADE_RADIUS));
		doc = doc.append(UPGRADE_DAMAGE, getUpgradeLevel(UPGRADE_DAMAGE));
		
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
		
		int currentAmmo = ((Number) getValue(UPGRADE_AMMO)).intValue() - 1;
		setValue(UPGRADE_AMMO, new Integer(currentAmmo));
		
		if (currentAmmo == 0)
			start();
		else
			item.setAmount(currentAmmo);
		
		Player p = player.getPlayer();
		
		Fireball fball = p.launchProjectile(Fireball.class);
		fball.setVelocity(fball.getVelocity().multiply(2));
		fball.setYield(((Number) getValue(UPGRADE_RADIUS)).floatValue());
		
		shoot.add(fball.getEntityId());
		return true;
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!shoot.contains(event.getDamager().getEntityId()))
			return;
		
		event.setDamage(((Number) getValue(UPGRADE_DAMAGE)).doubleValue());
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
	public boolean hasAmmo() {
		return ((Number) getValue(UPGRADE_AMMO)).intValue() > 0;
	}

	@Override
	protected void update() {
		item.setDurability((short) (item.getType().getMaxDurability() * done()));
	}

	@Override
	protected void finished() {
		item.setDurability((short) 0);
		resetValue(UPGRADE_AMMO);
		item.setAmount(((Number) getValue(UPGRADE_AMMO)).intValue());
	}
	
	@Override
	public void reset() {
		cancel();
		finished();
	}
	
	/*
	private static ShopGroup shop;
	
	static {
		shop = new ShopGroup(new ShopMaker() {
			
			@SuppressWarnings("deprecation")
			@Override
			public Shop createShop(final CannonFighter c) {
				final FireballCannon cannon = (FireballCannon) c.getCannon(NAME);
				
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIGHT_BLUE.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "Du hast " + ChatColor.GOLD + c.getCoins() + " Coins");
				fill.setItemMeta(meta);
				
				final ItemStack cooldownItem = new ItemStack(Material.AIR);
				setCooldownItem(cooldownItem, cannon.getUpgradeLevel(UPGRADE_COOLDOWN));
				final ItemStack ammoItem = new ItemStack(Material.AIR);
				setAmmoItem(ammoItem, cannon.getUpgradeLevel(UPGRADE_AMMO));
				final ItemStack radiusItem = new ItemStack(Material.AIR);
				setRadiusItem(radiusItem, cannon.getUpgradeLevel(UPGRADE_RADIUS));
				final ItemStack damageItem = new ItemStack(Material.AIR);
				setDamageItem(damageItem, cannon.getUpgradeLevel(UPGRADE_DAMAGE));
				
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
							if (getLevelsForUpgrade(NAME, UPGRADE_COOLDOWN) < cannon.levelCooldown + 1) {
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
							if (getLevelsForUpgrade(NAME, UPGRADE_AMMO) < cannon.levelAmmo + 1) {
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
							if (getLevelsForUpgrade(NAME, UPGRADE_RADIUS) < cannon.levelRadius + 1) {
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
							if (getLevelsForUpgrade(NAME, UPGRADE_DAMAGE) < cannon.levelDamage + 1) {
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
				
				Upgrade<Integer> oldLevel = getOrSetUpgrade(NAME, UPGRADE_COOLDOWN, levelCooldown, Integer.class);
				Upgrade<Integer> newLevel = getOrSetUpgrade(NAME, UPGRADE_COOLDOWN, levelCooldown + 1, Integer.class);
				
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
				
				Upgrade<Integer> oldLevel = getOrSetUpgrade(NAME, UPGRADE_AMMO, levelAmmo, Integer.class);
				Upgrade<Integer> newLevel = getOrSetUpgrade(NAME, UPGRADE_AMMO, levelAmmo + 1, Integer.class);
				
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
				
				Upgrade<Double> oldLevel = getOrSetUpgrade(NAME, UPGRADE_RADIUS, levelRadius, Double.class);
				Upgrade<Double> newLevel = getOrSetUpgrade(NAME, UPGRADE_RADIUS, levelRadius + 1, Double.class);
				
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
				
				Upgrade<Integer> oldLevel = getOrSetUpgrade(NAME, UPGRADE_DAMAGE, levelDamage, Integer.class);
				Upgrade<Integer> newLevel = getOrSetUpgrade(NAME, UPGRADE_DAMAGE, levelDamage + 1, Integer.class);
				
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
	 */
	public boolean upgradeCooldown() {
		int newLevel = getUpgradeLevel(UPGRADE_COOLDOWN) + 1;
		Upgrade<Integer> upgrade = (Upgrade<Integer>) getUpgrade(getName(), UPGRADE_COOLDOWN, newLevel);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		setUpgradeLevel(UPGRADE_COOLDOWN, newLevel);
		setTime(upgrade.getValue());
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + "." + UPGRADE_COOLDOWN, newLevel)));
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Cooldown auf Level " + newLevel);
		
		return true;
	}

	public boolean upgradeAmmo() {
		int newLevel = getUpgradeLevel(UPGRADE_AMMO) + 1;
		Upgrade<Integer> upgrade = (Upgrade<Integer>) getUpgrade(getName(), UPGRADE_AMMO, newLevel);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		setUpgradeLevel(UPGRADE_AMMO, newLevel);
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + "." + UPGRADE_AMMO, newLevel)));
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Ammo auf Level " + newLevel);
		
		return true;
	}

	public boolean upgradeRadius() {
		int newLevel = getUpgradeLevel(UPGRADE_RADIUS) + 1;
		Upgrade<Double> upgrade = (Upgrade<Double>) getUpgrade(NAME, UPGRADE_RADIUS, newLevel);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		setUpgradeLevel(UPGRADE_RADIUS, newLevel);
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + "." + UPGRADE_RADIUS, newLevel)));
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Radius auf Level " + newLevel);
		
		return true;
	}
	
	public boolean upgradeDamage() {
		int newLevel = getUpgradeLevel(UPGRADE_DAMAGE) + 1;
		Upgrade<Integer> upgrade = (Upgrade<Integer>) getUpgrade(NAME, UPGRADE_DAMAGE, newLevel);
		
		if (!player.hasEnoughCoins(upgrade.getPrice()))
			return false;
		
		setUpgradeLevel(UPGRADE_DAMAGE, newLevel);
		
		Collection.PLAYERS().updateOne(eq("uuid", player.getPlayer().getUniqueId().toString()), new Document("$set", new Document("cannons." + NAME + "." + UPGRADE_DAMAGE, newLevel)));
		
		player.takeCoins(upgrade.getPrice(), NAME + "-Upgrade: Damage auf Level " + newLevel);
		
		return true;
	}
	
	@Override
	public CannonConstructor getCannonConstructor() {
		return constructor;
	}

	@Override
	public String formatValueForUpgrade(String upgrade, Object value) {
		switch (upgrade) {
		case UPGRADE_AMMO:
		case UPGRADE_COOLDOWN:
			return Language.formatTime((int) value, TimeOutputs.SECONDS);
		case UPGRADE_DAMAGE:
			return Language.formatDamage((double) value);
		case UPGRADE_RADIUS:
			return Language.formatDistance((double) value);
		}
		return null;
	}
}
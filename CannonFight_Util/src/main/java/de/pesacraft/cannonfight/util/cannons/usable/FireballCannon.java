package de.pesacraft.cannonfight.util.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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

	@Override
	public CannonConstructor getCannonConstructor() {
		return constructor;
	}

	@Override
	public String formatValueForUpgrade(String upgrade, Object value) {
		switch (upgrade) {
		case UPGRADE_AMMO:
			return Language.formatAmmo(((Number) value).intValue());
		case UPGRADE_COOLDOWN:
			return Language.formatTime(((Number) value).intValue(), TimeOutputs.SECONDS);
		case UPGRADE_DAMAGE:
			return Language.formatDamage(((Number) value).doubleValue());
		case UPGRADE_RADIUS:
			return Language.formatDistance(((Number) value).doubleValue());
		}
		return null;
	}

	@Override
	protected void refreshUpgradeValue(String upgradeName) {
		if (upgradeName.equals(UPGRADE_COOLDOWN)) {
			Upgrade<Integer> upgrade = (Upgrade<Integer>) getUpgrade(getName(), UPGRADE_COOLDOWN, getUpgradeLevel(UPGRADE_COOLDOWN));
			setTime(upgrade.getValue());
		}
	}
}
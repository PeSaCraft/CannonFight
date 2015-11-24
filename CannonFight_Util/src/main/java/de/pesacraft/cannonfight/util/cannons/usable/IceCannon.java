package de.pesacraft.cannonfight.util.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.util.Map;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.ItemSerializer;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.PotionEffectOverCallback;
import de.pesacraft.cannonfight.util.Language.TimeOutputs;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.game.HitHandler;
import de.pesacraft.cannonfight.util.game.MovingParticle;
import de.pesacraft.cannonfight.util.shop.upgrade.DoubleUpgradeChanger;
import de.pesacraft.cannonfight.util.shop.upgrade.IntegerUpgradeChanger;
import de.pesacraft.cannonfight.util.shop.upgrade.Upgrade;

public class IceCannon extends Cannon implements Listener {
	private static final MongoCollection<Document> COLLECTION;
	
	/**
	 * ItemStack representing the cannon ingame
	 */
	protected static final ItemStack ITEM;
	/**
	 * The cannons name
	 */
	public static final String NAME = "IceCannon";
	
	private static final String UPGRADE_COOLDOWN = "cooldown";
	private static final String UPGRADE_AMMO = "ammo";
	private static final String UPGRADE_DAMAGE = "damage";
	private static final String UPGRADE_RADIUS = "radius";
	private static final String UPGRADE_DURATION = "duration";
	private static final String UPGRADE_STRENGTH = "strength";
	
	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM = ItemSerializer.deserialize((Document) doc.get("item"));
			
			registerUpgrade(NAME, UPGRADE_COOLDOWN, Integer.class, (Document) doc.get(UPGRADE_COOLDOWN), new IntegerUpgradeChanger(25, 100, 1, 5, 250, 10));
			registerUpgrade(NAME, UPGRADE_AMMO, Integer.class, (Document) doc.get(UPGRADE_AMMO), new IntegerUpgradeChanger(25, 100, 1, 4, 250, 1));
			registerUpgrade(NAME, UPGRADE_DAMAGE, Integer.class, (Document) doc.get(UPGRADE_DAMAGE), new IntegerUpgradeChanger(25, 100, 1, 4, 250, 2));
			registerUpgrade(NAME, UPGRADE_RADIUS, Double.class, (Document) doc.get(UPGRADE_RADIUS), new DoubleUpgradeChanger(25, 100, 0.1, 0.5, 500, 1.0));
			registerUpgrade(NAME, UPGRADE_DURATION, Integer.class, (Document) doc.get(UPGRADE_DURATION), new IntegerUpgradeChanger(25, 100, 1, 5, 250, 3));
			registerUpgrade(NAME, UPGRADE_STRENGTH, Double.class, (Document) doc.get(UPGRADE_STRENGTH), new DoubleUpgradeChanger(25, 100, 0.05, 0.25, 250, 0.05));
		}
		else {
			// Cannon not in database
			ITEM = new ItemStack(Material.FIREBALL);
			
			ItemMeta m = ITEM.getItemMeta();
			m.setDisplayName(NAME);
			ITEM.setItemMeta(m);
			
			registerUpgrade(NAME, UPGRADE_COOLDOWN, 10, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 5, 250, 10));
			registerUpgrade(NAME, UPGRADE_AMMO, 1, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 4, 250, 1));
			registerUpgrade(NAME, UPGRADE_DAMAGE, 2, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 4, 250, 2));
			registerUpgrade(NAME, UPGRADE_RADIUS, 1.0, Double.class, new DoubleUpgradeChanger(25, 100, 0.1, 0.5, 500, 1.0));
			registerUpgrade(NAME, UPGRADE_DURATION, 3, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 5, 250, 3));
			registerUpgrade(NAME, UPGRADE_STRENGTH, 0.05, Double.class, new DoubleUpgradeChanger(25, 100, 0.05, 0.25, 250, 0.05));
		
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
		
		ItemStack damageItem = new ItemStack(Material.REDSTONE);
		meta = damageItem.getItemMeta();
		meta.setDisplayName("Damage Upgrade");
		damageItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_DAMAGE, damageItem);
		
		ItemStack radiusItem = new ItemStack(Material.COMPASS);
		meta = radiusItem.getItemMeta();
		meta.setDisplayName("Radius Upgrade");
		radiusItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_RADIUS, radiusItem);
		
		ItemStack durationItem = new ItemStack(Material.WATCH);
		meta = durationItem.getItemMeta();
		meta.setDisplayName("Duration Upgrade");
		durationItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_DURATION, durationItem);
		
		ItemStack strengthItem = new ItemStack(Material.ANVIL);
		meta = strengthItem.getItemMeta();
		meta.setDisplayName("Strength Upgrade");
		strengthItem.setItemMeta(meta);
		setUpgradeItem(NAME, UPGRADE_STRENGTH, strengthItem);
		
	}
	
	private static CannonConstructor constructor;
	
	public static void setup() {
		constructor = new CannonConstructor() {
			
			@Override
			public Cannon construct(CannonFighter fighter, Map<String, Object> map) {
				int ammo = ((Number) map.get(UPGRADE_AMMO)).intValue();
				int cooldown = ((Number) map.get(UPGRADE_COOLDOWN)).intValue();
				int damage = ((Number) map.get(UPGRADE_DAMAGE)).intValue();
				int radius = ((Number) map.get(UPGRADE_RADIUS)).intValue();
				int duration = ((Number) map.get(UPGRADE_DURATION)).intValue();
				int strength = ((Number) map.get(UPGRADE_STRENGTH)).intValue();
				
				return new IceCannon(fighter, ammo, cooldown, damage, radius, duration, strength);
			}
			
			@Override
			public boolean canConstruct(String name) {
				return name.equals(NAME);
			}

			@Override
			public Cannon buyNew(CannonFighter fighter) {
				return new IceCannon(fighter);
			}

			@Override
			public int getPrice() {
				return getUpgrade(NAME, UPGRADE_AMMO, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_COOLDOWN, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_DAMAGE, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_RADIUS, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_DURATION, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_STRENGTH, 1).getPrice();
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
	
	public IceCannon(CannonFighter player, int levelAmmo, int levelCooldown, int levelDamage, int levelRadius, int levelDuration, int levelStrength) {
		super(((Number) getUpgrade(NAME, UPGRADE_COOLDOWN, levelCooldown).getValue()).intValue());
		
		setUpgradeLevel(UPGRADE_AMMO, levelAmmo);
		setUpgradeLevel(UPGRADE_COOLDOWN, levelCooldown);
		setUpgradeLevel(UPGRADE_DAMAGE, levelDamage);
		setUpgradeLevel(UPGRADE_RADIUS, levelRadius);
		setUpgradeLevel(UPGRADE_DURATION, levelRadius);
		setUpgradeLevel(UPGRADE_STRENGTH, levelRadius);
		
		this.player = player;
		
		item = ITEM.clone();
		item.setAmount(((Number) getValue(UPGRADE_AMMO)).intValue());
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
	}

	public IceCannon(CannonFighter player) {
		this(player, 1, 1, 1, 1, 1, 1);
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
		
		new MovingParticle(p, new HitHandler() {
			int radius = ((Number) getValue(UPGRADE_RADIUS)).intValue();
			
			@Override
			public void hitEntity(EntityDamageByEntityEvent event) {
				for (Entity e : event.getEntity().getNearbyEntities(radius, radius, radius))
					if (e instanceof Player)
						slowDown((Player) e);
			}

			@Override
			public void hitBlock(Location location) {
				int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
				double radius = this.radius * this.radius;
				 
				for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
					for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++)
						for (Entity e : new Location(location.getWorld(), location.getX() + (chX * 16), location.getY(), location.getZ() + (chZ * 16)).getChunk().getEntities())
							if (e instanceof Player && e.getLocation().distanceSquared(location) <= radius)
								slowDown((Player) e);
			}
			
			private void slowDown(final Player p) {
				CannonFighter c = CannonFighter.get((OfflinePlayer) p);
				System.out.println("Slow down " + c.getName());
				if (!CannonFightUtil.PLUGIN.isActivePlayer(c))
					// only slow down players ingame
					return;
				PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, ((Number) IceCannon.this.getValue(UPGRADE_DURATION)).intValue(), 0, true, false);
				System.out.println("add potioneffect " + c.getName());
				c.addPotionEffect(pe, new PotionEffectOverCallback() {
					
					@Override
					public void potionEffectEnded() {
						p.setWalkSpeed(1);
						System.out.println("potion vorbei " + p.getName());
					}
				});
				System.out.println("walkspeed " + (1.15f - ((Number) IceCannon.this.getValue(UPGRADE_STRENGTH)).floatValue()));
				p.setWalkSpeed(1.15f - ((Number) IceCannon.this.getValue(UPGRADE_STRENGTH)).floatValue());
			}
			
			@Override
			public void flying(Location location) {
				PacketPlayOutWorldParticles particlePacket = new PacketPlayOutWorldParticles(EnumParticle.SPELL_MOB, false, (float) location.getX(), (float) location.getY(), (float) location.getZ(), 1, 0, 1, 1, 0);
				
				for (int i = 0; i < 5; i++) {
					((CraftServer) Bukkit.getServer()).getHandle().sendAll(particlePacket, ((CraftWorld) location.getWorld()).getHandle());
				}
			}
		});
		return true;
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
		case UPGRADE_DURATION:
			return Language.formatTime(((Number) value).intValue(), TimeOutputs.SECONDS);
		case UPGRADE_STRENGTH:
			return Language.formatPercentage(((Number) value).doubleValue());
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
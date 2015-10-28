package de.pesacraft.cannonfight.util.cannons.usable;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.World;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
import de.pesacraft.cannonfight.util.cannons.usable.StandardCannon;
import de.pesacraft.cannonfight.util.game.BreakingBlock;
import de.pesacraft.cannonfight.util.game.HitHandler;
import de.pesacraft.cannonfight.util.game.MovingParticle;
import de.pesacraft.cannonfight.util.shop.upgrade.IntegerUpgradeChanger;
import de.pesacraft.cannonfight.util.shop.upgrade.Upgrade;

public class StandardCannon extends Cannon implements Listener {
	private static final MongoCollection<Document> COLLECTION;
	
	/**
	 * ItemStack representing the cannon ingame
	 */
	protected static final ItemStack ITEM;
	/**
	 * The cannons name
	 */
	public static final String NAME = "StandardCannon";
	
	private static final String UPGRADE_COOLDOWN = "cooldown";
	private static final String UPGRADE_AMMO = "ammo";
	private static final String UPGRADE_DAMAGE = "damage";

	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM = ItemSerializer.deserialize((Document) doc.get("item"));
			
			registerUpgrade(NAME, UPGRADE_COOLDOWN, Integer.class, (Document) doc.get(UPGRADE_COOLDOWN), new IntegerUpgradeChanger(25, 100, 1, 5, 250, 10));
			registerUpgrade(NAME, UPGRADE_AMMO, Integer.class, (Document) doc.get(UPGRADE_AMMO), new IntegerUpgradeChanger(25, 100, 1, 4, 250, 1));
			registerUpgrade(NAME, UPGRADE_DAMAGE, Integer.class, (Document) doc.get(UPGRADE_DAMAGE), new IntegerUpgradeChanger(25, 100, 1, 4, 250, 2));
		}
		else {
			// Cannon not in database
			ITEM = new ItemStack(Material.WOOD_HOE);
			
			ItemMeta m = ITEM.getItemMeta();
			m.setDisplayName(NAME);
			ITEM.setItemMeta(m);
			
			registerUpgrade(NAME, UPGRADE_COOLDOWN, 10, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 5, 250, 10));
			registerUpgrade(NAME, UPGRADE_AMMO, 1, Integer.class, new IntegerUpgradeChanger(25, 100, 1, 4, 250, 1));
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
				int damage = ((Number) map.get(UPGRADE_DAMAGE)).intValue();
				
				return new StandardCannon(fighter, ammo, cooldown, damage);
			}
			
			@Override
			public boolean canConstruct(String name) {
				return name.equals(NAME);
			}

			@Override
			public Cannon buyNew(CannonFighter fighter) {
				return new StandardCannon(fighter);
			}

			@Override
			public int getPrice() {
				return getUpgrade(NAME, UPGRADE_AMMO, 1).getPrice()
						+ getUpgrade(NAME, UPGRADE_COOLDOWN, 1).getPrice()
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
	
	public StandardCannon(CannonFighter player, int levelAmmo, int levelCooldown, int levelDamage) {
		super(((Number) getUpgrade(NAME, UPGRADE_COOLDOWN, levelCooldown).getValue()).intValue());
		
		setUpgradeLevel(UPGRADE_AMMO, levelAmmo);
		setUpgradeLevel(UPGRADE_COOLDOWN, levelCooldown);
		setUpgradeLevel(UPGRADE_DAMAGE, levelDamage);
		
		this.player = player;
		
		item = ITEM.clone();
		item.setAmount(((Number) getValue(UPGRADE_AMMO)).intValue());
		shoot = new ArrayList<Integer>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
	}

	public StandardCannon(CannonFighter player) {
		this(player, 1, 1, 1);
		
		Document doc = new Document(UPGRADE_AMMO, getUpgradeLevel(UPGRADE_AMMO));
		doc = doc.append(UPGRADE_COOLDOWN, getUpgradeLevel(UPGRADE_COOLDOWN));
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
		
		new MovingParticle(p, new HitHandler() {
			
			@Override
			public void hitEntity(EntityDamageByEntityEvent event) {
				event.setDamage(((Number) getValue(UPGRADE_DAMAGE)).doubleValue());
			}
			
			@Override
			public void hitBlock(Location location) {
				BreakingBlock.get(location).damage(((Number) getValue(UPGRADE_DAMAGE)).intValue() / 2);
			}

			@Override
			public void flying(Location location) {
				PacketPlayOutWorldParticles particlePacket = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK, false, (float) location.getX(), (float) location.getY(), (float) location.getZ(), 0, 0, 0, 0, 1, Material.WOOL.getId() + (DyeColor.GREEN.getWoolData() << 12));
				((CraftServer) Bukkit.getServer()).getHandle().sendAll(particlePacket, (World) location.getWorld());
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
			return Language.formatAmmo(((Number) value).intValue());
		case UPGRADE_COOLDOWN:
			return Language.formatTime(((Number) value).intValue(), TimeOutputs.SECONDS);
		case UPGRADE_DAMAGE:
			return Language.formatDamage(((Number) value).doubleValue());
		}
		return null;
	}
}
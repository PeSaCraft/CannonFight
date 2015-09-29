package de.pesacraft.cannonfight.util.cannons;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cooldown;
import de.pesacraft.cannonfight.util.shop.Upgrade;
import de.pesacraft.cannonfight.util.shop.UpgradeMap;

public abstract class Cannon extends Cooldown {
	private static final Map<String, UpgradeMap> UPGRADE_MAP = new HashMap<String, UpgradeMap>();
	
	public Cannon(int time) {
		super(time);
	}
	
	public abstract ItemStack getItem();
	
	public abstract String getName();
	
	public abstract int getMaxAmmo();
	
	public abstract boolean fire(ItemStack item);
	
	public abstract boolean hasAmmo();
	
	public abstract CannonConstructor getCannonConstructor();
	
	public abstract void openShop();

	public abstract void reset();
	
	protected final static <T> void registerUpgrade(String cannonName, String upgradeName, Class<T> type, Document upgradeDoc) {
		UpgradeMap upgrades;
		
		if (UPGRADE_MAP.containsKey(cannonName))
			upgrades = (UpgradeMap) UPGRADE_MAP.get(cannonName);
		else {
			upgrades = new UpgradeMap();
			UPGRADE_MAP.put(cannonName, upgrades);
		}
		
		for (Entry<String, Object> entry : upgradeDoc.entrySet())
			upgrades.setUpgrade(upgradeName, entry, type);
	}
	
	protected final static <T> void registerUpgrade(String cannonName, String upgradeName, Class<T> type) {
		UpgradeMap upgrades;
		
		if (UPGRADE_MAP.containsKey(cannonName))
			upgrades = (UpgradeMap) UPGRADE_MAP.get(cannonName);
		else {
			upgrades = new UpgradeMap();
			UPGRADE_MAP.put(cannonName, upgrades);
		}
		
		try {
			// try to add 2 default levels
			upgrades.setUpgrade(upgradeName, 2, 100, type.newInstance(), type);
		} catch (InstantiationException | IllegalAccessException ex) {
			// cannot instantiate it, initialize with null
			upgrades.setUpgrade(upgradeName, 2, 100, null, type);
		}
	}

	public final static <T> Upgrade<T> getUpgrade(String cannonName, String upgradeName, int level, Class<T> type) {
		if (!UPGRADE_MAP.containsKey(cannonName))
			throw new IllegalArgumentException("Cannon \"" + cannonName + "\" isn't registered!");
		
		return ((UpgradeMap) UPGRADE_MAP.get(cannonName)).getUpgrade(upgradeName, level, type);
	}
	
	public final static int getLevelsForUpgrade(String cannonName, String upgradeName) {
		if (!UPGRADE_MAP.containsKey(cannonName))
			throw new IllegalArgumentException("Cannon \"" + cannonName + "\" isn't registered!");
		
		return UPGRADE_MAP.get(cannonName).getLevels(upgradeName);
	}
	public final static Map<String, Object> serializeUpgrades(String cannonName) {
		if (!UPGRADE_MAP.containsKey(cannonName))
			throw new IllegalArgumentException("Cannon \"" + cannonName + "\" isn't registered!");
		
		return UPGRADE_MAP.get(cannonName).serialize();
	}
}

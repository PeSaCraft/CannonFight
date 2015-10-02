package de.pesacraft.cannonfight.util.cannons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cooldown;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.upgrade.Upgrade;
import de.pesacraft.cannonfight.util.shop.upgrade.UpgradeChanger;
import de.pesacraft.cannonfight.util.shop.upgrade.UpgradeMap;

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
	
	protected final static <T> void registerUpgrade(String cannonName, String upgradeName, Class<T> type, Document upgradeDoc, UpgradeChanger<T> upgradeChanger) {
		UpgradeMap upgrades;
		
		if (UPGRADE_MAP.containsKey(cannonName))
			upgrades = (UpgradeMap) UPGRADE_MAP.get(cannonName);
		else {
			upgrades = new UpgradeMap();
			UPGRADE_MAP.put(cannonName, upgrades);
		}
		
		for (Entry<String, Object> entry : upgradeDoc.entrySet())
			upgrades.setUpgrade(upgradeName, entry, type, upgradeChanger);
	}
	
	@Deprecated
	protected final static <T> void registerUpgrade(String cannonName, String upgradeName, Class<T> type) {
		try {
			// try to register with default beeing new instance of type
			registerUpgrade(cannonName, upgradeName, type.newInstance(), type, null);
		} catch (InstantiationException | IllegalAccessException ex) {
			// cannot instantiate type, default is null
			registerUpgrade(cannonName, upgradeName, null, type, null);
		}
	}

	protected final static <T> void registerUpgrade(String cannonName, String upgradeName, T defaultValue, Class<T> type, UpgradeChanger<T> upgradeChanger) {
		UpgradeMap upgrades;
		
		if (UPGRADE_MAP.containsKey(cannonName))
			upgrades = (UpgradeMap) UPGRADE_MAP.get(cannonName);
		else {
			upgrades = new UpgradeMap();
			UPGRADE_MAP.put(cannonName, upgrades);
		}
		
		// try add 2 default levels
		upgrades.setUpgrade(upgradeName, 2, 100, defaultValue, type, upgradeChanger);
	}
	
	public final static void setUpgradeItem(String cannonName, String upgradeName, ItemStack item) {
		if (!UPGRADE_MAP.containsKey(cannonName))
			throw new IllegalArgumentException("Cannon \"" + cannonName + "\" isn't registered!");
		
		((UpgradeMap) UPGRADE_MAP.get(cannonName)).setItemStack(upgradeName, item);
	}
	
	public final static ItemStack getUpgradeItem(String cannonName, String upgradeName) {
		if (!UPGRADE_MAP.containsKey(cannonName))
			throw new IllegalArgumentException("Cannon \"" + cannonName + "\" isn't registered!");
		
		return ((UpgradeMap) UPGRADE_MAP.get(cannonName)).getItemStack(upgradeName);
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

	public static Shop getUpgradeShop(String cannon) {
		if (!UPGRADE_MAP.containsKey(cannon))
			throw new IllegalArgumentException("Cannon \"" + cannon + "\" isn't registered!");
		
		final UpgradeMap upgrades = UPGRADE_MAP.get(cannon);
		
		int rows = (int) Math.ceil((double) upgrades.size() / 9);
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.ORANGE.getData());
		
		Shop s = new Shop(cannon + "-Setup", new ClickHandler() {
			
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				for (Entry<String, ItemStack> entry : upgrades.getItemMap().entrySet()) {
					if (item.isSimilar(entry.getValue())) {
						event.setNextShop(upgrades.getUpgradeShop(entry.getKey()));
						return;
					}
				}
			}

			@Override
			public void onInventoryClose(InventoryCloseEvent event) {}
		}, rows);
		
		s.fill(fill);
		
		int i = 0;
		
		for (Entry<String, ItemStack> entry : upgrades.getItemMap().entrySet()) {
			s.set(i++, entry.getValue());
		}
		
		return s;
	}
}

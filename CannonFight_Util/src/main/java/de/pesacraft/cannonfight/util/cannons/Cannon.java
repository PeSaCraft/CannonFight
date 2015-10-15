package de.pesacraft.cannonfight.util.cannons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.IllegalClassException;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
	
	private final Map<String, Integer> levels;
	private final Map<String, Object> currentValues;
	
	public Cannon(int time) {
		super(time);
		
		levels = new HashMap<String, Integer>();
		currentValues = new HashMap<String, Object>();
	}
	
	public abstract ItemStack getItem();
	
	public abstract String getName();
	
	public abstract boolean fire(ItemStack item);
	
	public abstract boolean hasAmmo();
	
	public abstract CannonConstructor getCannonConstructor();
	
	public abstract void reset();
	
	protected final boolean setUpgradeLevel(String upgradeName, int level) {
		Upgrade<?> upgrade = getUpgrade(getName(), upgradeName, level);
		
		if (upgrade == null)
			// this upgrade does not exist!
			return false;
		
		levels.put(upgradeName, level);
		resetValue(upgradeName);
		
		return true;
	}
	
	protected final int getUpgradeLevel(String upgradeName) {
		return levels.get(upgradeName);
	}
	
	protected final Object setValue(String upgradeName, Object value) {
		
		if (currentValues.get(upgradeName) != null) {
			Class<?> currentClass = currentValues.get(upgradeName).getClass();
		
			if (!currentClass.isAssignableFrom(value.getClass()))
				throw new IllegalClassException(currentClass, value);
		}
		
		return currentValues.put(upgradeName, value);
	}
	
	protected final Object getValue(String upgradeName) {
		return currentValues.get(upgradeName);
	}
	
	protected boolean resetValue(String upgradeName) {
		Upgrade<?> upgrade = getUpgrade(getName(), upgradeName, levels.get(upgradeName));
		
		Object old = currentValues.put(upgradeName, upgrade.getValue());
		
		return !upgrade.getValue().equals(old);
	}

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
		getUpgradeMap(cannonName).setItemStack(upgradeName, item);
	}
	
	public final static ItemStack getUpgradeItem(String cannonName, String upgradeName) {
		return getUpgradeMap(cannonName).getItemStack(upgradeName);
	}
	
	public final static Upgrade<?> getUpgrade(String cannonName, String upgradeName, int level) {
		return getUpgradeMap(cannonName).getUpgrade(upgradeName, level);
	}
	
	public final static <T> Upgrade<T> getOrSetUpgrade(String cannonName, String upgradeName, int level, Class<T> type) {
		return getUpgradeMap(cannonName).getOrSetUpgrade(upgradeName, level, type);
	}
	
	public final static int getLevelsForUpgrade(String cannonName, String upgradeName) {
		return getUpgradeMap(cannonName).getLevels(upgradeName);
	}
	public final static Map<String, Object> serializeUpgrades(String cannonName) {
		return getUpgradeMap(cannonName).serialize();
	}

	public final static UpgradeMap getUpgradeMap(String cannonName) {
		if (!UPGRADE_MAP.containsKey(cannonName))
			throw new IllegalArgumentException(Language.get("error.cannon.not-registered") + cannonName + "\" isn't registered!"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return UPGRADE_MAP.get(cannonName);
	}
	
	@SuppressWarnings("deprecation")
	public final static Shop getUpgradeSetupShop(String cannonName) {
		final UpgradeMap upgrades = getUpgradeMap(cannonName);
		
		int rows = (int) Math.ceil((double) upgrades.size() / 9);
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.ORANGE.getData());
		
		Shop s = new Shop(cannonName + Language.get("shop.name.cannon.specific.setup"), new ClickHandler() { //$NON-NLS-1$
			
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				for (Entry<String, ItemStack> entry : upgrades.getItemMap().entrySet()) {
					if (item.isSimilar(entry.getValue())) {
						event.setNextShop(upgrades.getUpgradeSetupShop(entry.getKey()));
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
	
	@SuppressWarnings("deprecation")
	public final Shop getUpgradeShop() {
		final UpgradeMap upgrades = getUpgradeMap(getName());
		
		int rows = (int) Math.ceil((double) upgrades.size() / 9);
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.CYAN.getData());
		
		Shop s = new Shop(Language.get("shop.name.cannon.specific.shop") + getName(), new ClickHandler() { //$NON-NLS-1$
			
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				for (Entry<String, ItemStack> entry : upgrades.getItemMap().entrySet()) {
					if (item.isSimilar(entry.getValue())) {
						event.setNextShop(getUpgradeShop());
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
			ItemStack item = entry.getValue().clone();
			
			int level = getUpgradeLevel(entry.getKey());
			Upgrade<?> oldUpgrade = upgrades.getUpgrade(entry.getKey(), level);
			Upgrade<?> newUpgrade = upgrades.getUpgrade(entry.getKey(), level + 1);
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<String>();
			
			lore.add(ChatColor.GOLD + Language.get("shop.upgrade.lore.current") + oldUpgrade.getValue()); //$NON-NLS-1$
			
			if (newUpgrade == null) {
				// maximum level of upgrade reached
				lore.add(ChatColor.GREEN + Language.get("shop.upgrade.lore.max-reached")); //$NON-NLS-1$
			}
			else {
				// upgradable
				lore.add(ChatColor.YELLOW + Language.get("shop.upgrade.lore.upgrade-to.level") + (level + 1)); //$NON-NLS-1$
				lore.add(ChatColor.AQUA + Language.get("shop.upgrade.lore.upgrade-to.price") + newUpgrade.getPrice()); //$NON-NLS-1$
				lore.add(ChatColor.LIGHT_PURPLE + Language.get("shop.upgrade.lore.upgrade-to.value") + newUpgrade.getValue()); //$NON-NLS-1$
			}
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			item.setAmount(level);
			
			s.set(i++, item);
		}
		
		return s;
	}
}

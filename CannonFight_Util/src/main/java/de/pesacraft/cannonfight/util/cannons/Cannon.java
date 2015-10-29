package de.pesacraft.cannonfight.util.cannons;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.IllegalClassException;
import org.bson.Document;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Collection;
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
		refreshUpgradeValue(upgradeName);
		
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
			throw new IllegalArgumentException(Language.getStringMaker("error.cannon.not-registered", false).replace("%cannon%", cannonName).getString());
		
		return UPGRADE_MAP.get(cannonName);
	}
	
	@SuppressWarnings("deprecation")
	public final static Shop getUpgradeSetupShop(String cannonName) {
		final UpgradeMap upgrades = getUpgradeMap(cannonName);
		
		int rows = (int) Math.ceil((double) upgrades.size() / 9);
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.ORANGE.getData());
		
		Shop s = new Shop(Language.getStringMaker("shop.specific-cannon.setup.name", false).replace("%cannon%", cannonName).getString(), new ClickHandler() {
			
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
		
		Shop s = new Shop(Language.getStringMaker("shop.specific-cannon.shop.name", false).replace("%cannon%", getName()).getString(), new ClickHandler() {
			
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				for (Entry<String, ItemStack> entry : upgrades.getItemMap().entrySet()) {
					// lore is changed, thus isSimilar or equals cannot be used
					// we just compare the display names instead, that should be enough
					if (item.getItemMeta().getDisplayName().equalsIgnoreCase(entry.getValue().getItemMeta().getDisplayName())) {
						// try to upgrade this cannon
						String upgradeName = entry.getKey();
						int newLevel = Cannon.this.getUpgradeLevel(upgradeName) + 1;
						
						Upgrade<?> upgrade = upgrades.getUpgrade(upgradeName, newLevel);
						
						CannonFighter p = event.getFighter();
						
						if (upgrade != null) {
							// upgradable
							if (p.hasEnoughCoins(upgrade.getPrice())) {
								// can buy
								p.takeCoins(upgrade.getPrice());
								
								Cannon.this.setUpgradeLevel(upgradeName, newLevel);
								
								event.setNextShop(getUpgradeShop());
							}
							else {
								// not enough money
								p.sendMessage(Language.getStringMaker("info.not-enough-coins", true).replace("%missing%", Language.formatCoins(p.getCoins() - upgrade.getPrice())).getString());
							}
						}
						else {
							// maximum reached
							p.sendMessage(Language.get("info.upgrade.max-reached", true));
						}
						return;
					}
				}
			}
		}, rows);
		
		s.fill(fill);
		
		int i = 0;
		
		for (Entry<String, ItemStack> entry : upgrades.getItemMap().entrySet()) {
			ItemStack item = entry.getValue().clone();
			
			int level = getUpgradeLevel(entry.getKey());
			Upgrade<?> oldUpgrade = upgrades.getUpgrade(entry.getKey(), level);
			Upgrade<?> newUpgrade = upgrades.getUpgrade(entry.getKey(), level + 1);
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.shop.upgrade.lore.current", false).replace("%value%", formatValueForUpgrade(entry.getKey(), oldUpgrade.getValue())).getString().split("\n"));
			
			if (newUpgrade == null) {
				// maximum level of upgrade reached
				lore.addAll(Lists.newArrayList(Language.get("shop.specific-cannon.shop.upgrade.lore.max-reached", false).split("\n")));
			}
			else {
				// upgradable
				lore.addAll(Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.shop.upgrade.lore.upgrade-to", false).replace("%level%", String.valueOf(level + 1)).replace("%price%", Language.formatCoins(newUpgrade.getPrice())).replace("%value%", formatValueForUpgrade(entry.getKey(), newUpgrade.getValue())).getString().split("\n")));
			}
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			item.setAmount(level);
			
			s.set(i++, item);
		}
		
		return s;
	}
	
	abstract public String formatValueForUpgrade(String upgrade, Object value);

	abstract protected void refreshUpgradeValue(String upgradeName);
	
	public final Document serializeLevels() {
		Document doc = new Document();
		
		for (Entry<String, Integer> level : levels.entrySet()) {
			doc.append(level.getKey(), level.getValue());
		}
		
		return doc;
	}
}

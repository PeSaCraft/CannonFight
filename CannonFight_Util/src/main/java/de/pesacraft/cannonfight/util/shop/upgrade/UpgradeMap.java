package de.pesacraft.cannonfight.util.shop.upgrade;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;

public class UpgradeMap extends HashMap<String, UpgradeList<?>> {
	
	private static final long serialVersionUID = 6809923567668862582L;

	public UpgradeMap() {
		super(1);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void setUpgrade(String name, Entry<String, Object> entry, Class<T> type, UpgradeChanger<T> upgradeChanger) {
		UpgradeList<T> upgrades;
		
		if (this.containsKey(name))
			upgrades = (UpgradeList<T>) this.get(name);
		else {
			upgrades = new UpgradeList<T>();
			this.put(name, upgrades);
		}
		
		int level = Integer.parseInt(entry.getKey());
		Upgrade<T> upgrade = new Upgrade<T>((Document) entry.getValue());
		upgrades.setLevel(level, upgrade);
	
		upgrades.setUpgradeChanger(upgradeChanger);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void setUpgrade(String name, int level, int price, T value, Class<T> type, UpgradeChanger<T> upgradeChanger) {
		UpgradeList<T> upgrades;
		
		if (this.containsKey(name))
			upgrades = (UpgradeList<T>) this.get(name);
		else {
			upgrades = new UpgradeList<T>();
			this.put(name, upgrades);
		}
		
		Upgrade<T> upgrade = new Upgrade<T>(price, value);
		upgrades.setLevel(level, upgrade);
		
		upgrades.setUpgradeChanger(upgradeChanger);
	}
	
	public <T> void setPrice(String name, int level, int price, Class<T> type) {
		getOrSetUpgrade(name, level, type).setPrice(price);
	}
	
	public <T> void setValue(String name, int level, T value, Class<T> type) {
		getOrSetUpgrade(name, level, type).setValue(value);
	}

	public Upgrade<?> getUpgrade(String name, int level) {
		return this.get(name).getForLevel(level);
	}
	
	public <T> Upgrade<T> getOrSetUpgrade(String name, int level, Class<T> type) {
		try {
			// no default value given, try to create new instance
			return getOrSetUpgrade(name, level, type.newInstance(), type);
		} catch (InstantiationException | IllegalAccessException e) {
			// couldn't create new instance, default will be null
			return getOrSetUpgrade(name, level, null, type);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> Upgrade<T> getOrSetUpgrade(String name, int level, T defaultValue, Class<T> type) {
		if (this.containsKey(name))
			return (Upgrade<T>) this.get(name).getForLevel(level);
	
		UpgradeList<T> upgrades = new UpgradeList<T>();
	
		// new upgrade has to be created
		Upgrade<T> upgrade = new Upgrade<T>(100, defaultValue);
		
		upgrades.setLevel(level, upgrade);
		this.put(name, upgrades);
		
		return upgrade;
	}
	
	public void setItemStack(String name, ItemStack item) {
		if (!this.containsKey(name))
			throw new IllegalStateException(Language.getStringMaker("error.upgrade.not-set", false).replace("%name%", name).getString());
		
		this.get(name).setItemStack(item);
	}
	
	public ItemStack getItemStack(String name) {
		if (!this.containsKey(name))
			throw new IllegalStateException(Language.getStringMaker("error.upgrade.not-set", false).replace("%name%", name).getString());
		
		return this.get(name).getItemStack();
	}
	
	public int getLevels(String name) {
		if (this.containsKey(name))
			return this.get(name).getLevels();
		
		// upgrade not existant: no levels for that upgrade
		return 0;
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (Entry<String, UpgradeList<?>> entry : this.entrySet())
			map.put(entry.getKey(), entry.getValue().serialize());

		return map;
	}

	public Map<String, ItemStack> getItemMap() {
		Map<String, ItemStack> map = new HashMap<String, ItemStack>();
		
		for (Entry<String, UpgradeList<?>> upgrade : this.entrySet())
			map.put(upgrade.getKey(), upgrade.getValue().getItemStack());	
		
		return map;
	}
	
	@SuppressWarnings("deprecation")
	public Shop getUpgradeSetupShop(String name) {
		if (!this.containsKey(name))
			throw new IllegalStateException(Language.getStringMaker("error.upgrade.not-set", false).replace("%name%", name).getString());
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.SILVER.getData());
		
		final UpgradeList<?> upgrades = this.get(name);
		
		final Map<ItemStack, Integer> items = new LinkedHashMap<ItemStack, Integer>();
		
		int i;
		
		for (i = 1; i <= upgrades.getLevels(); i++) {
			ItemStack item = new ItemStack(Material.WOOL, i, DyeColor.WHITE.getWoolData());
			
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.change", false).replace("%level%", String.valueOf(i)).getString());
			item.setItemMeta(meta);
			
			items.put(item, i);
		}
		
		ItemStack newLevelItem = new ItemStack(Material.WOOL, i, DyeColor.GRAY.getWoolData());
		
		ItemMeta meta = newLevelItem.getItemMeta();
		meta.setDisplayName(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.create", false).replace("%level%", String.valueOf(i)).getString());
		newLevelItem.setItemMeta(meta);
		
		items.put(newLevelItem, i);
		
		// space for one new level
		int rows = (int) Math.ceil((double) (upgrades.getLevels() + 1) / 9);
		
		Shop s = new Shop(Language.getStringMaker("shop.specific-cannon.setup.upgrade.name", false).replace("%name%", name).getString(), new ClickHandler() {
		
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				for (Entry<ItemStack, Integer> entry : items.entrySet()) {
					if (item.isSimilar(entry.getKey())) {
						event.setNextShop(upgrades.getUpgradeSetupShop(entry.getValue()));
						return;
					}
				}
			}
		}, rows);
		
		s.fill(fill);
		
		i = 0;
		for (ItemStack item : items.keySet())
			s.set(i++, item);

		return s;
	}
}

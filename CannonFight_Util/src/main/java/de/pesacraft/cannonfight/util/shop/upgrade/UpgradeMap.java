package de.pesacraft.cannonfight.util.shop.upgrade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.util.cannons.CannonConstructor;

public class UpgradeMap extends HashMap<String, UpgradeList<?>> {
	
	private static final long serialVersionUID = 6809923567668862582L;

	public UpgradeMap() {
		super(1);
	}
	
	public <T> void setUpgrade(String name, Entry<String, Object> entry, Class<T> type) {
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
	}
	
	public <T> void setUpgrade(String name, int level, int price, T value, Class<T> type) {
		UpgradeList<T> upgrades;
		
		if (this.containsKey(name))
			upgrades = (UpgradeList<T>) this.get(name);
		else {
			upgrades = new UpgradeList<T>();
			this.put(name, upgrades);
		}
		
		Upgrade<T> upgrade = new Upgrade<T>(price, value);
		upgrades.setLevel(level, upgrade);
	}
	
	public <T> void setPrice(String name, int level, int price, Class<T> type) {
		getUpgrade(name, level, type).setPrice(price);
	}
	
	public <T> void setValue(String name, int level, T value, Class<T> type) {
		getUpgrade(name, level, type).setValue(value);
	}

	public <T> Upgrade<T> getUpgrade(String name, int level, Class<T> type) {
		try {
			// no default value given, try to create new instance
			return getUpgrade(name, level, type.newInstance(), type);
		} catch (InstantiationException | IllegalAccessException e) {
			// couldn't create new instance, default will be null
			return getUpgrade(name, level, null, type);
		}
	}
	
	public <T> Upgrade<T> getUpgrade(String name, int level, T defaultValue, Class<T> type) {
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
			throw new IllegalStateException("Upgrade \"" + name + "\" has no ItemStack set!");
		
		this.get(name).setItemStack(item);
	}
	
	public ItemStack getItemStack(String name) {
		if (!this.containsKey(name))
			throw new IllegalStateException("Upgrade \"" + name + "\" has no ItemStack set!");
		
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
}
